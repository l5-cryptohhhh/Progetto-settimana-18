import { useState } from "react";
import { Link } from "react-router-dom";
import { CaretLeft, CaretRight, WarningCircle, CheckCircle } from "@phosphor-icons/react";
import { Silhouette } from "./Logo";
import { urlSicuro } from "../formato";

const baseBottone =
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-full font-semibold " +
  "transition-[transform,background-color,color,border-color] duration-200 ease-molla " +
  "active:scale-[0.98] disabled:pointer-events-none disabled:opacity-50";

const varianti = {
  primario: "bg-accento text-accento-testo hover:bg-accento-forte",
  secondario: "border border-linea bg-superficie text-testo hover:border-testo-2",
  fantasma: "text-testo-2 hover:text-testo hover:bg-superficie-2",
  pericolo: "bg-pericolo text-accento-testo hover:opacity-90",
  // Sul nero di testata e apertura, come «Contattaci» nel sito di riferimento.
  contorno: "border border-white/70 text-white hover:bg-white hover:text-nero",
};

const misure = {
  sm: "h-9 px-4 text-sm",
  md: "h-11 px-5 text-[15px]",
};

export function Bottone({ variante = "primario", misura = "md", className = "", ...resto }) {
  return <button className={`${baseBottone} ${varianti[variante]} ${misure[misura]} ${className}`} {...resto} />;
}

export function LinkBottone({ variante = "primario", misura = "md", className = "", ...resto }) {
  return <Link className={`${baseBottone} ${varianti[variante]} ${misure[misura]} ${className}`} {...resto} />;
}

export const classeInput =
  "h-11 w-full rounded-lg border border-linea bg-superficie px-3.5 text-[15px] text-testo " +
  "placeholder:text-testo-2/80 transition-colors hover:border-testo-2/60 " +
  "focus:border-accento focus:outline-none focus:ring-2 focus:ring-accento/25 " +
  "aria-[invalid=true]:border-pericolo";

/** Etichetta sopra, aiuto sotto, errore sotto. Mai il placeholder al posto dell'etichetta. */
export function Campo({ etichetta, nome, errore, aiuto, children, className = "" }) {
  return (
    <div className={`flex flex-col gap-2 ${className}`}>
      <label htmlFor={nome} className="text-sm font-medium text-testo">
        {etichetta}
      </label>
      {children}
      {aiuto && !errore && <p id={`${nome}-aiuto`} className="text-[13px] text-testo-2">{aiuto}</p>}
      {errore && (
        <p id={`${nome}-errore`} className="text-[13px] text-pericolo">
          {errore}
        </p>
      )}
    </div>
  );
}

export function Messaggio({ tipo = "errore", children, className = "" }) {
  const stile = tipo === "ok" ? "bg-ok-tenue text-ok" : "bg-pericolo-tenue text-pericolo";
  const Icona = tipo === "ok" ? CheckCircle : WarningCircle;
  return (
    <div role={tipo === "ok" ? "status" : "alert"} className={`flex items-start gap-2.5 rounded-lg px-4 py-3 text-sm ${stile} ${className}`}>
      <Icona size={18} weight="bold" className="mt-px shrink-0" />
      <span>{children}</span>
    </div>
  );
}

export function Vuoto({ titolo, children, azione }) {
  return (
    <div className="rounded-lg border border-dashed border-linea px-6 py-14 text-center">
      <p className="text-lg font-bold tracking-tight">{titolo}</p>
      {children && <p className="mx-auto mt-2 max-w-[48ch] text-testo-2">{children}</p>}
      {azione && <div className="mt-6 flex justify-center">{azione}</div>}
    </div>
  );
}

/**
 * Foto dell'auto. Se manca o non si carica, al suo posto c'è una tavola con la marca:
 * meglio una marca scritta bene che un'immagine rotta.
 */
export function FotoAuto({ auto, className = "", grande = false }) {
  const [rotta, setRotta] = useState(false);
  const src = urlSicuro(auto?.immagineUrl);

  if (src && !rotta) {
    return (
      <img
        src={src}
        alt={`${auto.marca} ${auto.modello}`}
        loading={grande ? "eager" : "lazy"}
        referrerPolicy="no-referrer"
        onError={() => setRotta(true)}
        className={`h-full w-full object-cover ${className}`}
      />
    );
  }

  return (
    <div
      role="img"
      aria-label={`${auto?.marca ?? ""} ${auto?.modello ?? ""}, foto non disponibile`}
      className={`relative flex h-full w-full flex-col justify-between overflow-hidden bg-nero p-5 ${className}`}
    >
      <Silhouette colore="rgb(255 255 255 / 0.35)" className={grande ? "w-48" : "w-24"} />
      <span
        className={`font-extrabold leading-[0.9] text-white/15 ${grande ? "text-7xl md:text-8xl" : "text-5xl"}`}
      >
        {auto?.marca}
      </span>
    </div>
  );
}

export function Paginazione({ pagina, totalePagine, onCambia }) {
  if (!totalePagine || totalePagine <= 1) return null;
  return (
    <nav aria-label="Pagine" className="mt-12 flex items-center justify-between gap-4">
      <Bottone variante="secondario" misura="sm" disabled={pagina === 0} onClick={() => onCambia(pagina - 1)}>
        <CaretLeft size={16} weight="bold" /> Precedente
      </Bottone>
      <span className="cifre text-sm text-testo-2">
        {pagina + 1} di {totalePagine}
      </span>
      <Bottone variante="secondario" misura="sm" disabled={pagina + 1 >= totalePagine} onClick={() => onCambia(pagina + 1)}>
        Successiva <CaretRight size={16} weight="bold" />
      </Bottone>
    </nav>
  );
}

export function Scheletro({ className = "" }) {
  return <div aria-hidden="true" className={`scheletro rounded-lg ${className}`} />;
}

/** Dal campo al JSON: vuoto → null, la virgola decimale italiana vale come punto. */
export const leggiNumero = (testo) => {
  if (testo === "" || testo === null || testo === undefined) return null;
  const n = Number(String(testo).trim().replace(",", "."));
  return Number.isFinite(n) ? n : NaN;
};
