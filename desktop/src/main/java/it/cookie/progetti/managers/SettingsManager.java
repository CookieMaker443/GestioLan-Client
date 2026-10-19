package it.cookie.progetti.managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import it.cookie.utils.config.AppPaths;
import it.cookie.utils.network.managers.NetworkManager;

public class SettingsManager {
    private static SettingsManager instance;
    private final Properties props = new Properties();

    // path (restano String per non rompere il resto del codice)
    public static final String LOGS_DIR  = AppPaths.dir("saved_logs").toString();
    public static final String CACHE_DIR = AppPaths.dir("cache").toString();

    public static final Path SETTINGS_FILE = AppPaths.settingsFile("settings.properties");
    public static final Path SERVER_FILE   = AppPaths.settingsFile("server.properties");

    // chiave per la versione http da usare (1.1 o 2)
    private static final String KEY_HTTP_VERSION = "httpVersion";
    private static final String DEFAULT_HTTP_VERSION = "HTTP_1_1";

    // chiavi
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_LAST_URL = "lastUrl";
    private static final String KEY_THEME = "theme";
    private static final String DEFAULT_THEME = "default_theme";

    // default
    private static final String DEFAULT_LANGUAGE = "it";
    private static final String DEFAULT_LAST_URL = "";

    // sessione corrente — sopravvive al reload della scena
    private String sessionUrl = "";

    public static SettingsManager getInstance() {
        if (instance == null) instance = new SettingsManager();
        return instance;
    }

    private SettingsManager() {
        load();
        NetworkManager.getInstance().init(SERVER_FILE);
    }

    private void load() {
        try (InputStream in = Files.newInputStream(SETTINGS_FILE)) {
            props.load(in);
            System.out.println("Settings loaded from " + SETTINGS_FILE.toAbsolutePath());
        } catch (IOException e) {
            props.setProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE);
            props.setProperty(KEY_LAST_URL, DEFAULT_LAST_URL);
            System.out.println("Error loading settings from " + SETTINGS_FILE.toAbsolutePath() + ": " + e.getMessage());
        }
    }

    private void save() {
        try (OutputStream out = Files.newOutputStream(SETTINGS_FILE)) {
            props.store(out, "GestioLan Settings");
            System.out.println("Settings saved to " + SETTINGS_FILE.toAbsolutePath());
        } catch (IOException e) {
            // vedi nota sotto
            System.err.println("Error saving settings to " + SETTINGS_FILE.toAbsolutePath() + ": " + e.getMessage());
        }
    }

    // --- GETTER / SETTER ---

    public String getLanguage() {
        // prova con KEY_LANGUAGE, se non c'è ritorna DEFAULT_LANGUAGE
        System.out.println("Current language: " + props.getProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE));  // Debug log
        return props.getProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public void setLanguage(String language) {
        props.setProperty(KEY_LANGUAGE, language);
        System.out.println("Language set to: " + language);  // Debug log
        save();
    }

    public String getLastUrl() {
        System.out.println("Current last URL: " + props.getProperty(KEY_LAST_URL, DEFAULT_LAST_URL));  // Debug log
        return props.getProperty(KEY_LAST_URL, DEFAULT_LAST_URL);
    }

    public void setLastUrl(String url) {
        props.setProperty(KEY_LAST_URL, url);
        System.out.println("Last URL set to: " + url);  // Debug log
        save();
    }


    // GETTER / SETTER per la configurazione del server con NetworkManager
    public void setServerConfig(String ip, int port) {
        System.out.println("Setting server config: IP = " + ip + ", Port = " + port);  // Debug log
        NetworkManager.getInstance().saveConfig(ip, port);
    }

    public String getServerIp() {
        System.out.println("Current server IP: " + NetworkManager.getInstance().getIP());  // Debug log
        return NetworkManager.getInstance().getIP();
    }

    public int getServerPort() {
        System.out.println("Current server port: " + NetworkManager.getInstance().getPort());  // Debug log
        return NetworkManager.getInstance().getPort();
    }


    // HTTP VERSION
    public String getHttpVersion() {
        System.out.println("Current HTTP version: " + props.getProperty(KEY_HTTP_VERSION, DEFAULT_HTTP_VERSION));  // Debug log
        return props.getProperty(KEY_HTTP_VERSION, DEFAULT_HTTP_VERSION);
    }

    public void setHttpVersion(String version) {
        props.setProperty(KEY_HTTP_VERSION, version);
        System.out.println("HTTP version set to: " + version);  // Debug log
        save();
    }

    // Getter e setter per la permanenza dei pacchetti al cambio scena
    public void saveSession(String url) {
        System.out.println("Saving session URL: " + url);  // Debug log
        this.sessionUrl = url;
    }

    public String getSessionUrl() { return sessionUrl; }
    public boolean hasSession() {
        System.out.println("Session URL: " + sessionUrl);  // Debug log
        return !sessionUrl.isEmpty();
    }

    // Theme
    public String getTheme() {
        System.out.println("Current theme: " + props.getProperty(KEY_THEME, DEFAULT_THEME));  // Debug log
        return props.getProperty(KEY_THEME, DEFAULT_THEME);
    }

    public void setTheme(String theme) {
        props.setProperty(KEY_THEME, theme);
        System.out.println("Theme set to: " + theme);  // Debug log
        save();
    }
}