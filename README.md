# Salone auto — Consegna D5

Un mini salone di automobili con avvisi di prezzo via mail.

Chi non ha fatto l'accesso sfoglia il catalogo, con ricerca e ordinamento.
Chi si registra salva le auto tra i preferiti e su ognuna può fissare una soglia
di prezzo. L'amministratore vede anche le bozze e il prezzo d'acquisto, crea e
modifica le auto e ne cambia il prezzo: se il nuovo prezzo scende sotto la soglia
di un utente, a quell'utente parte una mail.

## Com'è diviso

| Cartella | Cosa c'è |
|---|---|
| `backend/` | Spring Boot 4, Java 25, PostgreSQL |
| `frontend/` | React 19 + Vite + Tailwind v4, e i testi di Privacy e Cookie Policy in `contenuti/` |
| `render.yaml` | database, backend e frontend su Render |
| `resume.md` | stato dei lavori, API per intero e passi per il frontend |

## Partire

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

Servono PostgreSQL in locale e le variabili d'ambiente elencate in
`backend/.env.example`: senza `JWT_SECRET`, `ADMIN_PASSWORD`, `MAIL_USERNAME` e
`MAIL_PASSWORD` il server non parte, ed è voluto.

Il frontend, in un secondo terminale:

```bash
cd frontend
cp .env.example .env.local   # VITE_API_URL=http://localhost:8080
npm install
npm run dev                  # http://localhost:5173
```

## Da leggere

- **`resume.md`** — tutte le rotte, i payload e cosa manca da costruire.
- **`backend/RELAZIONE.md`** — le scelte tecniche e il perché: quando scatta un
  avviso, perché la mail parte dopo il commit, perché non ne partono due e cosa
  succede se Gmail non risponde.
