package org.example.progettosettimana18.controllers;

import jakarta.validation.Valid;
import org.example.progettosettimana18.entities.Utente;
import org.example.progettosettimana18.payloads.richieste.AggiungiPreferitoPayload;
import org.example.progettosettimana18.payloads.risposte.PreferitoRisposta;
import org.example.progettosettimana18.services.PreferitoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/preferiti")
public class PreferitiController {

    private final PreferitoService preferitoService;

    public PreferitiController(PreferitoService preferitoService) {
        this.preferitoService = preferitoService;
    }

    /** Non esiste una rotta per farsi dare i preferiti di un altro: si parte sempre da chi e' collegato. */
    @GetMapping
    public List<PreferitoRisposta> miei(@AuthenticationPrincipal Utente utente) {
        return preferitoService.elenco(utente).stream().map(PreferitoRisposta::da).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PreferitoRisposta aggiungi(@AuthenticationPrincipal Utente utente,
                                      @RequestBody @Valid AggiungiPreferitoPayload dati) {
        return PreferitoRisposta.da(preferitoService.aggiungi(utente, dati.autoId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rimuovi(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        preferitoService.rimuovi(utente, id);
    }
}
