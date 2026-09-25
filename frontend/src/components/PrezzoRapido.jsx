import { useState } from "react";
import { Check, X } from "@phosphor-icons/react";
import { api } from "../api";
import { classeInput, leggiNumero } from "./ui";

/**
 * Il cambio prezzo rapido usa PATCH /prezzo: è questa chiamata che, se il prezzo
 * attraversa una soglia scendendo, fa partire le mail dopo il commit.
 */
export function PrezzoRapido({ auto, onSalvato, compatto = false }) {
  const [valore, setValore] = useState(String(auto.prezzo));
  const [errore, setErrore] = useState(null);
  const [inCorso, setInCorso] = useState(false);

  const n = leggiNumero(valore);
  const cambiato = n !== null && !Number.isNaN(n) && n !== Number(auto.prezzo);

  const salva = async (e) => {
    e.preventDefault();
    if (n === null || Number.isNaN(n) || n < 0) {
      setErrore("Prezzo non valido");
      return;
    }
    setInCorso(true);
    setErrore(null);
    try {
      const aggiornata = await api.adminCambiaPrezzo(auto.id, n);
      setValore(String(aggiornata.prezzo));
      onSalvato(aggiornata);
    } catch (err) {
      setErrore(err.campi?.prezzo ?? err.message);
    } finally {
      setInCorso(false);
    }
  };

  return (
    <form onSubmit={salva} className="flex flex-col gap-1">
      <div className="flex items-center gap-1.5">
        <label htmlFor={`prezzo-${auto.id}`} className="sr-only">Prezzo di {auto.marca} {auto.modello}</label>
        <input
          id={`prezzo-${auto.id}`}
          type="number"
          inputMode="decimal"
          min="0"
          step="100"
          value={valore}
          onChange={(e) => setValore(e.target.value)}
          className={`${classeInput} cifre ${compatto ? "h-9 max-w-32 text-sm" : ""}`}
        />
        {cambiato && (
          <>
            <button type="submit" disabled={inCorso} aria-label="Salva il prezzo" className="grid size-9 shrink-0 place-items-center rounded-full bg-accento text-accento-testo transition-transform active:scale-95 disabled:opacity-50">
              <Check size={16} weight="bold" />
            </button>
            <button type="button" onClick={() => { setValore(String(auto.prezzo)); setErrore(null); }} aria-label="Annulla" className="grid size-9 shrink-0 place-items-center rounded-full text-testo-2 hover:bg-superficie-2">
              <X size={16} weight="bold" />
            </button>
          </>
        )}
      </div>
      {errore && <span className="text-[13px] text-pericolo">{errore}</span>}
    </form>
  );
}
