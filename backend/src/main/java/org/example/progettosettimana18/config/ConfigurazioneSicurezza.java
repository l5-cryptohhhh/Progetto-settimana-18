package org.example.progettosettimana18.config;

import jakarta.servlet.http.HttpServletResponse;
import org.example.progettosettimana18.payloads.risposte.ErroreRisposta;
import org.example.progettosettimana18.security.FiltroJwt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
public class ConfigurazioneSicurezza {

    private final FiltroJwt filtroJwt;
    private final ObjectMapper mapper;

    /** L'indirizzo esatto del frontend, niente asterischi. */
    @Value("${app.frontend.url}")
    private String urlFrontend;

    public ConfigurazioneSicurezza(FiltroJwt filtroJwt, ObjectMapper mapper) {
        this.filtroJwt = filtroJwt;
        this.mapper = mapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain catenaDiSicurezza(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(regoleCors()))
                .sessionManagement(sessioni -> sessioni.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotte -> rotte
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Il controllo di salute resta pubblico: serve a Render per capire
                        // se il servizio e' vivo, e non racconta niente di sensibile.
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                        // Registrazione e accesso
                        .requestMatchers(HttpMethod.POST, "/api/auth/registrazione", "/api/auth/login").permitAll()

                        // Il catalogo lo sfoglia anche chi non ha fatto l'accesso
                        .requestMatchers(HttpMethod.GET, "/api/auto", "/api/auto/*").permitAll()

                        // Il link "non avvisarmi piu'" della mail arriva da chi non e' loggato,
                        // la riconoscenza la fa il token monouso dentro il corpo.
                        .requestMatchers(HttpMethod.POST, "/api/avvisi/disattiva").permitAll()

                        // Creare auto e cambiare prezzi e' roba da amministratore:
                        // un utente collegato che ci prova si prende un 403.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(errori -> errori
                        .authenticationEntryPoint((richiesta, risposta, eccezione) ->
                                scrivi(risposta, HttpServletResponse.SC_UNAUTHORIZED, "Devi fare l'accesso"))
                        .accessDeniedHandler((richiesta, risposta, eccezione) ->
                                scrivi(risposta, HttpServletResponse.SC_FORBIDDEN, "Non hai i permessi per questa operazione"))
                )
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource regoleCors() {
        CorsConfiguration configurazione = new CorsConfiguration();
        configurazione.setAllowedOrigins(List.of(urlFrontend));
        configurazione.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configurazione.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configurazione.setAllowCredentials(false);
        configurazione.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource sorgente = new UrlBasedCorsConfigurationSource();
        sorgente.registerCorsConfiguration("/**", configurazione);
        return sorgente;
    }

    private void scrivi(HttpServletResponse risposta, int stato, String messaggio) throws java.io.IOException {
        risposta.setStatus(stato);
        risposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        risposta.setCharacterEncoding("UTF-8");
        mapper.writeValue(risposta.getWriter(), ErroreRisposta.di(stato, messaggio));
    }
}
