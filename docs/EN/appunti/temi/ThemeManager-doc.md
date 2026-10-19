# GestioLAN – Theme-system documentation

This document describes `ThemeManager`, `ThemeInfo`, and `ThemeManifest`: their structure, how they work together, what each method does, and how to create a theme.

> Replaces the proposed `ThemeManager` section of `GestioLAN-managers-docs.md`.

---

## 1. General idea

- **One component chooses the theme:** `ThemeManager`. FXML pages and controllers do not know a theme's name or path.
- **`SceneManager` applies the theme to every scene** immediately after creation and before display.
- **The selected theme is loaded over the base theme** (`default_theme`). A theme may redefine only what changes, such as the palette, without breaking the application by omitting a class.
- **Themes switch live.** JavaFX restyles nodes when `scene.getStylesheets()` changes; a page does not need reloading, and controller state, typed text, selections, and scroll position remain intact.
- **Two kinds of themes:** built-in themes in `resources/assets/css/<id>/`, whose manifest is optional; and user themes in `~/.config/gestiolan/themes/<id>/`, for which `theme.json` is required.

### File locations

```
resources/assets/css/                   (inside the jar)
└── default_theme/
    ├── theme.css                       ← base theme, one .root
    └── theme.json                      (optional)

~/.config/gestiolan/themes/             (Windows: %APPDATA%\gestiolan\themes)
└── frutiger_aero/
    ├── theme.json                      ← required: makes the folder a valid theme
    ├── theme.css
    ├── dashboard_bg.png                ← images referenced by the CSS
    └── preview.png                     (optional)
```

A theme's **id** is its folder name. The id, not its display name, is saved in `settings.properties` under the `theme` key.

### Flow

```
Start / scene change
   SceneManager.loadScene()
      → loader.load()                   creates nodes
      → new Scene(root)
      → ThemeManager.apply(scene)       registers scene and sets stylesheets
      → stage.show()                    scene is already styled

Theme change (from settings)
   ThemeManager.setTheme(id)
      → find(id)                        does the valid theme exist?
      → SettingsManager.setTheme(id)    writes settings.properties
      → style(scene) for every open scene: immediate restyling
```

---

## 2. `ThemeManifest`

**Package:** `it.cookie.progetti.managers`

This `record` represents the contents of `theme.json`, read by Jackson.

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record ThemeManifest(String name, String author, String version,
                            String description, String css) {}
```

| Field | Meaning | If absent |
|---|---|---|
| `name` | Name displayed in the UI. | The id (folder name) is used. |
| `author` | Author. | Empty string. |
| `version` | Theme version; currently informational only and not copied to `ThemeInfo`. | – |
| `description` | Brief description. | Empty string. |
| `css` | CSS filename inside the folder. | `theme.css`. |

`@JsonIgnoreProperties(ignoreUnknown = true)` ignores unknown fields, so a manifest written for a future version does not cause errors.

Example `theme.json`:

```json
{
  "name": "Frutiger Aero",
  "author": "Cookie",
  "version": "1.0",
  "description": "Glass, bubbles, and blue sky",
  "css": "theme.css"
}
```

If JSON is malformed, Jackson throws an `IOException` subclass and the theme is invalid.

---

## 3. `ThemeInfo`

**Package:** `it.cookie.progetti.managers`

An immutable `record` describing a checked theme ready for use. `ThemeManager` returns it to consumers such as the settings `ComboBox`.

```java
public record ThemeInfo(String id, String name, String author, String description,
                        String cssUrl, boolean builtin) {}
```

| Field | Meaning |
|---|---|
| `id` | Theme folder name and value saved in settings. |
| `name` | Display name from the manifest, otherwise the id. |
| `author` | Author, or an empty string. |
| `description` | Description, or an empty string. |
| `cssUrl` | Full CSS URL: `jar:file:...!/assets/css/x/theme.css` for built-ins, or `file:///home/.../theme.css` for user themes. Pass it to `getStylesheets()`. |
| `builtin` | `true` for a theme in the jar; `false` for a user theme. Useful for preventing deletion of built-ins. |

The default record `toString()` is unsuitable for a `ComboBox<ThemeInfo>`; use a `StringConverter`, as shown in section 6.

---

## 4. `ThemeManager`

**Package:** `it.cookie.progetti.managers` — singleton.

### 4.1 Constants and fields

| Element | Description |
|---|---|
| `BUILTIN_DIR = "/assets/css/"` | Classpath location for built-in themes. |
| `BUILTIN = List.of("default_theme")` | Built-in ids. Update manually when adding one: jar directory contents cannot be enumerated. |
| `BASE = "default_theme"` | Base theme, always loaded first. |
| `MANIFEST = "theme.json"` | Manifest filename. |
| `MAPPER` | Shared Jackson `ObjectMapper`. |
| `instance` | Singleton instance. |
| `userThemesDir` | `AppPaths.dir("themes")`, creating the user-theme directory if needed. |
| `scenes` | `WeakReference<Scene>` list of themed scenes; weak references do not retain closed scenes. |

### 4.2 Methods

#### `static ThemeManager getInstance()`

Returns the singleton, creating it on first use. It is not synchronized; use it on the JavaFX thread.

#### `Optional<ThemeInfo> find(String id)`

Looks up a theme by id. Built-in ids are delegated to `loadBuiltin`; every other id to `loadUser`. It returns `Optional.empty()` when the theme is missing or invalid and always reads from disk: there is no cache.

#### `private Optional<ThemeInfo> loadBuiltin(String id)`

1. Looks up `/assets/css/<id>/theme.css` with `getResource`; returns empty if absent.
2. Attempts to read `/assets/css/<id>/theme.json`. For built-in themes it is optional; if absent or unreadable, defaults are used.
3. Builds a `ThemeInfo` with `builtin = true` and the resource URL as `cssUrl`.

#### `private Optional<ThemeInfo> loadUser(String id)`

It validates a user theme step by step:

1. Build and **normalize** `themes/<id>`. If it no longer begins with `userThemesDir` (an attempt to escape using `..`) or is not a directory, return empty.
2. If `theme.json` does not exist, the folder is not a theme.
3. Read the manifest with Jackson; malformed JSON returns empty.
4. Select the manifest CSS name or `theme.css`, normalize it, and again ensure it remains inside the theme folder and is a regular file.
5. Return `ThemeInfo` with `builtin = false` and the file URI as `cssUrl`.

The `normalize()` plus `startsWith()` checks are important for future external-theme imports: a manifest with `"css": "../../x.css"` is rejected.

#### `private ThemeInfo info(String id, ThemeManifest m, String cssUrl, boolean builtin)`

Builds `ThemeInfo` with defaults: `name` is `m.name()` or the id, while `author` and `description` are empty when absent.

#### `List<ThemeInfo> listThemes()`

Lists valid built-ins first, then subdirectories in `themes/` whose id is not built-in and which pass `loadUser`. Invalid folders (no manifest, invalid JSON, missing CSS) are silently skipped. A user theme with a built-in id is ignored. User-theme filesystem order is not guaranteed; display them sorted with `Comparator.comparing(ThemeInfo::name)` if needed.

#### `List<String> cssUrls()`

Builds stylesheets in priority order (the last wins for equal specificity): load the selected id from `SettingsManager.getTheme()`, load the base URL, then return `[base, selected]` only if the selected valid theme differs from the base; otherwise return `[base]`. If the base theme is missing, `orElseThrow()` raises `NoSuchElementException`, a packaging error.

#### Applying and changing themes

- `apply(Scene scene)` registers a new scene and styles it; call it from `SceneManager`, including after a reused scene receives `setRoot`.
- `apply(DialogPane pane)` styles a dialog pane without registering it, since alerts are short-lived.
- `style(Scene scene)` calls `scene.getStylesheets().setAll(cssUrls())`, immediately restyling the scene.
- `register(Scene scene)` removes cleared weak references and adds the scene only once, by identity (`==`).
- `setTheme(String id)` does nothing if `find(id)` is empty; otherwise it saves the id in `settings.properties` and styles every live scene. Call it on the JavaFX thread.

---

## 5. Detecting added or removed themes

Theme discovery is lazy and on demand: there is no cache or background watcher. Every `find`, `listThemes`, and `cssUrls` call rereads the disk.

| Situation | Result |
|---|---|
| A theme folder is added while the app runs | It appears on the next `listThemes()` call; refresh when opening settings or offer a refresh button. |
| An unused theme is removed | It disappears on the next `listThemes()`. |
| The active theme is removed | Open scenes keep their already-loaded style. The next scene or `setTheme` falls back to base; the old id remains in settings until another theme is chosen. |
| Broken manifest or missing CSS | The theme is treated as nonexistent and omitted from `listThemes()`. |
| CSS is changed while the app runs | JavaFX caches CSS by URL. Remove and re-add the stylesheet, or append `?v=<timestamp>` to force a reread; mainly useful to theme developers. |

Possible future additions: a `reload()` method that restyles all scenes, a `WatchService` for real-time changes, or resetting an invalid saved id to `default_theme` at startup.

---

## 6. Using it from other classes

### `SceneManager`

```java
Parent root = loader.load();
Scene scene = new Scene(root, minWidth, minHeight);
ThemeManager.getInstance().apply(scene);     // AFTER scene creation
stage.setScene(scene);
```

For popups, style the newly created scene before assigning it to the stage. For alerts:

```java
Alert alert = new Alert(alertType);
// ...
ThemeManager.getInstance().apply(alert.getDialogPane());
alert.showAndWait();
```

### Settings controller

```java
@FXML private ComboBox<ThemeInfo> themeCombo;

@FXML
public void initialize() {
    ThemeManager tm = ThemeManager.getInstance();
    themeCombo.setConverter(new StringConverter<>() {
        @Override public String toString(ThemeInfo t) { return t == null ? "" : t.name(); }
        @Override public ThemeInfo fromString(String s) { return null; }
    });
    themeCombo.getItems().setAll(tm.listThemes());
    String current = SettingsManager.getInstance().getTheme();
    tm.listThemes().stream().filter(t -> t.id().equals(current))
      .findFirst().ifPresent(themeCombo::setValue);
}

@FXML
private void onApplyTheme() {
    ThemeInfo selected = themeCombo.getValue();
    if (selected != null) ThemeManager.getInstance().setTheme(selected.id());
}
```

The page does not need to be reloaded.

### FXML rules

- Do not put `stylesheets="@..."` on the root node: node stylesheets have higher priority than scene stylesheets and would override the theme.
- Controls outside the main scene, such as alerts and extra windows, must be explicitly themed.

---

## 7. Creating a theme

1. Create `~/.config/gestiolan/themes/<id>/`; the id is the folder name and is best kept without spaces.
2. Add required `theme.json` and `theme.css` files.
3. Override only what changes. A minimal palette override is:

   ```css
   .root {
       -gl-accent: #1E90D8;
       -gl-accent2: #8BE04E;
   }
   ```
4. Place images in the same folder and reference them relative to the CSS:

   ```css
   .dashboard-root {
       -fx-background-image: url("dashboard_bg.png");
       -fx-background-size: cover;
   }
   ```
5. Open settings and select the theme, or restart the app.

Do not make `-gl-surface` transparent in `.root`: it is also used by text fields, popups, and tooltips. Apply translucent colors only to panel classes such as `.sidebar`, `.top-section`, and `.item-card`. Real glass blur cannot be achieved with CSS alone; imitate it with transparency, gradients, light borders, and shadows.

### Built-in jar themes (once the feature is complete)

1. Copy the theme folder to `resources/assets/css/<id>/`.
2. Add the id to `ThemeManager`'s `BUILTIN` list.
3. The manifest is optional but recommended for a readable name and description.

---

## 8. Known limitations

- Manifest `version` is not propagated to `ThemeInfo`.
- `setTheme` does not report invalid ids externally (`void` return type).
- User-theme ordering in `listThemes()` is not guaranteed.
- `getInstance()` is not thread-safe; use the JavaFX thread.
- `BUILTIN` must be maintained manually.
- Alerts are themed only at creation; an alert already open during a theme change is not updated.
