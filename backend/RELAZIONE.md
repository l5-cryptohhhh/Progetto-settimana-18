# Relazione — Salone auto con avvisi di prezzo

Le scelte che la traccia lasciava aperte, e il perché.

---

## 1. Quando scatta un avviso

Un avviso è la riga che lega un utente a un'auto: ci sono la soglia, il flag `inviato`
e il flag `attivo`.

La condizione di scatto è **l'attraversamento**, non il semplice "prezzo sotto soglia":

```
prezzoVecchio > soglia   AND   prezzoNuovo <= soglia
```

Sta scritta una volta sola, dentro la query `AvvisoPrezzoRepository.daNotificare`.
Da lì scendono da sole tutte le altre regole della traccia:

| Cosa fa l'amministratore | Cosa succede | Perché |
|---|---|---|
| 12.000 → 8.000, soglia 9.000 | parte la mail | prima sopra, adesso sotto |
| 12.000 → 9.000, soglia 9.000 | parte la mail | "uguale o sotto" conta |
| salva di nuovo 8.000 | niente | `prezzoVecchio > soglia` è falso |
| 8.000 → 7.500 | niente | era già sotto, non c'è attraversamento |
| 8.000 → 12.000 | niente | il servizio esce prima ancora di interrogare il database |

Se il prezzo sale o resta identico, `notificaAttraversamentoSoglia` esce subito:
non ha senso far lavorare il database per una cosa che non può essere successa.

**Una mail sola per avviso.** Il flag `inviato` non torna mai a `false`, nemmeno
quando il prezzo risale e poi riscende. Per lo stesso motivo, cambiare la soglia
di un avviso già scattato non lo rimette in gioco: altrimenti sarebbe bastato
alzare e riabbassare la soglia per farsi mandare la stessa mail quante volte si vuole.
Chi vuole essere riavvisato cancella l'avviso e ne crea uno nuovo.

---

## 2. Perché la mail parte dopo il commit

Il cambio di prezzo pubblica un `PrezzoCambiatoEvento` **dentro** la transazione.
Chi lo ascolta è `AscoltatoreCambioPrezzo`:

```java
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
```

- **AFTER_COMMIT**: se il salvataggio del prezzo fallisce e la transazione torna
  indietro, il listener non viene mai chiamato. Non esiste il caso "mail partita
  per un prezzo che nel database non c'è".
- **@Async**: la chiamata dell'amministratore torna appena il prezzo è salvato.
  Se Gmail ci mette cinque secondi, quei cinque secondi non li aspetta lui.

Dentro l'evento ci sono solo `autoId`, `prezzoVecchio` e `prezzoNuovo`: numeri e
un id, nessuna entità. Il listener gira su un altro thread e a transazione chiusa,
quindi un `Auto` o un `Utente` passati lì dentro sarebbero oggetti staccati dalla
sessione di Hibernate.

---

## 3. Perché non partono due mail

Due modifiche di prezzo ravvicinate fanno partire due listener, che possono
trovare lo stesso avviso ancora con `inviato = false`.

Il segno si prende in un colpo solo, lasciando decidere al database:

```sql
UPDATE avvisi_prezzo SET inviato = true, inviato_il = ? WHERE id = ? AND inviato = false
```

La riga la aggiorna uno solo dei due: l'altro vede zero righe aggiornate e non
manda niente. Sta in `MarcatoreAvvisi`, in una classe a parte perché deve girare
nella **propria** transazione: chi lo chiama è il listener, che parte a transazione
già chiusa.

Il controllo è `righeAggiornate == 1`, non un `if (!avviso.isInviato())` in Java:
un controllo in memoria seguito da una scrittura è esattamente la finestra in cui
due thread passano tutti e due.

---

## 4. Se Gmail non risponde: **al massimo una volta**

La traccia lasciava scegliere. Qui il segno si prende **prima** di spedire e,
se la spedizione fallisce, **non si torna indietro**: l'avviso resta `inviato`
e quella mail è persa.

Perché questa e non l'altra:

1. **Un doppione dà più fastidio di una mancata.** È una mail commerciale.
   Chi la riceve due volte pensa che il sito sia rotto o che sia spam; chi non la
   riceve il prezzo aggiornato lo vede comunque sul sito, nei suoi avvisi e nei preferiti.
2. **Il doppione non sarebbe uno solo.** Rimettendo `inviato = false` l'avviso
   tornerebbe in gioco a ogni successivo ribasso: un problema temporaneo di Gmail
   diventerebbe una raffica di mail quando il servizio torna.
3. **Il dato non si perde.** Il fallimento finisce nei log con l'id dell'avviso,
   quindi è rintracciabile.

Il costo di questa scelta è dichiarato: **una mail persa non viene recuperata**.
Se un giorno servisse la garanzia opposta, la strada giusta non è rimettere il flag
a `false`, ma una tabella di messaggi in uscita con un tentativo periodico e una
chiave di deduplicazione — non l'ho fatta perché per questo progetto è sproporzionata.

---

## 5. I dati che arrivano dal client

**Ricerca e ordinamento.** Testo cercato e fasce di prezzo entrano come parametri
legati dentro `AutoRepository.cerca`: non c'è una sola stringa concatenata.
Il campo di ordinamento è il caso diverso, perché sta nella clausola `ORDER BY` e lì
un parametro non si può legare. Quello che arriva dal client non viene mai usato:
serve solo come chiave per cercare dentro un elenco chiuso (`OrdinamentoAuto`), e
quello che finisce nella query è il nome di proprietà scritto nel codice.
Fuori elenco → 400.

**Testo che finisce a video o nella mail.** La descrizione dell'auto e il nome
dell'utente escono dal backend come stringhe dentro il JSON, senza HTML. Il template
della mail usa solo `th:text`, che fa l'escape: non c'è un solo `th:utext` in tutto
il progetto. Lato frontend la regola corrispondente è: niente `dangerouslySetInnerHTML`.

**DTO, mai l'entità.** Registrazione, profilo, avvisi, preferiti e auto ricevono dei
`record` con dentro solo i campi che quell'operazione può toccare. `ruolo`, `inviato`
e `utenteId` in quei record non esistono: chi li aggiunge al JSON non cambia niente,
perché non c'è nessun codice che li legge. C'è un test che lo verifica
(`SicurezzaApiTest.ilRuoloNonArrivaDalClient`).

**Limite alla pagina.** `dimensione` è limitata a 50: nessuno si porta via il
catalogo intero con una richiesta sola.

---

## 6. Chi può fare che cosa

**Il ruolo lo decide il server.** `UtenteService.registra` scrive `Ruolo.UTENTE`,
punto. L'unico `ADMIN` nasce da `CaricamentoAdmin`, che legge `ADMIN_EMAIL` e
`ADMIN_PASSWORD` dalle variabili d'ambiente.

**Nel JWT solo il necessario:** id e ruolo, nient'altro. Un JWT è firmato ma non
cifrato: chi lo intercetta legge tutto quello che ci mettiamo dentro, quindi email
e nome restano fuori. Il ruolo viene comunque riletto dal database a ogni richiesta,
così un utente declassato non si tiene i vecchi permessi fino alla scadenza del token.

**403 contro 401.** Le rotte `/api/admin/**` sono chiuse con `hasRole("ADMIN")`:
chi non ha fatto l'accesso prende 401, un utente collegato che ci prova prende 403.

**404 contro 403 sulle cose degli altri.** Avvisi e preferiti si cercano per id e
proprietario insieme (`findByIdAndUtenteId`). Chi cambia `/api/avvisi/12` in
`/api/avvisi/13` riceve **404**: un 403 gli confermerebbe che quell'avviso esiste.
Il profilo sta su `/api/utenti/me` e non su `/api/utenti/{id}`, così la rotta per
chiedere i dati di un altro proprio non c'è.

**Il link di disattivazione.** Nella mail non c'è l'id dell'avviso ma un token di
32 byte da `SecureRandom`. È monouso: appena usato viene messo a `null`, quindi
un secondo clic non trova più niente.

**Log.** Nei log ci sono id, mai email né password. Il `toString` di `Utente` è
scritto a mano proprio per questo: quello generato da Lombok avrebbe stampato
l'email e l'hash della password alla prima eccezione.

---

## 7. Salute del servizio e Gmail

`/actuator/health` è pubblico perché serve a Render per capire se il servizio è vivo.

L'health indicator della mail è **disattivato** (`management.health.mail.enabled: false`).
Lasciandolo acceso, l'actuator apriva una connessione a Gmail a ogni controllo e,
se l'SMTP non rispondeva, `/actuator/health` tornava **503** e Render dava il
servizio per morto. Le mail partono in asincrono e il sito funziona lo stesso:
lo stato di Gmail non deve decidere se l'applicazione è viva.
Il problema è venuto fuori da un test, non in produzione.

---

## 8. I segreti

`JWT_SECRET`, `ADMIN_PASSWORD`, `MAIL_USERNAME` e `MAIL_PASSWORD` sono letti da
`application.yml` **senza valore di ripiego**:

```yaml
password: ${MAIL_PASSWORD}
```

Se la variabile manca il server non parte. È voluto: meglio accorgersene all'avvio
che scoprire in produzione che sta girando con una chiave di esempio. Nel file di
configurazione non c'è nessuna password vera, nemmeno commentata.
`.env.example` elenca i nomi delle variabili con valori chiaramente finti.

---

## 9. Come è provato

29 test, `./mvnw test`:

- `AvvisoPrezzoRepositoryTest` — l'attraversamento della soglia sul database vero
  (sotto, uguale, ribasso su ribasso, stesso prezzo, già inviato, disattivato) e il
  fatto che il segno si prenda una volta sola: prima chiamata 1 riga, seconda 0.
- `AvvisoPrezzoServiceTest` — prezzo in salita che non tocca il database, mail una
  volta sola, nessuna mail se il segno se l'è preso un altro, e il caso "Gmail non
  risponde" che non rimette l'avviso in coda.
- `OrdinamentoAutoTest` — l'elenco chiuso dei campi di ordinamento.
- `SicurezzaApiTest` — sulle rotte vere: 401 senza token, 403 per l'utente sulle
  rotte admin, 404 sull'avviso di un altro, il ruolo nel corpo che non serve a
  niente, la bozza invisibile e il prezzo d'acquisto che non esce.
