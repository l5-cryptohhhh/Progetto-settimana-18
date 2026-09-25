package org.example.progettosettimana18.payloads.risposte;

import org.example.progettosettimana18.entities.Preferito;

import java.time.LocalDateTime;

public record PreferitoRisposta(
        Long id,
        AutoPubblicaRisposta auto,
        LocalDateTime aggiuntoIl
) {
    public static PreferitoRisposta da(Preferito preferito) {
        return new PreferitoRisposta(
                preferito.getId(),
                AutoPubblicaRisposta.da(preferito.getAuto()),
                preferito.getAggiuntoIl()
        );
    }
}
