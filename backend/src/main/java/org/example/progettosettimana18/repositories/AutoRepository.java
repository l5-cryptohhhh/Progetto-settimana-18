package org.example.progettosettimana18.repositories;

import org.example.progettosettimana18.entities.Auto;
import org.example.progettosettimana18.entities.StatoAuto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface AutoRepository extends JpaRepository<Auto, Long> {

    /**
     * Ricerca del catalogo. Tutto quello che arriva dal client entra come parametro
     * legato: non c'e' nessuna stringa concatenata dentro la query.
     * Il campo di ordinamento non passa da qui, viene tradotto prima dal service
     * confrontandolo con un elenco chiuso di valori ammessi.
     */
    @Query("""
            SELECT a FROM Auto a
            WHERE a.stato = :stato
              AND (LOWER(a.marca) LIKE :testo OR LOWER(a.modello) LIKE :testo)
              AND a.prezzo >= :prezzoMin
              AND a.prezzo <= :prezzoMax
            """)
    Page<Auto> cerca(@Param("stato") StatoAuto stato,
                     @Param("testo") String testo,
                     @Param("prezzoMin") BigDecimal prezzoMin,
                     @Param("prezzoMax") BigDecimal prezzoMax,
                     Pageable pagina);

    /** Stessa ricerca ma senza filtro sullo stato: la usa solo l'amministratore. */
    @Query("""
            SELECT a FROM Auto a
            WHERE (LOWER(a.marca) LIKE :testo OR LOWER(a.modello) LIKE :testo)
              AND a.prezzo >= :prezzoMin
              AND a.prezzo <= :prezzoMax
            """)
    Page<Auto> cercaTutte(@Param("testo") String testo,
                          @Param("prezzoMin") BigDecimal prezzoMin,
                          @Param("prezzoMax") BigDecimal prezzoMax,
                          Pageable pagina);

    Optional<Auto> findByIdAndStato(Long id, StatoAuto stato);
}
