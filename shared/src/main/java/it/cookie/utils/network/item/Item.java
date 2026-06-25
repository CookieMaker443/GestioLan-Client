package it.cookie.utils.network.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// serve per dire a jackson di ignorare i campi non presenti in questa classe
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    
    @JsonProperty("idItem")
    private int id;

    @JsonProperty("itemName")
    private String name;

    @JsonProperty("description")    
    private String description;

    @JsonProperty("idImage")
    private int idImage;

    @JsonProperty("imageName")
    private String imageName;

    @JsonProperty("idCategory")
    private int idCategory;

    @JsonProperty("quantity")
    private int qty;

    @JsonProperty("typeQuantity")    
    private String typeQty;

    @JsonProperty("barcode")
    private String barcode;

    // Getter
    public int getId()          { return id; }
    public String getName()     { return name; }
    public String getDescription() { return description; }
    public int getIdImage()     { return idImage; }
    public String getImageName(){ return imageName; }
    public int getIdCategory()  { return idCategory; }
    public int getQty()         { return qty; }
    public String getTypeQty()  { return typeQty; }
    public String getBarcode()  { return barcode; }
}
