import { Navigate, Outlet, useLocation } from "react-router-dom";
import { LockKey } from "@phosphor-icons/react";
import { useAuth } from "../auth";
import { LinkBottone, Scheletro } from "./ui";

function Attesa() {
  return (
    <div className="mx-auto max-w-7xl px-4 pt-14 md:px-8">
      <Scheletro className="h-12 w-72" />
      <Scheletro className="mt-8 h-64 w-full" />
    </div>
  );
}

/** Serve l'accesso: chi non l'ha fatto va ad /accesso e poi torna qui. */
export function SoloCollegati() {
  const { utente, pronto } = useAuth();
  const { pathname } = useLocation();
  if (!pronto) return <Attesa />;
  if (!utente) return <Navigate to="/accesso" replace state={{ da: pathname }} />;
  return <Outlet />;
}

/**
 * Collegato ma non amministratore: è un 403, non un 401. Non lo buttiamo fuori,
 * gli diciamo che qui non ha i permessi e resta dov'è.
 */
export function SoloAdmin() {
  const { utente, pronto, admin } = useAuth();
  const { pathname } = useLocation();
  if (!pronto) return <Attesa />;
  if (!utente) return <Navigate to="/accesso" replace state={{ da: pathname }} />;
  if (!admin) {
    return (
      <div className="mx-auto flex max-w-xl flex-col items-start gap-5 px-4 pt-24">
        <span className="grid size-12 place-items-center rounded-full bg-superficie-2 text-testo-2">
          <LockKey size={24} weight="duotone" />
        </span>
        <h1 className="text-3xl font-bold tracking-tight">Non hai i permessi</h1>
        <p className="text-testo-2">Questa parte del sito è riservata all'amministratore del salone.</p>
        <LinkBottone to="/" variante="secondario">Torna al catalogo</LinkBottone>
      </div>
    );
  }
  return <Outlet />;
}

/**
 * Accesso e registrazione. Appena c'è un utente si esce da qui: si torna dove si era
 * (solo percorsi interni), altrimenti l'amministratore va alla gestione e gli altri al catalogo.
 */
export function SoloOspiti() {
  const { utente, pronto, admin } = useAuth();
  const { state } = useLocation();
  if (!pronto) return <Attesa />;
  if (utente) {
    const da = typeof state?.da === "string" && state.da.startsWith("/") && !state.da.startsWith("//") ? state.da : null;
    return <Navigate to={da ?? (admin ? "/admin/auto" : "/")} replace />;
  }
  return <Outlet />;
}
