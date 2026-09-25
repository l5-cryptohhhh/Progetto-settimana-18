import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { api, dimenticaToken, haToken, salvaToken, SESSIONE_SCADUTA } from "./api";

const ContestoAuth = createContext(null);

export function AuthProvider({ children }) {
  const [utente, setUtente] = useState(null);
  // Se c'è un token bisogna chiedere al server chi siamo prima di decidere che cosa mostrare.
  const [pronto, setPronto] = useState(() => !haToken());
  const naviga = useNavigate();
  const posizione = useLocation();

  useEffect(() => {
    if (!haToken()) return;
    api.profilo()
      .then(setUtente)
      .catch(() => setUtente(null))
      .finally(() => setPronto(true));
  }, []);

  useEffect(() => {
    const scaduta = () => {
      setUtente(null);
      naviga("/accesso", { state: { da: posizione.pathname, scaduta: true } });
    };
    window.addEventListener(SESSIONE_SCADUTA, scaduta);
    return () => window.removeEventListener(SESSIONE_SCADUTA, scaduta);
  }, [naviga, posizione.pathname]);

  const accedi = useCallback(async (credenziali) => {
    const { accessToken, utente } = await api.login(credenziali);
    salvaToken(accessToken);
    setUtente(utente);
    return utente;
  }, []);

  const esci = useCallback(() => {
    dimenticaToken();
    setUtente(null);
  }, []);

  const valore = useMemo(
    () => ({ utente, setUtente, pronto, accedi, esci, admin: utente?.ruolo === "ADMIN" }),
    [utente, pronto, accedi, esci]
  );

  return <ContestoAuth.Provider value={valore}>{children}</ContestoAuth.Provider>;
}

export const useAuth = () => useContext(ContestoAuth);
