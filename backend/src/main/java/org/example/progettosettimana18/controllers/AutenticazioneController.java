package org.example.progettosettimana18.controllers;

import jakarta.validation.Valid;
import org.example.progettosettimana18.entities.Utente;
import org.example.progettosettimana18.payloads.richieste.LoginPayload;
import org.example.progettosettimana18.payloads.richieste.RegistrazionePayload;
import org.example.progettosettimana18.payloads.risposte.AccessoRisposta;
import org.example.progettosettimana18.payloads.risposte.UtenteRisposta;
import org.example.progettosettimana18.security.GestoreToken;
import org.example.progettosettimana18.services.UtenteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AutenticazioneController {

    private final UtenteService utenteService;
    private final GestoreToken gestoreToken;

    public AutenticazioneController(UtenteService utenteService, GestoreToken gestoreToken) {
        this.utenteService = utenteService;
        this.gestoreToken = gestoreToken;
    }

    @PostMapping("/registrazione")
    @ResponseStatus(HttpStatus.CREATED)
    public UtenteRisposta registrazione(@RequestBody @Valid RegistrazionePayload dati) {
        return UtenteRisposta.da(utenteService.registra(dati));
    }

    @PostMapping("/login")
    public AccessoRisposta login(@RequestBody @Valid LoginPayload dati) {
        Utente utente = utenteService.verificaCredenziali(dati);
        return new AccessoRisposta(gestoreToken.creaToken(utente), UtenteRisposta.da(utente));
    }
}
