import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { motion } from "motion/react";
import { ArrowLeft, BellRinging, BellSlash, Heart, PencilSimple, Trash } from "@phosphor-icons/react";
import { api } from "../api";
import { useAuth } from "../auth";
import { ALIMENTAZIONI, chilometri, giorno, prezzo, useTitolo } from "../formato";
import { Bottone, Campo, FotoAuto, LinkBottone, Messaggio, Scheletro, Vuoto, classeInput, leggiNumero } from "../components/ui";

function PannelloAvviso({ auto, avviso, onCambio }) {
  const [soglia, setSoglia] = useState(avviso ? String(avviso.soglia) : "");
  const [modifica, setModifica] = useState(false);
  const [errore, setErrore] = useState(null);
  const [inCorso, setInCorso] = useState(false);

  useEffect(() => {
    setSoglia(avviso ? String(avviso.soglia) : "");
    setModifica(false);
  }, [avviso]);

  const valore = leggiNumero(soglia);
  const giaSotto = valore !== null && !Number.isNaN(valore) && valore >= Number(auto.prezzo);

  const salva = async (e) => {
    e.preventDefault();
    if (valore === null || Number.isNaN(valore) || valore < 1) {
      setErrore("Scrivi una cifra in euro, almeno 1");
      return;
    }
    setInCorso(true);
    setErrore(null);
    try {
      const nuovo = avviso ? await api.cambiaSoglia(avviso.id, valore) : await api.creaAvviso(auto.id, valore);
      onCambio(nuovo);
    } catch (err) {
      setErrore(err.campi?.soglia ?? err.message);
    } finally {
      setInCorso(false);
    }
  };

  const elimina = async () => {
    setInCorso(true);
    setErrore(null);
    try {
      await api.eliminaAvviso(avviso.id);
      onCambio(null);
    } catch (err) {
      setErrore(err.message);
    } finally {
      setInCorso(false);
    }
  };

  if (avviso && !modifica) {
    const scattato = avviso.inviato;
    const spento = !avviso.attivo;
    return (
      <div className="flex flex-col gap-4">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-sm text-testo-2">La tua soglia</p>
            <p className="cifre text-2xl font-semibold">{prezzo(avviso.soglia)}</p>
          </div>
          <span className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-[13px] font-medium ${scattato || spento ? "bg-superficie-2 text-testo-2" : "bg-ok-tenue text-ok"}`}>
            {scattato || spento ? <BellSlash size={14} weight="bold" /> : <BellRinging size={14} weight="bold" />}
            {scattato ? "Già scattato" : spento ? "Disattivato" : "Attivo"}
          </span>
        </div>

        {scattato && (
          <p className="rounded-lg bg-accento-tenue px-4 py-3 text-sm text-testo">
            Questo avviso è già scattato il {giorno(avviso.inviatoIl)}: la mail non ripartirà. Cancellalo e rifallo per essere riavvisato.
          </p>
        )}
        {!scattato && spento && (
          <p className="rounded-lg bg-superficie-2 px-4 py-3 text-sm text-testo-2">
            L'hai disattivato dal link nella mail. Cancellalo e rifallo se vuoi tornare a essere avvisato.
          </p>
        )}
        {!scattato && !spento && (
          <p className="text-sm text-testo-2">Ti scriviamo quando il prezzo scende a {prezzo(avviso.soglia)} o meno.</p>
        )}

        {errore && <Messaggio>{errore}</Messaggio>}

        <div className="flex flex-wrap gap-2">
          {!scattato && !spento && (
            <Bottone variante="secondario" misura="sm" onClick={() => setModifica(true)}>
              <PencilSimple size={16} /> Cambia soglia
            </Bottone>
          )}
          <Bottone variante="fantasma" misura="sm" onClick={elimina} disabled={inCorso}>
            <Trash size={16} /> Cancella avviso
          </Bottone>
        </div>
      </div>
    );
  }

  return (
    <form onSubmit={salva} noValidate className="flex flex-col gap-4">
      <Campo
        etichetta={avviso ? "Nuova soglia" : "Avvisami quando costa al massimo"}
        nome="soglia"
        errore={errore}
        aiuto={giaSotto ? "Il prezzo è già sotto questa cifra: la mail parte solo se scende da sopra la soglia." : "Una sola mail, quando il prezzo arriva alla soglia."}
      >
        <div className="relative">
          <input
            id="soglia"
            type="number"
            inputMode="decimal"
            min="1"
            step="100"
            value={soglia}
            onChange={(e) => setSoglia(e.target.value)}
            placeholder={String(Math.round(Number(auto.prezzo) * 0.9))}
            aria-invalid={Boolean(errore)}
            aria-describedby={errore ? "soglia-errore" : "soglia-aiuto"}
            className={`${classeInput} cifre pr-10`}
          />
          <span className="pointer-events-none absolute top-1/2 right-3.5 -translate-y-1/2 text-testo-2">€</span>
        </div>
      </Campo>
      <div className="flex gap-2">
        <Bottone type="submit" disabled={inCorso}>
          <BellRinging size={18} /> {avviso ? "Salva soglia" : "Avvisami"}
        </Bottone>
        {avviso && (
          <Bottone type="button" variante="fantasma" onClick={() => setModifica(false)}>
            Annulla
          </Bottone>
        )}
      </div>
    </form>
  );
}

function BottonePreferito({ auto, preferito, onCambio }) {
  const [inCorso, setInCorso] = useState(false);
  const [errore, setErrore] = useState(null);
  const attivo = Boolean(preferito);

  const cambia = async () => {
    setInCorso(true);
    setErrore(null);
    try {
      if (attivo) {
        await api.togliPreferito(preferito.id);
        onCambio(null);
      } else {
        onCambio(await api.aggiungiPreferito(auto.id));
      }
    } catch (err) {
      setErrore(err.message);
    } finally {
      setInCorso(false);
    }
  };

  return (
    <div className="flex flex-col items-end gap-1">
      <motion.button
        type="button"
        onClick={cambia}
        disabled={inCorso}
        whileTap={{ scale: 0.9 }}
        aria-pressed={attivo}
        aria-label={attivo ? "Togli dai preferiti" : "Aggiungi ai preferiti"}
        className={`grid size-12 place-items-center rounded-full border transition-colors ${
          attivo ? "border-accento bg-accento-tenue text-accento" : "border-linea bg-superficie text-testo-2 hover:text-testo"
        }`}
      >
        <motion.span key={String(attivo)} initial={{ scale: 0.6 }} animate={{ scale: 1 }} transition={{ type: "spring", stiffness: 420, damping: 16 }}>
          <Heart size={22} weight={attivo ? "fill" : "regular"} />
        </motion.span>
      </motion.button>
      {errore && <span className="text-[13px] text-pericolo">{errore}</span>}
    </div>
  );
}

export default function SchedaAuto() {
  const { id } = useParams();
  const { utente, admin, pronto } = useAuth();
  const [auto, setAuto] = useState(null);
  const [errore, setErrore] = useState(null);
  const [avviso, setAvviso] = useState(null);
  const [preferito, setPreferito] = useState(null);
  useTitolo(auto ? `${auto.marca} ${auto.modello}` : "Scheda auto");

  useEffect(() => {
    setAuto(null);
    setErrore(null);
    api.auto(id).then(setAuto).catch(setErrore);
  }, [id]);

  const caricaPersonali = useCallback(() => {
    if (!utente) return;
    Promise.all([api.avvisi(), api.preferiti()])
      .then(([avvisi, preferiti]) => {
        setAvviso(avvisi.find((a) => String(a.auto.id) === String(id)) ?? null);
        setPreferito(preferiti.find((p) => String(p.auto.id) === String(id)) ?? null);
      })
      .catch(() => {});
  }, [utente, id]);

  useEffect(() => {
    caricaPersonali();
  }, [caricaPersonali]);

  if (errore) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-24 md:px-8">
        <Vuoto
          titolo={errore.stato === 404 ? "Quest'auto non è in vetrina" : "Non riusciamo a caricare la scheda"}
          azione={<LinkBottone to="/" variante="secondario">Torna al catalogo</LinkBottone>}
        >
          {errore.stato === 404 ? "Potrebbe essere stata venduta o ritirata dal catalogo." : errore.message}
        </Vuoto>
      </div>
    );
  }

  return (
    <article className="mx-auto max-w-7xl px-4 pt-8 md:px-8">
      <Link to="/" className="inline-flex items-center gap-2 text-sm text-testo-2 hover:text-testo">
        <ArrowLeft size={16} /> Catalogo
      </Link>

      <div className="mt-6 grid gap-10 lg:grid-cols-[1.4fr_1fr] lg:gap-14">
        <div className="aspect-[4/3] overflow-hidden rounded-lg bg-superficie-2">
          {auto ? <FotoAuto auto={auto} grande /> : <Scheletro className="h-full w-full rounded-none" />}
        </div>

        <div className="flex flex-col gap-8">
          {!auto ? (
            <div className="flex flex-col gap-4">
              <Scheletro className="h-4 w-24" />
              <Scheletro className="h-10 w-64" />
              <Scheletro className="h-8 w-40" />
            </div>
          ) : (
            <>
              <header className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-testo-2">{auto.marca}</p>
                  <h1 className="text-4xl font-bold leading-[1.05] tracking-tight md:text-5xl">{auto.modello}</h1>
                  <p className="cifre mt-4 text-3xl font-semibold">{prezzo(auto.prezzo)}</p>
                </div>
                {pronto && utente && <BottonePreferito auto={auto} preferito={preferito} onCambio={setPreferito} />}
              </header>

              <dl className="grid grid-cols-3 gap-px overflow-hidden rounded-lg border border-linea bg-linea">
                {[
                  ["Anno", auto.anno],
                  ["Chilometri", chilometri(auto.chilometri)],
                  ["Alimentazione", ALIMENTAZIONI[auto.alimentazione]],
                ].map(([k, v]) => (
                  <div key={k} className="bg-superficie px-4 py-4">
                    <dt className="text-[13px] text-testo-2">{k}</dt>
                    <dd className="cifre mt-1 font-semibold">{v}</dd>
                  </div>
                ))}
              </dl>

              <section className="rounded-lg border border-linea bg-superficie p-5 md:p-6">
                {!pronto ? (
                  <Scheletro className="h-20 w-full" />
                ) : utente ? (
                  <PannelloAvviso auto={auto} avviso={avviso} onCambio={setAvviso} />
                ) : (
                  <div className="flex flex-col gap-4">
                    <div className="flex items-start gap-3">
                      <BellRinging size={22} weight="duotone" className="mt-0.5 shrink-0 text-accento" />
                      <p className="text-[15px] leading-relaxed">
                        Fissa il prezzo a cui la compreresti: quando ci arriva ti mandiamo una mail. Serve un account.
                      </p>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      <LinkBottone to="/accesso" state={{ da: `/auto/${auto.id}` }}>Accedi</LinkBottone>
                      <LinkBottone to="/registrazione" variante="secondario">Registrati</LinkBottone>
                    </div>
                  </div>
                )}
              </section>

              {admin && (
                <LinkBottone to={`/admin/auto/${auto.id}`} variante="secondario" className="self-start">
                  <PencilSimple size={16} /> Modifica in gestione
                </LinkBottone>
              )}
            </>
          )}
        </div>
      </div>

      {auto?.descrizione && (
        <section className="mt-14 max-w-[68ch]">
          <h2 className="text-xl font-bold tracking-tight">Descrizione</h2>
          {/* Testo e basta: niente HTML interpretato, gli a capo restano a capo. */}
          <p className="mt-4 whitespace-pre-line leading-relaxed text-testo-2">{auto.descrizione}</p>
        </section>
      )}
    </article>
  );
}
