package org.example.progettosettimana18.payloads.richieste;

import jakarta.validation.constraints.NotNull;

public record AggiungiPreferitoPayload(
        @NotNull(message = "l'auto e' obbligatoria")
        Long autoId
) {
}
