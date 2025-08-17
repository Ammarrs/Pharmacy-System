/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

import java.util.List;

/**
 *
 * @author ammar
 */
public class Pharmaisit 
{
    int pharmasistID;
    String name;
    Login loginInfo;
    Inventory inventory;

    public Pharmaisit(int pharmasistID, String name, Login loginInfo, Inventory inventory) {
        this.pharmasistID = pharmasistID;
        this.name = name;
        this.loginInfo = loginInfo;
        this.inventory = inventory;
    }
    
    public void login(String user, String pass)
    {
        if(loginInfo.validate())
            loginInfo.login();
        else
            System.out.println("Login failed: envalid credentials !");
    }
    
    public void processSale(Customer cx, List<OrderItem> items)
    {
        int count = 0;
        System.out.println("Processing for customer: " + cx.getName());
        
        int total = 0;
        for(OrderItem item: items)
        {
            Product product = item.getProduct();
            int purchasedQty = item.getQuantity();
            int currentQty = product.getQuantity();
            int newStock = currentQty - purchasedQty;
            
            if(newStock >= 0)
            {
                inventory.updateQuantity(product.getProductId(), newStock);
                double subtotal = item.calculateSubtotal();
                total += subtotal;
                System.out.println("Sold: " + product.getName() +
                               " | amount: " + purchasedQty +
                               " | Subtotal: $" + subtotal);
            }
            else
                System.out.println("Not enough stock for: " + product.getName());

        }
        System.out.println("Total Sale Amount: $" + total);
    }
    
    public void viewInventory()
    {
        inventory.displayInventory();
    }
    
}
