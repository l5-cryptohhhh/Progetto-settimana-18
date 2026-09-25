// L'unico file del frontend che chiama fetch. I componenti passano tutti da `api`.

const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

const leggiToken = () => localStorage.getItem("token");
export const salvaToken = (t) => localStorage.setItem("token", t);
export const dimenticaToken = () => localStorage.removeItem("token");
export const haToken = () => Boolean(leggiToken());

/** Lo ascolta AuthProvider: token scaduto → si esce e si torna ad /accesso. */
export const SESSIONE_SCADUTA = "sessione-scaduta";

export class ErroreApi extends Error {
  constructor(stato, messaggio, campi) {
    super(messaggio);
    this.stato = stato;
    this.campi = campi;
  }
}

/*
 * Su Render il backend gratuito si addormenta dopo 15 minuti e per risvegliarsi
 * può metterci più di un minuto: nel frattempo le richieste restano appese o
 * tornano 502/503/504. Le letture (GET) allora riprovano da sole; le scritture
 * no, perché ripetere un cambio di prezzo o un avviso potrebbe farlo due volte.
 * Layout ascolta i due eventi e mostra la barra «il server si sta risvegliando».
 */
export const SERVER_LENTO = "server-lento";
export const SERVER_PRONTO = "server-pronto";
const RIPROVABILI = new Set([502, 503, 504]);
const PAZIENZA_MS = 180_000;
const PAUSA_MS = 4_000;
const attendi = (ms) => new Promise((fatto) => setTimeout(fatto, ms));

async function chiama(rotta, opzioni = {}) {
  const token = leggiToken();
  const lettura = !opzioni.method || opzioni.method === "GET";
  const scadenza = Date.now() + PAZIENZA_MS;

  // Se dopo qualche secondo non ha ancora risposto, quasi sempre si sta risvegliando.
  let lento = false;
  const segnala = () => {
    if (!lento) {
      lento = true;
      window.dispatchEvent(new Event(SERVER_LENTO));
    }
  };
  const timer = setTimeout(segnala, PAUSA_MS);

  let risposta;
  try {
    for (;;) {
      try {
        risposta = await fetch(BASE + rotta, {
          ...opzioni,
          headers: {
            ...(opzioni.corpo !== undefined ? { "Content-Type": "application/json" } : {}),
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
            ...opzioni.headers,
          },
          body: opzioni.corpo !== undefined ? JSON.stringify(opzioni.corpo) : undefined,
        });
      } catch {
        risposta = null;
      }

      const daRifare = risposta === null || (lettura && RIPROVABILI.has(risposta.status));
      if (!daRifare || !lettura || Date.now() > scadenza) break;
      segnala();
      await attendi(PAUSA_MS);
    }
  } finally {
    clearTimeout(timer);
    if (lento) window.dispatchEvent(new Event(SERVER_PRONTO));
  }

  if (risposta === null) {
    throw new ErroreApi(0, "Il server non risponde. Se si sta risvegliando ci mette un minuto: riprova tra poco.");
  }

  if (risposta.status === 204) return null;

  const corpo = await risposta.json().catch(() => null);

  if (risposta.status === 401) {
    // Con un token in mano, 401 vuol dire che non vale più: si esce e si ricomincia.
    // Senza token è il login sbagliato, e il messaggio del server va mostrato così com'è.
    if (token) {
      dimenticaToken();
      window.dispatchEvent(new Event(SESSIONE_SCADUTA));
      throw new ErroreApi(401, "La sessione è scaduta, rifai l'accesso");
    }
    throw new ErroreApi(401, corpo?.messaggio ?? "Devi fare l'accesso");
  }

  if (!risposta.ok) {
    throw new ErroreApi(risposta.status, corpo?.messaggio ?? "Errore imprevisto", corpo?.campi);
  }

  return corpo;
}

/** Toglie i parametri vuoti, così l'URL resta pulito e il backend usa i suoi predefiniti. */
const query = (parametri = {}) =>
  new URLSearchParams(
    Object.entries(parametri).filter(([, v]) => v !== undefined && v !== null && v !== "")
  ).toString();

export const api = {
  registrazione: (dati) => chiama("/api/auth/registrazione", { method: "POST", corpo: dati }),
  login:         (dati) => chiama("/api/auth/login",         { method: "POST", corpo: dati }),

  catalogo: (parametri) => chiama("/api/auto?" + query(parametri)),
  auto:     (id)        => chiama(`/api/auto/${encodeURIComponent(id)}`),

  profilo:        ()      => chiama("/api/utenti/me"),
  salvaProfilo:   (dati)  => chiama("/api/utenti/me", { method: "PUT", corpo: dati }),
  eliminaAccount: ()      => chiama("/api/utenti/me", { method: "DELETE" }),

  preferiti:         ()       => chiama("/api/preferiti"),
  aggiungiPreferito: (autoId) => chiama("/api/preferiti", { method: "POST", corpo: { autoId } }),
  togliPreferito:    (id)     => chiama(`/api/preferiti/${encodeURIComponent(id)}`, { method: "DELETE" }),

  avvisi:          ()               => chiama("/api/avvisi"),
  creaAvviso:      (autoId, soglia) => chiama("/api/avvisi", { method: "POST", corpo: { autoId, soglia } }),
  cambiaSoglia:    (id, soglia)     => chiama(`/api/avvisi/${encodeURIComponent(id)}`, { method: "PUT", corpo: { soglia } }),
  eliminaAvviso:   (id)             => chiama(`/api/avvisi/${encodeURIComponent(id)}`, { method: "DELETE" }),
  disattivaAvviso: (token)          => chiama("/api/avvisi/disattiva", { method: "POST", corpo: { token } }),

  adminAuto:         (parametri) => chiama("/api/admin/auto?" + query(parametri)),
  adminDettaglio:    (id)        => chiama(`/api/admin/auto/${encodeURIComponent(id)}`),
  adminCrea:         (dati)      => chiama("/api/admin/auto", { method: "POST", corpo: dati }),
  adminAggiorna:     (id, dati)  => chiama(`/api/admin/auto/${encodeURIComponent(id)}`, { method: "PUT", corpo: dati }),
  adminCambiaPrezzo: (id, prezzo)=> chiama(`/api/admin/auto/${encodeURIComponent(id)}/prezzo`, { method: "PATCH", corpo: { prezzo } }),
  adminElimina:      (id)        => chiama(`/api/admin/auto/${encodeURIComponent(id)}`, { method: "DELETE" }),
};
