package it.cookie.progetti.controllers.MainMenu.Containers;

import it.cookie.utils.network.item.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ItemCardController {
    // ciascuna card rapresenta un item, e ciascuno è responsabile di fetchare la propria immagine
    @FXML private Label nameLabel;
    @FXML private Label qtyLabel;
    @FXML private ImageView itemImage;

    private Item item;

    public void setItem(Item item) {
        this.item = item;
        nameLabel.setText(item.getName());
        qtyLabel.setText(String.valueOf(item.getQty()));
        // itemImage.setImage(...) se hai l'immagine
    }
}
