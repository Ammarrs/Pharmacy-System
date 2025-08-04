package pharmacy;

import java.util.HashMap;

public class Login {
    private String username;
    private String password;
    private boolean isLoggedin;
    private static final HashMap<String, String> Pharmacists = new HashMap<>();

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Method to sign up and add new user
    public static boolean signup(String username, String password) {
        if (Pharmacists.containsKey(username)) {
            System.out.println("Username already exists.");
            return false;
        }
        Pharmacists.put(username, password);
        System.out.println("Signup successful!");
        return true;
    }

    // Login method
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
}
