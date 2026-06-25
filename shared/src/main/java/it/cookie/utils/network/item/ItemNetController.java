package it.cookie.utils.network.item;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import it.cookie.utils.interfaces.observer.Subject;
import it.cookie.utils.network.managers.NetworkManager;
import it.cookie.utils.network.managers.SessionManager;

// estende subject, cosi poi notifica il respondabile che gestisce i dati
// es: ItemCardManager per Desktop, ItemCardManager per Mobile
public class ItemNetController extends Subject {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public void fetchItems(
        String name,
        boolean useQty, int qtyValue, String selectedTypeQty, 
        boolean hasImageOnly, 
        boolean useCategory, int categoryId)
    {
        try{

            /* https://cookie.gestiolan.lan/api/Items/GetItems?
            name=string
            quantity=1
            type_quantity=item 
            has_image=true
            has_category=true
            id_category=2
            */

            // costruisce la richiesta
            // costruisce la query dell'url in base ai parametri passati
            StringBuilder urlBuilder = new StringBuilder(NetworkManager.getInstance().GetBaseURL() + "/api/Items?");
            if(name != null && !name.isEmpty()) {
                urlBuilder.append("name=").append(name).append("&");
            }
            if (useQty) {
                urlBuilder.append("quantity=")
                .append(qtyValue)
                .append("&type_quantity=")
                .append(selectedTypeQty)
                .append("&");
            }
            if (hasImageOnly) {
                urlBuilder.append("has_image=true&");
            }
            if (useCategory) {
                urlBuilder.append("has_category=true&")
                .append("id_category=")
                .append(categoryId)
                .append("&");
            }

            urlBuilder.setLength(urlBuilder.length() - 1); // Rimuove l'ultimo '&' o '?' se presente

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .timeout(java.time.Duration.ofMillis(NetworkManager.getInstance().getTimeout()))
                .header("Authorization", "Bearer " + SessionManager.getInstance().getJWT())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "GestioLan D-Client")
                .GET()
                .build();

            // Invia la richiesta e ottieni la risposta
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        handleSuccessfulLogin(response.body());
                    } else {
                        System.err.println("Errore Risposta Items: " + response.statusCode() + " - " + response.body());
                        String errorMessage = "items.CANNOT_FETCH"; // Default
                        Notify(errorMessage);
                        // creare un popup con il messaggio di errore dal server
                    }
                }).exceptionally(ex -> {
                    
                    // Verrà stampato qui se l'IP è sbagliato o il timer scade
                    System.err.println("--- ERRORE ITEMS ---");
                    System.err.println("Causa: " + ex.getCause()); 
                    System.err.println("Messaggio: " + ex.getMessage());
            
                    Throwable cause = ex.getCause();
                    String errorKey = "items.CANNOT_FETCH"; // Default

                    if (cause instanceof java.net.http.HttpConnectTimeoutException) {
                        errorKey = "login.SERVER_TIMEOUT";
                    } else if (cause instanceof java.net.ConnectException) {
                        errorKey = "login.SERVER_UNREACHABLE";
                    } else if (cause instanceof java.net.UnknownHostException) {
                        // TODO : JWT SCADUTO, SERVER OFFLINE, SERVER NON RAGGIUNGIBILE
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

    private void handleSuccessfulLogin(String responseBody){
        try {
            // Deserializza il JSON in una lista di Item
            List<Item> items = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Item.class)
            );
            Notify(items); // <-- manda la lista a chi ascolta
        } catch (Exception e) {
            System.err.println("Errore parsing items: " + e.getMessage());
            Notify("items.PARSING_ERROR");
        }
    }
}
