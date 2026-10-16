package it.cookie.progetti.controllers.Dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ChatPanelController {
    @FXML private VBox root;
    @FXML private Button closeButton;
    @FXML private ScrollPane messageScroll;
    @FXML private VBox messageList;
    @FXML private TextField messageField;
    @FXML private Button sendButton;

    @FXML
    private void initialize() {
    }

    @FXML private void onClose() { }
    @FXML private void onSend() { }
}