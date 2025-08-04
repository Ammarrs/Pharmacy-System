/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

/**
 *
 * @author HP
 */
import java.io.*;

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
}