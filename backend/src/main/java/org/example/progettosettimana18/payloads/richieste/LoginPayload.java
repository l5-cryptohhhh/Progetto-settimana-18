package org.example.progettosettimana18.payloads.richieste;

import jakarta.validation.constraints.NotBlank;

public record LoginPayload(
        @NotBlank(message = "l'email e' obbligatoria")
        String email,

        @NotBlank(message = "la password e' obbligatoria")
        String password
) {
}
