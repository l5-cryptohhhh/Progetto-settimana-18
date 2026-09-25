import { useEffect } from "react";

// Il manuale di marca scrive i prezzi col simbolo davanti: «€ 32.900».
// useGrouping "always": in italiano Intl non separerebbe le migliaia sotto le cinque cifre.
const euro = new Intl.NumberFormat("it-IT", { minimumFractionDigits: 0, maximumFractionDigits: 2, useGrouping: "always" });
const intero = new Intl.NumberFormat("it-IT", { useGrouping: "always" });
const data = new Intl.DateTimeFormat("it-IT", { day: "numeric", month: "long", year: "numeric" });

export const prezzo = (n) => (n === null || n === undefined ? "-" : `€ ${euro.format(Number(n))}`);
export const chilometri = (n) => `${intero.format(n ?? 0)} km`;
export const giorno = (s) => (s ? data.format(new Date(s)) : "-");

export const ALIMENTAZIONI = {
  BENZINA: "Benzina",
  DIESEL: "Diesel",
  GPL: "GPL",
  METANO: "Metano",
  IBRIDA: "Ibrida",
  ELETTRICA: "Elettrica",
};

/** I soli cinque valori che il backend accetta per `ordina`: il menu non ne offre altri. */
export const ORDINAMENTI = [
  { valore: "recenti", etichetta: "Più recenti" },
  { valore: "prezzo", etichetta: "Prezzo" },
  { valore: "anno", etichetta: "Anno" },
  { valore: "chilometri", etichetta: "Chilometri" },
  { valore: "marca", etichetta: "Marca" },
];

/** Solo http e https finiscono in un src: niente javascript: o data: da un campo compilato a mano. */
export const urlSicuro = (url) => {
  if (!url) return null;
  try {
    const u = new URL(url);
    return u.protocol === "https:" || u.protocol === "http:" ? u.href : null;
  } catch {
    return null;
  }
};

export function useTitolo(titolo) {
  useEffect(() => {
    document.title = titolo ? `${titolo} | Autoven` : "Autoven";
  }, [titolo]);
}
