package it.cookie.progetti.controllers.Dashboard;

import it.cookie.progetti.managers.ThemeManager.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DashboardController {
    @FXML
    private StackPane root;                     // nodo base

    @FXML
    private VBox sidebar;                       // Contenitore del logo e delle  categorie


    // Variabili per la gestione della sezione delle categorie
    @FXML
    private ScrollPane categoryScroll;          // contenitore del VBox categoryList per scrollare le righe

    @FXML
    private VBox categoryList;                  // Contenitore per le righe delle categorie

    @FXML
    private Button newCategoryButton;

    @FXML
    private Button deleteCategoriesButton;


    // Variabili per la gestione della sezione principale della dashboard
    @FXML
    private VBox mainArea;                      // Contenitore per la sezione principale della dashboard (barra di ricerca, filtri, visualizzazione item, ecc.)

    @FXML
    private VBox topSection;                    // Contenitore per la barra di ricerca e i filtri

    @FXML
    private Button avatarButton;                // Bottone per aprire il menu del profilo


    // Variabili per la gestione della ricerca e dei filtri
    @FXML
    private TextField searchField;              // barra di ricerca

    @FXML
    private ToggleButton searchByNameToggle;    // ricerca per nome

    @FXML
    private ToggleGroup searchModeGroup;

    @FXML
    private ToggleButton searchByIdToggle;      // ricerca per ID


    // Filtri di quantita
    @FXML
    private CheckBox quantityFilterCheck;       // checkbox per abilitare/disabilitare il filtro di quantità

    @FXML
    private TextField quantityField;            // filtro di quantità (in interi)

    @FXML
    private ComboBox compareCombo;              // filtro di maggiore o minore

    @FXML
    private ComboBox unitTypeCombo;             // filtro di tipo di unità (es. kg, l, item, ecc.)


    // Variabili sui filtri di categotrie e immagini
    @FXML
    private ToggleButton imagesAllToggle;

    @FXML
    private ToggleGroup imageFilterGroup;

    @FXML
    private ToggleButton imagesWithToggle;

    @FXML
    private ToggleButton imagesWithoutToggle;

    @FXML
    private ToggleButton categoriesAllToggle;

    @FXML
    private ToggleGroup categoryFilterGroup;

    @FXML
    private ToggleButton categoriesWithToggle;

    @FXML
    private ToggleButton categoriesWithoutToggle;


    // Variabili dei 4 tasti principali della dashboard
    @FXML
    private Button resetButton;

    @FXML
    private Button searchButton;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Label selectionHintLabel;


    // Variabili per la gestione della visualizzazione degli item
    @FXML
    private ScrollPane itemScroll;

    @FXML
    private Label noItemsLabel;

    @FXML
    private FlowPane itemGrid;

    /*
    @FXML
    private VBox ItemCard; // Da aggiungere
    */

    // Variabili per la gestione della paginazione
    @FXML
    private HBox paginationBar;

    @FXML
    private Label rangeLabel;

    @FXML
    private Button firstPageButton;

    @FXML
    private Button prevPageButton;

    @FXML
    private Label pageLabel;

    @FXML
    private Button nextPageButton;

    @FXML
    private Button lastPageButton;


    // Riferimetni per oggetti esterni
    @FXML 
    private ChatPanelController chatPanelController;
    @FXML 
    private ProfileMenuController profileMenuController;
    
    @FXML
    private VBox chatPanel;         // Contenitore per il pannello della chat (es. lista conversazioni, messaggi)

    @FXML
    private VBox profileMenu;       // Contenitore per il menu del profilo (es. impostazioni, logout)

    @FXML
    private Button chatFab;         // Floating Action Button per aprire/chiudere il pannello della chat

    
    // ----- Setup iniziale
    @FXML
    private void initialize() {
        // Hint: initialize() will be called when the associated FXML has been completely loaded.
        // istanzia ItemCardManager
        
        // Imposta la visibilità dei pannelli esterni (chat e menu profilo) in base alla loro proprietà "visible"
        chatPanel.managedProperty().bind(chatPanel.visibleProperty());
        profileMenu.managedProperty().bind(profileMenu.visibleProperty());

        // Imposta la callback per la chiusura del pannello chat
        chatPanelController.setOnCloseRequested(this::onOpenChat);
    }

    // ====== Sidebar categorie ======
    @FXML private void onNewCategory() { 
        // Controlla se sei un admin prima di aprire il popup per la creazione di una nuova categoria
    }
    @FXML private void onDeleteCategories() { }

    // ====== Testata / ricerca / filtri ======
    @FXML private void onToggleProfileMenu() { profileMenu.setVisible(!profileMenu.isVisible()); }
    @FXML private void onSearch() { ThemeManager.getInstance().setTheme("mono"); }

    @FXML private void onReset() { 
        // Filtri immagini e categorie -> "tutti gli articoli"
        imagesAllToggle.setSelected(true);
        categoriesAllToggle.setSelected(true);

        // Filtro quantità: checkbox spenta, quantità a 0, confronto e tipo senza valore
        quantityFilterCheck.setSelected(false);
        quantityField.setText("0");
        compareCombo.getSelectionModel().clearSelection();
        unitTypeCombo.getSelectionModel().clearSelection();

        // Barra di ricerca pulita
        searchField.clear();

        // TODO: deselezionare le categorie scelte nella sidebar

        // TODO: rilanciare la ricerca con i filtri azzerati (es. onSearch() / reload pagina 1)

    }  

    @FXML private void onAddOrConfirmDelete() { }
    @FXML private void onDeleteOrCancel() { }

    // ====== Paginazione ======
    @FXML private void onFirstPage() { }
    @FXML private void onPrevPage() { }
    @FXML private void onNextPage() { }
    @FXML private void onLastPage() { }

    // ====== Chat ======
    @FXML private void onOpenChat() { 
        setChatOpen(!chatPanel.isVisible());
    }

    private void setChatOpen(boolean open) {
        //se la chat è chiusa e il pannello delle impostazioni è aperto, chiudi il pannello delle impostazioni
        if (open && profileMenu.isVisible()) {
            profileMenu.setVisible(false);
        }

        // imposta la visibilità del pannello della chat e sposta il FAB di conseguenza
        chatPanel.setVisible(open);

        // sposta il FAB di 400px a sinistra se la chat è aperta, altrimenti lo riporta a destra
        chatFab.setTranslateX(open ? -400 : 0);
    }


}
