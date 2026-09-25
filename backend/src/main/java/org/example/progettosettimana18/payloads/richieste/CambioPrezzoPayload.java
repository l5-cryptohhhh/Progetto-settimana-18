package org.example.progettosettimana18.payloads.richieste;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CambioPrezzoPayload(
        @NotNull(message = "il prezzo e' obbligatorio")
        @DecimalMin(value = "0.00", message = "il prezzo non puo' essere negativo")
        @Digits(integer = 10, fraction = 2, message = "prezzo non valido")
        BigDecimal prezzo
) {
}
