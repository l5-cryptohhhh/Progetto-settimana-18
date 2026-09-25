package org.example.progettosettimana18.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.progettosettimana18.entities.Utente;
import org.example.progettosettimana18.repositories.UtenteRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class FiltroJwt extends OncePerRequestFilter {

    private final GestoreToken gestoreToken;
    private final UtenteRepository utenteRepository;

    public FiltroJwt(GestoreToken gestoreToken, UtenteRepository utenteRepository) {
        this.gestoreToken = gestoreToken;
        this.utenteRepository = utenteRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest richiesta,
                                    @NonNull HttpServletResponse risposta,
                                    @NonNull FilterChain catena) throws ServletException, IOException {

        String intestazione = richiesta.getHeader("Authorization");

        if (intestazione != null && intestazione.startsWith("Bearer ")) {
            Long idUtente = gestoreToken.leggiIdUtente(intestazione.substring(7));

            // Il ruolo lo rileggiamo dal database e non dal token: se l'amministratore
            // declassa qualcuno, il vecchio token non gli lascia i permessi di prima.
            if (idUtente != null) {
                Optional<Utente> trovato = utenteRepository.findById(idUtente);
                trovato.ifPresent(utente -> {
                    var permessi = List.of(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().name()));
                    var autenticazione = new UsernamePasswordAuthenticationToken(utente, null, permessi);
                    SecurityContextHolder.getContext().setAuthentication(autenticazione);
                });
            }
        }

        // Token assente o non valido: si va avanti da anonimi e ci pensa la catena
        // a rispondere 401 sulle rotte che richiedono l'accesso.
        catena.doFilter(richiesta, risposta);
    }
}
