/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

import java.util.List;
import java.util.ArrayList;
import pharmacy.Product;
/**
 *
 * @author ammar
 */
public class Inventory {
    private List<Product> products = new ArrayList<>();
    
    void addProduct (Product prod) {
        // Make changes
        for (Product p: products) {
            if (p.getProductId() == prod.getProductId()) {
                p.setQuantity(p.getQuantity() + prod.getQuantity());
                return;
            }
        }
        products.add(prod);
    }
    
    void updateQuantity (Product prod, int qtty) {
        // Make changes
        for (Product p: products) {
            if (p.getProductId() == prod.getProductId()) {
                p.setQuantity(p.getQuantity() + qtty);
                System.out.println("[INFO] Quantity of " + p.getName() + " updated to " + p.getQuantity());
                return;
            }
        }
        System.out.println("[WARNING] Product not found in inventory: " + prod.getName());
    }
    
    Product getProductById (int id) {
        // Make Changes
        for (Product p: products){
            if (p.getProductId() == id) {
                return p;
            }
        }
        System.out.println("Product not found");
        return null;
    }
    
    void displayInventory () {
        // Make changes
        if (products.isEmpty()) {
            System.out.println("[INFO] Inventory is Empty!");
            return;
        }
        
        System.out.println("============= INVENTORY =============");
        System.out.printf("%-4s %-15s %-7s %-5s%n", "ID", "Name", "Price", "Qty");
        System.out.println("-------------------------------------");
        
        for (Product p: products) {
            System.out.printf("%-4d %-15s %-7.2f %-5d%n",
                        p.getProductId(),
                        p.getName(),
                        p.getPrice(),
                        p.getQuantity()
                    );
        }
        System.out.println("=====================================");
    }
}
