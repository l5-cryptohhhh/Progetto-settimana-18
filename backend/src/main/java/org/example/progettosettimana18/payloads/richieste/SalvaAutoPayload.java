package org.example.progettosettimana18.payloads.richieste;

import jakarta.validation.constraints.*;
import org.example.progettosettimana18.entities.Alimentazione;
import org.example.progettosettimana18.entities.StatoAuto;

import java.math.BigDecimal;

/**
 * Creazione e modifica dell'auto, riservate all'amministratore.
 */
public record SalvaAutoPayload(
        @NotBlank(message = "la marca e' obbligatoria")
        @Size(max = 60, message = "la marca e' troppo lunga")
        String marca,

        @NotBlank(message = "il modello e' obbligatorio")
        @Size(max = 80, message = "il modello e' troppo lungo")
        String modello,

        @NotNull(message = "l'anno e' obbligatorio")
        @Min(value = 1950, message = "anno troppo vecchio")
        @Max(value = 2100, message = "anno non valido")
        Integer anno,

        @NotNull(message = "i chilometri sono obbligatori")
        @Min(value = 0, message = "i chilometri non possono essere negativi")
        Integer chilometri,

        @NotNull(message = "l'alimentazione e' obbligatoria")
        Alimentazione alimentazione,

        @Size(max = 4000, message = "la descrizione e' troppo lunga")
        String descrizione,

        @Size(max = 500, message = "l'indirizzo dell'immagine e' troppo lungo")
        String immagineUrl,

        @NotNull(message = "il prezzo e' obbligatorio")
        @DecimalMin(value = "0.00", message = "il prezzo non puo' essere negativo")
        @Digits(integer = 10, fraction = 2, message = "prezzo non valido")
        BigDecimal prezzo,

        @DecimalMin(value = "0.00", message = "il prezzo d'acquisto non puo' essere negativo")
        @Digits(integer = 10, fraction = 2, message = "prezzo d'acquisto non valido")
        BigDecimal prezzoAcquisto,

        @NotNull(message = "lo stato e' obbligatorio")
        StatoAuto stato
) {
}
