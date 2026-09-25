package org.example.progettosettimana18.payloads.risposte;

import org.example.progettosettimana18.entities.Alimentazione;
import org.example.progettosettimana18.entities.Auto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Quello che esce verso chiunque. Il prezzo d'acquisto e lo stato non stanno qui:
 * se un campo non entra nel record non c'e' modo che finisca nella risposta.
 */
public record AutoPubblicaRisposta(
        Long id,
        String marca,
        String modello,
        Integer anno,
        Integer chilometri,
        Alimentazione alimentazione,
        String descrizione,
        String immagineUrl,
        BigDecimal prezzo,
        LocalDateTime creataIl
) {
    public static AutoPubblicaRisposta da(Auto auto) {
        return new AutoPubblicaRisposta(
                auto.getId(),
                auto.getMarca(),
                auto.getModello(),
                auto.getAnno(),
                auto.getChilometri(),
                auto.getAlimentazione(),
                auto.getDescrizione(),
                auto.getImmagineUrl(),
                auto.getPrezzo(),
                auto.getCreataIl()
        );
    }
}
