/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

/**
 *
 * @author elkhedewy-group
 */
public class Cosmetic extends Product {
    private String suitableForSkinType;

    public Cosmetic(String suitableForSkinType, int productId, String name, double price, int quantity) {
        super(productId, name, price, quantity);
        this.suitableForSkinType = suitableForSkinType;
    }
    
     public boolean isSuitableFor(String skinType) {
        return this.suitableForSkinType.equalsIgnoreCase(skinType);
    }

    public String getSuitableForSkinType() {
        return suitableForSkinType;
    }

    public void setSuitableForSkinType(String suitableForSkinType) {
        this.suitableForSkinType = suitableForSkinType;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
     
}
