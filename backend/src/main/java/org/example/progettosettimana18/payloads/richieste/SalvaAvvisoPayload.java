package org.example.progettosettimana18.payloads.richieste;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Qui dentro non ci sono utenteId e inviato. Il proprietario dell'avviso lo prende
 * il server dal token, e il segno dell'invio lo mette solo il listener della mail:
 * chi li aggiunge al JSON non cambia niente.
 */
public record SalvaAvvisoPayload(
        @NotNull(message = "l'auto e' obbligatoria")
        Long autoId,

        @NotNull(message = "la soglia e' obbligatoria")
        @DecimalMin(value = "1.00", message = "la soglia deve essere almeno 1")
        @Digits(integer = 10, fraction = 2, message = "soglia non valida")
        BigDecimal soglia
) {
}
