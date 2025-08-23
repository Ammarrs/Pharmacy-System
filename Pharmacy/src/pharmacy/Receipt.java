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
 * @author ammar
 */
public class Receipt {
    private int id;
    private List<OrderItem> items = new ArrayList();
    private double totalPrice;
    private Order order; // not completed
    private Date date;
    
    Receipt(int id, Order order) {
        this.id = id;
        this.order = order;
        this.date = new Date();
        generate();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    void generate () {
        if (order != null) {
            this.items = new ArrayList<>(order.getItems());
            this.totalPrice = order.calculateTotal();
        }
    }
    
    void printReceipt () {
        System.out.println("===== RECEIPT =====");
        System.out.println("Receipt ID: " + id);
        System.out.println("Date: " + date);

        if (order != null && order.getCustomer() != null) {
            System.out.println("Customer: " + order.getCustomer());
            order.getCustomer().getCustomerinfo();
        }

        System.out.println("\nItems:");
        for (OrderItem item : items) {
            System.out.println("- " + item.getProduct().getName() +
                               " | Qty: " + item.getQuantity() +
                               " | Price: " + item.getProduct().getPrice() +
                               " | Subtotal: " + item.calculateSubtotal());
        }

        System.out.println("\nTotal Price: " + totalPrice);
        System.out.println("=====================");
    }

}
