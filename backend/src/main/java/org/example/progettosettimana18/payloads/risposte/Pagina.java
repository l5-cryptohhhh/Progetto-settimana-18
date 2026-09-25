package org.example.progettosettimana18.payloads.risposte;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Involucro nostro invece di serializzare direttamente la Page di Spring:
 * il frontend legge sempre gli stessi campi anche se cambia la versione.
 */
public record Pagina<T>(
        List<T> contenuto,
        int pagina,
        int dimensione,
        long totaleElementi,
        int totalePagine,
        boolean ultima
) {
    public static <E, T> Pagina<T> da(Page<E> page, Function<E, T> conversione) {
        return new Pagina<>(
                page.getContent().stream().map(conversione).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
