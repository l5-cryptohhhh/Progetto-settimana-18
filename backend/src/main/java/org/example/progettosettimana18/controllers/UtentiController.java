package org.example.progettosettimana18.controllers;

import jakarta.validation.Valid;
import org.example.progettosettimana18.entities.Utente;
import org.example.progettosettimana18.payloads.richieste.AggiornaProfiloPayload;
import org.example.progettosettimana18.payloads.risposte.UtenteRisposta;
import org.example.progettosettimana18.services.UtenteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utenti")
public class UtentiController {

    private final UtenteService utenteService;

    public UtentiController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    /**
     * Sempre "me", mai /api/utenti/{id}: cosi' non esiste proprio la rotta
     * per farsi dare il profilo di un altro.
     */
    @GetMapping("/me")
    public UtenteRisposta profilo(@AuthenticationPrincipal Utente utente) {
        return UtenteRisposta.da(utente);
    }

    @PutMapping("/me")
    public UtenteRisposta aggiornaProfilo(@AuthenticationPrincipal Utente utente,
                                          @RequestBody @Valid AggiornaProfiloPayload dati) {
        return UtenteRisposta.da(utenteService.aggiornaProfilo(utente, dati));
    }

    /** "Elimina il mio account": porta via anche avvisi e preferiti. */
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminaAccount(@AuthenticationPrincipal Utente utente) {
        utenteService.eliminaAccount(utente);
    }
}
