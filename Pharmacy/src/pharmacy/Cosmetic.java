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
}
