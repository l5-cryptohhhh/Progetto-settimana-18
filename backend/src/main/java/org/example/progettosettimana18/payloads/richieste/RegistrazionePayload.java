package org.example.progettosettimana18.payloads.richieste;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Il corpo della registrazione ha solo questi tre campi. Il ruolo non c'e':
 * chi lo aggiunge al JSON si ritrova comunque un UTENTE, perche' il campo
 * non esiste in questo record e nessuno lo legge.
 */
public record RegistrazionePayload(
        @NotBlank(message = "l'email e' obbligatoria")
        @Email(message = "l'email non e' valida")
        @Size(max = 180, message = "l'email e' troppo lunga")
        String email,

        @NotBlank(message = "la password e' obbligatoria")
        @Size(min = 8, max = 72, message = "la password deve avere tra 8 e 72 caratteri")
        String password,

        @NotBlank(message = "il nome e' obbligatorio")
        @Size(min = 2, max = 80, message = "il nome deve avere tra 2 e 80 caratteri")
        String nome
) {
}
