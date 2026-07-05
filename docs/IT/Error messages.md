# MessageText

## Overview

Il record `MessageText` fornisce un meccanismo robusto e flessibile per la gestione della messaggistica e degli errori. Permette di incapsulare sia errori noti, identificabili tramite chiavi di localizzazione (keyword), sia stringhe di errore grezze e impreviste (ad esempio, eccezioni relative a certificati TLS non riconosciuti dalla JVM durante l'uso di un reverse proxy locale sprovvisto di CA). Questo pattern architetturale garantisce che l'applicazione sia in grado di fornire sempre un feedback visivo all'utente, indipendentemente dal fatto che l'anomalia sia stata preventivamente mappata a sistema o meno.

---

## Tech Stack

* **Java Version:** Java 16+ (necessario per l'utilizzo dei `record` come feature standard).
* **Design Pattern**: Static Factory Method (per la creazione sicura e controllata delle istanze).
---

## Project Structure

* `it.cookie.utils.network.user`: Package responsabile delle classi di utilità per la gestione della rete e delle operazioni relative agli utenti (come i messaggi di login o gli errori di connessione).

---

## Classes & Interfaces

### MessageText

* **Type:** Record
* **Package:** `it.cookie.utils.network.user`
* **Description:** Rappresenta un contenitore immutabile per un messaggio di testo o un errore, differenziando semanticamente gli stati previsti (localizzabili tramite `ResourceBundle`) da quelli imprevisti (eccezioni native).

**Fields**

| Name | Type | Purpose |
| --- | --- | --- |
| `bundleKey` | `String` | Chiave del `ResourceBundle` usata per recuperare la traduzione dell'errore noto. |
| `rawMessage` | `String` | Messaggio di errore grezzo generato dalla JVM o dall'eccezione non gestita. |

**Methods**

| Signature | Parameters | Return Type | Description |
| --- | --- | --- | --- |
| `known(String bundleKey)` | `bundleKey`: La chiave di traduzione per il messaggio noto | `MessageText` | Metodo factory statico che restituisce un'istanza contenente solo la chiave per la localizzazione. |
| `unknown(String rawMessage)` | `rawMessage`: La stringa contenente l'errore generico | `MessageText` | Metodo factory statico che restituisce un'istanza contenente solo il messaggio di errore grezzo. |
| `isKnown()` | Nessuno | `boolean` | Verifica se l'istanza corrente rappresenta un errore noto (ovvero se `bundleKey` non è null). |

**Source Code Reference:**

```java
package it.cookie.utils.network.user;

/**
 * Rappresenta un errore di login o un messaggio di sistema.
 * - "known": errore previsto, identificato da una chiave del ResourceBundle.
 * - "unknown": errore non previsto, contiene il messaggio grezzo.
 */
public record MessageText(String bundleKey, String rawMessage) {

    public static MessageText known(String bundleKey) {
        return new MessageText(bundleKey, null);
    }

    public static MessageText unknown(String rawMessage) {
        return new MessageText(null, rawMessage);
    }

    public boolean isKnown() {
        return bundleKey != null;
    }
}

```

## Configuration

Per il corretto funzionamento dei messaggi di tipo `known`, è richiesta la configurazione di un `ResourceBundle` (es. `messages.properties`, `messages_it.properties`, ecc.) che contenga le chiavi di mappatura fornite al record (es. `login.SERVER_TIMEOUT=Il server non risponde`).

---

## Examples

### Flusso di utilizzo

L'utilizzo di questa classe prevede l'istanziazione del record tramite i metodi factory statici `known()` e `unknown()`. Questo design object-oriented permette di distinguere in modo deterministico un'eccezione non gestita da una chiave di localizzazione. Quest'ultima verrà successivamente risolta tramite un `ResourceBundle` per ottenere il messaggio nella lingua corretta. In fase di visualizzazione o elaborazione, è sufficiente verificare la natura dell'errore (tramite il metodo `isKnown()`) per estrarre la stringa corretta da mostrare all'utente.

### Snippet di Implementazione

Di seguito viene mostrato come creare e utilizzare sia un errore noto che uno sconosciuto:

```java
import it.cookie.utils.network.user.MessageText;
import java.util.Locale;
import java.util.ResourceBundle;

public class ErrorHandlingExample {
    public static void main(String[] args) {
        
        // Inizializzazione di un ipotetico ResourceBundle per le traduzioni
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        
        // 1. Creazione di un record di tipo "known" (errore noto/gestito)
        MessageText knownError = MessageText.known("login.SERVER_TIMEOUT");
        
        // 2. Creazione di un record di tipo "unknown" (errore grezzo/imprevisto)
        MessageText unknownError = MessageText.unknown("PKIX path building failed: unable to find valid certification path");
        
        // --- UTILIZZO ---
        
        // Risoluzione del messaggio per l'errore noto
        String message1 = knownError.isKnown() 
            ? bundle.getString(knownError.bundleKey()) 
            : knownError.rawMessage();
            
        // Presunto Output per message1:
        // "Il server ha impiegato troppo tempo per rispondere. Riprova." (Traduzione recuperata dal properties)
        System.out.println(message1);

        // Risoluzione del messaggio per l'errore sconosciuto
        String message2 = unknownError.isKnown() 
            ? bundle.getString(unknownError.bundleKey()) 
            : unknownError.rawMessage();
            
        // Presunto Output per message2: 
        // "PKIX path building failed: unable to find valid certification path" (Messaggio grezzo non tradotto)
        System.out.println(message2);
    }
}

```