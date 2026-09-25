package org.example.progettosettimana18.services;

import org.example.progettosettimana18.entities.*;
import org.example.progettosettimana18.eventi.PrezzoCambiatoEvento;
import org.example.progettosettimana18.repositories.AutoRepository;
import org.example.progettosettimana18.repositories.AvvisoPrezzoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Qui si controlla la regola della traccia: una mail sola, e solo quando
 * il prezzo attraversa davvero la soglia.
 */
@ExtendWith(MockitoExtension.class)
class AvvisoPrezzoServiceTest {

    @Mock
    private AvvisoPrezzoRepository avvisoPrezzoRepository;

    @Mock
    private AutoRepository autoRepository;

    @Mock
    private MarcatoreAvvisi marcatore;

    @Mock
    private ServizioMail servizioMail;

    @InjectMocks
    private AvvisoPrezzoService servizio;

    private AvvisoPrezzo avviso;

    @BeforeEach
    void preparaUnAvviso() {
        Utente utente = new Utente("mario@example.com", "hash", "Mario", Ruolo.UTENTE);
        utente.setId(7L);

        Auto auto = new Auto();
        auto.setId(3L);
        auto.setMarca("Fiat");
        auto.setModello("Panda");
        auto.setPrezzo(new BigDecimal("8500.00"));

        avviso = new AvvisoPrezzo(utente, auto, new BigDecimal("9000.00"), "token-finto");
        avviso.setId(42L);
    }

    @Test
    @DisplayName("se il prezzo sale non si tocca nemmeno il database")
    void prezzoInSalita() {
        servizio.notificaAttraversamentoSoglia(
                new PrezzoCambiatoEvento(3L, new BigDecimal("8500.00"), new BigDecimal("9500.00")));

        verifyNoInteractions(avvisoPrezzoRepository, marcatore, servizioMail);
    }

    @Test
    @DisplayName("il prezzo attraversa la soglia: parte una mail")
    void attraversamentoVerso() {
        when(avvisoPrezzoRepository.daNotificare(eq(3L), any(), any())).thenReturn(List.of(avviso));
        when(marcatore.prendiIlSegno(42L)).thenReturn(true);

        servizio.notificaAttraversamentoSoglia(
                new PrezzoCambiatoEvento(3L, new BigDecimal("9500.00"), new BigDecimal("8500.00")));

        verify(servizioMail, times(1)).inviaAvvisoPrezzo(eq(avviso), eq(new BigDecimal("9500.00")));
    }

    @Test
    @DisplayName("se il segno se l'e' preso qualcun altro la mail non parte")
    void segnoGiaPreso() {
        when(avvisoPrezzoRepository.daNotificare(anyLong(), any(), any())).thenReturn(List.of(avviso));
        when(marcatore.prendiIlSegno(42L)).thenReturn(false);

        servizio.notificaAttraversamentoSoglia(
                new PrezzoCambiatoEvento(3L, new BigDecimal("9500.00"), new BigDecimal("8500.00")));

        verifyNoInteractions(servizioMail);
    }

    @Test
    @DisplayName("se Gmail non risponde l'avviso resta inviato e non si riprova")
    void mailFallita() {
        when(avvisoPrezzoRepository.daNotificare(anyLong(), any(), any())).thenReturn(List.of(avviso));
        when(marcatore.prendiIlSegno(42L)).thenReturn(true);
        doThrow(new IllegalStateException("Gmail non risponde"))
                .when(servizioMail).inviaAvvisoPrezzo(any(), any());

        assertThatCode(() -> servizio.notificaAttraversamentoSoglia(
                new PrezzoCambiatoEvento(3L, new BigDecimal("9500.00"), new BigDecimal("8500.00"))))
                .doesNotThrowAnyException();

        // Nessuno rimette l'avviso in coda: e' la scelta "al massimo una volta".
        verify(avvisoPrezzoRepository, never()).save(any());
        verify(marcatore, times(1)).prendiIlSegno(42L);
    }

    @Test
    @DisplayName("nessun avviso da notificare: niente mail")
    void nessunCandidato() {
        when(avvisoPrezzoRepository.daNotificare(anyLong(), any(), any())).thenReturn(List.of());

        servizio.notificaAttraversamentoSoglia(
                new PrezzoCambiatoEvento(3L, new BigDecimal("9500.00"), new BigDecimal("8500.00")));

        verifyNoInteractions(marcatore, servizioMail);
    }
}
