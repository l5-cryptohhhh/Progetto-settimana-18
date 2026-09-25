import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { Suspense, useEffect } from "react";
import { MagnifyingGlass, SignOut } from "@phosphor-icons/react";
import { useAuth } from "../auth";
import { LinkBottone } from "./ui";
import { Logo, Silhouette } from "./Logo";

const voce = ({ isActive }) =>
  `relative inline-flex h-16 items-center px-3 text-[13px] font-medium transition-colors ${
    isActive ? "text-white" : "text-white/70 hover:text-white"
  } after:absolute after:inset-x-3 after:bottom-4 after:h-[2px] after:bg-rosso after:transition-transform after:duration-300 ${
    isActive ? "after:scale-x-100" : "after:scale-x-0"
  }`;

export default function Layout() {
  const { utente, admin, esci, pronto } = useAuth();
  const { pathname } = useLocation();
  const naviga = useNavigate();

  // Cambiando pagina si riparte dall'alto, come su un sito normale.
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);

  // La lente della testata porta alla ricerca del catalogo e ci mette il cursore.
  const cerca = () => {
    const vai = () => {
      document.getElementById("catalogo")?.scrollIntoView({ behavior: "smooth" });
      document.getElementById("q")?.focus({ preventScroll: true });
    };
    if (pathname === "/") vai();
    else {
      naviga("/");
      setTimeout(vai, 350);
    }
  };

  return (
    <div className="flex min-h-[100dvh] flex-col">
      <header className="sticky top-0 z-30 bg-nero">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-4 px-4 md:px-8">
          <Link to="/" aria-label="Autoven, vai al catalogo" className="shrink-0 text-[19px] sm:text-[23px]">
            <Logo variante="chiaro" />
          </Link>

          <nav aria-label="Principale" className="flex items-center">
            <span className="hidden sm:contents">
              <NavLink to="/" end className={voce}>Auto</NavLink>
            </span>
            {admin && <NavLink to="/admin/auto" className={voce}>Gestione</NavLink>}
            {pronto && utente && (
              <NavLink to="/profilo" className={voce}>
                {/* Il nome arriva dal server ed è testo: React lo stampa con l'escape. */}
                <span className="max-w-[14ch] truncate">{utente.nome}</span>
              </NavLink>
            )}
            {pronto && !utente && <NavLink to="/accesso" className={voce}>Accedi</NavLink>}

            <button onClick={cerca} aria-label="Cerca un'auto" className="grid size-10 place-items-center rounded-full text-white/80 transition-colors hover:text-white">
              <MagnifyingGlass size={19} weight="bold" />
            </button>

            {pronto && utente && (
              <button onClick={esci} aria-label="Esci" title="Esci" className="grid size-10 place-items-center rounded-full text-white/80 transition-colors hover:text-white">
                <SignOut size={19} weight="bold" />
              </button>
            )}
            {pronto && !utente && (
              <LinkBottone to="/registrazione" misura="sm" className="ml-1 px-3.5 sm:ml-2 sm:px-4">
                Registrati
              </LinkBottone>
            )}
          </nav>
        </div>
      </header>

      <main className="flex-1">
        <Suspense fallback={null}>
          <Outlet />
        </Suspense>
      </main>

      <footer className="relative mt-24 overflow-hidden bg-nero text-white">
        <div className="mx-auto grid max-w-7xl gap-10 px-4 pt-14 pb-10 md:grid-cols-[1fr_auto] md:items-start md:px-8">
          <div className="flex flex-col items-start gap-6">
            <Link to="/" aria-label="Autoven, vai al catalogo" className="text-[30px]">
              <Logo variante="chiaro" conMotto />
            </Link>
            <p className="max-w-[52ch] text-sm leading-relaxed text-white/60">
              Progetto didattico. Titolare del trattamento: Manuel Nunziata,{" "}
              <a href="mailto:nunziatamanuel5@gmail.com" className="text-white/85 underline decoration-white/30 underline-offset-4 hover:text-white">
                nunziatamanuel5@gmail.com
              </a>
            </p>
          </div>
          <nav aria-label="Informative" className="flex gap-8 text-sm font-medium">
            <Link to="/privacy" className="text-white/70 hover:text-white">Privacy Policy</Link>
            <Link to="/cookie" className="text-white/70 hover:text-white">Cookie Policy</Link>
          </nav>
        </div>
        <div className="relative mx-auto max-w-7xl px-4 pt-6 pb-10 md:px-8">
          <p className="text-[11px] font-medium tracking-[0.3em] text-white/50 md:text-right">
            AUTOVEN <span className="px-2 text-white/30">|</span> LA TUA PROSSIMA AUTO TI ASPETTA
          </p>
        </div>
        {/* L'elemento grafico del manuale: la linea bianca e rossa in basso a sinistra. */}
        <Silhouette colore="#ffffff" className="pointer-events-none absolute -bottom-2 -left-12 w-[300px] opacity-90 md:w-[420px]" />
      </footer>
    </div>
  );
}
