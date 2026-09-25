package org.example.progettosettimana18.services;

import org.example.progettosettimana18.entities.Auto;
import org.example.progettosettimana18.entities.StatoAuto;
import org.example.progettosettimana18.eventi.PrezzoCambiatoEvento;
import org.example.progettosettimana18.exceptions.NonTrovatoException;
import org.example.progettosettimana18.payloads.richieste.SalvaAutoPayload;
import org.example.progettosettimana18.repositories.AutoRepository;
import org.example.progettosettimana18.repositories.AvvisoPrezzoRepository;
import org.example.progettosettimana18.repositories.PreferitoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AutoService {

    private static final Logger log = LoggerFactory.getLogger(AutoService.class);

    /** Tetto alla dimensione della pagina: nessuno si porta via il catalogo in una botta sola. */
    private static final int DIMENSIONE_MASSIMA_PAGINA = 50;
    private static final BigDecimal PREZZO_MINIMO_PREDEFINITO = BigDecimal.ZERO;
    private static final BigDecimal PREZZO_MASSIMO_PREDEFINITO = new BigDecimal("99999999.99");

    private final AutoRepository autoRepository;
    private final PreferitoRepository preferitoRepository;
    private final AvvisoPrezzoRepository avvisoPrezzoRepository;
    private final ApplicationEventPublisher editore;

    public AutoService(AutoRepository autoRepository,
                       PreferitoRepository preferitoRepository,
                       AvvisoPrezzoRepository avvisoPrezzoRepository,
                       ApplicationEventPublisher editore) {
        this.autoRepository = autoRepository;
        this.preferitoRepository = preferitoRepository;
        this.avvisoPrezzoRepository = avvisoPrezzoRepository;
        this.editore = editore;
    }

    // ---------- catalogo pubblico ----------

    public Page<Auto> catalogoPubblico(String testoCercato, BigDecimal prezzoMin, BigDecimal prezzoMax,
                                       String ordina, String direzione, int pagina, int dimensione) {
        return autoRepository.cerca(
                StatoAuto.PUBBLICATA,
                preparaTesto(testoCercato),
                prezzoMin == null ? PREZZO_MINIMO_PREDEFINITO : prezzoMin,
                prezzoMax == null ? PREZZO_MASSIMO_PREDEFINITO : prezzoMax,
                preparaPagina(ordina, direzione, pagina, dimensione));
    }

    public Auto dettaglioPubblico(Long id) {
        return autoRepository.findByIdAndStato(id, StatoAuto.PUBBLICATA)
                .orElseThrow(() -> new NonTrovatoException("Auto non trovata"));
    }

    // ---------- lato amministratore ----------

    public Page<Auto> catalogoCompleto(String testoCercato, BigDecimal prezzoMin, BigDecimal prezzoMax,
                                       String ordina, String direzione, int pagina, int dimensione) {
        return autoRepository.cercaTutte(
                preparaTesto(testoCercato),
                prezzoMin == null ? PREZZO_MINIMO_PREDEFINITO : prezzoMin,
                prezzoMax == null ? PREZZO_MASSIMO_PREDEFINITO : prezzoMax,
                preparaPagina(ordina, direzione, pagina, dimensione));
    }

    public Auto trova(Long id) {
        return autoRepository.findById(id)
                .orElseThrow(() -> new NonTrovatoException("Auto non trovata"));
    }

    @Transactional
    public Auto crea(SalvaAutoPayload dati) {
        Auto auto = new Auto();
        copia(dati, auto);
        auto.setPrezzo(dati.prezzo());
        Auto salvata = autoRepository.save(auto);
        log.info("Creata auto {}", salvata.getId());
        return salvata;
    }

    /**
     * Modifica completa. Se dentro c'e' anche un prezzo diverso da prima,
     * l'evento parte da qui esattamente come parte dal cambio prezzo dedicato.
     */
    @Transactional
    public Auto aggiorna(Long id, SalvaAutoPayload dati) {
        Auto auto = trova(id);
        copia(dati, auto);
        applicaPrezzo(auto, dati.prezzo());
        return autoRepository.save(auto);
    }

    @Transactional
    public Auto cambiaPrezzo(Long id, BigDecimal nuovoPrezzo) {
        Auto auto = trova(id);
        applicaPrezzo(auto, nuovoPrezzo);
        return autoRepository.save(auto);
    }

    @Transactional
    public void elimina(Long id) {
        Auto auto = trova(id);
        // Prima vanno via avvisi e preferiti, altrimenti la chiave esterna si mette di traverso.
        avvisoPrezzoRepository.deleteByAutoId(auto.getId());
        preferitoRepository.deleteByAutoId(auto.getId());
        autoRepository.delete(auto);
        log.info("Eliminata auto {}", id);
    }

    // ---------- pezzi interni ----------

    /**
     * Il prezzo si cambia solo da qui, cosi' l'evento non si scorda mai.
     * Se il prezzo e' identico a prima non pubblichiamo niente: risalvare
     * lo stesso numero non e' un cambio di prezzo.
     */
    private void applicaPrezzo(Auto auto, BigDecimal nuovoPrezzo) {
        BigDecimal prezzoVecchio = auto.getPrezzo();
        auto.setPrezzo(nuovoPrezzo);

        if (prezzoVecchio != null && prezzoVecchio.compareTo(nuovoPrezzo) != 0) {
            editore.publishEvent(new PrezzoCambiatoEvento(auto.getId(), prezzoVecchio, nuovoPrezzo));
        }
    }

    private void copia(SalvaAutoPayload dati, Auto auto) {
        auto.setMarca(dati.marca().trim());
        auto.setModello(dati.modello().trim());
        auto.setAnno(dati.anno());
        auto.setChilometri(dati.chilometri());
        auto.setAlimentazione(dati.alimentazione());
        auto.setDescrizione(dati.descrizione());
        auto.setImmagineUrl(dati.immagineUrl());
        auto.setPrezzoAcquisto(dati.prezzoAcquisto());
        auto.setStato(dati.stato());
    }

    private String preparaTesto(String testoCercato) {
        if (testoCercato == null || testoCercato.isBlank()) return "%";
        return "%" + testoCercato.trim().toLowerCase() + "%";
    }

    private Pageable preparaPagina(String ordina, String direzione, int pagina, int dimensione) {
        int numero = Math.max(pagina, 0);
        int quante = Math.clamp(dimensione, 1, DIMENSIONE_MASSIMA_PAGINA);
        return PageRequest.of(numero, quante, OrdinamentoAuto.traduci(ordina, direzione));
    }
}
