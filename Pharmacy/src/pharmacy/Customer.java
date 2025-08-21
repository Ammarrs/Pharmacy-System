/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

/**
 *
 * @author HP
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Customer {
    private static int lastId = 0;
    private int customerid;
    private String name;
    private String phone;

    public Customer(String name, String phone) {
        this.customerid = ++lastId; // توليد ID تلقائي
        this.name = name;
        this.phone = phone;
    }

    // ===================== Load Customers =====================
    public static List<Customer> loadCustomersFromFile(String fileName) {
        List<Customer> customers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int maxId = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String phone = parts[2];

                    // نعمل كائن جديد من Customer بس نحافظ على نفس ID
                    Customer c = new Customer(name, phone);
                    c.customerid = id;  
                    customers.add(c);

                    if (id > maxId) {
                        maxId = id;
                    }
                }
            }
            lastId = maxId; // تحديث آخر ID
        } catch (IOException e) {
            System.out.println("No existing customer file. Starting fresh.");
        }
        return customers;
    }

    // ===================== باقي الكود زي ما عندك =====================
    public static void initializeLastId(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
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
            lastId = maxId; // تخزين آخر ID
        } catch (IOException e) {
            System.out.println("No existing customer file. Starting from ID = 1");
        }
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

    public void getCustomerinfo () {
        System.out.println("Customer Id: " + customerid + ", Name: " + name + ", Phone: " + phone);
    }

    public void saveToFile(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(customerid + "," + name + "," + phone);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Customer{" + "customerid=" + customerid + ", name=" + name + ", phone=" + phone + '}';
    }
    
}