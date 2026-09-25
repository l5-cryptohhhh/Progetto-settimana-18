package org.example.progettosettimana18.services;

import org.example.progettosettimana18.exceptions.RichiestaNonValidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdinamentoAutoTest {

    @Test
    @DisplayName("il nome della proprieta' e' quello dell'elenco, non quello scritto dal client")
    void traduceIValoriAmmessi() {
        Sort ordine = OrdinamentoAuto.traduci("prezzo", "asc");

        assertThat(ordine.getOrderFor("prezzo")).isNotNull();
        assertThat(ordine.getOrderFor("prezzo").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("senza campo ordina per data, dalla piu' recente")
    void valorePredefinito() {
        Sort ordine = OrdinamentoAuto.traduci(null, null);

        assertThat(ordine.getOrderFor("creataIl")).isNotNull();
        assertThat(ordine.getOrderFor("creataIl").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("un campo fuori elenco non arriva mai alla query")
    void rifiutaICampiNonAmmessi() {
        assertThatThrownBy(() -> OrdinamentoAuto.traduci("prezzoAcquisto", "asc"))
                .isInstanceOf(RichiestaNonValidaException.class);
    }

    @Test
    @DisplayName("nemmeno un tentativo di iniezione dentro l'ORDER BY")
    void rifiutaLIniezione() {
        assertThatThrownBy(() -> OrdinamentoAuto.traduci("prezzo; DROP TABLE auto", "asc"))
                .isInstanceOf(RichiestaNonValidaException.class);

        assertThatThrownBy(() -> OrdinamentoAuto.traduci("(SELECT 1)", "desc"))
                .isInstanceOf(RichiestaNonValidaException.class);
    }

    @Test
    @DisplayName("una direzione strana non fa danni, si torna a discendente")
    void direzioneStrana() {
        Sort ordine = OrdinamentoAuto.traduci("anno", "qualunque cosa");

        assertThat(ordine.getOrderFor("anno").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
