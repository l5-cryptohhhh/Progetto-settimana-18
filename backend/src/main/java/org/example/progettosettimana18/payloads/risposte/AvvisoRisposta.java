package org.example.progettosettimana18.payloads.risposte;

import org.example.progettosettimana18.entities.AvvisoPrezzo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Il token di disattivazione non e' in questo record: sta solo dentro la mail.
 */
public record AvvisoRisposta(
        Long id,
        AutoPubblicaRisposta auto,
        BigDecimal soglia,
        boolean inviato,
        boolean attivo,
        LocalDateTime creatoIl,
        LocalDateTime inviatoIl
) {
    public static AvvisoRisposta da(AvvisoPrezzo avviso) {
        return new AvvisoRisposta(
                avviso.getId(),
                AutoPubblicaRisposta.da(avviso.getAuto()),
                avviso.getSogliaPrezzo(),
                avviso.isInviato(),
                avviso.isAttivo(),
                avviso.getCreatoIl(),
                avviso.getInviatoIl()
        );
    }
}
