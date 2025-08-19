/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author HP
 */


/**
 * Order class to complete the pharmacy system
 * @author PharmacySystem
 */
public class Order {
    private static int orderCounter = 0;
    private int orderId;
    private Customer customer;
    private List<OrderItem> items;
    private double totalAmount;
    private Date orderDate;
    private String status; // "Pending", "Completed", "Cancelled"
    
    public Order(Customer customer) {
        this.orderId = ++orderCounter;
        this.customer = customer;
        this.items = new ArrayList<>();
        this.orderDate = new Date();
        this.status = "Pending";
        this.totalAmount = 0.0;
    }
    
    public void addItem(Product product, int quantity) {
        if (product.isAvailable(quantity)) {
            OrderItem existingItem = findItemByProduct(product);
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
            } else {
                items.add(new OrderItem(product, quantity));
            }
            calculateTotal();
        } else {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }
    }
    
    public void removeItem(Product product) {
        items.removeIf(item -> item.getProduct().getProductId() == product.getProductId());
        calculateTotal();
    }
    
    private OrderItem findItemByProduct(Product product) {
        for (OrderItem item : items) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                return item;
            }
        }
        return null;
    }
    
    public double calculateTotal() {
        totalAmount = 0.0;
        for (OrderItem item : items) {
            totalAmount += item.calculateSubtotal();
        }
        return totalAmount;
    }
    
    public void completeOrder() {
        this.status = "Completed";
        // Reduce inventory quantities would be handled by the calling class
    }

    public static int getOrderCounter() {
        return orderCounter;
    }

    public static void setOrderCounter(int orderCounter) {
        Order.orderCounter = orderCounter;
    }
    
    // Getters and Setters
    public int getOrderId() { return orderId; }
    public Customer getCustomer() { return customer; }
    public List<OrderItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public Date getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    
    public void setStatus(String status) { this.status = status; }
}
