package org.example.progettosettimana18.config;

import java.net.URI;

/**
 * Render passa il database in una sola variabile, scritta come
 * postgresql://utente:password@host:5432/nomedb. JDBC quella forma non la capisce,
 * quindi la spezziamo qui e la rimettiamo insieme come si aspetta il driver.
 *
 * Gira dal main, prima che Spring parta: quando il contesto legge
 * spring.datasource.url le proprieta' sono gia' al loro posto.
 */
public final class DatabaseUrl {

    private DatabaseUrl() {
    }

    public static void sistema() {
        String grezza = System.getenv("DATABASE_URL");

        // In locale non c'e' nessuna DATABASE_URL e va bene cosi': restano i valori
        // dell'application.yml. Se invece e' gia' in formato jdbc non c'e' niente da fare.
        if (grezza == null || grezza.isBlank() || grezza.startsWith("jdbc:")) {
            return;
        }

        URI indirizzo = URI.create(grezza);
        String credenziali = indirizzo.getUserInfo();

        if (credenziali == null || !credenziali.contains(":")) {
            throw new IllegalStateException("DATABASE_URL senza utente e password");
        }

        String[] pezzi = credenziali.split(":", 2);
        String porta = indirizzo.getPort() == -1 ? "" : ":" + indirizzo.getPort();
        String urlJdbc = "jdbc:postgresql://" + indirizzo.getHost() + porta + indirizzo.getPath();

        System.setProperty("spring.datasource.url", urlJdbc);
        System.setProperty("spring.datasource.username", pezzi[0]);
        System.setProperty("spring.datasource.password", pezzi[1]);

        // Nel log finisce l'host e basta: utente e password restano fuori.
        System.out.println("Database configurato su " + indirizzo.getHost());
    }
}
