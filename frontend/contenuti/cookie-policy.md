# Cookie Policy

*Ultimo aggiornamento: 25 settembre 2026*

## In breve

Questo sito **non usa cookie di profilazione, non usa cookie pubblicitari e non
usa strumenti di statistica di terze parti**. Non c'è niente da accettare o
rifiutare, perché non c'è niente che ti segua.

Quello che il sito lascia nel tuo browser serve solo a tenerti collegato.

## Che cosa resta nel tuo browser

| Che cos'è | Dove sta | A che cosa serve | Quanto dura |
|---|---|---|---|
| `token` | **localStorage** | è il token di accesso (JWT) che si riceve entrando: viene rimandato al server a ogni richiesta, altrimenti dovresti scrivere email e password a ogni pagina | resta finché non esci o non lo cancelli; il token perde comunque validità dopo 12 ore |

### Sul token: perché ne parliamo qui

Tecnicamente il token **non è un cookie**: sta nel `localStorage`, che è un altro
meccanismo di memoria del browser. La differenza però interessa a noi, non a te:
è comunque un dato che questo sito scrive sul tuo dispositivo e che ti tiene
riconosciuto tra una pagina e l'altra, quindi è giusto che sia scritto qui.

Cose da sapere:

- il token contiene **soltanto** il tuo identificativo numerico e il tuo ruolo
  (utente o amministratore). Non contiene la tua email, il tuo nome né la password;
- resta **sul tuo dispositivo**, in questo browser: non si sposta su altri computer
  e non viene condiviso con nessuno;
- **esci dall'account** e il token viene cancellato dal browser;
- scade comunque da solo dopo 12 ore.

## Cookie tecnici del server

Il sito è una applicazione a pagina singola che parla con il backend tramite
chiamate API autenticate con il token. Il server non apre sessioni e **non
imposta cookie propri**.

## Servizi esterni

Le pagine non caricano contenuti incorporati da terze parti (niente mappe, niente
video, niente riquadri social, niente font o script esterni: i caratteri sono
serviti dal sito stesso).

L'unica eccezione sono le **foto delle auto**: il browser le scarica dall'indirizzo
che l'amministratore indica per ciascuna auto, che può stare su un altro sito.
Vengono chieste come semplici immagini, senza inviare l'indirizzo della pagina che
stai guardando, e questo sito non usa quelle richieste per riconoscerti.

Il tema chiaro o scuro segue l'impostazione del tuo sistema e non viene salvato.

Il sito è ospitato su **Render**, che può registrare dati tecnici di connessione
(come l'indirizzo IP) nei propri log di servizio, per motivi di sicurezza e di
funzionamento dell'infrastruttura. Sono log del fornitore di hosting, non cookie,
e noi non li usiamo per riconoscerti.

## Come cancellare quello che è rimasto

- **Dal sito**: esci dall'account, il token viene rimosso.
- **Dal browser**: nelle impostazioni, alla voce dei dati dei siti, si cancella
  tutto quello che questo sito ha salvato.

Cancellare il token ti disconnette e basta: il tuo account, i preferiti e gli
avvisi restano dove sono. Per cancellare anche quelli si usa «Elimina il mio
account» dal profilo, come spiegato nella [Privacy Policy](./privacy-policy.md).
