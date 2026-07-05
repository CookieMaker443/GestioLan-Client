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
                .uri(URI.create(NetworkManager.getInstance().GetBaseURL() + "/api/Users/Login"))
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
                    
                    int status = response.statusCode();

                    if (status == 200) {
                        handleSuccessfulLogin(response.body());
                    } else if (status == 401 || status == 403) {
                        // Wrong credentials, or account not authorized
                        System.err.println("Errore Login: " + status + " - " + response.body());
                        Notify(MessageText.known("login.INVALID_CREDENTIALS"));
                    } else if (status >= 500) {
                        // Server-side failure: not the user's fault, nothing they can fix by retyping
                        System.err.println("Errore Login: " + status + " - " + response.body());
                        Notify(MessageText.known("login.SERVER_ERROR"));
                    } else if (status >= 300 && status < 400) {
                        // Redirect (e.g. reverse proxy forcing http->https): the client hit the wrong scheme/URL,
                        // not an application error. The HTML body here is not meaningful to show the user.
                        System.err.println("Errore Login: redirect inatteso (" + status + ") - " + response.body());
                        Notify(MessageText.known("login.UNEXPECTED_REDIRECT"));
                    } else {
                        // Any other unexpected status code: keep the raw info for debugging,
                        // but don't dump server HTML into the UI as-is
                        System.err.println("Errore Login: " + status + " - " + response.body());
                        Notify(MessageText.unknown("HTTP " + status));
                    }
                }).exceptionally(ex -> {
                    
                    // Verrà stampato qui se l'IP è sbagliato o il timer scade
                    System.err.println("--- ERRORE DI RETE RILEVATO ---");
                    System.err.println("Causa: " + ex.getCause()); 
                    System.err.println("Messaggio: " + ex.getMessage());
            
                 
                    Throwable cause = ex.getCause();
                        MessageText error;
 
                        if (cause instanceof java.net.http.HttpConnectTimeoutException) {
                        // Server did not respond within the configured timeout
                        error = MessageText.known("login.SERVER_TIMEOUT");
                    } else if (cause instanceof javax.net.ssl.SSLHandshakeException) {
                        // Must be checked BEFORE generic SSLException, since it extends it.
                        // Server presented a certificate the JVM does not trust (self-signed / unknown CA)
                        error = MessageText.known("login.CERT_UNTRUSTED");
                    } else if (cause instanceof javax.net.ssl.SSLException) {
                        // Other TLS issues (e.g. client tried HTTPS on a plain HTTP endpoint)
                        error = MessageText.known("login.SSL_ERROR");
                    } else if (cause instanceof java.net.ConnectException) {
                        // Host/port not reachable at all (wrong IP, server down, firewall)
                        error = MessageText.known("login.SERVER_UNREACHABLE");
                    } else {
                        // Unexpected case: fall back to the raw JVM/exception message
                        String rawMsg = (cause != null) ? cause.toString() : ex.getMessage();
                        error = MessageText.unknown(rawMsg != null ? rawMsg : "Errore sconosciuto");
                    }
 
                        System.err.println("Errore rilevato: " + error);
 
                        Notify(error);
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

            // Update() del SessionManager
            Notify(loggedUser);
            
            
            System.out.println("Login effettuato con successo!");
        } catch (Exception e) {
            System.err.println("Errore nel parsing della risposta: " + e.getMessage());
            Notify(MessageText.known("login.ParsingError"));
        }
        
    }

    public void UserRegister() {}

    public void UserDelete() {}

    public void UserModify(user user) {
        // #TODO: qui chiama il notify per aggiornare i dati
        Notify(user);
    }
}
