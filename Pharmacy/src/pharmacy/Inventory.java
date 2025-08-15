/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import pharmacy.Product;
/**
 *
 * @author ammar
 */
public class Inventory {
    private List<Product> products = new ArrayList<>();
    private static final String FILE_NAME = "inventory.txt";
    
    public Inventory() {
        loadFromFile();
    }
    
    void addProduct (Product prod) {
        // Make changes
        for (Product p: products) {
            if (p.getProductId() == prod.getProductId()) {
                p.setQuantity(p.getQuantity() + prod.getQuantity());
                saveToFile();
                return;
            }
        }
        products.add(prod);
        saveToFile();
    }
    
    void updateQuantity (Product prod, int qtty) {
        // Make changes
        for (Product p: products) {
            if (p.getProductId() == prod.getProductId()) {
                p.setQuantity(p.getQuantity() + qtty);
                System.out.println("[INFO] Quantity of " + p.getName() + " updated to " + p.getQuantity());
                saveToFile();
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
    
    // for files
    
    private void saveToFile() {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Product p: products) {
                writer.write(p.getProductId() + "," + p.getName() + "," + p.getPrice() + "," + p.getQuantity());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("[ERR], Failedto save Inventory: " + e.getMessage());
        }
    }
    
    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if(!file.exists()) {
            return;
        }
        
        try(BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            line = reader.readLine();
            while(line != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    double price = Double.parseDouble(parts[2]);
                    int quantity = Integer.parseInt(parts[3]);
                    products.add(new Product(id, name, price, quantity));
                }
            }
        } catch (IOException e){
            System.out.println("[ERR], Failed to load Inventory " + e.getMessage());
        }
    }
}
