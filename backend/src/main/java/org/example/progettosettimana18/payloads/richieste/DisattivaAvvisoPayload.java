package org.example.progettosettimana18.payloads.richieste;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Arriva dal link della mail: dentro c'e' il token casuale, non l'id dell'avviso.
 */
public record DisattivaAvvisoPayload(
        @NotBlank(message = "il token e' obbligatorio")
        @Size(max = 64, message = "token non valido")
        String token
) {
}
