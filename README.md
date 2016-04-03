# OpenGrest

OpenGrest è un programma per gestire contenuto multimediale (video, musica, testo, immagini...) su uno schermo esteso proiettato visibile ad un pubblico.

Il vantaggio di questa applicazione sta nella facilità di comunicazione fra i due schermi senza che ci sia interazione diretta dell'utente nello schermo esteso, cosa che è fastidiosa per il pubblico e difficile per l'utente che muove il mouse fra gli schermi. Inoltre OpenGrest permette di gestire molti contenuti in una sola finestra.

Quest'applicazione è un progetto personale, sviluppato negli anni per esigenze personali - precisamente gestire il computer durante un Grest ;) - pertanto potreste trovare varie funzioni più fastidiose che utili. In ogni caso il progetto è rilasciato sotto licenza GNU GPL v3, quindi sentitevi liberi di sviluppare la vostra versione adatta alle vostre esigenze.

## Funzionalità

L'app è composta da due finestra: il pannello principale e il pannello di controllo.

Quest'ultimo permette di aggiungere, modificare o rimuovere contenuto dal pannello principale, in particolare:

 - modificare o nascondere titolo e sottotitolol;

 - modificare o nascondere il countdown verso una precisa data e ora in fondo al pannello principale;

 - aggiungere testo formattato (colore, stile e dimensione);

 - mostrare il testo di una canzone (salvato in formato XML in un file apposito) con una dimensione a piacere;

 - aggiungere una file media (audio/video) con possibilità di autoplay, loop infinito e fullscreen;

 - aggiungere una slideshow di immagini (prossimamente);

## Guida

Il pannello va posizionato sullo schermo esteso (consiglio di massimizzarlo su tutto lo schermo), mentre il pannello di controllo deve rimanere sullo schermo principale, visibile solo all'utente che opera il computer.

L'utente al computer potrà usare i vari controlli (disposti per sezioni) per far comparire del contenuto sul pannello principale.

### Impostazioni principali

La parte in alto del pannello di controllo permette di impostare titolo, sottotitolo, giorno, ora e obbiettivo del countdown e il colore dello sfondo del pannello principale.

Per rendere le modifiche visibili sul pannello principale cliccare sul pulsante "Applica".

### Testo

Il testo da mostrare va inserito nell'apposito campo a sinistra, mentre con i controlli a destra è possibile regolarne la formattazione (stile, dimensione e colore).

Premere il pulsante "Aggiungi" per mostrare il testo.

NB: Il pulsante "Aggiungi" è disponibile solo se è stato inserito del testo. Per aggiungere una linea vuota inserire uno spazio e poi premere "Aggiungi".

### Testi canzoni

È possibile preparare alcuni testi di canzoni da mostrare caricando l'apposito file e cliccando il pulsante "Aggiungi".

I testi verranno inseriti in un riquadro apposito all'interno del pannello principale.

I file contenenti i testi dovranno essere in formato TXT (*.txt) ed avere la seguente struttura:

```
<?xml version="1.0"?>
<canzone titolo="Titolo di prova">
<block tipo="strofa">Una strofa di prova
con due versi.</block>
<block tipo="rit">Il ritornello
della canzone
ha tre versi.</block>
<block tipo="strofa">Una strofa di prova da un singolo verso.</block>
<block tipo="strofa">Un'altra strofa
ancora da due versi.</block>
<block tipo="strofa">Un ultimo ritornello da un verso.</block>
</canzone>
```

Le caratteristiche da rispettare sono:

 - tutta la struttura va scritta minuscola, a parte il testo della canzone;

 - mai andare a capo se non per segnalare i versi del testo o dopo un tag di chiusura (`</block>`)

 - specificare sempre il titolo nel tag `<canzone>` e il tipo nel tag `<block>`.

Questi file possono essere ovunque sul PC, ma è consigliabile salvarli in una cartella "Testi" nella stessa cartella dove è posizionato il programma OpenGrest.

### Audio/video

Cliccando sul pulsante "Sfoglia" è possibile aggiungere al pannello principale un file audio o video. Il video/audio dispone di allcuni controlli per:

 - metterlo in pausa/riprodurlo;

 - riprodurlo a tutto schermo;

 - riportare all'inizio la riproduzione;

 - determinare la percentuale di traccia riprodotta;

 - controllare il voulme dell'audio;

I formati supportati sono: aif/aiff, flv, fxm, mp4/m4a/m4v, mp3, wav, m3u8. Può accadere che alcuni file, pur avendo questi formati non possano essere riprodotti a causa di problemi con i codec.

Questi file possono essere ovunque sul PC, ma è è consigliabile salvarli in una cartella "Media" nella stessa cartella dove è posizionato il programma OpenGrest.

NB: non è possibile chiudere alcun pannello mentre un file è a tutto schermo: se ci si prova verrà visualizzato un popup di errore.

### Pulsanti

Il pulsante "Principale" mostra o nasconde il pannello principale, mentre il pulsante "Reset" elimina tutti gli elementi aggiunti al pannello principale.

## LICENZA

OpenGrest è rilasciato sotto licenza GNU GPL v3 (vedi il [file LICENSE](http://github.com/GioBonvi/OpenGrest/blob/master/LICENSE)).
