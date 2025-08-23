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
public class OrderItem {
    private Product product;
    private int quantity;
    private double subtotal;
    private static final String FILE_NAME = "order_items.txt";

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.subtotal = calculateSubtotal();
    }

    public static String getFILE_NAME() {
        return FILE_NAME;
    }
    
    public static void saveOrderItems(List<OrderItem> items) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
        for (OrderItem item : items) {
            writer.write(item.getProduct().getProductId() + "," +
                         item.getProduct().getName() + "," +
                         item.getProduct().getPrice() + "," +
                         item.getQuantity() + "," +
                         item.getSubtotal());
            writer.newLine();
        }
        System.out.println("✅ Order items saved successfully.");
    } catch (IOException e) {
        System.err.println("[ERROR] Failed to save order items: " + e.getMessage());
    }
}


    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        subtotal = product.getPrice()*quantity;
        return subtotal;
    }
    
    public double calculateSubtotal() {
        return getSubtotal();
    }
    
    public static List<OrderItem> loadOrderItems() {
        List<OrderItem> items = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            System.out.println("[INFO] No order items file found. Starting empty.");
            return items;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length != 5) {
                    System.out.println("[WARNING] Skipping invalid line: " + line);
                    continue;
                }

                try {
                    int productId = Integer.parseInt(data[0]);
                    String name = data[1];
                    double price = Double.parseDouble(data[2]);
                    int quantity = Integer.parseInt(data[3]);

                    Product p = new Product(productId, name, price, quantity);
                    items.add(new OrderItem(p, quantity));

                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Skipping invalid number in line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load order items: " + e.getMessage());
        }

        return items;
    }
}

