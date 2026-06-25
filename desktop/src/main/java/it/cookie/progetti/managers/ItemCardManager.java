package it.cookie.progetti.managers;

import java.io.IOException;
import java.util.List;

import it.cookie.progetti.controllers.MainMenu.Containers.ItemCardController;
import it.cookie.progetti.managers.SceneManager.SceneKeys;
import it.cookie.utils.interfaces.observer.Observer;
import it.cookie.utils.interfaces.observer.Subject;
import it.cookie.utils.network.item.Item;
import it.cookie.utils.network.item.ItemNetController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class ItemCardManager implements Observer {
    private final ItemNetController itemNetController;
    public static ItemCardManager istance;
    private Pane cardsContainer; // FlowPane, VBox, ecc.

    private ItemCardManager() {
        this.itemNetController = new ItemNetController();
        this.itemNetController.Attach(this); // si registra per ricevere le notifiche
    }

    public static ItemCardManager getInstance() {
        if (istance == null) {
            istance = new ItemCardManager();
        }
        return istance;
    }

    public void setContainer(Pane container) {
        this.cardsContainer = container;
    }

    // Chiama questo per avviare il fetch
    public void loadItems(String name,boolean useQty, int qtyValue, String selectedTypeQty,
                          boolean hasImageOnly, boolean useCategory, int categoryId) {
        itemNetController.fetchItems(name, useQty, qtyValue, selectedTypeQty,
                                     hasImageOnly, useCategory, categoryId);
    }

    @Override
    public void Update(Subject subject, Object data) {
        if (data instanceof List<?> list) {
            // Cast sicuro
            List<Item> items = list.stream()
                .filter(o -> o instanceof Item)
                .map(o -> (Item) o)
                .toList();

            // Aggiorna la GUI sul thread corretto (JavaFX / Swing)
            Platform.runLater(() -> buildCards(items));

        } else if (data instanceof String errorKey) {
            System.err.println("Errore ricevuto: " + errorKey);
            // mostra messaggio errore nella GUI
        }
    }

    private void buildCards(List<Item> items) {
        // cardsContainer è un VBox o HBox che contiene le card degli item, da iniettare
        if (cardsContainer == null) {
            System.err.println("cardsContainer non impostato!");
            return;
        }
        cardsContainer.getChildren().clear();

        String cardPath = SceneManager.getInstance().getPath(SceneKeys.ITEM_CARD);

        for (Item item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(cardPath));
                Node cardNode = loader.load();
                ItemCardController controller = loader.getController();
                controller.setItem(item);
                cardsContainer.getChildren().add(cardNode);
            } catch (IOException e) {
                System.err.println("Errore caricamento card: " + e.getMessage());
            }
        }
    }
}
