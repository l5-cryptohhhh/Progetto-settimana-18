package org.example.progettosettimana18.controllers;

import jakarta.validation.Valid;
import org.example.progettosettimana18.entities.Utente;
import org.example.progettosettimana18.payloads.richieste.AggiornaSogliaPayload;
import org.example.progettosettimana18.payloads.richieste.DisattivaAvvisoPayload;
import org.example.progettosettimana18.payloads.richieste.SalvaAvvisoPayload;
import org.example.progettosettimana18.payloads.risposte.AvvisoRisposta;
import org.example.progettosettimana18.payloads.risposte.EsitoRisposta;
import org.example.progettosettimana18.services.AvvisoPrezzoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avvisi")
public class AvvisiController {

    private final AvvisoPrezzoService avvisoPrezzoService;

    public AvvisiController(AvvisoPrezzoService avvisoPrezzoService) {
        this.avvisoPrezzoService = avvisoPrezzoService;
    }

    @GetMapping
    public List<AvvisoRisposta> miei(@AuthenticationPrincipal Utente utente) {
        return avvisoPrezzoService.elenco(utente).stream().map(AvvisoRisposta::da).toList();
    }

    /**
     * Cerchiamo per id e proprietario insieme. Chi cambia /api/avvisi/12 in
     * /api/avvisi/13 riceve 404, non 403: un 403 gli direbbe che quell'avviso esiste.
     */
    @GetMapping("/{id}")
    public AvvisoRisposta dettaglio(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        return AvvisoRisposta.da(avvisoPrezzoService.dettaglio(utente, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvvisoRisposta crea(@AuthenticationPrincipal Utente utente,
                               @RequestBody @Valid SalvaAvvisoPayload dati) {
        return AvvisoRisposta.da(avvisoPrezzoService.crea(utente, dati.autoId(), dati.soglia()));
    }

    @PutMapping("/{id}")
    public AvvisoRisposta aggiornaSoglia(@AuthenticationPrincipal Utente utente,
                                         @PathVariable Long id,
                                         @RequestBody @Valid AggiornaSogliaPayload dati) {
        return AvvisoRisposta.da(avvisoPrezzoService.aggiornaSoglia(utente, id, dati.soglia()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@AuthenticationPrincipal Utente utente, @PathVariable Long id) {
        avvisoPrezzoService.elimina(utente, id);
    }

    /**
     * Rotta pubblica: ci arriva chi clicca "non avvisarmi piu'" dentro la mail,
     * e a quel punto non ha nessun token di accesso in mano.
     * A riconoscerlo e' il token monouso dentro il corpo della richiesta.
     */
    @PostMapping("/disattiva")
    public EsitoRisposta disattiva(@RequestBody @Valid DisattivaAvvisoPayload dati) {
        avvisoPrezzoService.disattivaConToken(dati.token());
        return new EsitoRisposta("Avviso disattivato, non riceverai altre mail per questa auto");
    }
}
