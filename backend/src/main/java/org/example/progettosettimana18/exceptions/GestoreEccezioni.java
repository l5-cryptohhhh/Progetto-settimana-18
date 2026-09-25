package org.example.progettosettimana18.exceptions;

import org.example.progettosettimana18.payloads.risposte.ErroreRisposta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GestoreEccezioni {

    private static final Logger log = LoggerFactory.getLogger(GestoreEccezioni.class);

    @ExceptionHandler(NonTrovatoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroreRisposta nonTrovato(NonTrovatoException ex) {
        return ErroreRisposta.di(404, ex.getMessage());
    }

    @ExceptionHandler(RichiestaNonValidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroreRisposta richiestaNonValida(RichiestaNonValidaException ex) {
        return ErroreRisposta.di(400, ex.getMessage());
    }

    @ExceptionHandler(ConflittoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroreRisposta conflitto(ConflittoException ex) {
        return ErroreRisposta.di(409, ex.getMessage());
    }

    /**
     * Messaggio volutamente generico: non diciamo se a sbagliare e' stata l'email
     * o la password, e non ripetiamo l'email dentro la risposta.
     */
    @ExceptionHandler(CredenzialiErrateException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErroreRisposta credenzialiErrate(CredenzialiErrateException ex) {
        return ErroreRisposta.di(401, "Email o password non corretti");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroreRisposta validazione(MethodArgumentNotValidException ex) {
        Map<String, String> campi = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(errore -> campi.put(errore.getField(), errore.getDefaultMessage()));
        return ErroreRisposta.diValidazione(400, "Dati non validi", campi);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroreRisposta corpoIllegibile(Exception ex) {
        return ErroreRisposta.di(400, "Richiesta non leggibile");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroreRisposta vincoloViolato(DataIntegrityViolationException ex) {
        log.warn("Vincolo del database violato");
        return ErroreRisposta.di(409, "Operazione in conflitto con dati gia' presenti");
    }

    /**
     * Ultima rete. Lo stack trace resta nei log del server, al client va una frase secca:
     * i messaggi interni raccontano troppo di come e' fatto il backend.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroreRisposta imprevisto(Exception ex) {
        log.error("Errore non previsto", ex);
        return ErroreRisposta.di(500, "Qualcosa e' andato storto, riprova piu' tardi");
    }
}
