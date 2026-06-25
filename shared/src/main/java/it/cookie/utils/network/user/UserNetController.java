package it.cookie.utils.network.user;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.cookie.utils.interfaces.observer.Subject;
import it.cookie.utils.network.managers.NetworkManager;

public class UserNetController extends Subject {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    
    // Salva i dati nel SessionManager
    public void UserLogin(String username, String password) {
        try {
        
        // Crea il corpo della richiesta in formato JSON
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);
        
        // Mappa in json
        String jsonBody = objectMapper.writeValueAsString(credentials);

        // con un Builder, crea il pacchetto HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + NetworkManager.getInstance().getIP() + ":" + NetworkManager.getInstance().getPort() + "/api/Users/Login"))
                .timeout(java.time.Duration.ofMillis(NetworkManager.getInstance().getTimeout())) // Se dopo X secondi non risponde, "uccidi" la richiesta
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "GestioLan D-Client")
                .header("Autentication", "Bearer ") // Token vuoto per il login
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Invia la richiesta e ottieni la risposta
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        handleSuccessfulLogin(response.body());
                    } else {
                        System.err.println("Errore Login: " + response.statusCode() + " - " + response.body());
                        String errorMessage = "login.INVALID_CREDENTIALS"; // Default
                        Notify(errorMessage);
                        // creare un popup con il messaggio di errore dal server
                    }
                }).exceptionally(ex -> {
                    
                    // Verrà stampato qui se l'IP è sbagliato o il timer scade
                    System.err.println("--- ERRORE DI RETE RILEVATO ---");
                    System.err.println("Causa: " + ex.getCause()); 
                    System.err.println("Messaggio: " + ex.getMessage());
            
                    Throwable cause = ex.getCause();
                    String errorKey = "login.CONNECTION_ERROR"; // Default

                    if (cause instanceof java.net.http.HttpConnectTimeoutException) {
                        errorKey = "login.SERVER_TIMEOUT";
                    } else if (cause instanceof java.net.ConnectException) {
                        errorKey = "login.SERVER_UNREACHABLE";
                    }

                    System.err.println("Errore rilevato: " + errorKey);
    
                    
                    Notify(errorKey); // <--- PASSIAMO LA STRINGA, NON NULL
                    return null;
            });
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSuccessfulLogin(String responseBody) {
        try {
            // Trasforma la risposta JSON in oggetto user (Image non gestita qui    )
            user loggedUser = objectMapper.readValue(responseBody, user.class);
            
            // TODO: rimuovere questo JWT temporaneo
            loggedUser.setJWT("temp_jwt_token");

            // Update() del SessionManager
            Notify(loggedUser);
            
            
            System.out.println("Login effettuato con successo!");
        } catch (Exception e) {
            System.err.println("Errore nel parsing della risposta: " + e.getMessage());
            Notify("login.ParsingError");
        }
        
    }

    public void UserRegister() {}

    public void UserDelete() {}

    public void UserModify(user user) {
        // #TODO: qui chiama il notify per aggiornare i dati
        Notify(user);
    }
}
