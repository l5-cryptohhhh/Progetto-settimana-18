package org.example.progettosettimana18.payloads.richieste;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dal profilo si cambia solo il nome. Email e ruolo non si toccano da qui.
 */
public record AggiornaProfiloPayload(
        @NotBlank(message = "il nome e' obbligatorio")
        @Size(min = 2, max = 80, message = "il nome deve avere tra 2 e 80 caratteri")
        String nome
) {
}
