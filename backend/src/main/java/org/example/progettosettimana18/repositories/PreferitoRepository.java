package org.example.progettosettimana18.repositories;

import org.example.progettosettimana18.entities.Preferito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PreferitoRepository extends JpaRepository<Preferito, Long> {

    @Query("SELECT p FROM Preferito p JOIN FETCH p.auto WHERE p.utente.id = :utenteId ORDER BY p.aggiuntoIl DESC")
    List<Preferito> elencoDi(Long utenteId);

    /**
     * Id e proprietario insieme: chi prova l'id di un altro non trova niente e si becca un 404.
     */
    Optional<Preferito> findByIdAndUtenteId(Long id, Long utenteId);

    boolean existsByUtenteIdAndAutoId(Long utenteId, Long autoId);

    void deleteByUtenteId(Long utenteId);

    void deleteByAutoId(Long autoId);
}
