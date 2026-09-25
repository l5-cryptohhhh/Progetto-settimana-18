package org.example.progettosettimana18.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auto")
@Getter
@Setter
@NoArgsConstructor
public class Auto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modello;

    @Column(nullable = false)
    private Integer anno;

    @Column(nullable = false)
    private Integer chilometri;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Alimentazione alimentazione;

    /**
     * Testo libero scritto dall'amministratore. Esce dal backend come stringa dentro
     * il JSON e il frontend lo stampa come testo, mai come HTML.
     */
    @Column(length = 4000)
    private String descrizione;

    @Column(name = "immagine_url", length = 500)
    private String immagineUrl;

    /** Prezzo di vendita: pubblico. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prezzo;

    /** Prezzo d'acquisto: lo vede solo l'amministratore, non entra nei DTO pubblici. */
    @Column(name = "prezzo_acquisto", precision = 12, scale = 2)
    private BigDecimal prezzoAcquisto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoAuto stato = StatoAuto.BOZZA;

    @Column(name = "creata_il", nullable = false)
    private LocalDateTime creataIl;

    @Column(name = "aggiornata_il")
    private LocalDateTime aggiornataIl;

    @PrePersist
    void primaDiSalvare() {
        if (creataIl == null) creataIl = LocalDateTime.now();
    }

    @PreUpdate
    void primaDiAggiornare() {
        aggiornataIl = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Auto#" + id + " " + marca + " " + modello;
    }
}
