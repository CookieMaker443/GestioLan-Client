package it.cookie.progetti;


import it.cookie.progetti.managers.LanguageManager;
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
        //final double MIN_WIDTH = 768;
        //final double MIN_HEIGHT = 544;
        final double MIN_WIDTH = 1500;
        final double MIN_HEIGHT = 720;

        SessionManager.getInstance().ClearSession();    // Pulisce la Sessione ad ogni avvio
        LanguageManager.getInstance();                  // Inizializza il LanguageManager
        // ThemeManager.getInstance();                     // Inizializza il ThemeManager

        
        // Inizializza lo SceneManager
        SceneManager.getInstance()
                .loadScene(primaryStage, SceneManager.SceneKeys.DASHBOARD_VIEW, // TODO: Cambiare in LOGIN_VIEW finita la dashboard
                    LanguageManager.getInstance().getBundle().getString("app.title"), MIN_WIDTH, MIN_HEIGHT);
    }

    
}
