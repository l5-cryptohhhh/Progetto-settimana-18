import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { BellRinging, BellSlash, Heart, Trash, WarningOctagon } from "@phosphor-icons/react";
import { api } from "../api";
import { useAuth } from "../auth";
import { giorno, prezzo, useTitolo } from "../formato";
import { Bottone, Campo, FotoAuto, LinkBottone, Messaggio, Scheletro, Vuoto, classeInput } from "../components/ui";

function Dati() {
  const { utente, setUtente } = useAuth();
  const [nome, setNome] = useState(utente.nome);
  const [esito, setEsito] = useState(null);
  const [inCorso, setInCorso] = useState(false);

  const salva = async (e) => {
    e.preventDefault();
    setInCorso(true);
    setEsito(null);
    try {
      // Il corpo porta solo il nome: email e ruolo non si toccano da qui.
      setUtente(await api.salvaProfilo({ nome: nome.trim() }));
      setEsito({ ok: true, testo: "Nome aggiornato." });
    } catch (err) {
      setEsito({ ok: false, testo: err.campi?.nome ?? err.message });
    } finally {
      setInCorso(false);
    }
  };

  return (
    <form onSubmit={salva} noValidate className="flex flex-col gap-5">
      <Campo etichetta="Nome" nome="nome">
        <input id="nome" value={nome} onChange={(e) => setNome(e.target.value)} minLength={2} maxLength={80} className={classeInput} />
      </Campo>
      <div className="flex flex-col gap-2">
        <span className="text-sm font-medium">Email</span>
        <p className="flex h-11 items-center rounded-lg bg-superficie-2 px-3.5 text-[15px] text-testo-2">{utente.email}</p>
      </div>
      <p className="text-sm text-testo-2">Iscritto dal {giorno(utente.creatoIl)}</p>
      {esito && <Messaggio tipo={esito.ok ? "ok" : "errore"}>{esito.testo}</Messaggio>}
      <Bottone type="submit" variante="secondario" disabled={inCorso || nome.trim() === utente.nome} className="self-start">
        Salva il nome
      </Bottone>
    </form>
  );
}

function StatoAvviso({ avviso }) {
  if (avviso.inviato) return <span className="inline-flex items-center gap-1.5 text-[13px] text-testo-2"><BellSlash size={14} /> Già scattato il {giorno(avviso.inviatoIl)}</span>;
  if (!avviso.attivo) return <span className="inline-flex items-center gap-1.5 text-[13px] text-testo-2"><BellSlash size={14} /> Disattivato</span>;
  return <span className="inline-flex items-center gap-1.5 text-[13px] text-ok"><BellRinging size={14} /> Attivo</span>;
}

function ZonaPericolosa() {
  const { esci } = useAuth();
  const naviga = useNavigate();
  const [conferma, setConferma] = useState(false);
  const [errore, setErrore] = useState(null);
  const [inCorso, setInCorso] = useState(false);

  const elimina = async () => {
    setInCorso(true);
    setErrore(null);
    try {
      await api.eliminaAccount();
      esci();
      naviga("/", { replace: true });
    } catch (err) {
      setErrore(err.message);
      setInCorso(false);
    }
  };

  return (
    <section className="rounded-lg border border-pericolo/40 p-6">
      <div className="flex items-start gap-3">
        <WarningOctagon size={22} className="mt-0.5 shrink-0 text-pericolo" />
        <div>
          <h2 className="text-lg font-bold tracking-tight">Elimina il mio account</h2>
          <p className="mt-1 max-w-[60ch] text-[15px] text-testo-2">
            Cancella subito account, preferiti e avvisi. Da quel momento non ti arriva più nessuna mail. Non si può annullare.
          </p>
        </div>
      </div>
      {errore && <Messaggio className="mt-4">{errore}</Messaggio>}
      <div className="mt-5 flex flex-wrap gap-2">
        {!conferma ? (
          <Bottone variante="secondario" onClick={() => setConferma(true)}>Elimina il mio account</Bottone>
        ) : (
          <>
            <Bottone variante="pericolo" onClick={elimina} disabled={inCorso}>
              <Trash size={18} /> Sì, elimina tutto
            </Bottone>
            <Bottone variante="fantasma" onClick={() => setConferma(false)} disabled={inCorso}>Annulla</Bottone>
          </>
        )}
      </div>
    </section>
  );
}

export default function Profilo() {
  useTitolo("Il tuo profilo");
  const { utente } = useAuth();
  const [preferiti, setPreferiti] = useState(null);
  const [avvisi, setAvvisi] = useState(null);
  const [errore, setErrore] = useState(null);

  useEffect(() => {
    Promise.all([api.preferiti(), api.avvisi()])
      .then(([p, a]) => { setPreferiti(p); setAvvisi(a); })
      .catch((e) => setErrore(e.message));
  }, []);

  const togliPreferito = async (id) => {
    try {
      await api.togliPreferito(id);
      setPreferiti((l) => l.filter((p) => p.id !== id));
    } catch (e) { setErrore(e.message); }
  };

  const eliminaAvviso = async (id) => {
    try {
      await api.eliminaAvviso(id);
      setAvvisi((l) => l.filter((a) => a.id !== id));
    } catch (e) { setErrore(e.message); }
  };

  return (
    <div className="mx-auto max-w-7xl px-4 pt-10 md:px-8 md:pt-14">
      <h1 className="text-4xl font-bold tracking-tight md:text-5xl">
        Ciao, <span className="text-testo-2">{utente.nome}</span>
      </h1>

      {errore && <Messaggio className="mt-6">{errore}</Messaggio>}

      <div className="mt-12 grid gap-14 lg:grid-cols-[minmax(0,22rem)_1fr]">
        <aside className="flex flex-col gap-6">
          <h2 className="text-lg font-bold tracking-tight">I tuoi dati</h2>
          <Dati />
        </aside>

        <div className="flex flex-col gap-14">
          <section>
            <h2 className="flex items-center gap-2 text-lg font-bold tracking-tight">
              <BellRinging size={20} className="text-accento" /> Avvisi di prezzo
            </h2>
            <div className="mt-5">
              {!avvisi ? (
                <Scheletro className="h-24 w-full" />
              ) : avvisi.length === 0 ? (
                <Vuoto titolo="Nessun avviso" azione={<LinkBottone to="/" variante="secondario">Vai al catalogo</LinkBottone>}>
                  Apri la scheda di un'auto e fissa il prezzo a cui la compreresti.
                </Vuoto>
              ) : (
                <ul className="divide-y divide-linea overflow-hidden rounded-lg border border-linea bg-superficie">
                  {avvisi.map((a) => (
                    <li key={a.id} className="grid grid-cols-[4.5rem_1fr_auto] items-center gap-4 p-4">
                      <Link to={`/auto/${a.auto.id}`} className="aspect-[4/3] overflow-hidden rounded-lg">
                        <FotoAuto auto={a.auto} />
                      </Link>
                      <div className="min-w-0">
                        <Link to={`/auto/${a.auto.id}`} className="block truncate font-medium hover:underline">
                          {a.auto.marca} {a.auto.modello}
                        </Link>
                        <p className="cifre mt-0.5 text-sm text-testo-2">
                          soglia {prezzo(a.soglia)} <span className="font-sans">ora</span> {prezzo(a.auto.prezzo)}
                        </p>
                        <div className="mt-1"><StatoAvviso avviso={a} /></div>
                      </div>
                      <Bottone variante="fantasma" misura="sm" onClick={() => eliminaAvviso(a.id)} aria-label={`Cancella l'avviso su ${a.auto.marca} ${a.auto.modello}`}>
                        <Trash size={16} />
                      </Bottone>
                    </li>
                  ))}
                </ul>
              )}
              {avvisi?.some((a) => a.inviato) && (
                <p className="mt-3 text-sm text-testo-2">
                  Un avviso già scattato non manda altre mail, nemmeno cambiando la soglia: cancellalo e rifallo dalla scheda dell'auto.
                </p>
              )}
            </div>
          </section>

          <section>
            <h2 className="flex items-center gap-2 text-lg font-bold tracking-tight">
              <Heart size={20} className="text-accento" /> Preferiti
            </h2>
            <div className="mt-5">
              {!preferiti ? (
                <Scheletro className="h-40 w-full" />
              ) : preferiti.length === 0 ? (
                <Vuoto titolo="Ancora nessun preferito">Tocca il cuore sulla scheda di un'auto per ritrovarla qui.</Vuoto>
              ) : (
                <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
                  {preferiti.map((p) => (
                    <li key={p.id} className="overflow-hidden rounded-lg border border-linea bg-superficie">
                      <Link to={`/auto/${p.auto.id}`} className="block aspect-[16/10] overflow-hidden">
                        <FotoAuto auto={p.auto} />
                      </Link>
                      <div className="flex items-center justify-between gap-3 p-4">
                        <div className="min-w-0">
                          <Link to={`/auto/${p.auto.id}`} className="block truncate font-medium hover:underline">
                            {p.auto.marca} {p.auto.modello}
                          </Link>
                          <p className="cifre text-sm text-testo-2">{prezzo(p.auto.prezzo)}</p>
                        </div>
                        <Bottone variante="fantasma" misura="sm" onClick={() => togliPreferito(p.id)} aria-label={`Togli ${p.auto.marca} ${p.auto.modello} dai preferiti`}>
                          <Heart size={18} weight="fill" className="text-accento" />
                        </Bottone>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </section>

          <ZonaPericolosa />
        </div>
      </div>
    </div>
  );
}
