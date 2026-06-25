package it.cookie.progetti.controllers.Login.Settings;

import it.cookie.utils.network.managers.NetworkManager;
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
        ipField.setText(NetworkManager.getInstance().getIP()); // Default value
        portField.setText(Integer.toString(NetworkManager.getInstance().getPort()));    // Default value
    }

    @FXML
    private void handleConnectButton() {
        String ip = ipField.getText();
        int port = Integer.parseInt(portField.getText());
        NetworkManager.getInstance().saveConfig(ip, port);
    }

    
}
