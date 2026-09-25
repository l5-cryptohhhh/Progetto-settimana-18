package org.example.progettosettimana18;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Le regole delle slide sugli attacchi, provate sulle rotte vere:
 * chi puo' fare che cosa e che cosa succede a chi prova a barare.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SicurezzaApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Value("${app.admin.email}")
    private String emailAdmin;

    @Value("${app.admin.password}")
    private String passwordAdmin;

    // ---------- rotte pubbliche ----------

    @Test
    @DisplayName("il catalogo si sfoglia senza aver fatto l'accesso")
    void catalogoPubblico() throws Exception {
        mvc.perform(get("/api/auto"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("il controllo di salute resta pubblico, serve a Render")
    void saluteAperta() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // ---------- chi puo' fare che cosa ----------

    @Test
    @DisplayName("senza token il cambio prezzo risponde 401")
    void senzaTokenNienteAdmin() throws Exception {
        mvc.perform(patch("/api/admin/auto/1/prezzo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prezzo\": 100.00}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un utente collegato che prova a cambiare il prezzo riceve 403")
    void utenteNormaleNonCambiaIPrezzi() throws Exception {
        String token = tokenDiUnNuovoUtente("curioso@example.com");

        mvc.perform(patch("/api/admin/auto/1/prezzo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prezzo\": 100.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("il ruolo scritto nel corpo della registrazione non serve a niente")
    void ilRuoloNonArrivaDalClient() throws Exception {
        String corpo = """
                {
                  "email": "furbo@example.com",
                  "password": "passwordlunga1",
                  "nome": "Furbo",
                  "ruolo": "ADMIN"
                }
                """;

        mvc.perform(post("/api/auth/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ruolo").value("UTENTE"));
    }

    @Test
    @DisplayName("l'avviso di un altro utente risponde 404, non 403")
    void avvisoDiUnAltro() throws Exception {
        String tokenProprietario = tokenDiUnNuovoUtente("proprietario@example.com");
        String tokenIntruso = tokenDiUnNuovoUtente("intruso@example.com");
        long idAuto = creaAutoPubblicata("Volkswagen", "Golf", "15000.00");

        String rispostaAvviso = mvc.perform(post("/api/avvisi")
                        .header("Authorization", "Bearer " + tokenProprietario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"autoId\": " + idAuto + ", \"soglia\": 13000.00}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long idAvviso = mapper.readTree(rispostaAvviso).get("id").asLong();

        mvc.perform(get("/api/avvisi/" + idAvviso)
                        .header("Authorization", "Bearer " + tokenIntruso))
                .andExpect(status().isNotFound());
    }

    // ---------- i dati in ingresso ----------

    @Test
    @DisplayName("un campo di ordinamento fuori elenco risponde 400")
    void ordinamentoNonAmmesso() throws Exception {
        mvc.perform(get("/api/auto").param("ordina", "prezzoAcquisto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("una password corta non passa la validazione")
    void passwordCorta() throws Exception {
        String corpo = """
                {"email": "corta@example.com", "password": "123", "nome": "Tizio"}
                """;

        mvc.perform(post("/api/auth/registrazione")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campi.password").exists());
    }

    // ---------- le bozze ----------

    @Test
    @DisplayName("una bozza non si vede dal catalogo pubblico")
    void bozzaInvisibile() throws Exception {
        long idBozza = creaAuto("Audi", "A3", "22000.00", "BOZZA");

        mvc.perform(get("/api/auto/" + idBozza))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("il prezzo d'acquisto non esce dalle rotte pubbliche")
    void prezzoAcquistoRiservato() throws Exception {
        long idAuto = creaAutoPubblicata("Renault", "Clio", "11000.00");

        mvc.perform(get("/api/auto/" + idAuto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prezzoAcquisto").doesNotExist())
                .andExpect(jsonPath("$.stato").doesNotExist());
    }

    // ---------- aiutanti ----------

    private String tokenDiUnNuovoUtente(String email) throws Exception {
        String registrazione = """
                {"email": "%s", "password": "passwordlunga1", "nome": "Utente di prova"}
                """.formatted(email);

        mvc.perform(post("/api/auth/registrazione")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrazione));

        return accedi(email, "passwordlunga1");
    }

    private String accedi(String email, String password) throws Exception {
        String risposta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return mapper.readTree(risposta).get("accessToken").asString();
    }

    private long creaAutoPubblicata(String marca, String modello, String prezzo) throws Exception {
        return creaAuto(marca, modello, prezzo, "PUBBLICATA");
    }

    private long creaAuto(String marca, String modello, String prezzo, String stato) throws Exception {
        String tokenAdmin = accedi(emailAdmin, passwordAdmin);

        String corpo = """
                {
                  "marca": "%s",
                  "modello": "%s",
                  "anno": 2020,
                  "chilometri": 45000,
                  "alimentazione": "BENZINA",
                  "descrizione": "Auto di prova",
                  "prezzo": %s,
                  "prezzoAcquisto": 9000.00,
                  "stato": "%s"
                }
                """.formatted(marca, modello, prezzo, stato);

        String risposta = mvc.perform(post("/api/admin/auto")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return mapper.readTree(risposta).get("id").asLong();
    }
}
