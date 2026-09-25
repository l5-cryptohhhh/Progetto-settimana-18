package org.example.progettosettimana18.services;

import org.example.progettosettimana18.entities.Ruolo;
import org.example.progettosettimana18.entities.Utente;
import org.example.progettosettimana18.exceptions.ConflittoException;
import org.example.progettosettimana18.exceptions.CredenzialiErrateException;
import org.example.progettosettimana18.payloads.richieste.AggiornaProfiloPayload;
import org.example.progettosettimana18.payloads.richieste.LoginPayload;
import org.example.progettosettimana18.payloads.richieste.RegistrazionePayload;
import org.example.progettosettimana18.repositories.AvvisoPrezzoRepository;
import org.example.progettosettimana18.repositories.PreferitoRepository;
import org.example.progettosettimana18.repositories.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UtenteService {

    private static final Logger log = LoggerFactory.getLogger(UtenteService.class);

    private final UtenteRepository utenteRepository;
    private final PreferitoRepository preferitoRepository;
    private final AvvisoPrezzoRepository avvisoPrezzoRepository;
    private final PasswordEncoder codificatore;

    public UtenteService(UtenteRepository utenteRepository,
                         PreferitoRepository preferitoRepository,
                         AvvisoPrezzoRepository avvisoPrezzoRepository,
                         PasswordEncoder codificatore) {
        this.utenteRepository = utenteRepository;
        this.preferitoRepository = preferitoRepository;
        this.avvisoPrezzoRepository = avvisoPrezzoRepository;
        this.codificatore = codificatore;
    }

    /**
     * Il ruolo non arriva dal client: qui e' scritto UTENTE e basta.
     * Se qualcuno infila "ruolo": "ADMIN" nel JSON, quel campo non esiste
     * nel payload e nessuno lo legge.
     */
    @Transactional
    public Utente registra(RegistrazionePayload dati) {
        String email = dati.email().trim().toLowerCase();

        if (utenteRepository.existsByEmail(email)) {
            throw new ConflittoException("Esiste gia' un account con questa email");
        }

        Utente utente = new Utente(
                email,
                codificatore.encode(dati.password()),
                dati.nome().trim(),
                Ruolo.UTENTE);

        Utente salvato = utenteRepository.save(utente);
        log.info("Registrato nuovo utente {}", salvato.getId());
        return salvato;
    }

    /**
     * Email sbagliata e password sbagliata danno lo stesso errore: altrimenti
     * si capirebbe quali indirizzi sono registrati.
     */
    public Utente verificaCredenziali(LoginPayload dati) {
        Utente utente = utenteRepository.findByEmail(dati.email().trim().toLowerCase())
                .orElseThrow(() -> new CredenzialiErrateException("credenziali non valide"));

        if (!codificatore.matches(dati.password(), utente.getPasswordHash())) {
            log.warn("Tentativo di accesso fallito per l'utente {}", utente.getId());
            throw new CredenzialiErrateException("credenziali non valide");
        }

        return utente;
    }

    @Transactional
    public Utente aggiornaProfilo(Utente utente, AggiornaProfiloPayload dati) {
        utente.setNome(dati.nome().trim());
        return utenteRepository.save(utente);
    }

    /**
     * "Elimina il mio account": prima spariscono avvisi e preferiti, poi l'utente.
     * Da qui in avanti non parte piu' nessuna mail verso quell'indirizzo.
     */
    @Transactional
    public void eliminaAccount(Utente utente) {
        Long id = utente.getId();
        avvisoPrezzoRepository.deleteByUtenteId(id);
        preferitoRepository.deleteByUtenteId(id);
        utenteRepository.deleteById(id);
        log.info("Eliminato account {} con avvisi e preferiti", id);
    }
}
