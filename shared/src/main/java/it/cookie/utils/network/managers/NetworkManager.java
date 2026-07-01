package it.cookie.utils.network.managers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class NetworkManager {
    String ip_addr;  
    int port;
    int timeout = 2000; // in millisecondi

    private boolean useHttps = true;

    public static NetworkManager istance;

    private final Properties props = new Properties();
    private final String HTTP = "http://";
    private final String HTTPS = "https://";
    private final String PATH_TO_RES = "src/main/resources/";
    private final String PATH = "config/";
    private final String FILE_NAME = "server.properties";
    private final String CONFIG_PATH = PATH_TO_RES + PATH + FILE_NAME;

    private NetworkManager() {}

    public void init(String path) {
        loadConfig(path);
    }

    public static synchronized NetworkManager getInstance() {
        if(istance == null) {
            istance = new NetworkManager();
        }
        return istance;
    }
    
    public void ConnectToServer(String ip, String port) {}

    public void saveConfig(String newIp, int newPort) {
        this.ip_addr = newIp;
        this.port = newPort;
        
        // #TODO: Salvare in un file esterno
        // di solito si usa un percorso nel filesystem (es. cartella utente)
        try (OutputStream output = new FileOutputStream(CONFIG_PATH)) {
            props.setProperty("server.ip", newIp);
            props.setProperty("server.port", String.valueOf(newPort));
            props.store(output, "Server Configuration");
        } catch (IOException io) {
            // o.printStackTrace();
            System.out.println("Salvataggio configurazione non riuscita");
        }
    }

    public void saveConfig(String savePath, String newIp, int newPort) {
        this.ip_addr = newIp;
        this.port = newPort;

        Properties fileProps = new Properties();

        // carica quello che c'è già nel file, per non perdere le altre chiavi
        try (InputStream input = new FileInputStream(savePath)) {
            fileProps.load(input);
        } catch (IOException ignored) {
            // file non esiste ancora: si parte da vuoto, va bene così
        }

        // aggiorna solo le chiavi di rete
        fileProps.setProperty("server.ip", newIp);
        if (newPort == 0) {
            // porta non impostata -> non la scriviamo/la rimuoviamo, nessun errore
            fileProps.remove("server.port");
        } else {
            fileProps.setProperty("server.port", String.valueOf(newPort));
        }

        // riscrivi tutto il file (vecchie chiavi + nuove)
        try (OutputStream output = new FileOutputStream(savePath)) {
            fileProps.store(output, "Server Configuration");
        } catch (IOException io) {
            System.out.println("Salvataggio configurazione non riuscita");
        }
    }

    // Carica i dati dal file
    private void loadConfig() {
        // getResourceAsStream - il file è dentro il JAR/Risorse
        //try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_PATH)) 
        try (InputStream input = new FileInputStream(CONFIG_PATH)){
            props.load(input);
            this.ip_addr = props.getProperty("server.ip");
            this.port = Integer.parseInt(props.getProperty("server.port"));
        } catch (IOException ex) {
            // ex.printStackTrace();
            System.out.println("Configurazione non trovata, uso valori di default");
            this.ip_addr = "localhost";
            this.port = 8080;
        }
    }

    // Carica i dati dal file
    private void loadConfig(String loadPath) {
    try (InputStream input = new FileInputStream(loadPath)) {
        props.load(input);
        this.ip_addr = props.getProperty("server.ip");
        String portStr = props.getProperty("server.port");
        this.port = (portStr != null) ? Integer.parseInt(portStr) : 0;
    } catch (IOException | NumberFormatException ex) {
        // file assente o porta corrotta: nessun errore visibile, solo default
        System.out.println("Configurazione non trovata, uso valori di default");
        this.ip_addr = "localhost";
        this.port = 0; // non 8080 forzato: coerente col "non impostata"
    }
}

    public String GetBaseURL() {
        String scheme = useHttps ? HTTPS : HTTP;
        if (port == 0) {
            return scheme + ip_addr;
        }
        return scheme + ip_addr + ":" + port;
    }

    public String getIP() { return ip_addr; }

    public int getPort() { return port; }

    public int getTimeout() { return timeout; }

    public void setTimeout(int timeout) { this.timeout = timeout; }

    public void setUseHttps(boolean useHttps) { this.useHttps = useHttps; }

    public boolean isUseHttps() { return useHttps; }
}
