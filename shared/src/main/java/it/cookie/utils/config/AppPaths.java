package it.cookie.utils.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AppPaths {
    private static final String APP_NAME = "gestiolan";
    private static final Path BASE_DIR = resolveBaseDir();

    private AppPaths() {}

    private static Path resolveBaseDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");
        Path base;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            base = (appData != null && !appData.isBlank())
                    ? Path.of(appData)
                    : Path.of(home, "AppData", "Roaming");
        } else if (os.contains("mac")) {
            base = Path.of(home, "Library", "Application Support");
        } else {
            String xdg = System.getenv("XDG_CONFIG_HOME");
            base = (xdg != null && !xdg.isBlank())
                    ? Path.of(xdg)
                    : Path.of(home, ".config");
        }
        return base.resolve(APP_NAME);
    }

    /** Cartella root dell'app (~/.config/gestiolan). */
    public static Path baseDir() { return BASE_DIR; }

    /** Sottocartella della root; la crea se non esiste. */
    public static Path dir(String name) {
        Path p = BASE_DIR.resolve(name);
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossibile creare " + p, e);
        }
        return p;
    }

    public static Path settingsDir() { return dir("settings"); }

    /** Percorso di un file dentro settings/ (es. "server.properties"). */
    public static Path settingsFile(String fileName) {
        return settingsDir().resolve(fileName);
    }
}