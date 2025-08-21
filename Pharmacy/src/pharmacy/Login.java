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

    // ✅ validate function
    public boolean validate() {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username cannot be empty.");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }
        if (!Pharmacists.containsKey(username)) {
            System.out.println("Username not found.");
            return false;
        }
        if (!Pharmacists.get(username).equals(password)) {
            System.out.println("Incorrect password.");
            return false;
        }
        return true;
    }

    // ✅ login function uses validate()
    public boolean login() {
        if (validate()) {
            isLoggedin = true;
            System.out.println("You have successfully logged in.");
            return true;
        } else {
            System.out.println("Login failed.");
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
