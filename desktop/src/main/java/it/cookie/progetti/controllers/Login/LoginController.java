package it.cookie.progetti.controllers.Login;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import it.cookie.progetti.managers.SceneManager;
import it.cookie.utils.interfaces.observer.Observer;
import it.cookie.utils.interfaces.observer.Subject;
import it.cookie.utils.network.managers.SessionManager;
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
    Locale locale = Locale.forLanguageTag("it-IT");
    ResourceBundle bundle = ResourceBundle.getBundle("i18n.language", locale);
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
        System.out.println("LoginController inizializzato: collego i componenti...");
        
        // Colleghiamo la rete al SessionManager
        userNetController.Attach(SessionManager.getInstance());
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleLoginButtonAction() throws IOException {
        if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            System.out.println("Username or Password is empty!");
            SceneManager.getInstance().showAlert(bundle.getString("login.Warning"), bundle.getString("login.MISSING_FIELDS"), AlertType.WARNING);
            
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
            System.out.println("LoginController ha ricevuto Update dal SessionManager.");
            SessionManager.getInstance().Detach(this);
            loginButton.setDisable(false);

            if (state instanceof user) {
                SceneManager.getInstance()
                    .loadScene((Stage)loginButton.getScene().getWindow(), 
                        SceneManager.SceneKeys.MAIN_MENU_VIEW, 
                        bundle.getString("menu.TITLE"), 1500, 750);    
            } else if (state instanceof String errorKey){
                // Qui state è una stringa di errore del bundle. 
                SceneManager.getInstance().showAlert(bundle.getString("login.Error"), bundle.getString(errorKey), AlertType.ERROR);
            } else {
                // Caso generico se state fosse null
                SceneManager.getInstance().showAlert(bundle.getString("login.Error"), bundle.getString("login.CONNECTION_ERROR"), AlertType.ERROR);
            }
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleChangeServerButton(ActionEvent event) throws IOException {
        SceneManager.getInstance().loadPopupScene(event, SceneManager.SceneKeys.SERVER_CONFIG_POPUP, 
            bundle.getString("serverchange.title"), 500, 300);
    }
}
