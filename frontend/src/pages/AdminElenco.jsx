import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowDown, ArrowUp, MagnifyingGlass, PencilSimple, Plus, Trash } from "@phosphor-icons/react";
import { api } from "../api";
import { ORDINAMENTI, chilometri, prezzo, useTitolo } from "../formato";
import { PrezzoRapido } from "../components/PrezzoRapido";
import { Bottone, FotoAuto, LinkBottone, Messaggio, Paginazione, Scheletro, Vuoto, classeInput } from "../components/ui";

function Stato({ stato }) {
  return stato === "PUBBLICATA" ? (
    <span className="inline-flex rounded-full bg-ok-tenue px-2.5 py-0.5 text-[13px] font-medium text-ok">Pubblicata</span>
  ) : (
    <span className="inline-flex rounded-full bg-superficie-2 px-2.5 py-0.5 text-[13px] font-medium text-testo-2">Bozza</span>
  );
}

export default function AdminElenco() {
  useTitolo("Gestione auto");
  const [testo, setTesto] = useState("");
  const [q, setQ] = useState("");
  const [ordina, setOrdina] = useState("recenti");
  const [direzione, setDirezione] = useState("desc");
  const [pagina, setPagina] = useState(0);
  const [risultato, setRisultato] = useState(null);
  const [errore, setErrore] = useState(null);
  const [daEliminare, setDaEliminare] = useState(null);

  useEffect(() => {
    const t = setTimeout(() => { setQ(testo.trim()); setPagina(0); }, 350);
    return () => clearTimeout(t);
  }, [testo]);

  useEffect(() => {
    let annullato = false;
    setErrore(null);
    api.adminAuto({ q, ordina, direzione, pagina, dimensione: 20 })
      .then((r) => !annullato && setRisultato(r))
      .catch((e) => !annullato && setErrore(e.message));
    return () => { annullato = true; };
  }, [q, ordina, direzione, pagina]);

  const sostituisci = (aggiornata) =>
    setRisultato((r) => ({ ...r, contenuto: r.contenuto.map((a) => (a.id === aggiornata.id ? aggiornata : a)) }));

  const elimina = async (auto) => {
    try {
      await api.adminElimina(auto.id);
      setRisultato((r) => ({ ...r, contenuto: r.contenuto.filter((a) => a.id !== auto.id), totaleElementi: r.totaleElementi - 1 }));
      setDaEliminare(null);
    } catch (e) {
      setErrore(e.message);
    }
  };

  return (
    <div className="mx-auto max-w-7xl px-4 pt-10 md:px-8 md:pt-14">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-4xl font-bold tracking-tight">Gestione auto</h1>
          <p className="mt-2 max-w-[60ch] text-testo-2">
            Bozze comprese. Abbassando il prezzo da qui, chi ha una soglia più alta riceve la mail.
          </p>
        </div>
        <LinkBottone to="/admin/auto/nuova">
          <Plus size={18} weight="bold" /> Nuova auto
        </LinkBottone>
      </div>

      <div className="mt-8 grid gap-3 sm:grid-cols-[1fr_auto_auto]">
        <div className="relative">
          <label htmlFor="cerca-admin" className="sr-only">Cerca marca o modello</label>
          <MagnifyingGlass size={18} className="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-testo-2" />
          <input id="cerca-admin" type="search" value={testo} onChange={(e) => setTesto(e.target.value)} placeholder="Cerca marca o modello" className={`${classeInput} pl-10`} />
        </div>
        <label htmlFor="ordina-admin" className="sr-only">Ordina per</label>
        <select id="ordina-admin" value={ordina} onChange={(e) => { setOrdina(e.target.value); setPagina(0); }} className={classeInput}>
          {ORDINAMENTI.map((o) => <option key={o.valore} value={o.valore}>{o.etichetta}</option>)}
        </select>
        <Bottone variante="secondario" onClick={() => { setDirezione((d) => (d === "asc" ? "desc" : "asc")); setPagina(0); }}>
          {direzione === "asc" ? <ArrowUp size={16} weight="bold" /> : <ArrowDown size={16} weight="bold" />}
          {direzione === "asc" ? "Crescente" : "Decrescente"}
        </Bottone>
      </div>

      {errore && <Messaggio className="mt-6">{errore}</Messaggio>}

      <div className="mt-6">
        {!risultato && !errore && <Scheletro className="h-72 w-full" />}

        {risultato && risultato.contenuto.length === 0 && (
          <Vuoto titolo={q ? "Nessuna auto trovata" : "Nessuna auto ancora"} azione={<LinkBottone to="/admin/auto/nuova">Nuova auto</LinkBottone>}>
            Le auto salvate come bozza compaiono solo qui, finché non le pubblichi.
          </Vuoto>
        )}

        {risultato && risultato.contenuto.length > 0 && (
          <div className="overflow-x-auto rounded-lg border border-linea bg-superficie">
            <table className="w-full min-w-[820px] text-left text-sm">
              <thead className="text-[13px] text-testo-2">
                <tr className="border-b border-linea">
                  <th scope="col" className="px-4 py-3 font-medium">Auto</th>
                  <th scope="col" className="px-4 py-3 font-medium">Stato</th>
                  <th scope="col" className="px-4 py-3 font-medium">Prezzo</th>
                  <th scope="col" className="px-4 py-3 font-medium">Acquisto</th>
                  <th scope="col" className="px-4 py-3 font-medium">Margine</th>
                  <th scope="col" className="px-4 py-3"><span className="sr-only">Azioni</span></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-linea">
                {risultato.contenuto.map((a) => {
                  const margine = a.prezzoAcquisto != null ? Number(a.prezzo) - Number(a.prezzoAcquisto) : null;
                  return (
                    <tr key={a.id} className="align-middle">
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-3">
                          <div className="aspect-[4/3] w-16 shrink-0 overflow-hidden rounded-[8px]"><FotoAuto auto={a} /></div>
                          <div className="min-w-0">
                            <Link to={`/admin/auto/${a.id}`} className="block truncate font-medium hover:underline">{a.marca} {a.modello}</Link>
                            <span className="cifre text-[13px] text-testo-2">{a.anno}, {chilometri(a.chilometri)}</span>
                          </div>
                        </div>
                      </td>
                      <td className="px-4 py-3"><Stato stato={a.stato} /></td>
                      <td className="px-4 py-3"><PrezzoRapido key={`${a.id}-${a.prezzo}`} auto={a} onSalvato={sostituisci} compatto /></td>
                      <td className="cifre px-4 py-3 text-testo-2">{prezzo(a.prezzoAcquisto)}</td>
                      <td className={`cifre px-4 py-3 ${margine != null && margine < 0 ? "text-pericolo" : ""}`}>{margine == null ? "-" : prezzo(margine)}</td>
                      <td className="px-4 py-3">
                        <div className="flex justify-end gap-1">
                          {daEliminare === a.id ? (
                            <>
                              <Bottone variante="pericolo" misura="sm" onClick={() => elimina(a)}>Elimina</Bottone>
                              <Bottone variante="fantasma" misura="sm" onClick={() => setDaEliminare(null)}>No</Bottone>
                            </>
                          ) : (
                            <>
                              <LinkBottone to={`/admin/auto/${a.id}`} variante="fantasma" misura="sm" aria-label={`Modifica ${a.marca} ${a.modello}`}>
                                <PencilSimple size={16} />
                              </LinkBottone>
                              <Bottone variante="fantasma" misura="sm" onClick={() => setDaEliminare(a.id)} aria-label={`Elimina ${a.marca} ${a.modello}`}>
                                <Trash size={16} />
                              </Bottone>
                            </>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {risultato && <Paginazione pagina={risultato.pagina} totalePagine={risultato.totalePagine} onCambia={setPagina} />}
      </div>
    </div>
  );
}
