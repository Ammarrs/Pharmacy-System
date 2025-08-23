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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
    
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
    
    public void saveToFile() {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Product p: products) {
                String type = "Product";
                String special = "";

                if (p instanceof Medicine) {
                    type = "Medicine";
                    Medicine med = (Medicine) p;
                    special = String.valueOf(med.isPrescriptionRequired());
                } else if (p instanceof Cosmetic) {
                    type = "Cosmetic";
                    Cosmetic cos = (Cosmetic) p;
                    special = cos.getSuitableForSkinType();
                }

                writer.write(p.getProductId() + "," + p.getName() + "," + p.getPrice() + "," + p.getQuantity() + "," + type + "," + special);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("[ERR], Failed to save Inventory: " + e.getMessage());
        }
    }
    
    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length >= 4) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        double price = Double.parseDouble(parts[2].trim());
                        int quantity = Integer.parseInt(parts[3].trim());

                        Product product;

                        if (parts.length >= 6) {
                            String type = parts[4].trim();
                            String special = parts[5].trim();

                            if ("Medicine".equals(type)) {
                                boolean prescriptionRequired = Boolean.parseBoolean(special);
                                product = new Medicine(prescriptionRequired, id, name, price, quantity);
                            } else if ("Cosmetic".equals(type)) {
                                product = new Cosmetic(special, id, name, price, quantity);
                            } else {
                                product = new Product(id, name, price, quantity);
                            }
                        } else {
                            product = new Product(id, name, price, quantity);
                        }

                        products.add(product);

                    } catch (NumberFormatException e) {
                        System.out.println("[WARN] Skipping invalid line: " + line);
                    }
                } else {
                    System.out.println("[WARN] Invalid data format: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("[ERR] Failed to load Inventory: " + e.getMessage());
        }
    }
    public boolean removeProduct(int productId) {
    for (int i = 0; i < products.size(); i++) {
        if (products.get(i).getProductId() == productId) {
            products.remove(i);
            return true;
        }
    }
    return false;
}
}