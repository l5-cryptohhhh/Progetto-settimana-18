package org.example.progettosettimana18.exceptions;

public class RichiestaNonValidaException extends RuntimeException {
    public RichiestaNonValidaException(String messaggio) {
        super(messaggio);
    }
}
