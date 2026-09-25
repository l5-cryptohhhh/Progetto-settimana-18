package org.example.progettosettimana18.services;

import org.example.progettosettimana18.exceptions.RichiestaNonValidaException;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * Il campo su cui ordinare arriva dal client e finisce dentro la clausola ORDER BY:
 * li' non si puo' legare come parametro, quindi non lo si tocca mai direttamente.
 * Quello che arriva si confronta con questo elenco chiuso e si usa il nome
 * della proprieta' che sta scritto qui, non quello che ha scritto l'utente.
 */
public final class OrdinamentoAuto {

    private static final Map<String, String> CAMPI_AMMESSI = Map.of(
            "prezzo", "prezzo",
            "anno", "anno",
            "chilometri", "chilometri",
            "marca", "marca",
            "recenti", "creataIl"
    );

    private static final String PREDEFINITO = "recenti";

    private OrdinamentoAuto() {
    }

    public static Sort traduci(String campoRichiesto, String direzioneRichiesta) {
        String chiave = (campoRichiesto == null || campoRichiesto.isBlank())
                ? PREDEFINITO
                : campoRichiesto.trim().toLowerCase();

        String proprieta = CAMPI_AMMESSI.get(chiave);
        if (proprieta == null) {
            // Nel messaggio non rimettiamo dentro quello che ha scritto l'utente:
            // gli diciamo solo quali sono i valori buoni.
            throw new RichiestaNonValidaException(
                    "Ordinamento non ammesso. Valori validi: " + String.join(", ", CAMPI_AMMESSI.keySet()));
        }

        Sort.Direction direzione = "asc".equalsIgnoreCase(direzioneRichiesta)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direzione, proprieta);
    }
}
