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

    private Runnable onCloseRequested;      // Callback per gestire la chiusura del pannello chat

    public void setOnCloseRequested(Runnable r) {
        this.onCloseRequested = r;
    }

    @FXML
    private void initialize() {
    }


    @FXML private void onClose() { 
        // Esegui la callback per chiudere il pannello chat
        if (onCloseRequested != null) onCloseRequested.run();
    }
    
    @FXML private void onSend() { }

}