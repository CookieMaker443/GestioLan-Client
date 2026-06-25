package it.cookie.progetti.controllers.MainMenu;

import it.cookie.progetti.managers.ItemCardManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;


public class MainMenuController {

    @FXML
    private TextField nameField;

    @FXML
    private Spinner qtySpinner;

    @FXML
    private ChoiceBox<String> typeQtyChoiceBox;

    @FXML
    private ToggleButton useQtyToggle;

    @FXML
    private ToggleButton hasImageToggle;

    @FXML
    private Button resetButton;

    @FXML
    private Button requestButton;

    // -----

    @FXML
    private CheckBox useCategoryButton;

    @FXML
    private FlowPane itemsFlowPane; // Contenitore per le card degli item ancora da includere nel FXML

    // -----


    @FXML
    public void initialize() {
        ItemCardManager.getInstance().setContainer(itemsFlowPane);
        //handleRequestButton(null); // carica subito al primo accesso
    }

    @FXML
    private void handleRequestButton(ActionEvent event) {
        // Leggi i ToggleButton (restituiscono true se premuti, false altrimenti)
        boolean useQty = useQtyToggle.isSelected();
        boolean hasImageOnly = hasImageToggle.isSelected();
        boolean useCategory = useCategoryButton.isSelected();

        // ved se c'è un nome da cercare
        String name = nameField.getText();

        if (String.valueOf(name).isEmpty()) {
            name = null;
        }

        // 2. Leggi lo Spinner (se useQty è disattivato, potresti voler ignorare il valore)
        int qtyValue = 0;
        if (useQty) {
            qtyValue = (int)qtySpinner.getValue();
        }

        // cerca anche usando la categoria
        int categoryId = 0;
        if (useCategory) {
            // TODO: 
            // Legge gli id delle categorie selezionate, li somma e li da come parametro
        }
        
        

        // Leggi la ChoiceBox (restituisce null se non è selezionato nulla)
        String selectedTypeQty = typeQtyChoiceBox.getValue();

        // ---- LOGICA DI INVIO AL SERVER ----
        ItemCardManager.getInstance().loadItems(name, 
            useQty, qtyValue, selectedTypeQty, 
            hasImageOnly, 
            useCategory, categoryId);
    }
}



