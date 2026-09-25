package org.example.progettosettimana18.payloads.risposte;

import org.example.progettosettimana18.entities.Ruolo;
import org.example.progettosettimana18.entities.Utente;

import java.time.LocalDateTime;

public record UtenteRisposta(
        Long id,
        String email,
        String nome,
        Ruolo ruolo,
        LocalDateTime creatoIl
) {
    public static UtenteRisposta da(Utente utente) {
        return new UtenteRisposta(
                utente.getId(),
                utente.getEmail(),
                utente.getNome(),
                utente.getRuolo(),
                utente.getCreatoIl()
        );
    }
}
