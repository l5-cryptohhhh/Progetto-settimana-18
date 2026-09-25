package org.example.progettosettimana18.repositories;

import org.example.progettosettimana18.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Le due query che reggono tutta la storia degli avvisi, provate sul database vero
 * e non su un mock: quella che trova chi ha attraversato la soglia e quella
 * che prende il segno dell'invio.
 */
@DataJpaTest
@ActiveProfiles("test")
class AvvisoPrezzoRepositoryTest {

    @Autowired
    private AvvisoPrezzoRepository avvisoPrezzoRepository;

    @Autowired
    private TestEntityManager em;

    private Auto panda;
    private AvvisoPrezzo avvisoDiMario;

    @BeforeEach
    void preparaIDati() {
        Utente mario = em.persist(new Utente("mario@example.com", "hash", "Mario", Ruolo.UTENTE));

        panda = new Auto();
        panda.setMarca("Fiat");
        panda.setModello("Panda");
        panda.setAnno(2019);
        panda.setChilometri(60000);
        panda.setAlimentazione(Alimentazione.BENZINA);
        panda.setPrezzo(new BigDecimal("10000.00"));
        panda.setStato(StatoAuto.PUBBLICATA);
        panda = em.persist(panda);

        // Mario vuole essere avvisato sotto i 9000
        avvisoDiMario = em.persist(new AvvisoPrezzo(mario, panda, new BigDecimal("9000.00"), "token-di-mario"));
        em.flush();
    }

    @Test
    @DisplayName("prima sopra la soglia, adesso sotto: l'avviso c'e'")
    void attraversamentoVeroeProprio() {
        List<AvvisoPrezzo> trovati = avvisoPrezzoRepository.daNotificare(
                panda.getId(), new BigDecimal("10000.00"), new BigDecimal("8500.00"));

        assertThat(trovati).extracting(AvvisoPrezzo::getId).containsExactly(avvisoDiMario.getId());
    }

    @Test
    @DisplayName("il prezzo nuovo uguale alla soglia conta come attraversamento")
    void prezzoEsattamenteSullaSoglia() {
        List<AvvisoPrezzo> trovati = avvisoPrezzoRepository.daNotificare(
                panda.getId(), new BigDecimal("10000.00"), new BigDecimal("9000.00"));

        assertThat(trovati).hasSize(1);
    }

    @Test
    @DisplayName("era gia' sotto soglia e scende ancora: non scatta niente")
    void ribassoSuRibasso() {
        List<AvvisoPrezzo> trovati = avvisoPrezzoRepository.daNotificare(
                panda.getId(), new BigDecimal("8500.00"), new BigDecimal("8000.00"));

        assertThat(trovati).isEmpty();
    }

    @Test
    @DisplayName("lo stesso prezzo risalvato non e' un attraversamento")
    void stessoPrezzo() {
        List<AvvisoPrezzo> trovati = avvisoPrezzoRepository.daNotificare(
                panda.getId(), new BigDecimal("8500.00"), new BigDecimal("8500.00"));

        assertThat(trovati).isEmpty();
    }

    @Test
    @DisplayName("un avviso gia' inviato resta fuori")
    void avvisoGiaInviato() {
        avvisoDiMario.setInviato(true);
        em.persist(avvisoDiMario);
        em.flush();

        List<AvvisoPrezzo> trovati = avvisoPrezzoRepository.daNotificare(
                panda.getId(), new BigDecimal("10000.00"), new BigDecimal("8500.00"));

        assertThat(trovati).isEmpty();
    }

    @Test
    @DisplayName("un avviso disattivato dal link della mail resta fuori")
    void avvisoDisattivato() {
        avvisoDiMario.setAttivo(false);
        em.persist(avvisoDiMario);
        em.flush();

        List<AvvisoPrezzo> trovati = avvisoPrezzoRepository.daNotificare(
                panda.getId(), new BigDecimal("10000.00"), new BigDecimal("8500.00"));

        assertThat(trovati).isEmpty();
    }

    @Test
    @DisplayName("il segno si prende una volta sola: la seconda aggiorna zero righe")
    void ilSegnoSiPrendeUnaVoltaSola() {
        int primaVolta = avvisoPrezzoRepository.segnaComeInviato(avvisoDiMario.getId(), LocalDateTime.now());
        int secondaVolta = avvisoPrezzoRepository.segnaComeInviato(avvisoDiMario.getId(), LocalDateTime.now());

        assertThat(primaVolta).isEqualTo(1);
        assertThat(secondaVolta).isZero();
    }

    @Test
    @DisplayName("l'avviso di un altro utente non si trova cercando per id e proprietario")
    void nonSiLeggeLAvvisoDiUnAltro() {
        Utente luigi = em.persist(new Utente("luigi@example.com", "hash", "Luigi", Ruolo.UTENTE));
        em.flush();

        assertThat(avvisoPrezzoRepository.findByIdAndUtenteId(avvisoDiMario.getId(), luigi.getId()))
                .isEmpty();
    }
}
