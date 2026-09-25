import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { motion } from "motion/react";
import { ArrowDown, ArrowRight, ArrowUp, BellRinging, Car, Heart, MagnifyingGlass, Tag } from "@phosphor-icons/react";
import { api } from "../api";
import { useAuth } from "../auth";
import { ORDINAMENTI, prezzo, urlSicuro, useTitolo } from "../formato";
import { SchedaCatalogo, SchedaCatalogoVuota } from "../components/SchedaCatalogo";
import { Bottone, LinkBottone, Messaggio, Paginazione, Vuoto, classeInput } from "../components/ui";

const DIMENSIONE = 12;
const ORDINI_AMMESSI = new Set(ORDINAMENTI.map((o) => o.valore));

/** Aspetta che l'utente smetta di scrivere prima di chiedere al server. */
function useRitardato(valore, ms = 350) {
  const [ritardato, setRitardato] = useState(valore);
  useEffect(() => {
    const t = setTimeout(() => setRitardato(valore), ms);
    return () => clearTimeout(t);
  }, [valore, ms]);
  return ritardato;
}

function Vetrina() {
  const { utente } = useAuth();
  const [auto, setAuto] = useState(null);

  useEffect(() => {
    api.catalogo({ ordina: "recenti", dimensione: 6 })
      .then((p) => setAuto(p.contenuto.find((a) => urlSicuro(a.immagineUrl)) ?? null))
      .catch(() => setAuto(null));
  }, []);

  const vaiAlCatalogo = () => document.getElementById("catalogo")?.scrollIntoView({ behavior: "smooth" });

  return (
    <section className="relative isolate overflow-hidden bg-nero text-white">
      {/* La foto dell'ultima arrivata a destra, che sfuma nel nero come nel sito di riferimento. */}
      {auto && (
        <motion.div
          initial={{ opacity: 0, scale: 1.04 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 1.2, ease: [0.16, 1, 0.3, 1] }}
          className="absolute inset-y-0 right-0 -z-10 w-full md:w-[68%]"
        >
          <img src={urlSicuro(auto.immagineUrl)} alt="" referrerPolicy="no-referrer" className="h-full w-full object-cover" />
          <div className="absolute inset-0 bg-gradient-to-r from-nero via-nero/70 to-nero/10" />
          {/* Da telefono la foto sta dietro al testo: serve un velo in più perché si legga. */}
          <div className="absolute inset-0 bg-nero/55 md:hidden" />
          <div className="absolute inset-0 bg-gradient-to-t from-nero/80 via-transparent to-transparent" />
        </motion.div>
      )}

      <div className="mx-auto flex min-h-[520px] max-w-7xl flex-col justify-center px-4 py-20 md:min-h-[600px] md:px-8">
        <motion.div
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.15, ease: [0.16, 1, 0.3, 1] }}
          className="flex max-w-[34rem] flex-col items-start gap-6"
        >
          <h1 className="text-4xl leading-[1.08] font-bold md:text-[3.4rem]">
            La tua prossima auto ti aspetta.
          </h1>
          <p className="max-w-[42ch] text-base leading-relaxed text-white/80 md:text-lg">
            Auto usate selezionate, con un prezzo che puoi seguire: fissa la tua soglia e ti scriviamo quando ci arriva.
          </p>
          <div className="flex flex-wrap gap-3">
            <Bottone onClick={vaiAlCatalogo}>Vedi le auto</Bottone>
            {utente ? (
              <LinkBottone to="/profilo" variante="contorno">I tuoi avvisi</LinkBottone>
            ) : (
              <LinkBottone to="/registrazione" variante="contorno">Crea un account</LinkBottone>
            )}
          </div>
        </motion.div>

        {auto && (
          <Link
            to={`/auto/${auto.id}`}
            className="mt-14 hidden items-center gap-3 self-start text-sm md:inline-flex text-white/75 transition-colors hover:text-white md:absolute md:right-8 md:bottom-8 md:mt-0 lg:right-[max(2rem,calc((100vw-80rem)/2+2rem))]"
          >
            <span className="h-px w-8 bg-rosso" aria-hidden="true" />
            <span>
              Ultima arrivata: <span className="font-semibold text-white">{auto.marca} {auto.modello}</span>
            </span>
            <span className="cifre font-bold text-white">{prezzo(auto.prezzo)}</span>
            <ArrowRight size={16} weight="bold" className="text-rosso" />
          </Link>
        )}
      </div>
    </section>
  );
}

/** La fascia bianca sotto l'apertura: quello che il sito fa davvero, niente servizi che non esistono. */
const PUNTI = [
  { Icona: Car, righe: ["Auto usate", "e selezionate"] },
  { Icona: BellRinging, righe: ["Avvisi di prezzo", "via mail"] },
  { Icona: Heart, righe: ["Preferiti sempre", "a portata di mano"] },
  { Icona: Tag, righe: ["Una sola mail", "per ogni soglia"] },
];

function Servizi() {
  return (
    <section className="bg-superficie shadow-[0_1px_0_var(--linea)]">
      <ul className="mx-auto grid max-w-7xl grid-cols-2 gap-y-8 px-4 py-10 md:grid-cols-4 md:px-8">
        {PUNTI.map(({ Icona, righe }, i) => (
          <motion.li
            key={righe[0]}
            initial={{ opacity: 0, y: 10 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, amount: 0.6 }}
            transition={{ duration: 0.5, delay: i * 0.07, ease: [0.16, 1, 0.3, 1] }}
            className={`flex items-center gap-4 md:justify-center ${i > 0 ? "md:border-l md:border-linea" : ""}`}
          >
            <Icona size={34} weight="light" className="shrink-0 text-testo" />
            <p className="text-[13px] leading-snug font-medium text-testo-2">
              {righe[0]}
              <br />
              {righe[1]}
            </p>
          </motion.li>
        ))}
      </ul>
    </section>
  );
}

export default function Catalogo() {
  useTitolo(null);
  const [parametri, setParametri] = useSearchParams();

  const ordina = ORDINI_AMMESSI.has(parametri.get("ordina")) ? parametri.get("ordina") : "recenti";
  const direzione = parametri.get("direzione") === "asc" ? "asc" : "desc";
  const pagina = Math.max(0, Number.parseInt(parametri.get("pagina") ?? "0", 10) || 0);

  // I campi di testo vivono in locale e arrivano all'URL solo quando si smette di scrivere.
  const [testo, setTesto] = useState(parametri.get("q") ?? "");
  const [min, setMin] = useState(parametri.get("prezzoMin") ?? "");
  const [max, setMax] = useState(parametri.get("prezzoMax") ?? "");
  const q = useRitardato(testo.trim());
  const prezzoMin = useRitardato(min);
  const prezzoMax = useRitardato(max);

  const [risultato, setRisultato] = useState(null);
  const [errore, setErrore] = useState(null);
  const [caricamento, setCaricamento] = useState(true);

  const aggiorna = (modifiche, azzeraPagina = true) => {
    setParametri((attuali) => {
      const nuovi = new URLSearchParams(attuali);
      Object.entries(modifiche).forEach(([k, v]) => (v === "" || v == null ? nuovi.delete(k) : nuovi.set(k, v)));
      if (azzeraPagina) nuovi.delete("pagina");
      return nuovi;
    }, { replace: true });
  };

  useEffect(() => {
    if (q !== (parametri.get("q") ?? "") || prezzoMin !== (parametri.get("prezzoMin") ?? "") || prezzoMax !== (parametri.get("prezzoMax") ?? "")) {
      aggiorna({ q, prezzoMin, prezzoMax });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [q, prezzoMin, prezzoMax]);

  useEffect(() => {
    let annullato = false;
    setCaricamento(true);
    setErrore(null);
    api.catalogo({
      q: parametri.get("q"),
      prezzoMin: parametri.get("prezzoMin"),
      prezzoMax: parametri.get("prezzoMax"),
      ordina,
      direzione,
      pagina,
      dimensione: DIMENSIONE,
    })
      .then((r) => !annullato && setRisultato(r))
      .catch((e) => !annullato && setErrore(e.message))
      .finally(() => !annullato && setCaricamento(false));
    return () => { annullato = true; };
  }, [parametri, ordina, direzione, pagina]);

  const cambiaPagina = (n) => {
    aggiorna({ pagina: n || "" }, false);
    document.getElementById("catalogo")?.scrollIntoView({ behavior: "smooth" });
  };

  const filtriAttivi = parametri.get("q") || parametri.get("prezzoMin") || parametri.get("prezzoMax");

  return (
    <>
      <Vetrina />
      <Servizi />

      <section id="catalogo" className="mx-auto max-w-7xl scroll-mt-20 px-4 pt-16 md:px-8">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <h2 className="text-2xl font-bold md:text-3xl">Le nostre auto</h2>
          {risultato && (
            <p className="cifre text-sm font-semibold text-rosso" aria-live="polite">
              {risultato.totaleElementi} {risultato.totaleElementi === 1 ? "auto disponibile" : "auto disponibili"}
            </p>
          )}
        </div>

        <form
          role="search"
          onSubmit={(e) => e.preventDefault()}
          className="mt-8 grid gap-4 rounded-lg bg-superficie p-4 shadow-[0_1px_2px_rgb(13_13_13/0.06)] md:grid-cols-2 md:p-5 lg:grid-cols-[minmax(0,1.6fr)_repeat(2,minmax(0,0.7fr))_minmax(0,1fr)_auto] lg:items-end"
        >
          <div className="flex flex-col gap-2">
            <label htmlFor="q" className="text-sm font-medium">Cerca</label>
            <div className="relative">
              <MagnifyingGlass size={18} className="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-testo-2" />
              <input id="q" type="search" value={testo} onChange={(e) => setTesto(e.target.value)} placeholder="Marca o modello" maxLength={80} className={`${classeInput} pl-10`} />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4 md:col-span-1 lg:contents">
            <div className="flex flex-col gap-2">
              <label htmlFor="prezzoMin" className="text-sm font-medium">Prezzo da</label>
              <input id="prezzoMin" type="number" inputMode="numeric" min="0" step="500" value={min} onChange={(e) => setMin(e.target.value)} placeholder="€" className={`${classeInput} cifre`} />
            </div>
            <div className="flex flex-col gap-2">
              <label htmlFor="prezzoMax" className="text-sm font-medium">Prezzo fino a</label>
              <input id="prezzoMax" type="number" inputMode="numeric" min="0" step="500" value={max} onChange={(e) => setMax(e.target.value)} placeholder="€" className={`${classeInput} cifre`} />
            </div>
          </div>
          <div className="flex flex-col gap-2">
            <label htmlFor="ordina" className="text-sm font-medium">Ordina per</label>
            <select id="ordina" value={ordina} onChange={(e) => aggiorna({ ordina: e.target.value === "recenti" ? "" : e.target.value })} className={classeInput}>
              {ORDINAMENTI.map((o) => (
                <option key={o.valore} value={o.valore}>{o.etichetta}</option>
              ))}
            </select>
          </div>
          <Bottone
            type="button"
            variante="secondario"
            onClick={() => aggiorna({ direzione: direzione === "asc" ? "" : "asc" })}
            aria-label={`Direzione: ${direzione === "asc" ? "crescente" : "decrescente"}. Cambia`}
            className="md:self-end"
          >
            {direzione === "asc" ? <ArrowUp size={16} weight="bold" /> : <ArrowDown size={16} weight="bold" />}
            {direzione === "asc" ? "Crescente" : "Decrescente"}
          </Bottone>
        </form>

        <div className="mt-10">
          {errore && <Messaggio>{errore}</Messaggio>}

          {!errore && caricamento && !risultato && (
            <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {Array.from({ length: 6 }, (_, i) => <SchedaCatalogoVuota key={i} />)}
            </ul>
          )}

          {!errore && risultato && risultato.contenuto.length === 0 && (
            <Vuoto
              titolo={filtriAttivi ? "Nessuna auto con questi filtri" : "Il salone è vuoto, per ora"}
              azione={filtriAttivi && (
                <Bottone variante="secondario" onClick={() => { setTesto(""); setMin(""); setMax(""); }}>
                  Togli i filtri
                </Bottone>
              )}
            >
              {filtriAttivi ? "Prova ad allargare la fascia di prezzo o a cercare un'altra marca." : "Le auto compaiono qui appena vengono pubblicate."}
            </Vuoto>
          )}

          {!errore && risultato && risultato.contenuto.length > 0 && (
            <ul className={`grid grid-cols-1 gap-6 transition-opacity sm:grid-cols-2 lg:grid-cols-3 ${caricamento ? "opacity-60" : ""}`}>
              {risultato.contenuto.map((a, i) => <SchedaCatalogo key={a.id} auto={a} indice={i} />)}
            </ul>
          )}

          {risultato && <Paginazione pagina={risultato.pagina} totalePagine={risultato.totalePagine} onCambia={cambiaPagina} />}
        </div>
      </section>
    </>
  );
}
