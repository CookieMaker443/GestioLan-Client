package it.cookie.progetti;


import java.util.Locale;
import java.util.ResourceBundle;

import it.cookie.progetti.managers.SceneManager;
import it.cookie.utils.network.managers.SessionManager;
import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application
{
    public static void main( String[] args )
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale locale = Locale.forLanguageTag("it-IT");
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.language", locale);

        final double MIN_WIDTH = 768;
        final double MIN_HEIGHT = 544;

        SessionManager.getInstance().ClearSession();    // Pulisce la Sessione ad ogni avvio
        // LanguageManager.getInstance();               // Inizializza il LanguageManager
        // Inizializza lo SceneManager
        SceneManager.getInstance()
                .loadScene(primaryStage, SceneManager.SceneKeys.LOGIN_VIEW, 
                    bundle.getString("app.title"), MIN_WIDTH, MIN_HEIGHT);
    }

    
}
