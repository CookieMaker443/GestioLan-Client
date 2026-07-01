package it.cookie.progetti.controllers.Login.Settings;

import it.cookie.progetti.managers.SettingsManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ServerConfigController {
    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private Button connectButton;

    @FXML
    private void initialize() {
        // Hint: initialize() will be called when the associated FXML has been completely loaded.
        String ip = SettingsManager.getInstance().getServerIp();
        ipField.setText(ip != null ? ip : "");
        int port = SettingsManager.getInstance().getServerPort();
        // porta non impostata (0) -> campo vuoto, non "0"
        portField.setText(port == 0 ? "" : Integer.toString(port));
    }

    @FXML
    private void handleConnectButton() {
        String ip = ipField.getText();
        String portText = portField.getText();

        int port;
        if (portText == null || portText.isBlank()) {
            port = 0; // non impostata: non verrà salvata da NetworkManager
        } else {
            try {
                port = Integer.parseInt(portText.trim());
            } catch (NumberFormatException e) {
                // porta non valida: qui puoi mostrare un alert invece di procedere
                // per ora non salviamo nulla per evitare di scrivere dati sporchi
                return;
            }
        }

        SettingsManager.getInstance().setServerConfig(ip, port);
    }

    // TODO: aggiungere un pulsante "Test Connection" che prova a connettersi al server e mostra un alert con il risultato
    // TODO: aggiungere un pulsante per scegieret tra il protocollo (HTTP/HTTPS) e salvare la scelta in SettingsManager
}
