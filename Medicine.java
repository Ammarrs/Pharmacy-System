/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy_system;

/**
 *
 * @author elkhedewy-group
 */
public class Medicine extends Product {
    private boolean prescriptionRequired;

    public Medicine(boolean prescriptionRequired, int productId, String name, double price, int quantity) {
        super(productId, name, price, quantity);
        this.prescriptionRequired = prescriptionRequired;
    }
}
