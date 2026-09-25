package org.example.progettosettimana18.payloads.risposte;

public record AccessoRisposta(
        String accessToken,
        UtenteRisposta utente
) {
}
