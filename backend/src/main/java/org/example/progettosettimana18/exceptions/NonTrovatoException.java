package org.example.progettosettimana18.exceptions;

public class NonTrovatoException extends RuntimeException {
    public NonTrovatoException(String messaggio) {
        super(messaggio);
    }
}
