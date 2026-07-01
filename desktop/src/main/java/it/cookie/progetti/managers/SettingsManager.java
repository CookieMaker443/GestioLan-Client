package it.cookie.progetti.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import it.cookie.utils.network.managers.NetworkManager;
import javafx.scene.control.Alert.AlertType;

public class SettingsManager {
    private static SettingsManager instance;
    private final Properties props = new Properties();
    
    // path dei salvataggi nel filesystem
    private static final String BASE_DIR = getBasePath();

    public static final String SETTINGS_FILE = BASE_DIR + "/settings.properties";
    public static final String LOGS_DIR     = BASE_DIR + "/saved_logs";
    public static final String CSS_DIR = BASE_DIR + "/themes";
    public static final String CACHE_DIR = BASE_DIR + "/cache";
    public static final String DEFAULT_CSS = CSS_DIR + "/default.css";

    // chiave per la versione http da usare (1.1 o 2)
    private static final String KEY_HTTP_VERSION = "httpVersion";
    private static final String DEFAULT_HTTP_VERSION = "HTTP_1_1";

    // chiavi
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_LAST_URL = "lastUrl";
    private static final String KEY_THEME = "theme";
    private static final String DEFAULT_THEME = "default.css";
    private static final String SERVER_IP = "server.ip";
    private static final String SERVER_PORT = "server.port";

    // default
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_LAST_URL = "";

    // sessione corrente — sopravvive al reload della scena
    private String sessionUrl = "";

    public static SettingsManager getInstance() {
        if (instance == null) instance = new SettingsManager();
        return instance;
    }

    private SettingsManager() {
        load();
        NetworkManager.getInstance().init(SETTINGS_FILE);
    }

    // Detect dell'os
    private static String getBasePath() {
        String os = System.getProperty("os.name").toLowerCase();
        String dir;
        if (os.contains("win")) {
            dir = System.getenv("APPDATA") + "/gestiolan";
        } else {
            dir = System.getProperty("user.home") + "/.config/gestiolan";
        }
        // crea tutte le cartelle se non esistono
        new File(dir).mkdirs();
        new File(dir + "/cache").mkdirs();
        new File(dir + "/saved_logs").mkdirs();
        new File(dir + "/themes").mkdirs();
        return dir;
    }

    // --- LOAD / SAVE ---

    private void load() {
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            // file non esiste ancora, usiamo i default
            props.setProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE);
            props.setProperty(KEY_LAST_URL, DEFAULT_LAST_URL);
        }
    }

    private void save() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            props.store(fos, "GestioLan Settings");
        } catch (IOException e) {
            SceneManager.getInstance().showAlert(
                LanguageManager.getInstance().getBundle().getString("alert.errorTitle"),
                LanguageManager.getInstance().getBundle().getString("alert.errorSaveSettings") + " " + e.getMessage(),
                AlertType.ERROR);
        }
    }

    // --- GETTER / SETTER ---

    public String getLanguage() {
        // prova con KEY_LANGUAGE, se non c'è ritorna DEFAULT_LANGUAGE
        return props.getProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public void setLanguage(String language) {
        props.setProperty(KEY_LANGUAGE, language);
        save();
    }

    public String getLastUrl() {
        return props.getProperty(KEY_LAST_URL, DEFAULT_LAST_URL);
    }

    public void setLastUrl(String url) {
        props.setProperty(KEY_LAST_URL, url);
        save();
    }


    // GETTER / SETTER per la configurazione del server con NetworkManager
    public void setServerConfig(String ip, int port) {
        NetworkManager.getInstance().saveConfig(SETTINGS_FILE, ip, port);
    }

    public String getServerIp() {
        return NetworkManager.getInstance().getIP();
    }

    public int getServerPort() {
        return NetworkManager.getInstance().getPort();
    }


    // HTTP VERSION
    public String getHttpVersion() {
        return props.getProperty(KEY_HTTP_VERSION, DEFAULT_HTTP_VERSION);
    }

    public void setHttpVersion(String version) {
        props.setProperty(KEY_HTTP_VERSION, version);
        save();
    }

    // Getter e setter per la permanenza dei pacchetti al cambio scena
    public void saveSession(String url) {
        this.sessionUrl = url;
    }

    public String getSessionUrl() { return sessionUrl; }
    public boolean hasSession() {
        return !sessionUrl.isEmpty();
    }

    // Theme
    public String getTheme() {
        return props.getProperty(KEY_THEME, DEFAULT_THEME);
    }

    public void setTheme(String theme) {
        props.setProperty(KEY_THEME, theme);
        save();
    }
}