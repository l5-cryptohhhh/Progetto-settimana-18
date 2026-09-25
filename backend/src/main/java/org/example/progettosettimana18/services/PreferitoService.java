package org.example.progettosettimana18.services;

import org.example.progettosettimana18.entities.Auto;
import org.example.progettosettimana18.entities.Preferito;
import org.example.progettosettimana18.entities.StatoAuto;
import org.example.progettosettimana18.entities.Utente;
import org.example.progettosettimana18.exceptions.ConflittoException;
import org.example.progettosettimana18.exceptions.NonTrovatoException;
import org.example.progettosettimana18.repositories.AutoRepository;
import org.example.progettosettimana18.repositories.PreferitoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PreferitoService {

    private final PreferitoRepository preferitoRepository;
    private final AutoRepository autoRepository;

    public PreferitoService(PreferitoRepository preferitoRepository, AutoRepository autoRepository) {
        this.preferitoRepository = preferitoRepository;
        this.autoRepository = autoRepository;
    }

    public List<Preferito> elenco(Utente utente) {
        return preferitoRepository.elencoDi(utente.getId());
    }

    @Transactional
    public Preferito aggiungi(Utente utente, Long autoId) {
        // Solo auto pubblicate: sulle bozze non si mette il cuoricino.
        Auto auto = autoRepository.findByIdAndStato(autoId, StatoAuto.PUBBLICATA)
                .orElseThrow(() -> new NonTrovatoException("Auto non trovata"));

        if (preferitoRepository.existsByUtenteIdAndAutoId(utente.getId(), auto.getId())) {
            throw new ConflittoException("Questa auto e' gia' tra i tuoi preferiti");
        }

        return preferitoRepository.save(new Preferito(utente, auto));
    }

    /**
     * Cerchiamo per id e proprietario insieme: l'id di un altro utente
     * qui dentro semplicemente non esiste.
     */
    @Transactional
    public void rimuovi(Utente utente, Long preferitoId) {
        Preferito preferito = preferitoRepository.findByIdAndUtenteId(preferitoId, utente.getId())
                .orElseThrow(() -> new NonTrovatoException("Preferito non trovato"));
        preferitoRepository.delete(preferito);
    }
}
