package org.example.progettosettimana18.controllers;

import jakarta.validation.Valid;
import org.example.progettosettimana18.payloads.richieste.CambioPrezzoPayload;
import org.example.progettosettimana18.payloads.richieste.SalvaAutoPayload;
import org.example.progettosettimana18.payloads.risposte.AutoAdminRisposta;
import org.example.progettosettimana18.payloads.risposte.Pagina;
import org.example.progettosettimana18.services.AutoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Tutto quello che sta sotto /api/admin passa dalla regola hasRole("ADMIN")
 * scritta nella configurazione della sicurezza: un utente collegato che
 * arriva qui si prende un 403, non un 401.
 */
@RestController
@RequestMapping("/api/admin/auto")
public class AdminAutoController {

    private final AutoService autoService;

    public AdminAutoController(AutoService autoService) {
        this.autoService = autoService;
    }

    /** Qui dentro ci sono anche le bozze e il prezzo d'acquisto. */
    @GetMapping
    public Pagina<AutoAdminRisposta> elenco(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal prezzoMin,
            @RequestParam(required = false) BigDecimal prezzoMax,
            @RequestParam(required = false) String ordina,
            @RequestParam(required = false) String direzione,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int dimensione) {

        var risultato = autoService.catalogoCompleto(q, prezzoMin, prezzoMax, ordina, direzione, pagina, dimensione);
        return Pagina.da(risultato, AutoAdminRisposta::da);
    }

    @GetMapping("/{id}")
    public AutoAdminRisposta dettaglio(@PathVariable Long id) {
        return AutoAdminRisposta.da(autoService.trova(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutoAdminRisposta crea(@RequestBody @Valid SalvaAutoPayload dati) {
        return AutoAdminRisposta.da(autoService.crea(dati));
    }

    @PutMapping("/{id}")
    public AutoAdminRisposta aggiorna(@PathVariable Long id, @RequestBody @Valid SalvaAutoPayload dati) {
        return AutoAdminRisposta.da(autoService.aggiorna(id, dati));
    }

    /** Il cambio di prezzo da solo: e' l'operazione che fa partire gli avvisi. */
    @PatchMapping("/{id}/prezzo")
    public AutoAdminRisposta cambiaPrezzo(@PathVariable Long id, @RequestBody @Valid CambioPrezzoPayload dati) {
        return AutoAdminRisposta.da(autoService.cambiaPrezzo(id, dati.prezzo()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long id) {
        autoService.elimina(id);
    }
}
