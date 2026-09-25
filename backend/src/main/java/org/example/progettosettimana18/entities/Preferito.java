package org.example.progettosettimana18.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "preferiti",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_preferito_utente_auto",
                columnNames = {"utente_id", "auto_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Preferito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utente_id", nullable = false)
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auto_id", nullable = false)
    private Auto auto;

    @Column(name = "aggiunto_il", nullable = false)
    private LocalDateTime aggiuntoIl;

    public Preferito(Utente utente, Auto auto) {
        this.utente = utente;
        this.auto = auto;
    }

    @PrePersist
    void primaDiSalvare() {
        if (aggiuntoIl == null) aggiuntoIl = LocalDateTime.now();
    }
}
