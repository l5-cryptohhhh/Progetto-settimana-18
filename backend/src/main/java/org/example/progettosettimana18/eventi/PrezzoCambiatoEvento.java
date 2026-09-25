package org.example.progettosettimana18.eventi;

import java.math.BigDecimal;

/**
 * Dentro l'evento ci sono solo numeri e un id, nessuna entita'.
 * Il listener gira su un altro thread e a transazione chiusa: se qui dentro
 * mettessimo un'Auto o un Utente ci ritroveremmo con oggetti staccati dalla sessione.
 */
public record PrezzoCambiatoEvento(
        Long autoId,
        BigDecimal prezzoVecchio,
        BigDecimal prezzoNuovo
) {
}
