package org.example.progettosettimana18.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "utenti")
@Getter
@Setter
@NoArgsConstructor
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    /** Qui dentro finisce solo l'hash BCrypt, mai la password in chiaro. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 80)
    private String nome;

    /**
     * Il ruolo lo decide il server: la registrazione crea sempre un UTENTE,
     * l'ADMIN nasce solo dal seed che legge le variabili d'ambiente.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Ruolo ruolo = Ruolo.UTENTE;

    @Column(name = "creato_il", nullable = false)
    private LocalDateTime creatoIl;

    public Utente(String email, String passwordHash, String nome, Ruolo ruolo) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nome = nome;
        this.ruolo = ruolo;
    }

    @PrePersist
    void primaDiSalvare() {
        if (creatoIl == null) creatoIl = LocalDateTime.now();
    }

    /**
     * Niente email e niente hash qui dentro: il toString finisce nei log
     * appena qualcosa va storto.
     */
    @Override
    public String toString() {
        return "Utente#" + id + " (" + ruolo + ")";
    }
}
