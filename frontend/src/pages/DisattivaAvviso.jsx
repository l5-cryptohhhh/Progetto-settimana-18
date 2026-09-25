import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { BellSlash, LinkBreak, WarningCircle } from "@phosphor-icons/react";
import { api } from "../api";
import { useTitolo } from "../formato";
import { LinkBottone, Scheletro } from "../components/ui";

/*
 * Il token vale una volta sola. In sviluppo StrictMode monta l'effetto due volte:
 * senza questa cache la seconda chiamata prenderebbe 404 e la pagina direbbe
 * «già usato» anche al primo clic. Una promessa per token, e basta.
 */
const richieste = new Map();
const disattiva = (token) => {
  if (!richieste.has(token)) richieste.set(token, api.disattivaAvviso(token));
  return richieste.get(token);
};

export default function DisattivaAvviso() {
  useTitolo("Disattiva l'avviso");
  const { token } = useParams();
  const [stato, setStato] = useState({ fase: "attesa" });

  useEffect(() => {
    disattiva(token)
      .then(() => setStato({ fase: "fatto" }))
      .catch((e) => setStato({ fase: e.stato === 404 ? "usato" : "errore", messaggio: e.message }));
  }, [token]);

  const contenuto = {
    fatto: {
      Icona: BellSlash,
      titolo: "Avviso disattivato",
      testo: "Per quest'auto non ti scriviamo più. Gli altri tuoi avvisi restano come sono.",
    },
    usato: {
      Icona: LinkBreak,
      titolo: "Questo link è già stato usato",
      testo: "Ogni link di disattivazione vale una volta sola. Se l'avviso c'è ancora, lo trovi nel tuo profilo.",
    },
    errore: {
      Icona: WarningCircle,
      titolo: "Non siamo riusciti a disattivarlo",
      testo: stato.messaggio,
    },
  }[stato.fase];

  return (
    <div className="mx-auto flex max-w-xl flex-col items-start gap-6 px-4 pt-20 md:pt-28">
      {stato.fase === "attesa" ? (
        <>
          <Scheletro className="size-12 rounded-full" />
          <Scheletro className="h-9 w-72" />
          <Scheletro className="h-5 w-full" />
        </>
      ) : (
        <>
          <span className={`grid size-12 place-items-center rounded-full ${stato.fase === "fatto" ? "bg-accento-tenue text-accento" : "bg-superficie-2 text-testo-2"}`}>
            <contenuto.Icona size={24} weight="duotone" />
          </span>
          <h1 className="text-3xl font-bold tracking-tight md:text-4xl">{contenuto.titolo}</h1>
          <p className="leading-relaxed text-testo-2">{contenuto.testo}</p>
          <div className="flex flex-wrap gap-3">
            <LinkBottone to="/">Vai al catalogo</LinkBottone>
            {stato.fase !== "fatto" && <LinkBottone to="/profilo" variante="secondario">Il tuo profilo</LinkBottone>}
          </div>
        </>
      )}
    </div>
  );
}
