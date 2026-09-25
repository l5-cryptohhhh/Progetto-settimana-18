package org.example.progettosettimana18.payloads.richieste;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AggiornaSogliaPayload(
        @NotNull(message = "la soglia e' obbligatoria")
        @DecimalMin(value = "1.00", message = "la soglia deve essere almeno 1")
        @Digits(integer = 10, fraction = 2, message = "soglia non valida")
        BigDecimal soglia
) {
}
