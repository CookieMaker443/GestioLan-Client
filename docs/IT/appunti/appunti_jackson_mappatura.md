## Suggerimenti ottenuti tramite l aiuto di LLM per la documentazione e/o per didattica

Se l'API C# usa la maiuscola (Username), ma tu la minuscola, usa questa:
```java
@com.fasterxml.jackson.annotation.JsonProperty("Username")
public void SetUsername(String username) { this.username = username; }
```

## per trasferire oggtti bisogna:
creare un DTO (un oggetto che rappresenta cio che l API si aspetta)

1 - creare una mappa chiave-valore degli oggetti da mandare 
2 - preparare l'url di dove mandare la richiesta
3 - creare una richiesta POST 
4 - Inviare la richiesta ( meglio se asincrona )
5 - leggere la risposta e trasformarla in un oggetto (tipo "user")
6 - (opzionale) chiamare "notify"

Esempio con user.java

`import com.fasterxml.jackson.annotation.JsonIgnoreProperties;`

### serve per dire a jackson di ignorare i campi non presenti in questa classe
```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class user ....
```

### Usiamo @JsonProperty per mappare esattamente il nome del JSON
```java
@JsonProperty("username")
private String username;
```

`@JsonIgnoreProperties(ignoreUnknown = true):` Questo è fondamentale. 
Quando l'API manda "password": null, Jackson ora dirà: 
"Ok, non so dove metterlo, quindi lo ignoro invece di bloccarmi".

@JsonProperty("username"): Forza Jackson a collegare la chiave del JSON direttamente alla tua variabile, 
ignorando il fatto che il tuo metodo si chiami GetUsername o getUsername.

`LocalDateTime`: La tua API manda un formato ISO (2025-12-27T14:28:59). 
`java.sql.Date` gestisce solo la data (anno-mese-giorno) e spesso fallisce con i timestamp completi. 
Usando LocalDateTime e il modulo JavaTimeModule che abbiamo configurato prima nell'ObjectMapper, funzionerà perfettamente.

## Lato client
Questo sistema ha senso anche nel lato client.

#### Se devo fare delle query con piu categorie
- nel tab sinistro premo le icone delle categorie
- quando seleziono X categorie e mando la query, il client manda un vettore di interi (che sono quelli che rappresentano le categorie scelte)
- l'api si occuperà di ottenere gli items corretti e di rimandarli
- poi successivamente servità usare il per mostrare le icone corrette

#### Mostrare le icone corrette nel panel
- l'`ItemContainerController` dovrà occuparsi di prendere l'int dell'item che contiene, fare un controllo bitwise con la mappa delle categorie (gia prese dal database dall inizio dell'esecuzion e del software), e per ogni categoria "true", inserire il `label` corrispettivo
    - Se i'id è `0` allora l'item è senza categoria, quindi avrà un label di default
- tenere in memoria i `label` segnati
- in caso di modifica, aggiornare il label con il metodo sopracitato
