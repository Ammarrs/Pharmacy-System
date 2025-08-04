
package pharmacy;

import java.io.*;
import java.util.HashMap;

public class Login {
    private String username;
    private String password;
    private boolean isLoggedin;
    private static final HashMap<String, String> Pharmacists = new HashMap<>();
    private static final String FILE_NAME = "users.txt";

    // Load users from file once at the beginning
    static {
        loadUsersFromFile();
    }

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static boolean signup(String username, String password) {
        if (Pharmacists.containsKey(username)) {
            System.out.println("Username already exists.");
            return false;
        }

        Pharmacists.put(username, password);
        System.out.println("Signup successful!");

        // Save to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(username + "," + password);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }

        return true;
    }

    public boolean login() {
        if (Pharmacists.containsKey(username) && Pharmacists.get(username).equals(password)) {
            isLoggedin = true;
            System.out.println("You have successfully logged in.");
            return true;
        } else {
            System.out.println("Invalid username or password.");
            return false;
        }
    }

    public void logout() {
        if (isLoggedin) {
            isLoggedin = false;
            System.out.println("Logged out successfully.");
        } else {
            System.out.println("You are not logged in.");
        }
    }

    public boolean isLoggedin() {
        return isLoggedin;
    }

    public String getUsername() {
        return username;
    }

    private static void loadUsersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    Pharmacists.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("No user file found yet. Starting fresh.");
        }
    }
}