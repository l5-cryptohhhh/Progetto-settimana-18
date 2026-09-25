package org.example.progettosettimana18.services;

import org.example.progettosettimana18.repositories.AvvisoPrezzoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Sta in una classe sua perche' il segno deve girare nella propria transazione:
 * chi lo chiama e' il listener, che parte a transazione gia' chiusa e su un altro thread.
 */
@Component
public class MarcatoreAvvisi {

    private final AvvisoPrezzoRepository avvisoPrezzoRepository;

    public MarcatoreAvvisi(AvvisoPrezzoRepository avvisoPrezzoRepository) {
        this.avvisoPrezzoRepository = avvisoPrezzoRepository;
    }

    /**
     * UPDATE ... SET inviato = true WHERE id = ? AND inviato = false.
     * Il database aggiorna una riga sola: se due modifiche di prezzo ravvicinate
     * arrivano insieme, una delle due si porta a casa la riga e l'altra vede zero
     * righe aggiornate e non manda niente.
     *
     * @return true solo se il segno l'abbiamo preso noi
     */
    @Transactional
    public boolean prendiIlSegno(Long avvisoId) {
        return avvisoPrezzoRepository.segnaComeInviato(avvisoId, LocalDateTime.now()) == 1;
    }
}
