package org.example.progettosettimana18.payloads.risposte;

import org.example.progettosettimana18.entities.Alimentazione;
import org.example.progettosettimana18.entities.Auto;
import org.example.progettosettimana18.entities.StatoAuto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * La stessa auto vista dall'amministratore: qui ci sono anche le bozze
 * e il prezzo d'acquisto. Questo record esce solo dalle rotte /api/admin.
 */
public record AutoAdminRisposta(
        Long id,
        String marca,
        String modello,
        Integer anno,
        Integer chilometri,
        Alimentazione alimentazione,
        String descrizione,
        String immagineUrl,
        BigDecimal prezzo,
        BigDecimal prezzoAcquisto,
        StatoAuto stato,
        LocalDateTime creataIl,
        LocalDateTime aggiornataIl
) {
    public static AutoAdminRisposta da(Auto auto) {
        return new AutoAdminRisposta(
                auto.getId(),
                auto.getMarca(),
                auto.getModello(),
                auto.getAnno(),
                auto.getChilometri(),
                auto.getAlimentazione(),
                auto.getDescrizione(),
                auto.getImmagineUrl(),
                auto.getPrezzo(),
                auto.getPrezzoAcquisto(),
                auto.getStato(),
                auto.getCreataIl(),
                auto.getAggiornataIl()
        );
    }
}
