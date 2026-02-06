## GestioLAN per client desktop ##

For the english version of this readme, check "README_EN.md"
Software client del database "GestioLAN"

## Framework & Librerie utilizzate per lo sviluppo

- JavaFX
- Jackson
- Monster energy drink e parecchi caffe macchiati :3

## Come funziona?

### Login
L'utente effetua il login, facendo l `POST` dei dati all'api

### Registrazione utente
> TO DO (per implementare questa cosa, quando l'utente prova a cambiare server e preme connetti, o all avvio del software, esso fa un controllo automatico e nel caso, apre una schermata di registrazione)
- Il primo utente sarà registrato da un client, se nel database a cui ci si cerca di connettere non ha nessun utente (il primo utente avrà i permessi piu alti nel database)
- per registrare altri utenti, deve essere un utente con permessi piu alti, che dalle impostazioni del client crea un utente, definendo i vari dati
*per permettere di essere autentico, il post verrà fatto mandando anche il JWT dell utente che registra*

# TODO

Nella cartella "FXML" ci sono i file che gestiscono la struttura grafica delle pagine
Nella cartella "Controllers" ci sono i file che gestiscono la logica delle pagine
    Nota: ogni cartella dentro controller gestisce pagina, se dentro di essa ci sono altri
    oggetti, per una questione di ordine ho separato la responsabilità a piu classi, che pero
    sono delle sottocartelle del controller "principale"

## Struttura e mappatura
ogni file FXML ha il corrispettico controller .java

### Path dei controller e degli fxml
`src/main/java/it/cookie/progetti/controllers`
`src/main/resources/FXML`

partendo dai rispettivi path, i file .java e .fxml seguono la stessa struttura gerarchica

nel codice, per avere piu ordine e permettere una manutenzione migliore futura, per ottenere i file .fxml ho usato `scene.properties` per mappare il file .fxml al suo rispettivo percorso