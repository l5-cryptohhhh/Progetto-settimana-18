package org.example.progettosettimana18.payloads.risposte;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroreRisposta(
        int stato,
        String messaggio,
        LocalDateTime quando,
        Map<String, String> campi
) {
    public static ErroreRisposta di(int stato, String messaggio) {
        return new ErroreRisposta(stato, messaggio, LocalDateTime.now(), null);
    }

    public static ErroreRisposta diValidazione(int stato, String messaggio, Map<String, String> campi) {
        return new ErroreRisposta(stato, messaggio, LocalDateTime.now(), campi);
    }
}
