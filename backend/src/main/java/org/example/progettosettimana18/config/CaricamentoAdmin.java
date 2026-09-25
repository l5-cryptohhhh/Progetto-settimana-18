package org.example.progettosettimana18.config;

import org.example.progettosettimana18.entities.Ruolo;
import org.example.progettosettimana18.entities.Utente;
import org.example.progettosettimana18.repositories.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * L'unico modo per avere un ADMIN. La password arriva da ADMIN_PASSWORD:
 * non e' scritta da nessuna parte nel progetto, nemmeno come valore di ripiego.
 */
@Component
public class CaricamentoAdmin implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CaricamentoAdmin.class);

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder codificatore;
    private final String emailAdmin;
    private final String passwordAdmin;
    private final String nomeAdmin;

    public CaricamentoAdmin(UtenteRepository utenteRepository,
                            PasswordEncoder codificatore,
                            @Value("${app.admin.email}") String emailAdmin,
                            @Value("${app.admin.password}") String passwordAdmin,
                            @Value("${app.admin.nome}") String nomeAdmin) {
        this.utenteRepository = utenteRepository;
        this.codificatore = codificatore;
        this.emailAdmin = emailAdmin;
        this.passwordAdmin = passwordAdmin;
        this.nomeAdmin = nomeAdmin;
    }

    @Override
    public void run(String... argomenti) {
        String email = emailAdmin.trim().toLowerCase();

        if (utenteRepository.existsByEmail(email)) {
            log.info("Amministratore gia' presente, non tocco niente");
            return;
        }

        Utente admin = new Utente(email, codificatore.encode(passwordAdmin), nomeAdmin, Ruolo.ADMIN);
        utenteRepository.save(admin);

        // Nel log non finisce ne' l'indirizzo ne' la password.
        log.info("Creato l'amministratore iniziale");
    }
}
