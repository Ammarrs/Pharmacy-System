/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package pharmacy;

import java.util.Scanner;

/**
 *
 * @author elkhedewy-group
 */
public class Pharmacy_System {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

 Scanner scanner = new Scanner(System.in);

        System.out.println("=== Pharmacy Login System ===");

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // إنشاء Object من Login
        Login login = new Login(username, password);

        // محاولة تسجيل الدخول
        if (login.login()) {
            System.out.println("Welcome, " + login.getUsername() + "!");

            // جرب logout
            System.out.print("Do you want to logout? (y/n): ");
            String choice = scanner.nextLine();
            if (choice.equalsIgnoreCase("y")) {
                login.logout();
            }
        } else {
            System.out.println("Access denied.");
        }

        scanner.close();
//        Customer customer = new Customer("Farida", "01012345678");
//        Customer customer2 = new Customer("Haneen", "01012345678");
//        Customer customer3 = new Customer("Ahmed", "01012345678");
//
//        customer3.getCustomerinfo();
////
////        customer.saveToFile("customers.txt");
////        customer2.saveToFile("customers.txt");
//        customer3.saveToFile("customers.txt");
//
//        System.out.println("Customer data saved to file.");

    }

}
