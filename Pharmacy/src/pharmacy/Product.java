/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

/**
 *
 * @author elkhedewy-group
 */
public class Product {
    protected int productId;
    protected String name;
    protected double price;
    protected int quantity;

    public Product(int productId, String name, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
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
}
