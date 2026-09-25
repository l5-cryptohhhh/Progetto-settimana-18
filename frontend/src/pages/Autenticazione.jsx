import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { api } from "../api";
import { useAuth } from "../auth";
import { useTitolo } from "../formato";
import { Bottone, Campo, Messaggio, classeInput } from "../components/ui";
import { Logo, Monogramma, Silhouette } from "../components/Logo";

function Cornice({ titolo, sotto, children, piede }) {
  return (
    <div className="mx-auto grid max-w-7xl gap-12 px-4 pt-12 md:px-8 md:pt-20 lg:grid-cols-[1fr_minmax(0,28rem)]">
      <div className="relative hidden min-h-[520px] flex-col justify-between overflow-hidden rounded-lg bg-nero p-10 text-white lg:flex">
        <span className="text-[34px]"><Logo variante="chiaro" conMotto /></span>
        <div className="relative z-10 flex flex-col gap-4">
          <p className="max-w-[20ch] text-3xl leading-tight font-bold">Il prezzo giusto arriva. Tu dicci qual è.</p>
          <p className="max-w-[42ch] text-white/70">
            Salva le auto che ti interessano e fissa una soglia su ognuna. Quando il prezzo la raggiunge, ti arriva una mail.
          </p>
        </div>
        {/* Gli elementi grafici del manuale: la linea dell'auto e il monogramma in trasparenza. */}
        <Monogramma colore="rgb(255 255 255 / 0.05)" className="pointer-events-none absolute -top-12 -right-12 w-[300px]" />
        <Silhouette colore="#ffffff" className="pointer-events-none absolute right-0 bottom-0 w-[46%] translate-x-[12%] translate-y-[30%] opacity-80" />
      </div>
      <div className="flex flex-col gap-8 lg:py-6">
        <div>
          <h1 className="text-3xl font-bold">{titolo}</h1>
          <p className="mt-2 text-testo-2">{sotto}</p>
        </div>
        {children}
        <p className="text-sm text-testo-2">{piede}</p>
      </div>
    </div>
  );
}

export function Accesso() {
  useTitolo("Accedi");
  const { accedi } = useAuth();
  const { state } = useLocation();
  const [dati, setDati] = useState({ email: "", password: "" });
  const [errore, setErrore] = useState(null);
  const [campi, setCampi] = useState({});
  const [inCorso, setInCorso] = useState(false);

  const invia = async (e) => {
    e.preventDefault();
    setInCorso(true);
    setErrore(null);
    setCampi({});
    try {
      // Dove andare dopo lo decide SoloOspiti, appena l'utente c'è.
      await accedi(dati);
    } catch (err) {
      setErrore(err.message);
      setCampi(err.campi ?? {});
      setInCorso(false);
    }
  };

  const campo = (k) => (e) => setDati({ ...dati, [k]: e.target.value });

  return (
    <Cornice
      titolo="Accedi"
      sotto="Entra per vedere i tuoi preferiti e i tuoi avvisi."
      piede={<>Non hai un account? <Link to="/registrazione" className="font-medium text-testo underline underline-offset-4">Registrati</Link></>}
    >
      <form onSubmit={invia} noValidate className="flex flex-col gap-5">
        {state?.scaduta && !errore && <Messaggio>La sessione è scaduta, rifai l'accesso.</Messaggio>}
        {errore && <Messaggio>{errore}</Messaggio>}
        <Campo etichetta="Email" nome="email" errore={campi.email}>
          <input id="email" type="email" autoComplete="email" required value={dati.email} onChange={campo("email")} aria-invalid={Boolean(campi.email)} className={classeInput} />
        </Campo>
        <Campo etichetta="Password" nome="password" errore={campi.password}>
          <input id="password" type="password" autoComplete="current-password" required value={dati.password} onChange={campo("password")} aria-invalid={Boolean(campi.password)} className={classeInput} />
        </Campo>
        <Bottone type="submit" disabled={inCorso} className="mt-2">
          {inCorso ? "Accesso in corso" : "Accedi"}
        </Bottone>
      </form>
    </Cornice>
  );
}

export function Registrazione() {
  useTitolo("Registrati");
  const { accedi } = useAuth();
  // Nel corpo vanno solo questi tre campi: il ruolo lo decide il server.
  const [dati, setDati] = useState({ nome: "", email: "", password: "" });
  const [errore, setErrore] = useState(null);
  const [campi, setCampi] = useState({});
  const [inCorso, setInCorso] = useState(false);

  const invia = async (e) => {
    e.preventDefault();
    setInCorso(true);
    setErrore(null);
    setCampi({});
    try {
      await api.registrazione({ nome: dati.nome.trim(), email: dati.email.trim(), password: dati.password });
      await accedi({ email: dati.email.trim(), password: dati.password });
    } catch (err) {
      setErrore(err.stato === 409 ? "Esiste già un account con questa email." : err.message);
      setCampi(err.campi ?? {});
      setInCorso(false);
    }
  };

  const campo = (k) => (e) => setDati({ ...dati, [k]: e.target.value });

  return (
    <Cornice
      titolo="Crea un account"
      sotto="Ti servono solo nome, email e una password."
      piede={<>Hai già un account? <Link to="/accesso" className="font-medium text-testo underline underline-offset-4">Accedi</Link></>}
    >
      <form onSubmit={invia} noValidate className="flex flex-col gap-5">
        {errore && <Messaggio>{errore}</Messaggio>}
        <Campo etichetta="Nome" nome="nome" errore={campi.nome}>
          <input id="nome" autoComplete="given-name" required minLength={2} maxLength={80} value={dati.nome} onChange={campo("nome")} aria-invalid={Boolean(campi.nome)} className={classeInput} />
        </Campo>
        <Campo etichetta="Email" nome="email" errore={campi.email} aiuto="È qui che arrivano gli avvisi di prezzo.">
          <input id="email" type="email" autoComplete="email" required maxLength={180} value={dati.email} onChange={campo("email")} aria-invalid={Boolean(campi.email)} className={classeInput} />
        </Campo>
        <Campo etichetta="Password" nome="password" errore={campi.password} aiuto="Tra 8 e 72 caratteri.">
          <input id="password" type="password" autoComplete="new-password" required minLength={8} maxLength={72} value={dati.password} onChange={campo("password")} aria-invalid={Boolean(campi.password)} className={classeInput} />
        </Campo>
        <p className="text-[13px] leading-relaxed text-testo-2">
          Registrandoti accetti che trattiamo email, nome, preferiti e soglie come spiegato nella{" "}
          <Link to="/privacy" className="underline underline-offset-4 hover:text-testo">Privacy Policy</Link>.
        </p>
        <Bottone type="submit" disabled={inCorso}>
          {inCorso ? "Un momento" : "Crea l'account"}
        </Bottone>
      </form>
    </Cornice>
  );
}
