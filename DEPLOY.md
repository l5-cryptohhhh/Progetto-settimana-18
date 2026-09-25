# Deploy su Render — passo passo

**Render non si installa.** È un sito: si fa tutto dal browser su
[render.com](https://render.com). L'unica cosa che serve sul tuo PC è Git, che hai già.

Tempo realistico la prima volta: **circa un'ora**, di cui buona parte ad aspettare
che compili.

---

## Prima di aprire Render: due cose da preparare

### A. La password per le app di Gmail

Va fatta **prima**, perché se non hai la verifica in due passaggi attiva devi
attivarla e aspettare.

1. Vai su [myaccount.google.com/security](https://myaccount.google.com/security).
2. Attiva la **Verifica in due passaggi**, se non è già attiva.
   Senza questa, il passo 3 non esiste proprio nel menu.
3. Vai su [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords).
4. Crea una password per l'app, chiamala `Salone auto`.
5. Google ti mostra 16 caratteri tipo `abcd efgh ijkl mnop`.
   **Copiali togliendo gli spazi**: `abcdefghijklmnop`.
   Non te li rimostra più, quindi tienili da parte adesso.

Questa NON è la password con cui entri in Gmail. Se metti quella, l'invio fallisce
con un errore di autenticazione.

### B. La chiave di firma dei token

Serve una stringa lunga e casuale. Da **Git Bash** (quello che usi per i comandi):

```bash
openssl rand -base64 48
```

Oppure da **PowerShell**:

```powershell
$b = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
[Convert]::ToBase64String($b)
```

Copia il risultato: è il tuo `JWT_SECRET`. Deve essere lungo almeno 32 caratteri,
altrimenti il backend non parte (HS256 lo rifiuta).

---

## Passo 1 — Mandare il codice su GitHub

Render legge il codice da GitHub. **Adesso sul tuo GitHub non c'è niente**: la
repository `l5-cryptohhhh/Progetto-settimana-18` esiste ma ha solo il commit iniziale,
tutto il lavoro è ancora solo sul tuo PC.

```bash
cd "C:/Users/nunzi/OneDrive/Desktop/Corso Epicode/Progetto-settimana-18"
git add .
git status          # controlla che NON compaiano file .env
git commit -m "Backend salone auto con avvisi di prezzo"
git push origin main
```

Il `git status` prima del commit è importante: il `.gitignore` esclude già i file
`.env`, ma vale la pena guardare. Deve comparire `.env.example` (va bene, ha solo
valori finti) e **non** un `.env` senza estensione.

Apri la pagina della repository su GitHub e verifica che si vedano le cartelle
`backend/` e `frontend/` e il file `render.yaml`.

---

## Passo 2 — Creare l'account Render

1. Vai su [render.com](https://render.com) e clicca **Get Started**.
2. Scegli **Sign in with GitHub**: è la strada più corta, perché così Render vede
   già le tue repository senza altri collegamenti.
3. GitHub ti chiede di autorizzare Render: accetta.
4. Quando ti chiede a quali repository dare accesso, puoi scegliere
   **Only select repositories** e indicare solo `Progetto-settimana-18`.

Il piano gratuito basta per tutto quello che ti serve.

---

## Passo 3 — Creare i servizi dal Blueprint

`render.yaml` descrive già database e backend: Render lo legge e crea tutto da solo.

1. Nella dashboard: **New +** → **Blueprint**.
2. Scegli la repository `Progetto-settimana-18`.
3. Render legge `render.yaml` e ti mostra cosa creerà:
   - `salone-auto-db` — il database PostgreSQL
   - `salone-auto-backend` — il backend
4. Ti chiede un nome per il blueprint: metti quello che vuoi, `salone-auto`.
5. Clicca **Apply**.

A questo punto Render crea i servizi e **prova subito a costruire il backend**.
Quel primo tentativo **fallirà**, ed è normale: mancano ancora le variabili
d'ambiente del passo 4. Non spaventarti se vedi rosso.

---

## Passo 4 — Riempire le variabili d'ambiente

Nella dashboard apri **salone-auto-backend** → **Environment** (menu a sinistra).

Trovi già `DATABASE_URL` compilata da sola: **non toccarla**, la collega Render al
database. Le altre sono vuote e le compili tu con **Add Environment Variable**:

| Chiave | Valore da mettere |
|---|---|
| `JWT_SECRET` | la stringa del punto B qui sopra |
| `ADMIN_EMAIL` | `nunziatamanuel5@gmail.com` |
| `ADMIN_PASSWORD` | una password che scegli tu, lunga — è con questa che entri come amministratore |
| `MAIL_USERNAME` | `nunziatamanuel5@gmail.com` |
| `MAIL_PASSWORD` | i 16 caratteri del punto A, **senza spazi** |
| `ALLOWED_ORIGIN` | per ora `http://localhost:5173` |

Su `ALLOWED_ORIGIN`: il frontend ancora non esiste, quindi ci metti l'indirizzo del
tuo Vite locale. Così puoi già sviluppare il frontend sul PC parlando con il backend
vero su Render. Quando pubblicherai anche il frontend lo cambierai (passo 7).

Salva. Render riavvia e ricostruisce da solo.

---

## Passo 5 — Aspettare la costruzione e controllare

Vai su **Logs**. La prima costruzione è lenta: Maven scarica tutte le dipendenze,
**dai 5 ai 10 minuti**. Le volte dopo è più veloce perché Docker riusa gli strati.

Quando è finita devi vedere nei log, in ordine:

```
Database configurato su dpg-xxxxx-a
...
Creato l'amministratore iniziale
...
Started ProgettoSettimana18Application in X seconds
```

Se vedi queste tre righe, il backend è vivo e l'amministratore esiste.

In alto nella pagina del servizio c'è il suo indirizzo, tipo
`https://salone-auto-backend.onrender.com`. **Copialo, ti serve da qui in poi.**

### Prova che risponde

Apri nel browser:

```
https://salone-auto-backend.onrender.com/actuator/health
```

Deve rispondere `{"status":"UP"}`.

Poi prova a entrare come amministratore, da Git Bash:

```bash
curl -X POST https://salone-auto-backend.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nunziatamanuel5@gmail.com","password":"LA-TUA-ADMIN-PASSWORD"}'
```

Se torna un JSON con `accessToken`, hai finito: backend e database funzionano.

---

## Passo 6 — Provare che le mail partono davvero

Questa è la parte che la traccia valuta di più, e finché non la provi non sai se
funziona. Il giro completo è descritto in `resume.md` §4; la versione breve, tutta
con `curl` senza aspettare il frontend:

1. **Entra come admin** e tieni da parte il token (passo 5).
2. **Crea un'auto pubblicata a 12.000**:

```bash
curl -X POST https://salone-auto-backend.onrender.com/api/admin/auto \
  -H "Authorization: Bearer IL-TOKEN-ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"marca":"Fiat","modello":"Panda","anno":2019,"chilometri":60000,
       "alimentazione":"BENZINA","descrizione":"Prova","prezzo":12000.00,
       "prezzoAcquisto":9000.00,"stato":"PUBBLICATA"}'
```

3. **Registrati come utente normale** con un indirizzo vero a cui accedi
   (`POST /api/auth/registrazione`), poi fai login e tieni quel token.
4. **Fissa la soglia a 9.000** con quel token:
   `POST /api/avvisi` con `{"autoId": 1, "soglia": 9000.00}`.
5. **Torna admin e abbassa il prezzo a 8.500**:
   `PATCH /api/admin/auto/1/prezzo` con `{"prezzo": 8500.00}`.
6. **Guarda la casella**: deve arrivare la mail.
   Controlla anche in **spam**, la prima volta ci finisce spesso.
7. Rimetti 8.500 e poi 8.000: **non deve arrivare niente**.
8. Riporta a 12.000 e poi di nuovo a 8.500: **non deve arrivare niente**
   (`inviato` non torna indietro).

Se la mail non arriva, apri i **Logs** del backend e cerca `avviso`:
il fallimento è scritto con l'id dell'avviso. La causa quasi sempre è
`MAIL_PASSWORD` sbagliata — password dell'account invece che password per le app,
o copiata con gli spazi dentro.

---

## Passo 7 — Il frontend

Il blocco `salone-auto-frontend` in `render.yaml` è già attivo.

1. `git push`: Render se ne accorge e crea il servizio statico
   (se il Blueprint esisteva già, apri **Blueprint → Sync**).
2. Sul servizio frontend imposta `VITE_API_URL` con l'indirizzo del backend.
3. **Torna sul backend** e cambia `ALLOWED_ORIGIN` con l'indirizzo esatto del
   frontend, `https://salone-auto-frontend.onrender.com`, **senza barra finale**.
4. Riavvia il backend.

Il punto 3 è quello che si dimentica sempre. Se `ALLOWED_ORIGIN` è sbagliato
succedono due cose insieme: il frontend prende errori CORS, **e** i link dentro le
mail (`/auto/:id` e `/disattiva-avviso/:token`) puntano nel posto sbagliato.

---

## Cose del piano gratuito da sapere prima

Non sono errori tuoi, sono limiti del piano free. Meglio saperli adesso che
scoprirli il giorno della consegna.

- **Il database gratuito scade dopo 30 giorni.** Render lo cancella. Segnati la data
  di creazione: se la consegna è più in là, o lo ricrei (e rifai il seed) o passi al
  piano a pagamento. **È il limite più insidioso di tutti.**
- **Il backend si addormenta dopo 15 minuti di inattività.** La prima richiesta dopo
  la pausa ci mette anche **un minuto** a rispondere, perché deve riaccendere il
  contenitore. Se fai vedere il progetto a qualcuno, apri il sito qualche minuto
  prima così è già sveglio.
- **La prima costruzione è lenta** (5-10 minuti). Le successive meno.
- **I siti statici non si addormentano**: quando pubblicherai il frontend, quello
  risponderà sempre subito. Sarà il backend a far aspettare.

---

## Se qualcosa va storto

| Cosa vedi | Quasi sempre è |
|---|---|
| Build del frontend fallita su `npm ci` | `frontend/package-lock.json` non è stato committato |
| `Started...` non compare, log pieni di errori su una variabile | una delle chiavi del passo 4 è vuota o scritta male |
| `/actuator/health` dà 503 | il database non risponde: controlla che sia `available` e nella stessa regione |
| Login dà 401 con la password giusta | `ADMIN_PASSWORD` è stata cambiata **dopo** il primo avvio: l'amministratore era già stato creato con la vecchia e non viene ricreato |
| La mail non arriva, nei log `invio della mail fallito` | `MAIL_PASSWORD` sbagliata o copiata con gli spazi |
| Errori CORS dal frontend | `ALLOWED_ORIGIN` con la barra finale, o con `http` invece di `https` |

Sulla riga del login: se sbagli `ADMIN_PASSWORD` al primo avvio, cambiarla dopo non
serve, perché `CaricamentoAdmin` crea l'amministratore **solo se non esiste già**.
Per rimediare: cancella quell'utente dal database (dalla console PostgreSQL di
Render), correggi la variabile e riavvia.
