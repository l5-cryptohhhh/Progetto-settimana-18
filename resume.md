# Resume — da dove riprendere

**Stato al 25 settembre 2026.**

Il **backend è finito e provato** (29 test verdi con `./mvnw test`).
Il **frontend è fatto**: React 19 + Vite + React Router + Tailwind v4, tutte le
pagine della tabella al §3, `npm run build` pulito. Resta da fare il giro delle
mail vere (§4) e il deploy (§5).

```
Progetto-settimana-18/
├── backend/            <-- fatto
│   ├── src/
│   ├── Dockerfile
│   ├── .env.example
│   ├── RELAZIONE.md    <-- le scelte e il perché (leggila, serve alla consegna)
│   └── pom.xml
├── frontend/           <-- fatto
│   ├── src/            api.js, auth.jsx, components/, pages/
│   └── contenuti/
│       ├── privacy-policy.md
│       └── cookie-policy.md
├── render.yaml
└── resume.md           <-- questo file
```

---

## 1. Far partire il backend in locale

Serve **PostgreSQL** in ascolto su `localhost:5432` con un database `salone_auto`,
e serve passare le variabili d'ambiente: `JWT_SECRET`, `ADMIN_PASSWORD`,
`MAIL_USERNAME` e `MAIL_PASSWORD` **non hanno un valore di ripiego**, quindi
senza di loro il server non parte (è voluto, vedi RELAZIONE.md §8).

I nomi e la forma sono in `backend/.env.example` (solo valori finti: quel file va
su GitHub). I valori veri stanno in `backend/.env`, escluso dal `.gitignore`, che
`application.yml` importa da solo se esiste; su Render il file non c'è e valgono
le variabili della dashboard.

Da terminale:

```bash
cd backend
./mvnw test          # 29 test, non serve né database né SMTP
./mvnw spring-boot:run
```

Il backend risponde su `http://localhost:8080`.
Al primo avvio crea l'amministratore con `ADMIN_EMAIL` / `ADMIN_PASSWORD`.

---

## 2. Le API, per intero

Base: `http://localhost:8080` in locale, l'indirizzo del servizio Render in produzione.

Il token si manda con l'header `Authorization: Bearer <token>`.
Gli errori hanno tutti la stessa forma:

```json
{ "stato": 400, "messaggio": "Dati non validi", "quando": "...", "campi": { "password": "..." } }
```

`campi` compare solo sugli errori di validazione.

### Pubbliche (senza token)

| Metodo | Rotta | Corpo | Risposta |
|---|---|---|---|
| `POST` | `/api/auth/registrazione` | `{ email, password, nome }` | `201` `{ id, email, nome, ruolo, creatoIl }` |
| `POST` | `/api/auth/login` | `{ email, password }` | `200` `{ accessToken, utente }` |
| `GET` | `/api/auto` | — | `200` pagina di auto pubbliche |
| `GET` | `/api/auto/{id}` | — | `200` auto pubblica, `404` se è una bozza |
| `POST` | `/api/avvisi/disattiva` | `{ token }` | `200` `{ messaggio }` |
| `GET` | `/actuator/health` | — | `200` |

**Password**: minimo 8 caratteri, massimo 72.

**Parametri di `/api/auto`** (tutti facoltativi):

| Parametro | Valori |
|---|---|
| `q` | testo cercato su marca e modello |
| `prezzoMin`, `prezzoMax` | numeri |
| `ordina` | **solo** `prezzo`, `anno`, `chilometri`, `marca`, `recenti` — qualsiasi altra cosa dà `400` |
| `direzione` | `asc` oppure `desc` (predefinito `desc`) |
| `pagina` | da 0 (predefinito 0) |
| `dimensione` | predefinito 12, massimo 50 |

**Forma della pagina** (vale per `/api/auto` e `/api/admin/auto`):

```json
{
  "contenuto": [ ... ],
  "pagina": 0,
  "dimensione": 12,
  "totaleElementi": 37,
  "totalePagine": 4,
  "ultima": false
}
```

**Auto pubblica**:

```json
{
  "id": 1, "marca": "Fiat", "modello": "Panda", "anno": 2019,
  "chilometri": 60000, "alimentazione": "BENZINA",
  "descrizione": "...", "immagineUrl": "...",
  "prezzo": 8500.00, "creataIl": "2026-09-20T10:00:00"
}
```

`alimentazione`: `BENZINA`, `DIESEL`, `GPL`, `METANO`, `IBRIDA`, `ELETTRICA`.
Qui dentro **non** ci sono `prezzoAcquisto` e `stato`: quelli escono solo dalle rotte admin.

### Utente collegato

| Metodo | Rotta | Corpo | Risposta |
|---|---|---|---|
| `GET` | `/api/utenti/me` | — | profilo |
| `PUT` | `/api/utenti/me` | `{ nome }` | profilo aggiornato |
| `DELETE` | `/api/utenti/me` | — | `204` — cancella account, avvisi e preferiti |
| `GET` | `/api/preferiti` | — | `[ { id, auto, aggiuntoIl } ]` |
| `POST` | `/api/preferiti` | `{ autoId }` | `201`, `409` se c'è già |
| `DELETE` | `/api/preferiti/{id}` | — | `204` |
| `GET` | `/api/avvisi` | — | `[ avviso ]` |
| `GET` | `/api/avvisi/{id}` | — | avviso, **`404` se è di un altro** |
| `POST` | `/api/avvisi` | `{ autoId, soglia }` | `201`, `409` se c'è già un avviso su quell'auto |
| `PUT` | `/api/avvisi/{id}` | `{ soglia }` | avviso aggiornato |
| `DELETE` | `/api/avvisi/{id}` | — | `204` |

**Avviso**:

```json
{
  "id": 5, "auto": { ...auto pubblica... }, "soglia": 9000.00,
  "inviato": false, "attivo": true,
  "creatoIl": "...", "inviatoIl": null
}
```

`inviato: true` vuol dire che la mail per quell'avviso è già partita e **non
ripartirà più**, nemmeno cambiando la soglia. Mostralo nell'interfaccia:
*«questo avviso è già scattato — cancellalo e rifallo per essere riavvisato»*.

### Amministratore (`ROLE_ADMIN`)

Un utente normale che chiama queste rotte riceve **403**, non collegato **401**.

| Metodo | Rotta | Corpo |
|---|---|---|
| `GET` | `/api/admin/auto` | stessi parametri del catalogo, **incluse le bozze** |
| `GET` | `/api/admin/auto/{id}` | — |
| `POST` | `/api/admin/auto` | auto completa → `201` |
| `PUT` | `/api/admin/auto/{id}` | auto completa |
| `PATCH` | `/api/admin/auto/{id}/prezzo` | `{ prezzo }` ← **è questa che fa partire le mail** |
| `DELETE` | `/api/admin/auto/{id}` | — → `204` |

Corpo di creazione/modifica:

```json
{
  "marca": "Fiat", "modello": "Panda", "anno": 2019, "chilometri": 60000,
  "alimentazione": "BENZINA", "descrizione": "...", "immagineUrl": "...",
  "prezzo": 8500.00, "prezzoAcquisto": 6000.00, "stato": "PUBBLICATA"
}
```

`stato`: `BOZZA` oppure `PUBBLICATA`. La risposta admin ha in più `prezzoAcquisto`,
`stato` e `aggiornataIl`.

---

## 3. Che cosa costruire nel frontend

Consiglio: **Vite + React + React Router**, `npm create vite@latest frontend -- --template react`.
Attenzione: la cartella `frontend/contenuti/` esiste già, non sovrascriverla.

### Rotte obbligate

Due indirizzi **non sono negoziabili**, perché il backend li scrive dentro le mail:

| Rotta | Da dove arriva |
|---|---|
| `/auto/:id` | il bottone «Vai alla scheda dell'auto» nella mail |
| `/disattiva-avviso/:token` | il link «Disattiva l'avviso» nella mail |

La pagina `/disattiva-avviso/:token` prende il token dall'URL e fa
`POST /api/avvisi/disattiva` con `{ token }`. Il token vale **una volta sola**:
al secondo tentativo arriva `404`, e il messaggio da mostrare è
*«questo link è già stato usato»*, non un errore generico.

### Le altre pagine

| Rotta | Chi | Cosa c'è |
|---|---|---|
| `/` | tutti | catalogo con ricerca, ordinamento, paginazione |
| `/auto/:id` | tutti | scheda auto; se sei collegato: cuore preferiti + campo soglia |
| `/registrazione`, `/accesso` | ospiti | i due form |
| `/profilo` | collegato | dati, elenco preferiti, elenco avvisi, **«Elimina il mio account»** |
| `/admin/auto` | admin | elenco con bozze e prezzo d'acquisto |
| `/admin/auto/nuova`, `/admin/auto/:id` | admin | form, e il cambio prezzo rapido |
| `/privacy`, `/cookie` | tutti | i testi in `frontend/contenuti/` |
| `/disattiva-avviso/:token` | tutti | vedi sopra |

**Privacy e Cookie devono essere raggiungibili da ogni pagina**: mettile nel footer
del layout, non in una pagina sola. I due testi sono già completi: titolare e
indirizzo di contatto sono compilati, non resta niente da riempire.

### Le regole delle slide, lato frontend

1. **Niente `dangerouslySetInnerHTML`.** La descrizione dell'auto arriva come testo
   e va stampata come testo, `{auto.descrizione}` e basta. Vale anche per il nome utente.
2. **Ogni `fetch` passa da `api.js`.** Nessun componente chiama `fetch` per conto suo.
3. **Il token in `localStorage`**, e la Cookie Policy lo dichiara già.
4. **`403` e `401` sono due cose diverse**: `401` → butta fuori e manda ad `/accesso`;
   `403` → «non hai i permessi», restando dov'è.
5. **Il campo `ordina`** va preso da un menu a tendina con i cinque valori ammessi,
   non da un input libero: il backend rifiuta il resto con `400`, ma tanto vale non chiederlo.

### `src/api.js` — lo scheletro da cui partire

```js
const BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

const leggiToken = () => localStorage.getItem("token");
export const salvaToken = (t) => localStorage.setItem("token", t);
export const dimenticaToken = () => localStorage.removeItem("token");

async function chiama(rotta, opzioni = {}) {
  const token = leggiToken();

  const risposta = await fetch(BASE + rotta, {
    ...opzioni,
    headers: {
      ...(opzioni.corpo !== undefined ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...opzioni.headers,
    },
    body: opzioni.corpo !== undefined ? JSON.stringify(opzioni.corpo) : undefined,
  });

  // Il token è scaduto o non vale più: si esce e si ricomincia dall'accesso.
  if (risposta.status === 401) {
    dimenticaToken();
    throw new ErroreApi(401, "Devi fare l'accesso");
  }

  if (risposta.status === 204) return null;

  const corpo = await risposta.json().catch(() => null);

  if (!risposta.ok) {
    throw new ErroreApi(risposta.status, corpo?.messaggio ?? "Errore imprevisto", corpo?.campi);
  }

  return corpo;
}

export class ErroreApi extends Error {
  constructor(stato, messaggio, campi) {
    super(messaggio);
    this.stato = stato;
    this.campi = campi;
  }
}

export const api = {
  registrazione: (dati) => chiama("/api/auth/registrazione", { method: "POST", corpo: dati }),
  login:         (dati) => chiama("/api/auth/login",         { method: "POST", corpo: dati }),

  catalogo: (parametri) => chiama("/api/auto?" + new URLSearchParams(parametri)),
  auto:     (id)        => chiama(`/api/auto/${id}`),

  profilo:        ()      => chiama("/api/utenti/me"),
  salvaProfilo:   (dati)  => chiama("/api/utenti/me", { method: "PUT", corpo: dati }),
  eliminaAccount: ()      => chiama("/api/utenti/me", { method: "DELETE" }),

  preferiti:        ()       => chiama("/api/preferiti"),
  aggiungiPreferito:(autoId) => chiama("/api/preferiti", { method: "POST", corpo: { autoId } }),
  togliPreferito:   (id)     => chiama(`/api/preferiti/${id}`, { method: "DELETE" }),

  avvisi:        ()             => chiama("/api/avvisi"),
  creaAvviso:    (autoId, soglia)=> chiama("/api/avvisi", { method: "POST", corpo: { autoId, soglia } }),
  cambiaSoglia:  (id, soglia)   => chiama(`/api/avvisi/${id}`, { method: "PUT", corpo: { soglia } }),
  eliminaAvviso: (id)           => chiama(`/api/avvisi/${id}`, { method: "DELETE" }),
  disattivaAvviso:(token)       => chiama("/api/avvisi/disattiva", { method: "POST", corpo: { token } }),

  adminAuto:        (parametri) => chiama("/api/admin/auto?" + new URLSearchParams(parametri)),
  adminDettaglio:   (id)        => chiama(`/api/admin/auto/${id}`),
  adminCrea:        (dati)      => chiama("/api/admin/auto", { method: "POST", corpo: dati }),
  adminAggiorna:    (id, dati)  => chiama(`/api/admin/auto/${id}`, { method: "PUT", corpo: dati }),
  adminCambiaPrezzo:(id, prezzo)=> chiama(`/api/admin/auto/${id}/prezzo`, { method: "PATCH", corpo: { prezzo } }),
  adminElimina:     (id)        => chiama(`/api/admin/auto/${id}`, { method: "DELETE" }),
};
```

In sviluppo crea `frontend/.env.local` con `VITE_API_URL=http://localhost:8080`
(`.env.local` è già escluso dal `.gitignore`).

---

## 4. Provare che le mail funzionano davvero

Con backend e frontend accesi:

1. entra come amministratore, crea un'auto **PUBBLICATA** a 12.000;
2. esci, registrati come utente normale con un indirizzo Gmail vero;
3. sulla scheda dell'auto fissa la soglia a **9.000**;
4. torna amministratore e porta il prezzo a **8.500** → **arriva la mail**;
5. rimetti 8.500 e poi 8.000 → **non arriva niente** (era già sotto soglia);
6. riporta a 12.000 e poi di nuovo a 8.500 → **non arriva niente**
   (`inviato` non torna indietro);
7. clicca «Disattiva l'avviso» nella mail → finisci su `/disattiva-avviso/:token`,
   l'avviso si spegne; ricarica quella pagina → `404`, il token è monouso.

Se la mail non parte, guarda i log del backend: il fallimento viene scritto con
l'id dell'avviso. Errore tipico: `MAIL_PASSWORD` con la password dell'account
Google invece della **password per le app**.

---

## 5. Andare su Render

`render.yaml` è già scritto e definisce tre cose: database, backend Docker,
frontend statico.

Ordine delle operazioni:

1. **Blueprint** su Render puntato alla repository → crea i tre servizi.
2. Sul backend, dalla dashboard, compila le variabili con `sync: false`:
   `JWT_SECRET` (`openssl rand -base64 48`), `ADMIN_EMAIL`, `ADMIN_PASSWORD`,
   `MAIL_USERNAME`, `MAIL_PASSWORD` (password per le app di Gmail).
   `DATABASE_URL` si compila da sola.
3. Sul frontend: `VITE_API_URL` = indirizzo del backend.
4. **Torna sul backend** e metti `ALLOWED_ORIGIN` = indirizzo esatto del frontend,
   `https://...onrender.com`, **senza barra finale e senza asterischi**.
   Poi riavvia il backend: quel valore finisce sia nel CORS sia nei link delle mail.
5. Controlla `https://<backend>.onrender.com/actuator/health` → deve dare
   `{"status":"UP"}`.

Il punto 4 è quello che si dimentica sempre: se `ALLOWED_ORIGIN` è sbagliato, il
frontend prende errori CORS **e** i link nelle mail portano nel posto sbagliato.

---

## 6. Prima di consegnare

- [ ] `cd backend && ./mvnw test` → 29 verdi
- [x] Privacy e Cookie raggiungibili dal footer di **ogni** pagina
- [x] titolare e contatto compilati nella Privacy Policy
- [x] nessun `dangerouslySetInnerHTML` nel frontend
- [x] nessun `fetch` fuori da `api.js`
- [ ] nessuna password o chiave nei file committati (`grep -ri "password" --include=*.yml`)
- [ ] `/actuator/health` pubblico e `UP`
- [ ] il giro delle mail del punto 4 provato sul sito pubblicato
- [ ] `backend/RELAZIONE.md` riletta: è lì che stanno le risposte alle domande
      della traccia, compreso il «decidi tu» su Gmail che non risponde

---

## 7. Il frontend, com'è fatto

- **Stile**: brand identity Autoven. Rosso `#E63946`, nero `#0D0D0D`, grigio `#6B6B6B`,
  bianco `#F5F5F5`, Montserrat servito dal sito stesso (niente Google Fonts). Testata,
  apertura e piede neri; il contenuto segue il tema chiaro/scuro del sistema. Il logo
  è in `src/components/Logo.jsx` (versione chiara, scura e monogramma «A»); anche la
  mail di avviso usa gli stessi colori, con il marchio scritto in testo.
  Il tema non viene salvato nel browser: la Cookie Policy dichiara solo il token.
- **`src/api.js`** è l'unico file con `fetch`. Rispetto allo scheletro: un `401`
  *con* token in mano cancella il token e manda ad `/accesso`; un `401` *senza*
  token (login sbagliato) mostra il messaggio del server. Un backend che non
  risponde (Render addormentato) dà un messaggio chiaro invece di un errore muto.
- **`403`**: `SoloAdmin` mostra «Non hai i permessi» e lascia l'utente dov'è.
- **Cambio prezzo**: in gestione e nella pagina di modifica passa sempre da
  `PATCH /prezzo`; il form completo rimanda il prezzo attuale senza toccarlo.
- **`/disattiva-avviso/:token`**: una sola richiesta per token anche con lo
  StrictMode di React, così il primo clic non risulta «già usato».
- **Immagini**: solo indirizzi `http`/`https` finiscono in `src`, richieste con
  `referrerPolicy="no-referrer"`; se mancano o non si caricano compare una tavola
  con la marca. La Cookie Policy ora lo dice.
- **Informative**: `react-markdown` legge i file di `contenuti/` e costruisce
  elementi React, senza interpretare HTML.
