package org.example.progettosettimana18.controllers;

import org.example.progettosettimana18.payloads.risposte.AutoPubblicaRisposta;
import org.example.progettosettimana18.payloads.risposte.Pagina;
import org.example.progettosettimana18.services.AutoService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Il catalogo che sfoglia chiunque, anche senza accesso.
 * Escono solo le auto pubblicate e solo i campi pubblici.
 */
@RestController
@RequestMapping("/api/auto")
public class AutoController {

    private final AutoService autoService;

    public AutoController(AutoService autoService) {
        this.autoService = autoService;
    }

    @GetMapping
    public Pagina<AutoPubblicaRisposta> catalogo(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal prezzoMin,
            @RequestParam(required = false) BigDecimal prezzoMax,
            @RequestParam(required = false) String ordina,
            @RequestParam(required = false) String direzione,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int dimensione) {

        var risultato = autoService.catalogoPubblico(q, prezzoMin, prezzoMax, ordina, direzione, pagina, dimensione);
        return Pagina.da(risultato, AutoPubblicaRisposta::da);
    }

    /** Una bozza qui dentro risponde 404: per chi non e' amministratore non esiste. */
    @GetMapping("/{id}")
    public AutoPubblicaRisposta dettaglio(@PathVariable Long id) {
        return AutoPubblicaRisposta.da(autoService.dettaglioPubblico(id));
    }
}
