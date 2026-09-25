import { StrictMode, lazy } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { MotionConfig } from "motion/react";
import "@fontsource-variable/montserrat";
import "./index.css";

import { AuthProvider } from "./auth";
import Layout from "./components/Layout";
import { SoloAdmin, SoloCollegati, SoloOspiti } from "./components/Guardie";
import { LinkBottone, Vuoto } from "./components/ui";
import Catalogo from "./pages/Catalogo";
import SchedaAuto from "./pages/SchedaAuto";
import { Accesso, Registrazione } from "./pages/Autenticazione";
import DisattivaAvviso from "./pages/DisattivaAvviso";

// Gestione e informative si scaricano solo quando servono.
const Profilo = lazy(() => import("./pages/Profilo"));
const AdminElenco = lazy(() => import("./pages/AdminElenco"));
const AdminForm = lazy(() => import("./pages/AdminForm"));
const Informativa = lazy(() => import("./pages/Informativa"));

function NonTrovata() {
  return (
    <div className="mx-auto max-w-3xl px-4 py-24">
      <Vuoto titolo="Pagina non trovata" azione={<LinkBottone to="/" variante="secondario">Torna al catalogo</LinkBottone>}>
        L'indirizzo non corrisponde a nessuna pagina del salone.
      </Vuoto>
    </div>
  );
}

createRoot(document.getElementById("root")).render(
  <StrictMode>
    {/* reducedMotion="user": chi chiede meno movimento al sistema vede solo dissolvenze. */}
    <MotionConfig reducedMotion="user">
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route element={<Layout />}>
              <Route index element={<Catalogo />} />
              {/* Queste due rotte sono scritte dentro le mail dal backend: non si toccano. */}
              <Route path="auto/:id" element={<SchedaAuto />} />
              <Route path="disattiva-avviso/:token" element={<DisattivaAvviso />} />

              <Route path="privacy" element={<Informativa tipo="privacy" />} />
              <Route path="cookie" element={<Informativa tipo="cookie" />} />

              <Route element={<SoloOspiti />}>
                <Route path="accesso" element={<Accesso />} />
                <Route path="registrazione" element={<Registrazione />} />
              </Route>

              <Route element={<SoloCollegati />}>
                <Route path="profilo" element={<Profilo />} />
              </Route>

              <Route path="admin" element={<SoloAdmin />}>
                <Route path="auto" element={<AdminElenco />} />
                <Route path="auto/nuova" element={<AdminForm />} />
                <Route path="auto/:id" element={<AdminForm />} />
              </Route>

              <Route path="*" element={<NonTrovata />} />
            </Route>
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </MotionConfig>
  </StrictMode>
);
