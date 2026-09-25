package org.example.progettosettimana18.repositories;

import org.example.progettosettimana18.entities.AvvisoPrezzo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AvvisoPrezzoRepository extends JpaRepository<AvvisoPrezzo, Long> {

    @Query("""
            SELECT a FROM AvvisoPrezzo a
            JOIN FETCH a.auto
            WHERE a.utente.id = :utenteId
            ORDER BY a.creatoIl DESC
            """)
    List<AvvisoPrezzo> elencoDi(Long utenteId);

    /**
     * Id e proprietario insieme. Chi cambia /api/avvisi/12 in /api/avvisi/13
     * non trova niente: non deve nemmeno sapere che l'avviso di un altro esiste.
     */
    Optional<AvvisoPrezzo> findByIdAndUtenteId(Long id, Long utenteId);

    Optional<AvvisoPrezzo> findByUtenteIdAndAutoId(Long utenteId, Long autoId);

    Optional<AvvisoPrezzo> findByTokenDisattivazione(String tokenDisattivazione);

    /**
     * Gli avvisi che il cambio di prezzo ha attraversato: prima la soglia era sopra
     * il prezzo vecchio, adesso il prezzo nuovo e' uguale o sotto la soglia.
     * Chi era gia' sotto soglia non rientra, quindi risalvare lo stesso prezzo
     * o abbassarlo ancora non fa partire niente.
     */
    @Query("""
            SELECT a FROM AvvisoPrezzo a
            JOIN FETCH a.utente
            JOIN FETCH a.auto
            WHERE a.auto.id = :autoId
              AND a.attivo = true
              AND a.inviato = false
              AND a.sogliaPrezzo < :prezzoVecchio
              AND a.sogliaPrezzo >= :prezzoNuovo
            """)
    List<AvvisoPrezzo> daNotificare(@Param("autoId") Long autoId,
                                    @Param("prezzoVecchio") BigDecimal prezzoVecchio,
                                    @Param("prezzoNuovo") BigDecimal prezzoNuovo);

    /**
     * Il segno si prende in un colpo solo: se due modifiche ravvicinate arrivano
     * insieme, una sola delle due si porta a casa la riga e manda la mail.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE AvvisoPrezzo a
            SET a.inviato = true, a.inviatoIl = :ora
            WHERE a.id = :id AND a.inviato = false
            """)
    int segnaComeInviato(@Param("id") Long id, @Param("ora") LocalDateTime ora);

    void deleteByUtenteId(Long utenteId);

    void deleteByAutoId(Long autoId);
}
