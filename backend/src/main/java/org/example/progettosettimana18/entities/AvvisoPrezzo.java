package org.example.progettosettimana18.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lega un utente a un'auto, con la soglia e il segno che la mail e' gia' partita.
 */
@Entity
@Table(
        name = "avvisi_prezzo",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_avviso_utente_auto",
                columnNames = {"utente_id", "auto_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class AvvisoPrezzo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auto_id", nullable = false)
    private Auto auto;

    @Column(name = "soglia_prezzo", nullable = false, precision = 12, scale = 2)
    private BigDecimal sogliaPrezzo;

    /**
     * Una volta true non torna piu' indietro: ogni avviso manda una mail sola,
     * anche se il prezzo risale e poi riscende.
     */
    @Column(nullable = false)
    private boolean inviato = false;

    @Column(nullable = false)
    private boolean attivo = true;

    /**
     * Token casuale e monouso per il link "non avvisarmi piu'" dentro la mail.
     * Nel link non finisce mai l'id dell'avviso.
     */
    @Column(name = "token_disattivazione", unique = true, length = 64)
    private String tokenDisattivazione;

    @Column(name = "creato_il", nullable = false)
    private LocalDateTime creatoIl;

    @Column(name = "inviato_il")
    private LocalDateTime inviatoIl;

    public AvvisoPrezzo(Utente utente, Auto auto, BigDecimal sogliaPrezzo, String tokenDisattivazione) {
        this.utente = utente;
        this.auto = auto;
        this.sogliaPrezzo = sogliaPrezzo;
        this.tokenDisattivazione = tokenDisattivazione;
    }

    @PrePersist
    void primaDiSalvare() {
        if (creatoIl == null) creatoIl = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "AvvisoPrezzo#" + id;
    }
}
