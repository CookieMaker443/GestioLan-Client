# GestioLAN – Documentazione del sistema dei temi

Questo documento descrive `ThemeManager`, `ThemeInfo` e `ThemeManifest`: come sono fatti, come lavorano insieme, cosa fa ogni metodo e come si crea un tema.

> Sostituisce la sezione "`ThemeManager` (proposto)" di `GestioLAN-managers-docs.md`.

---

## 1. Idea generale

- **Un solo punto decide il tema:** `ThemeManager`. Le pagine FXML e i controller non conoscono né il nome né il percorso del tema.
- **`SceneManager` applica il tema a ogni scena** subito dopo averla creata e prima di mostrarla.
- **Il tema scelto si carica sopra il tema base** (`default_theme`). Un tema può quindi limitarsi a ridefinire ciò che cambia (ad esempio solo la palette) e non può "rompere" l'app dimenticando una classe.
- **Il cambio tema avviene a caldo.** JavaFX ristilizza i nodi quando cambia la lista `scene.getStylesheets()`: non serve ricaricare la pagina, e controller e stato (testo digitato, selezioni, scroll) restano intatti.
- **Due tipi di temi:**
  - *inclusi nel jar* (`resources/assets/css/<id>/`): il manifest è facoltativo;
  - *dell'utente* (`~/.config/gestiolan/themes/<id>/`): il manifest `theme.json` è obbligatorio.

### Dove stanno i file

```
resources/assets/css/                   (dentro il jar)
└── default_theme/
    ├── theme.css                       ← tema base, un solo .root
    └── theme.json                      (facoltativo)

~/.config/gestiolan/themes/             (Windows: %APPDATA%\gestiolan\themes)
└── frutiger_aero/
    ├── theme.json                      ← obbligatorio: rende la cartella un tema valido
    ├── theme.css
    ├── dashboard_bg.png                ← immagini referenziate dal CSS
    └── preview.png                     (facoltativo)
```

L'**id** di un tema è il nome della sua cartella. È l'id (non il nome visualizzato) che viene salvato in `settings.properties` con la chiave `theme`.

### Flusso

```
Avvio / cambio scena
   SceneManager.loadScene()
      → loader.load()                      crea i nodi
      → new Scene(root)
      → ThemeManager.apply(scene)          registra la scena e imposta gli stylesheet
      → stage.show()                       la scena appare già con lo stile

Cambio tema (dalle impostazioni)
   ThemeManager.setTheme(id)
      → find(id)                           il tema esiste ed è valido?
      → SettingsManager.setTheme(id)       salva su settings.properties
      → style(scena) per ogni scena aperta ristilizzazione immediata
```

---

## 2. `ThemeManifest`

**Package:** `it.cookie.progetti.managers`

`record` che rappresenta il contenuto di `theme.json`, letto con Jackson.

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record ThemeManifest(String name, String author, String version,
                            String description, String css) {}
```

| Campo | Significato | Se manca |
|---|---|---|
| `name` | Nome mostrato nell'interfaccia. | Si usa l'id (nome della cartella). |
| `author` | Autore. | Stringa vuota. |
| `version` | Versione del tema. Al momento è solo informativa e **non** viene copiata in `ThemeInfo`. | – |
| `description` | Breve descrizione. | Stringa vuota. |
| `css` | Nome del file CSS dentro la cartella. | `theme.css`. |

`@JsonIgnoreProperties(ignoreUnknown = true)` fa ignorare i campi sconosciuti: un manifest scritto per una versione futura non causa errori.

Esempio di `theme.json`:

```json
{
  "name": "Frutiger Aero",
  "author": "Cookie",
  "version": "1.0",
  "description": "Vetro, bolle e cielo azzurro",
  "css": "theme.css"
}
```

Se il JSON è malformato, Jackson lancia un'eccezione (sottoclasse di `IOException`) e il tema viene considerato non valido.

---

## 3. `ThemeInfo`

**Package:** `it.cookie.progetti.managers`

`record` immutabile che descrive un tema **già verificato e pronto all'uso**. È ciò che `ThemeManager` restituisce a chi vuole mostrare o usare un tema (ad esempio la `ComboBox` delle impostazioni).

```java
public record ThemeInfo(String id, String name, String author, String description,
                        String cssUrl, boolean builtin) {}
```

| Campo | Significato |
|---|---|
| `id` | Nome della cartella del tema. È il valore salvato nelle impostazioni. |
| `name` | Nome da mostrare (dal manifest, altrimenti l'id). |
| `author` | Autore, o stringa vuota. |
| `description` | Descrizione, o stringa vuota. |
| `cssUrl` | URL completo del CSS: `jar:file:...!/assets/css/x/theme.css` per i temi inclusi, `file:///home/.../theme.css` per quelli dell'utente. È il valore da passare a `getStylesheets()`. |
| `builtin` | `true` se il tema è nel jar, `false` se è nella cartella utente. Utile per vietare la cancellazione dei temi inclusi. |

Il `toString()` dei record è poco leggibile (`ThemeInfo[id=..., ...]`). Per mostrare il nome in una `ComboBox<ThemeInfo>` serve un `StringConverter` (esempio al paragrafo 6).

---

## 4. `ThemeManager`

**Package:** `it.cookie.progetti.managers` — singleton.

### 4.1 Costanti e campi

| Elemento | Descrizione |
|---|---|
| `BUILTIN_DIR = "/assets/css/"` | Percorso nel classpath dei temi inclusi. |
| `BUILTIN = List.of("default_theme")` | Elenco degli id dei temi inclusi. **Va aggiornato a mano** quando se ne aggiungono: dentro un jar non si può elencare il contenuto di una cartella. |
| `BASE = "default_theme"` | Tema base, sempre caricato per primo. |
| `MANIFEST = "theme.json"` | Nome del file manifest. |
| `MAPPER` | `ObjectMapper` di Jackson, condiviso. |
| `instance` | Istanza unica del singleton. |
| `userThemesDir` | `AppPaths.dir("themes")`: cartella dei temi dell'utente (creata se manca). |
| `scenes` | Lista di `WeakReference<Scene>` con le scene a cui è stato applicato il tema. I riferimenti deboli non impediscono di liberare scene chiuse. |

### 4.2 Metodi

#### `static ThemeManager getInstance()`
Restituisce l'istanza unica, creandola al primo utilizzo. Non è sincronizzato: usalo dal thread JavaFX.

#### `Optional<ThemeInfo> find(String id)`
Punto d'ingresso per cercare un tema per id. Se l'id è in `BUILTIN` delega a `loadBuiltin`, altrimenti a `loadUser`. Restituisce `Optional.empty()` se il tema non esiste o non è valido. **Legge sempre dal disco**: non c'è cache.

#### `private Optional<ThemeInfo> loadBuiltin(String id)`
1. Cerca la risorsa `/assets/css/<id>/theme.css` con `getResource`. Se manca, restituisce vuoto.
2. Prova a leggere `/assets/css/<id>/theme.json`. Per i temi inclusi è facoltativo: se manca o è illeggibile, `m` resta `null` e si usano i valori di default.
3. Costruisce un `ThemeInfo` con `builtin = true` e come `cssUrl` l'URL della risorsa.

#### `private Optional<ThemeInfo> loadUser(String id)`
Valida un tema nella cartella dell'utente, passo per passo:
1. Costruisce `themes/<id>` e lo **normalizza**. Se il risultato non inizia con `userThemesDir` (tentativo di uscire dalla cartella con `..`) o non è una cartella, restituisce vuoto.
2. Se `theme.json` non esiste, la cartella **non è un tema**: vuoto.
3. Legge il manifest con Jackson. Se è malformato, vuoto.
4. Determina il nome del CSS (`css` del manifest, oppure `theme.css`), lo normalizza e controlla di nuovo che resti dentro la cartella del tema e che sia un file regolare.
5. Restituisce un `ThemeInfo` con `builtin = false` e come `cssUrl` l'URI del file.

I due controlli `normalize()` + `startsWith()` sono importanti quando in futuro si potranno importare temi esterni: un manifest con `"css": "../../x.css"` viene rifiutato.

#### `private ThemeInfo info(String id, ThemeManifest m, String cssUrl, boolean builtin)`
Costruisce il `ThemeInfo` applicando i default: `name` = `m.name()` oppure l'id; `author` e `description` = stringa vuota se assenti.

#### `List<ThemeInfo> listThemes()`
Elenca i temi disponibili:
1. prima i temi di `BUILTIN` (quelli validi);
2. poi ogni sottocartella di `themes/` il cui id non coincide con un tema incluso e che supera `loadUser`.

Le cartelle non valide (senza manifest, JSON errato, CSS mancante) vengono **saltate in silenzio**. Un tema utente con lo stesso id di uno incluso viene ignorato. L'ordine dei temi utente è quello restituito dal filesystem, quindi non è garantito: per mostrarli ordinati usa `sorted(Comparator.comparing(ThemeInfo::name))`.

#### `List<String> cssUrls()`
Calcola gli stylesheet da applicare, **in ordine di priorità** (l'ultimo vince a parità di specificità):
1. legge l'id scelto da `SettingsManager.getTheme()`;
2. prende l'URL del tema base;
3. se il tema scelto esiste ed è diverso dal base → `[base, scelto]`;
4. se il tema scelto non esiste più o non è valido → `[base]` (**ripiego automatico**).

Se anche il tema base manca dal jar, `orElseThrow()` lancia `NoSuchElementException`: è un errore di packaging, non una situazione normale.

#### `void apply(Scene scene)`
Da chiamare da `SceneManager` per ogni scena nuova (e dopo ogni `setRoot` se si riusa la scena). Registra la scena (`register`) e imposta gli stylesheet (`style`).

#### `void apply(DialogPane pane)`
Imposta gli stylesheet su un `DialogPane` (usato per gli `Alert`, che hanno una scena propria). Non registra la scena: gli alert sono modali e di breve durata, quindi non servono aggiornamenti a caldo.

#### `private void style(Scene scene)`
`scene.getStylesheets().setAll(cssUrls())`: sostituisce l'intera lista. Chiamando `setAll` JavaFX ristilizza subito tutta la scena.

#### `private void register(Scene scene)`
Rimuove dalla lista i riferimenti deboli già svuotati (`get() == null`) e aggiunge la scena se non è già presente (confronto per identità, `==`).

#### `void setTheme(String id)`
Cambia tema a caldo:
1. se `find(id)` è vuoto, **esce senza fare nulla** (nessun errore, nessun valore restituito);
2. salva `id` in `settings.properties`;
3. chiama `style` su tutte le scene ancora vive.

Va chiamato dal thread JavaFX (ad esempio da un'azione di un controller).

---

## 5. Come il manager si accorge di temi aggiunti o rimossi

**Fa tutto da solo, in modo "pigro" (on demand).** Non esiste una cache né un controllo in background: ogni chiamata a `find`, `listThemes` e `cssUrls` interroga di nuovo il disco. Nessuna classe esterna deve avvisarlo.

| Situazione | Cosa succede |
|---|---|
| **Aggiungo una cartella tema mentre l'app è aperta** | La vedrà alla prossima chiamata di `listThemes()`. Basta richiamarla quando si apre la pagina delle impostazioni, o con un pulsante "Aggiorna elenco". |
| **Rimuovo un tema che non è quello in uso** | Sparisce dall'elenco alla prossima chiamata di `listThemes()`. |
| **Rimuovo il tema in uso** | Le scene già aperte mantengono lo stile già caricato. Alla prossima scena (o a un `setTheme`) `cssUrls()` non trova il tema e usa il base. In `settings.properties` resta il vecchio id: ogni avvio ricade sul base senza errori finché non si sceglie un altro tema. |
| **Manifest rotto, CSS mancante** | Il tema viene trattato come inesistente e non compare in `listThemes()`. |
| **Modifico un `theme.css` mentre l'app gira** | JavaFX tiene in cache il CSS già letto per URL, quindi la modifica non appare da sola. Per forzare la rilettura togli e rimetti lo stylesheet, oppure aggiungi un parametro all'URL (ad esempio `?v=<timestamp>`). Utile solo a chi sviluppa temi. |

Possibili evoluzioni, solo se servono:
- un metodo `reload()` che richiama `style` su tutte le scene (per il pulsante "Aggiorna");
- un `WatchService` (`java.nio.file`) sulla cartella `themes/` per rilevare le modifiche in tempo reale: normalmente è eccessivo rispetto a un controllo all'apertura delle impostazioni;
- in `setTheme` / all'avvio, se il tema salvato non esiste più, riportare l'impostazione a `default_theme` invece di ricadere ogni volta sul base.

---

## 6. Uso dalle altre classi

### `SceneManager`

```java
Parent root = loader.load();
Scene scene = new Scene(root, minWidth, minHeight);
ThemeManager.getInstance().apply(scene);     // DOPO la creazione della scena
stage.setScene(scene);
```

Per i popup, applica il tema alla scena appena creata (non a `popupStage.getScene()`, che è `null` finché non chiami `setScene`):

```java
Scene popupScene = new Scene(root, minWidth, minHeight);
ThemeManager.getInstance().apply(popupScene);
popupStage.setScene(popupScene);
```

Per gli alert:

```java
Alert alert = new Alert(alertType);
// ...
ThemeManager.getInstance().apply(alert.getDialogPane());
alert.showAndWait();
```

### Controller delle impostazioni

```java
@FXML private ComboBox<ThemeInfo> themeCombo;

@FXML
public void initialize() {
    ThemeManager tm = ThemeManager.getInstance();
    themeCombo.setConverter(new StringConverter<>() {
        @Override public String toString(ThemeInfo t) { return t == null ? "" : t.name(); }
        @Override public ThemeInfo fromString(String s) { return null; }
    });
    themeCombo.getItems().setAll(tm.listThemes());      // rilegge il disco ogni volta
    String current = SettingsManager.getInstance().getTheme();
    tm.listThemes().stream().filter(t -> t.id().equals(current))
      .findFirst().ifPresent(themeCombo::setValue);
}

@FXML
private void onApplyTheme() {
    ThemeInfo selected = themeCombo.getValue();
    if (selected != null) {
        ThemeManager.getInstance().setTheme(selected.id());   // salva + aggiorna tutte le scene
    }
}
```

La pagina non va ricaricata.

### Regole da rispettare negli FXML

- **Nessun `stylesheets="@..."` sul nodo root.** Gli stylesheet impostati su un nodo hanno priorità più alta di quelli della scena e annullerebbero il tema.
- I controlli fuori dalla scena principale (alert, finestre aggiuntive) devono ricevere il tema esplicitamente.

---

## 7. Come si crea un tema

1. Crea la cartella `~/.config/gestiolan/themes/<id>/` (l'id sarà il nome della cartella, meglio senza spazi).
2. Aggiungi `theme.json` (obbligatorio) e `theme.css`.
3. Nel CSS ridefinisci ciò che cambia. Esempio minimo (solo palette):

   ```css
   .root {
       -gl-accent: #1E90D8;
       -gl-accent2: #8BE04E;
   }
   ```
4. Le immagini si mettono nella stessa cartella e si referenziano con percorso **relativo al CSS**:

   ```css
   .dashboard-root {
       -fx-background-image: url("dashboard_bg.png");
       -fx-background-size: cover;
   }
   ```
5. Apri le impostazioni e scegli il tema (o riavvia l'app).

Consigli:
- Non rendere trasparente `-gl-surface` nel `.root`: è usata anche da campi di testo, popup e tooltip. Per far vedere lo sfondo usa colori semitrasparenti solo sulle classi dei pannelli (`.sidebar`, `.top-section`, `.item-card`).
- Il "vetro" con sfocatura reale non si ottiene col solo CSS: si imitano trasparenza, gradienti, bordi chiari e ombre.

### Temi inclusi nel jar (da fare a feature finita)

1. Copia la cartella del tema in `resources/assets/css/<id>/`.
2. Aggiungi l'id alla lista `BUILTIN` di `ThemeManager`.
3. Il manifest è facoltativo ma consigliato (nome e descrizione leggibili).

---

## 8. Limiti noti

- `version` del manifest non viene propagata a `ThemeInfo`.
- `setTheme` non segnala all'esterno un id non valido (restituisce `void`).
- L'ordine dei temi utente in `listThemes()` non è garantito.
- `getInstance()` non è thread-safe: usare il thread JavaFX.
- `BUILTIN` va mantenuto a mano.
- Gli `Alert` ricevono il tema solo al momento della creazione: se il tema cambia mentre un alert è aperto, quello non viene aggiornato.
