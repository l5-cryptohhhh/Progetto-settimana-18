package org.example.progettosettimana18.services;

import org.example.progettosettimana18.entities.Auto;
import org.example.progettosettimana18.entities.AvvisoPrezzo;
import org.example.progettosettimana18.entities.StatoAuto;
import org.example.progettosettimana18.entities.Utente;
import org.example.progettosettimana18.eventi.PrezzoCambiatoEvento;
import org.example.progettosettimana18.exceptions.ConflittoException;
import org.example.progettosettimana18.exceptions.NonTrovatoException;
import org.example.progettosettimana18.repositories.AutoRepository;
import org.example.progettosettimana18.repositories.AvvisoPrezzoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class AvvisoPrezzoService {

    private static final Logger log = LoggerFactory.getLogger(AvvisoPrezzoService.class);

    private static final SecureRandom CASO = new SecureRandom();
    private static final Base64.Encoder CODIFICA = Base64.getUrlEncoder().withoutPadding();

    private final AvvisoPrezzoRepository avvisoPrezzoRepository;
    private final AutoRepository autoRepository;
    private final MarcatoreAvvisi marcatore;
    private final ServizioMail servizioMail;

    public AvvisoPrezzoService(AvvisoPrezzoRepository avvisoPrezzoRepository,
                               AutoRepository autoRepository,
                               MarcatoreAvvisi marcatore,
                               ServizioMail servizioMail) {
        this.avvisoPrezzoRepository = avvisoPrezzoRepository;
        this.autoRepository = autoRepository;
        this.marcatore = marcatore;
        this.servizioMail = servizioMail;
    }

    // ---------- quello che fa l'utente ----------

    public List<AvvisoPrezzo> elenco(Utente utente) {
        return avvisoPrezzoRepository.elencoDi(utente.getId());
    }

    public AvvisoPrezzo dettaglio(Utente utente, Long avvisoId) {
        return avvisoPrezzoRepository.findByIdAndUtenteId(avvisoId, utente.getId())
                .orElseThrow(() -> new NonTrovatoException("Avviso non trovato"));
    }

    @Transactional
    public AvvisoPrezzo crea(Utente utente, Long autoId, BigDecimal soglia) {
        Auto auto = autoRepository.findByIdAndStato(autoId, StatoAuto.PUBBLICATA)
                .orElseThrow(() -> new NonTrovatoException("Auto non trovata"));

        if (avvisoPrezzoRepository.findByUtenteIdAndAutoId(utente.getId(), auto.getId()).isPresent()) {
            throw new ConflittoException("Hai gia' un avviso su questa auto");
        }

        AvvisoPrezzo avviso = new AvvisoPrezzo(utente, auto, soglia, nuovoToken());
        return avvisoPrezzoRepository.save(avviso);
    }

    /**
     * Si cambia la soglia, non il segno dell'invio: un avviso gia' scattato
     * resta scattato, altrimenti bastava rialzare e riabbassare la soglia
     * per farsi mandare la stessa mail quante volte si vuole.
     */
    @Transactional
    public AvvisoPrezzo aggiornaSoglia(Utente utente, Long avvisoId, BigDecimal soglia) {
        AvvisoPrezzo avviso = dettaglio(utente, avvisoId);
        avviso.setSogliaPrezzo(soglia);
        return avvisoPrezzoRepository.save(avviso);
    }

    @Transactional
    public void elimina(Utente utente, Long avvisoId) {
        AvvisoPrezzo avviso = dettaglio(utente, avvisoId);
        avvisoPrezzoRepository.delete(avviso);
    }

    /**
     * Arriva dal link della mail, senza accesso fatto. Il token vale una volta sola:
     * appena usato lo togliamo, cosi' un secondo clic non trova piu' niente
     * e chi prova token a caso non scopre l'avviso di nessuno.
     */
    @Transactional
    public void disattivaConToken(String token) {
        AvvisoPrezzo avviso = avvisoPrezzoRepository.findByTokenDisattivazione(token)
                .orElseThrow(() -> new NonTrovatoException("Link non valido o gia' usato"));

        avviso.setAttivo(false);
        avviso.setTokenDisattivazione(null);
        avvisoPrezzoRepository.save(avviso);
        log.info("Disattivato avviso {} dal link della mail", avviso.getId());
    }

    // ---------- quello che succede quando cambia il prezzo ----------

    /**
     * Chiamato dal listener dopo il commit. Manda una mail per ogni avviso
     * che il cambio di prezzo ha attraversato, e una volta sola.
     */
    public void notificaAttraversamentoSoglia(PrezzoCambiatoEvento evento) {
        // Se il prezzo e' salito o e' rimasto uguale non c'e' nessuna soglia
        // attraversata verso il basso: si esce subito senza toccare il database.
        if (evento.prezzoVecchio().compareTo(evento.prezzoNuovo()) <= 0) {
            return;
        }

        List<AvvisoPrezzo> candidati = avvisoPrezzoRepository.daNotificare(
                evento.autoId(), evento.prezzoVecchio(), evento.prezzoNuovo());

        if (candidati.isEmpty()) {
            return;
        }

        log.info("Auto {}: {} avvisi hanno attraversato la soglia", evento.autoId(), candidati.size());

        for (AvvisoPrezzo avviso : candidati) {
            if (!marcatore.prendiIlSegno(avviso.getId())) {
                // Qualcun altro ha gia' preso questa riga: la mail la manda lui.
                continue;
            }

            try {
                servizioMail.inviaAvvisoPrezzo(avviso, evento.prezzoVecchio());
                log.info("Mandata la mail dell'avviso {}", avviso.getId());
            } catch (Exception ex) {
                // Scelta: il segno resta preso e la mail e' persa.
                // Rimetterlo a false vorrebbe dire rischiare il doppione al prossimo
                // cambio di prezzo, e una mail commerciale doppia da' piu' fastidio
                // di una mancata: il prezzo aggiornato l'utente lo vede comunque sul sito.
                log.warn("Mail dell'avviso {} non partita, l'avviso resta segnato come inviato",
                        avviso.getId(), ex);
            }
        }
    }

    /** 32 byte di casualita': non si indovina e non si risale all'id dell'avviso. */
    private String nuovoToken() {
        byte[] materiale = new byte[32];
        CASO.nextBytes(materiale);
        return CODIFICA.encodeToString(materiale);
    }
}
