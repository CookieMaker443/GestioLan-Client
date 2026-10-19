package it.cookie.progetti.controllers.Login;

import java.io.IOException;

import it.cookie.progetti.managers.LanguageManager;
import it.cookie.progetti.managers.SceneManager;
import it.cookie.utils.interfaces.observer.Observer;
import it.cookie.utils.interfaces.observer.Subject;
import it.cookie.utils.network.managers.SessionManager;
import it.cookie.utils.network.user.MessageText;
import it.cookie.utils.network.user.UserNetController;
import it.cookie.utils.network.user.user;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController implements Observer{
    UserNetController userNetController = new UserNetController();

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button ChangeServerButton;
;
    @FXML
    public void initialize() {
        System.out.println("LoginController initialized: connecting components...");
        
        // Colleghiamo la rete al SessionManager
        userNetController.Attach(SessionManager.getInstance());
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleLoginButtonAction() throws IOException {
        if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            System.out.println("Username or Password is empty!");
            SceneManager.getInstance().showAlert(LanguageManager.getInstance().getBundle().getString("login.Warning"), LanguageManager.getInstance().getBundle().getString("login.MISSING_FIELDS"), AlertType.WARNING);
            
            return;
        }
        // disabilita il bottone per evitare spam
        loginButton.setDisable(true);

        String username = usernameField.getText();
        String password = passwordField.getText();
        
        System.out.println("Attempting login with Username: " + username);

        // appena  il SessionManager e pronto, chiama questo update()
        SessionManager.getInstance().Attach(this);  

        // passa la richiesta di login al UserNetController ed esegui il login
        userNetController.UserLogin(username, password); 
    }

    // possibile refactor
    @Override
    public void Update(Subject subject, Object state) {
        javafx.application.Platform.runLater(() -> {
            // Disabilitato l'ascolto per evitare doppie chiamate istantanee
            System.out.println("LoginController updated by SessionManager.");
            SessionManager.getInstance().Detach(this);
            loginButton.setDisable(false);

            if (state instanceof user) {
                SceneManager.getInstance()
                    .loadScene((Stage)loginButton.getScene().getWindow(), 
                        SceneManager.SceneKeys.DASHBOARD_VIEW, 
                        LanguageManager.getInstance().getBundle().getString("app.title"), 1500, 750);    
            } 
            else if (state instanceof MessageText error) {
                // errore noto -> traduci dal bundle; errore non previsto -> messaggio grezzo della JVM
                String message = error.isKnown()
                    ? LanguageManager.getInstance().getBundle().getString(error.bundleKey())
                    : error.rawMessage();
                SceneManager.getInstance().showAlert(LanguageManager.getInstance().getBundle().getString("login.Error"), message, AlertType.ERROR);
            } else {
                // rete di sicurezza: non dovrebbe mai capitare, dato che ogni percorso
                // di errore in UserNetController passa da un LoginError
                SceneManager.getInstance().showAlert(LanguageManager.getInstance().getBundle().getString("login.Error"), LanguageManager.getInstance().getBundle().getString("login.CONNECTION_ERROR"), AlertType.ERROR);
            }

        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleChangeServerButton(ActionEvent event) throws IOException {
        SceneManager.getInstance().loadPopupScene(event, SceneManager.SceneKeys.SERVER_CONFIG_POPUP, 
            LanguageManager.getInstance().getBundle().getString("serverchange.title"), 500, 300);
    }
}
