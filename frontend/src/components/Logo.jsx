/*
 * Il marchio Autoven: la linea dell'auto sopra la scritta, «AUTO» nel colore del
 * fondo opposto e «VEN» in rosso. Due varianti come da manuale del logo:
 *   chiaro    → su sfondo nero, AUTO bianco
 *   scuro     → su sfondo chiaro, AUTO nero
 * Mai sfumature, ombre o colori fuori palette.
 */

/** La silhouette dell'auto: tetto nel colore del testo, coda rossa. */
export function Silhouette({ className = "", colore = "currentColor" }) {
  return (
    <svg viewBox="0 0 200 32" aria-hidden="true" className={className} fill="none">
      <path
        d="M3 30C30 24 52 7 104 4c34-2 62 7 88 21-27-10-54-15-88-13C58 14 34 26 3 30Z"
        fill={colore}
      />
      <path d="M118 12c30-1 57 6 79 19-24-9-49-13-80-14l1-5Z" fill="var(--rosso)" />
    </svg>
  );
}

export function Logo({ variante = "chiaro", conMotto = false, className = "" }) {
  const auto = variante === "chiaro" ? "#ffffff" : "var(--nero)";
  return (
    <span className={`inline-flex flex-col items-center leading-none ${className}`}>
      {/* Larga quanto la scritta, come nel logo principale: misura in em, segue la dimensione del testo. */}
      <Silhouette colore={auto} className="-mb-[0.08em] w-[5.6em] translate-x-[0.15em]" />
      <span className="font-extrabold tracking-[0.02em]" style={{ fontSize: "1em" }}>
        <span style={{ color: auto }}>AUTO</span>
        <span style={{ color: "var(--rosso)" }}>VEN</span>
      </span>
      {conMotto && (
        <span className="mt-[0.5em] text-[0.19em] font-semibold tracking-[0.26em] whitespace-nowrap" style={{ color: auto }}>
          LA TUA PROSSIMA AUTO TI ASPETTA
        </span>
      )}
    </span>
  );
}

/** Il monogramma: una «A» con la linea rossa, per gli spazi piccoli. */
export function Monogramma({ className = "", colore = "#ffffff" }) {
  return (
    <svg viewBox="0 0 32 32" aria-hidden="true" className={className}>
      <path d="M13.2 5h5.6L28 27h-5.4l-6.6-16.4L9.4 27H4Z" fill={colore} />
      <path d="M2 23.5c8.5-5 17.5-7.6 28-7.4-9.3 1.6-17.5 4.6-24.6 9.3Z" fill="var(--rosso)" />
    </svg>
  );
}
