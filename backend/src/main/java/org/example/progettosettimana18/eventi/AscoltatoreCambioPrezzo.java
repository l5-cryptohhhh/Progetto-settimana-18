package org.example.progettosettimana18.eventi;

import org.example.progettosettimana18.services.AvvisoPrezzoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AscoltatoreCambioPrezzo {

    private static final Logger log = LoggerFactory.getLogger(AscoltatoreCambioPrezzo.class);

    private final AvvisoPrezzoService avvisoPrezzoService;

    public AscoltatoreCambioPrezzo(AvvisoPrezzoService avvisoPrezzoService) {
        this.avvisoPrezzoService = avvisoPrezzoService;
    }

    /**
     * AFTER_COMMIT: se il salvataggio del prezzo fallisce e la transazione torna
     * indietro, questo metodo non viene mai chiamato e non parte nessuna mail.
     * Async: l'amministratore riceve la risposta subito, senza restare appeso a Gmail.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void quandoCambiaIlPrezzo(PrezzoCambiatoEvento evento) {
        try {
            avvisoPrezzoService.notificaAttraversamentoSoglia(evento);
        } catch (Exception ex) {
            // Siamo su un thread a parte: se lasciamo scappare l'eccezione nessuno la vede.
            log.error("Errore mentre gestivo gli avvisi dell'auto {}", evento.autoId(), ex);
        }
    }
}
