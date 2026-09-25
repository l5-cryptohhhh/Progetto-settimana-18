package org.example.progettosettimana18.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.progettosettimana18.entities.Utente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class GestoreToken {

    private final SecretKey chiave;
    private final long durataMillisecondi;

    public GestoreToken(@Value("${app.jwt.secret}") String segreto,
                        @Value("${app.jwt.durata-ore}") long durataOre) {
        // HS256 vuole almeno 32 byte: se JWT_SECRET e' corto il server non parte,
        // meglio accorgersene qui che avere un token firmato male in produzione.
        this.chiave = Keys.hmacShaKeyFor(segreto.getBytes(StandardCharsets.UTF_8));
        this.durataMillisecondi = Duration.ofHours(durataOre).toMillis();
    }

    /**
     * Dentro il token ci finiscono solo l'id e il ruolo. Niente email e niente nome:
     * un JWT non e' cifrato, chiunque lo intercetti legge quello che ci mettiamo.
     */
    public String creaToken(Utente utente) {
        long adesso = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(utente.getId()))
                .claim("ruolo", utente.getRuolo().name())
                .issuedAt(new Date(adesso))
                .expiration(new Date(adesso + durataMillisecondi))
                .signWith(chiave)
                .compact();
    }

    /**
     * Torna l'id dell'utente se il token e' firmato bene e non e' scaduto,
     * altrimenti null: chi chiama decide se e' un problema o no.
     */
    public Long leggiIdUtente(String token) {
        try {
            Claims contenuto = Jwts.parser()
                    .verifyWith(chiave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(contenuto.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
