/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

/**
 *
 * @author HP
 */
public class Customer {
    private int customerid;
    private String name;
    private String phone;

    public Customer(int customerid, String name, String phone) {
        this.customerid = customerid;
        this.name = name;
        this.phone = phone;
    }

    public int getCustomerid() {
        return customerid;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
    
    public void getCustomerinfo ()
    {
        System.out.println("Customer Id: " + getCustomerid() + "Name: " + getName() + "Phone: " + getPhone());
    }
}