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

    public static NetworkManager istance;

    private final Properties props = new Properties();
    private final String HTTP = "http://";
    private final String HTTPS = "https://";
    private final String PATH_TO_RES = "src/main/resources/";
    private final String PATH = "config/";
    private final String FILE_NAME = "server.properties";
    private final String CONFIG_PATH = PATH_TO_RES + PATH + FILE_NAME;

    private NetworkManager() {
        loadConfig();
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

    public String GetBaseURL() {
        /*if(port == null || port == 0) {
            return HTTP + ip_addr;
        }*/
        return HTTP + ip_addr + ":" + port;
    }

    public String getIP() { return ip_addr; }

    public int getPort() { return port; }

    public int getTimeout() { return timeout; }

    public void setTimeout(int timeout) { this.timeout = timeout; }
}
