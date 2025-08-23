/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author elkhedewy-group
 */
public class Product {
    protected int productId;
    protected String name;
    protected double price;
    protected int quantity;
    private static final String FILE_NAME = "products.txt";
    
    // added default constructor to make an easy to invoke wout args
    public Product() {}

    public Product(int productId, String name, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
     public static void saveProducts(List<Product> products) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Product p : products) {
                writer.write(p.getProductId() + "," + p.getName() + "," + p.getPrice() + "," + p.getQuantity());
                writer.newLine();
            }
            System.out.println("✅ Products saved successfully.");
        } catch (IOException e) {
             System.err.println("[ERROR] Failed to save products: " + e.getMessage());
        }
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
    
    public boolean isAvailable(int qty){
        return quantity >= qty;
    }
    
    public void reduceQuantity(int qty){
        if (isAvailable(qty)) {
            quantity -= qty;
            System.out.println("[INFO]\n" + name + " quantity reduced by " + qty + ". Remaining: " + quantity);
        } else {
            System.out.println("[WARNING] Not enough stock for product: " + name);
        }
    }
    
    //LoadProducts
    public static List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        File file = new File(FILE_NAME);

        //Verify that the file exists
        if (!file.exists()) {
            System.out.println("[INFO] No products file found. Starting with an empty list.");
            return products;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                //Verify that there are only 4 elements
                if (data.length != 4) {
                    System.out.println("[WARNING] Skipping invalid line: " + line);
                    continue;
                }

                try {
                    int id = Integer.parseInt(data[0]);
                    String name = data[1];
                    double price = Double.parseDouble(data[2]);
                    int quantity = Integer.parseInt(data[3]);
                    products.add(new Product(id, name, price, quantity));
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Skipping invalid number in line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load products: " + e.getMessage());
        }

        return products;
    }
}

