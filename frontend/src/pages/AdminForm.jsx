import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Eye } from "@phosphor-icons/react";
import { api } from "../api";
import { ALIMENTAZIONI, prezzo, useTitolo } from "../formato";
import { PrezzoRapido } from "../components/PrezzoRapido";
import { Bottone, Campo, FotoAuto, LinkBottone, Messaggio, Scheletro, Vuoto, classeInput, leggiNumero } from "../components/ui";

const VUOTA = {
  marca: "", modello: "", anno: "", chilometri: "", alimentazione: "BENZINA",
  descrizione: "", immagineUrl: "", prezzo: "", prezzoAcquisto: "", stato: "BOZZA",
};

const inTesto = (auto) =>
  Object.fromEntries(Object.keys(VUOTA).map((k) => [k, auto[k] == null ? "" : String(auto[k])]));

export default function AdminForm() {
  const { id } = useParams();
  const nuova = !id;
  const naviga = useNavigate();
  useTitolo(nuova ? "Nuova auto" : "Modifica auto");

  const [auto, setAuto] = useState(null);
  const [dati, setDati] = useState(VUOTA);
  const [campi, setCampi] = useState({});
  const [errore, setErrore] = useState(null);
  const [esito, setEsito] = useState(null);
  const [inCorso, setInCorso] = useState(false);
  const [nonTrovata, setNonTrovata] = useState(false);

  useEffect(() => {
    if (nuova) return;
    api.adminDettaglio(id)
      .then((a) => { setAuto(a); setDati(inTesto(a)); })
      .catch((e) => (e.stato === 404 ? setNonTrovata(true) : setErrore(e.message)));
  }, [id, nuova]);

  const campo = (k) => (e) => setDati({ ...dati, [k]: e.target.value });

  const salva = async (e) => {
    e.preventDefault();
    setInCorso(true);
    setErrore(null);
    setEsito(null);
    setCampi({});
    const corpo = {
      marca: dati.marca.trim(),
      modello: dati.modello.trim(),
      anno: leggiNumero(dati.anno),
      chilometri: leggiNumero(dati.chilometri),
      alimentazione: dati.alimentazione,
      descrizione: dati.descrizione.trim() || null,
      immagineUrl: dati.immagineUrl.trim() || null,
      // In modifica il prezzo non passa da questo form: si cambia dal riquadro dedicato.
      prezzo: nuova ? leggiNumero(dati.prezzo) : Number(auto.prezzo),
      prezzoAcquisto: leggiNumero(dati.prezzoAcquisto),
      stato: dati.stato,
    };
    try {
      const salvata = nuova ? await api.adminCrea(corpo) : await api.adminAggiorna(id, corpo);
      if (nuova) {
        naviga(`/admin/auto/${salvata.id}`, { replace: true });
      } else {
        setAuto(salvata);
        setDati(inTesto(salvata));
        setEsito("Modifiche salvate.");
      }
    } catch (err) {
      setErrore(err.message);
      setCampi(err.campi ?? {});
    } finally {
      setInCorso(false);
    }
  };

  if (nonTrovata) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-24">
        <Vuoto titolo="Auto non trovata" azione={<LinkBottone to="/admin/auto" variante="secondario">Torna alla gestione</LinkBottone>} />
      </div>
    );
  }

  const input = (k, props = {}) => (
    <input id={k} value={dati[k]} onChange={campo(k)} aria-invalid={Boolean(campi[k])} className={`${classeInput} ${props.type === "number" ? "cifre" : ""}`} {...props} />
  );

  return (
    <div className="mx-auto max-w-7xl px-4 pt-8 md:px-8">
      <Link to="/admin/auto" className="inline-flex items-center gap-2 text-sm text-testo-2 hover:text-testo">
        <ArrowLeft size={16} /> Gestione
      </Link>
      <h1 className="mt-4 text-4xl font-bold tracking-tight">
        {nuova ? "Nuova auto" : auto ? `${auto.marca} ${auto.modello}` : "Modifica auto"}
      </h1>

      {!nuova && !auto && !errore ? (
        <Scheletro className="mt-10 h-96 w-full" />
      ) : (
        <div className="mt-10 grid gap-10 lg:grid-cols-[1fr_minmax(0,22rem)]">
          <form onSubmit={salva} noValidate className="flex flex-col gap-6">
            {errore && <Messaggio>{errore}</Messaggio>}
            {esito && <Messaggio tipo="ok">{esito}</Messaggio>}

            <div className="grid gap-5 md:grid-cols-2">
              <Campo etichetta="Marca" nome="marca" errore={campi.marca}>{input("marca", { maxLength: 60, required: true })}</Campo>
              <Campo etichetta="Modello" nome="modello" errore={campi.modello}>{input("modello", { maxLength: 80, required: true })}</Campo>
              <Campo etichetta="Anno" nome="anno" errore={campi.anno}>{input("anno", { type: "number", min: 1950, max: 2100, inputMode: "numeric" })}</Campo>
              <Campo etichetta="Chilometri" nome="chilometri" errore={campi.chilometri}>{input("chilometri", { type: "number", min: 0, inputMode: "numeric" })}</Campo>
              <Campo etichetta="Alimentazione" nome="alimentazione" errore={campi.alimentazione}>
                <select id="alimentazione" value={dati.alimentazione} onChange={campo("alimentazione")} className={classeInput}>
                  {Object.entries(ALIMENTAZIONI).map(([v, e]) => <option key={v} value={v}>{e}</option>)}
                </select>
              </Campo>
              <Campo etichetta="Stato" nome="stato" errore={campi.stato} aiuto="Le bozze non si vedono nel catalogo pubblico.">
                <select id="stato" value={dati.stato} onChange={campo("stato")} className={classeInput}>
                  <option value="BOZZA">Bozza</option>
                  <option value="PUBBLICATA">Pubblicata</option>
                </select>
              </Campo>
              {nuova && (
                <Campo etichetta="Prezzo di vendita (€)" nome="prezzo" errore={campi.prezzo}>{input("prezzo", { type: "number", min: 0, step: "0.01", inputMode: "decimal" })}</Campo>
              )}
              <Campo etichetta="Prezzo d'acquisto (€)" nome="prezzoAcquisto" errore={campi.prezzoAcquisto} aiuto="Lo vede solo l'amministratore.">
                {input("prezzoAcquisto", { type: "number", min: 0, step: "0.01", inputMode: "decimal" })}
              </Campo>
            </div>

            <Campo etichetta="Indirizzo dell'immagine" nome="immagineUrl" errore={campi.immagineUrl} aiuto="Un indirizzo https a una foto.">
              {input("immagineUrl", { type: "url", maxLength: 500, placeholder: "https://" })}
            </Campo>

            <Campo etichetta="Descrizione" nome="descrizione" errore={campi.descrizione} aiuto="Testo semplice: viene mostrato così com'è, senza formattazione.">
              <textarea id="descrizione" rows={7} maxLength={4000} value={dati.descrizione} onChange={campo("descrizione")} aria-invalid={Boolean(campi.descrizione)} className={`${classeInput} h-auto py-3 leading-relaxed`} />
            </Campo>

            <div className="flex flex-wrap gap-3">
              <Bottone type="submit" disabled={inCorso}>{nuova ? "Crea l'auto" : "Salva le modifiche"}</Bottone>
              {!nuova && auto?.stato === "PUBBLICATA" && (
                <LinkBottone to={`/auto/${auto.id}`} variante="secondario"><Eye size={18} /> Vedi la scheda</LinkBottone>
              )}
            </div>
          </form>

          <aside className="flex flex-col gap-6">
            <div className="aspect-[4/3] overflow-hidden rounded-lg bg-superficie-2">
              <FotoAuto auto={{ ...dati, marca: dati.marca || "Anteprima" }} />
            </div>

            {!nuova && auto && (
              <section className="rounded-lg border border-linea bg-superficie p-5">
                <h2 className="font-bold tracking-tight">Prezzo di vendita</h2>
                <p className="mt-1 text-sm text-testo-2">
                  Ora {prezzo(auto.prezzo)}. Se lo abbassi sotto la soglia di qualcuno, a quella persona parte una mail. Una sola, anche se poi lo cambi ancora.
                </p>
                <div className="mt-4">
                  <PrezzoRapido key={auto.prezzo} auto={auto} onSalvato={(a) => { setAuto(a); setEsito("Prezzo aggiornato."); }} />
                </div>
              </section>
            )}
          </aside>
        </div>
      )}
    </div>
  );
}
