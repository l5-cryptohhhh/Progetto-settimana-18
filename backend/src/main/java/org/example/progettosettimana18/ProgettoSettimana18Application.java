package org.example.progettosettimana18;

import org.example.progettosettimana18.config.DatabaseUrl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProgettoSettimana18Application {

    public static void main(String[] args) {
        // Su Render il database arriva come DATABASE_URL in formato postgresql://...
        // Va tradotto in un url JDBC prima che Spring provi a connettersi.
        DatabaseUrl.sistema();

        SpringApplication.run(ProgettoSettimana18Application.class, args);
    }
}
