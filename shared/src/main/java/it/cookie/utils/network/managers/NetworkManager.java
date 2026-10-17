package it.cookie.utils.network.managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class NetworkManager {
    String ip_addr;  
    int port;
    int timeout = 2000;                 // in millisecondi

    private Path configFile;
    private final Properties props = new Properties();

    private final String HTTP = "http://";
    private final String HTTPS = "https://";
    private boolean useHttps = false;

    public static NetworkManager istance;


    
    private NetworkManager() {}

    public void init(Path file) {
        this.configFile = file;
        loadConfig();
    }

    public static synchronized NetworkManager getInstance() {
        if(istance == null) {
            istance = new NetworkManager();
        }
        return istance;
    }
    
    public void ConnectToServer(String ip, String port) {}

    private void loadConfig() {
        try (InputStream in = Files.newInputStream(configFile)) {
            props.load(in);
            ip_addr = props.getProperty("server.ip", "localhost");
            String portStr = props.getProperty("server.port");
            port = (portStr != null) ? Integer.parseInt(portStr) : 0;
        } catch (IOException | NumberFormatException e) {
            System.out.println("Configurazione non trovata, uso valori di default");
            ip_addr = "localhost";
            port = 0;
        }
    }

    public void saveConfig(String newIp, int newPort) {
        ip_addr = newIp;
        port = newPort;

        props.setProperty("server.ip", newIp);
        if (newPort == 0) props.remove("server.port");
        else props.setProperty("server.port", String.valueOf(newPort));

        try (OutputStream out = Files.newOutputStream(configFile)) {
            props.store(out, "Server Configuration");
        } catch (IOException e) {
            System.out.println("Salvataggio configurazione non riuscita");
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
