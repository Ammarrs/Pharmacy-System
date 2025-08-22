package pharmacy;

import java.io.*;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Enhanced Order class with file operations
 * @author PharmacySystem
 */
public class Order {
    private static int orderCounter = 0;
    private int orderId;
    private Customer customer;
    private List<OrderItem> items;
    private double totalAmount;
    private Date orderDate;
    private String status;
    private static final String ORDERS_FILE = "orders.txt";
    private static final String ORDER_ITEMS_FILE = "order_details.txt";
    private String soldBy;
    
    static {
        loadOrderCounter();
    }

    
    public Order(Customer customer, String soldBy) {
        this.orderId = ++orderCounter;
        this.customer = customer;
        this.items = new ArrayList<>();
        this.orderDate = new Date();
        this.status = "Pending";
        this.totalAmount = 0.0;
        this.soldBy = soldBy;
    }
    

    // Constructor for loading from file
    public Order(int orderId, Customer customer, Date orderDate, String status, double totalAmount, String soldBy) {
        this.orderId = orderId;
        this.customer = customer;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.soldBy = soldBy;
        this.items = new ArrayList<>();
        if (orderId > orderCounter) orderCounter = orderId;
    }
    
    public String getSoldBy() { return soldBy; }
    
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

        saveOrderToFile();
        saveOrderItemsToFile();
    }
    
    // Save order header to file
    public void saveOrderToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDERS_FILE, true))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            writer.write(orderId + "," + 
                customer.getCustomerid() + "," + 
                customer.getName() + "," + 
                dateFormat.format(orderDate) + "," + 
                status + "," + 
                String.format("%.2f", totalAmount) + "," +
                (soldBy != null ? soldBy : "Unknown"));
            writer.newLine();
            System.out.println("✅ Order " + orderId + " saved to file successfully.");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save order: " + e.getMessage());
        }
    }
    
    // Save order items to file
    public void saveOrderItemsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ORDER_ITEMS_FILE, true))) {
            for (OrderItem item : items) {
                writer.write(orderId + "," + 
                            item.getProduct().getProductId() + "," + 
                            item.getProduct().getName() + "," + 
                            item.getQuantity() + "," + 
                            String.format("%.2f", item.getProduct().getPrice()) + "," + 
                            String.format("%.2f", item.calculateSubtotal()));
                writer.newLine();
            }
            System.out.println("✅ Order items for order " + orderId + " saved successfully.");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save order items: " + e.getMessage());
        }
    }
    
    // Load all orders from file
    public static List<Order> loadOrdersFromFile(List<Customer> customers) {
        List<Order> orders = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    int orderId = Integer.parseInt(parts[0]);
                    int customerId = Integer.parseInt(parts[1]);
                    String customerName = parts[2];
                    Date orderDate = dateFormat.parse(parts[3]);
                    String status = parts[4];
                    double totalAmount = Double.parseDouble(parts[5]);
                    String soldBy = parts.length > 6 ? parts[6] : "Unknown";
                    
                    // Find customer
                    Customer customer = null;
                    for (Customer c : customers) {
                        if (c.getCustomerid() == customerId) {
                            customer = c;
                            break;
                        }
                    }
                    
                    if (customer == null) {
                        // Create customer if not found (backup)
                        customer = new Customer( customerName, "Unknown");
                    }
                    
                    Order order = new Order(orderId, customer, orderDate, status, totalAmount, soldBy);
                    orders.add(order);
                }
            }
        } catch (Exception e) {
            System.out.println("No existing orders file or error reading: " + e.getMessage());
        }
        
        return orders;
    }
    
    // Load order items for a specific order
    public void loadOrderItems(List<Product> allProducts) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDER_ITEMS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    int orderIdFromFile = Integer.parseInt(parts[0]);
                    if (orderIdFromFile == this.orderId) {
                        int productId = Integer.parseInt(parts[1]);
                        String productName = parts[2];
                        int quantity = Integer.parseInt(parts[3]);
                        double price = Double.parseDouble(parts[4]);
                        
                        // Find product in inventory or create temporary one
                        Product product = null;
                        for (Product p : allProducts) {
                            if (p.getProductId() == productId) {
                                product = p;
                                break;
                            }
                        }
                        
                        if (product == null) {
                            // Create temporary product for historical data
                            product = new Product(productId, productName, price, 0);
                        }
                        
                        this.items.add(new OrderItem(product, quantity));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("No order items file found or error reading: " + e.getMessage());
        }
    }
    
    // Load and set the order counter from existing files
    private static void loadOrderCounter() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDERS_FILE))) {
            String line;
            int maxId = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1) {
                    int id = Integer.parseInt(parts[0]);
                    if (id > maxId) {
                        maxId = id;
                    }
                }
            }
            orderCounter = maxId;
        } catch (IOException e) {
            System.out.println("No existing orders file. Starting from Order ID = 1");
            orderCounter = 0;
        }

    }
    
    // Getters and Setters
    public int getOrderId() { return orderId; }
    public Customer getCustomer() { return customer; }
    public List<OrderItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public Date getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    
    public void setStatus(String status) { this.status = status; }

    
    @Override
    public String toString() {
        return "Order #" + orderId + " - " + customer.getName() + " - $" + String.format("%.2f", totalAmount);
    }
}

