package it.cookie.progetti.managers.ThemeManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.cookie.progetti.managers.SettingsManager;
import it.cookie.utils.config.AppPaths;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;


public class ThemeManager {
    // tema default nel jar
    private static final String BUILTIN_DIR = "/assets/css/";

    // dentro un jar non si può elencare una cartella: i temi inclusi vanno dichiarati qui
    private static final List<String> BUILTIN = List.of(
        "default_theme",
        "mono",
        "fruitiger_aero");
    private static final String BASE = "default_theme";
    private static final String MANIFEST = "theme.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static ThemeManager instance;

    private final Path userThemesDir = AppPaths.dir("themes");
    private final List<WeakReference<Scene>> scenes = new ArrayList<>();

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    // ---------- ricerca temi ----------

    public Optional<ThemeInfo> find(String id) {
        // cerca prima tra i temi inclusi nel jar, poi tra quelli dell'utente
        return BUILTIN.contains(id) ? loadBuiltin(id) : loadUser(id);
    }

    private Optional<ThemeInfo> loadBuiltin(String id) {
        // cerca il file theme.css nel jar
        URL css = getClass().getResource(BUILTIN_DIR + id + "/theme.css");

        // cerca il file theme.json nel jar (facoltativo)
        if (css == null) return Optional.empty();

        // legge il manifest (facoltativo)
        ThemeManifest m = null;
        try (InputStream in = getClass().getResourceAsStream(BUILTIN_DIR + id + "/" + MANIFEST)) {
            if (in != null) m = MAPPER.readValue(in, ThemeManifest.class);   // per i temi inclusi è facoltativo
        } catch (IOException ignored) {}
        return Optional.of(info(id, m, css.toExternalForm(), true));
    }

    private Optional<ThemeInfo> loadUser(String id) {
        // cerca la cartella del tema nella cartella dell'utente
        Path dir = userThemesDir.resolve(id).normalize();

        // sicurezza: non deve uscire dalla cartella dei temi dell'utente e deve essere una cartella
        if (!dir.startsWith(userThemesDir) || !Files.isDirectory(dir)) return Optional.empty();

        // cerca il manifest (facoltativo)
        Path manifestFile = dir.resolve(MANIFEST);
        if (!Files.isRegularFile(manifestFile)) return Optional.empty();   // senza manifest: non è un tema

        // legge il manifest e il file CSS
        try {
            ThemeManifest m = MAPPER.readValue(manifestFile.toFile(), ThemeManifest.class);
            String cssName = (m.css() == null || m.css().isBlank()) ? "theme.css" : m.css();
            Path css = dir.resolve(cssName).normalize();
            if (!css.startsWith(dir) || !Files.isRegularFile(css)) return Optional.empty();
            return Optional.of(info(id, m, css.toUri().toString(), false));
        } catch (IOException e) {
            return Optional.empty();   // JSON non valido
        }
    }

    private ThemeInfo info(String id, ThemeManifest m, String cssUrl, boolean builtin) {
        // legge i campi dal manifest (facoltativi)
        String name = (m != null && m.name() != null) ? m.name() : id;
        String author = m != null ? Objects.requireNonNullElse(m.author(), "") : "";
        String desc = m != null ? Objects.requireNonNullElse(m.description(), "") : "";
        return new ThemeInfo(id, name, author, desc, cssUrl, builtin);
    }

    public List<ThemeInfo> listThemes() {
        // include i temi inclusi nel jar
        List<ThemeInfo> list = new ArrayList<>();

        // aggiunge i temi inclusi nel jar
        for (String id : BUILTIN) loadBuiltin(id).ifPresent(list::add);

        // aggiunge i temi dell'utente (sottocartelle con theme.json e theme.css)
        try (Stream<Path> s = Files.list(userThemesDir)) {
            s.filter(Files::isDirectory)
             .map(p -> p.getFileName().toString())
             .filter(id -> !BUILTIN.contains(id))
             .forEach(id -> loadUser(id).ifPresent(list::add));
        } catch (IOException ignored) {}
        return list;
    }

    // ---------- applicazione ----------

    /** Ordine = priorità: prima il tema base, poi quello scelto che lo sovrascrive. */
    public List<String> cssUrls() {
        // prende il nome del tema dalle settings.properties (default_theme se non c'è)
        String id = SettingsManager.getInstance().getTheme();
        // prende il CSS del tema base (default_theme) e quello del tema scelto (se valido)
        String base = find(BASE).orElseThrow().cssUrl();

        System.out.println("Applying theme: " + id + " (base: " + base + ")");

        // se il tema scelto non è valido o è il base, ritorna solo il base
        List<String> finalTheme = find(id)
                .filter(t -> !BASE.equals(t.id()))
                .map(t -> List.of(base, t.cssUrl()))
                .orElse(List.of(base));    // tema mancante o non valido -> solo base
        
        System.out.println("Final theme CSS URLs: " + finalTheme);
        
        return finalTheme;
    }

    public void apply(Scene scene) {
        // Applica il tema alla scena: registra la scena e applica i CSS
        register(scene);
        style(scene);
    }

    public void apply(DialogPane pane) {
        // Applica il tema al DialogPane: registra la scena e applica i CSS
        pane.getStylesheets().setAll(cssUrls());
    }

    private void style(Scene scene) {
        // Applica i CSS alla scena: rimuove quelli vecchi e aggiunge quelli nuovi
        scene.getStylesheets().setAll(cssUrls());
    }

    private void register(Scene scene) {
        // rimuove le scene chiuse (WeakReference.get() == null)
        scenes.removeIf(r -> r.get() == null);
        if (scenes.stream().noneMatch(r -> r.get() == scene)) {
            scenes.add(new WeakReference<>(scene));
            System.out.println("Scene registered for theme updates: " + scene);
        }
    }

    /** Cambia tema a caldo: nessun ricaricamento, controller e stato restano intatti. */
    public void setTheme(String id) {
        // ignora temi non validi: se il tema non esiste, non fa nulla
        if (find(id).isEmpty()) return;               // ignora temi non validi

        // salva la scelta nelle settings.properties
        SettingsManager.getInstance().setTheme(id);

        // aggiorna tutte le scene aperte
        for (WeakReference<Scene> r : scenes) {
            Scene s = r.get();
            if (s != null) style(s);
        }
    }
}