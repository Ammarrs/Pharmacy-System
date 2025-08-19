/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy;

/**
 *
 * @author HP
 */



import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.List;

import java.util.List;

/**
 * Complete Pharmacy Management System GUI using Java Swing
 * @author PharmacySystem
 */
public class PharmacyGUI extends JFrame {
    
    // Core components
    private Login currentLogin;
    private Inventory inventory;
    private List<Customer> customers;
    private List<Order> orders;
    private Order currentOrder;
    
    // GUI Components
    private JTabbedPane mainTabbedPane;
    private JPanel loginPanel, mainPanel;
    private JTextField usernameField, passwordField;
    private JButton loginButton, logoutButton;
    
    // Product Management
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private JTextField productNameField, productPriceField, productQuantityField;
    private JComboBox<String> productTypeCombo;
    private JCheckBox prescriptionRequiredBox;
    private JTextField skinTypeField;
    
    // Customer Management
    private JTable customerTable;
    private DefaultTableModel customerTableModel;
    private JTextField customerNameField, customerPhoneField;
    
    // Sales Management
    private JTable availableProductsTable, cartTable;
    private DefaultTableModel availableProductsModel, cartModel;
    private JComboBox<Customer> customerComboBox;
    private JLabel totalLabel;
    private List<OrderItem> currentCart;
    
    // Order History
    private JTable orderHistoryTable;
    private DefaultTableModel orderHistoryModel;
    
    public PharmacyGUI() {
        initializeData();
        initializeGUI();
        showLoginScreen();
    }
    
    private void initializeData() {
        // Initialize core data structures
        inventory = new Inventory();
        customers = new ArrayList<>();
        orders = new ArrayList<>();
        currentCart = new ArrayList<>();
        
        // Load existing customers
        Customer.loadCustomersFromFile("customers.txt");
        
        // Add some sample data if files are empty
        if (customers.isEmpty()) {
            customers.add(new Customer("Farida", "01012345678"));
            customers.add(new Customer("Haneen", "01012345679"));
            customers.add(new Customer("Ahmed", "01012345680"));
        }
        
        // Initialize sample inventory if empty
        if (inventory.getProducts().isEmpty()) {
            inventory.addProduct(new Medicine(false, 1, "Panadol", 15.50, 100));
            inventory.addProduct(new Medicine(true, 2, "Insulin", 120.00, 25));
            inventory.addProduct(new Cosmetic("Normal", 3, "Face Cream", 45.00, 30));
            inventory.addProduct(new Medicine(false, 4, "Aspirin", 12.00, 75));
        }
    }
    
    private void initializeGUI() {
        setTitle("Pharmacy Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Create main container
        setLayout(new BorderLayout());
        
        createLoginPanel();
        createMainPanel();
        
        // Initially show login panel
        add(loginPanel, BorderLayout.CENTER);
    }
    
    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(240, 248, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Pharmacy Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 100, 0));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1; gbc.gridy = 1;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        loginPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        loginPanel.add(passwordField, gbc);
        
        // Login Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 150, 0));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.addActionListener(e -> performLogin());
        loginPanel.add(loginButton, gbc);
        
        // Demo credentials info
        gbc.gridy = 4;
        JLabel infoLabel = new JLabel("<html><center>Demo Credentials:<br>Username: Farida<br>Password: 123456</center></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setForeground(Color.GRAY);
        loginPanel.add(infoLabel, gbc);
        
        // Enter key support
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 100, 0));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel headerLabel = new JLabel("Pharmacy Management System");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(200, 50, 50));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> performLogout());
        
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        // Main Tabbed Pane
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("Dashboard", createDashboardPanel());
        mainTabbedPane.addTab("Products", createProductPanel());
        mainTabbedPane.addTab("Customers", createCustomerPanel());
        mainTabbedPane.addTab("Sales", createSalesPanel());
        mainTabbedPane.addTab("Order History", createOrderHistoryPanel());
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel(new GridLayout(2, 2, 10, 10));
        dashboard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Statistics Cards
        dashboard.add(createStatCard("Total Products", String.valueOf(inventory.getProducts().size()), new Color(52, 152, 219)));
        dashboard.add(createStatCard("Total Customers", String.valueOf(customers.size()), new Color(46, 204, 113)));
        dashboard.add(createStatCard("Orders Today", String.valueOf(orders.size()), new Color(155, 89, 182)));
        dashboard.add(createStatCard("Low Stock Items", String.valueOf(getLowStockCount()), new Color(231, 76, 60)));
        
        return dashboard;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(color);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createProductPanel() {
        JPanel productPanel = new JPanel(new BorderLayout(10, 10));
        productPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Product Table
        productTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Type", "Price", "Quantity", "Special"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshProductTable();
        
        JScrollPane productScrollPane = new JScrollPane(productTable);
        productScrollPane.setBorder(new TitledBorder("Product Inventory"));
        
        // Product Form Panel
        JPanel productFormPanel = createProductFormPanel();
        
        productPanel.add(productScrollPane, BorderLayout.CENTER);
        productPanel.add(productFormPanel, BorderLayout.SOUTH);
        
        return productPanel;
    }
    
    private JPanel createProductFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Add/Update Product"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Product Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        productNameField = new JTextField(15);
        formPanel.add(productNameField, gbc);
        
        // Product Type
        gbc.gridx = 2;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 3;
        productTypeCombo = new JComboBox<>(new String[]{"Medicine", "Cosmetic"});
        productTypeCombo.addActionListener(e -> toggleProductSpecificFields());
        formPanel.add(productTypeCombo, gbc);
        
        // Price
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        productPriceField = new JTextField(15);
        formPanel.add(productPriceField, gbc);
        
        // Quantity
        gbc.gridx = 2;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 3;
        productQuantityField = new JTextField(15);
        formPanel.add(productQuantityField, gbc);
        
        // Medicine specific - Prescription Required
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Prescription Required:"), gbc);
        gbc.gridx = 1;
        prescriptionRequiredBox = new JCheckBox();
        formPanel.add(prescriptionRequiredBox, gbc);
        
        // Cosmetic specific - Skin Type
        gbc.gridx = 2;
        formPanel.add(new JLabel("Skin Type:"), gbc);
        gbc.gridx = 3;
        skinTypeField = new JTextField(15);
        formPanel.add(skinTypeField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JButton addProductBtn = new JButton("Add Product");
        addProductBtn.setBackground(new Color(0, 150, 0));
        addProductBtn.setForeground(Color.WHITE);
        addProductBtn.addActionListener(e -> addProduct());
        formPanel.add(addProductBtn, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 2;
        JButton clearBtn = new JButton("Clear Fields");
        clearBtn.addActionListener(e -> clearProductFields());
        formPanel.add(clearBtn, gbc);
        
        toggleProductSpecificFields();
        return formPanel;
    }
    
    private JPanel createCustomerPanel() {
        JPanel customerPanel = new JPanel(new BorderLayout(10, 10));
        customerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Customer Table
        customerTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Phone"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(customerTableModel);
        refreshCustomerTable();
        
        JScrollPane customerScrollPane = new JScrollPane(customerTable);
        customerScrollPane.setBorder(new TitledBorder("Customer List"));
        
        // Customer Form
        JPanel customerFormPanel = createCustomerFormPanel();
        
        customerPanel.add(customerScrollPane, BorderLayout.CENTER);
        customerPanel.add(customerFormPanel, BorderLayout.SOUTH);
        
        return customerPanel;
    }
    
    private JPanel createCustomerFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Add Customer"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Customer Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        customerNameField = new JTextField(20);
        formPanel.add(customerNameField, gbc);
        
        // Customer Phone
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        customerPhoneField = new JTextField(20);
        formPanel.add(customerPhoneField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 2;
        JButton addCustomerBtn = new JButton("Add Customer");
        addCustomerBtn.setBackground(new Color(0, 150, 0));
        addCustomerBtn.setForeground(Color.WHITE);
        addCustomerBtn.addActionListener(e -> addCustomer());
        formPanel.add(addCustomerBtn, gbc);
        
        gbc.gridx = 1;
        JButton clearBtn = new JButton("Clear Fields");
        clearBtn.addActionListener(e -> clearCustomerFields());
        formPanel.add(clearBtn, gbc);
        
        return formPanel;
    }
    
    private JPanel createSalesPanel() {
        JPanel salesPanel = new JPanel(new BorderLayout(10, 10));
        salesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Left side - Available Products
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        availableProductsModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        availableProductsTable = new JTable(availableProductsModel);
        refreshAvailableProductsTable();
        
        JScrollPane availableProductsScrollPane = new JScrollPane(availableProductsTable);
        availableProductsScrollPane.setBorder(new TitledBorder("Available Products"));
        
        JButton addToCartBtn = new JButton("Add to Cart");
        addToCartBtn.setBackground(new Color(0, 150, 0));
        addToCartBtn.setForeground(Color.WHITE);
        addToCartBtn.addActionListener(e -> addToCart());
        
        leftPanel.add(availableProductsScrollPane, BorderLayout.CENTER);
        leftPanel.add(addToCartBtn, BorderLayout.SOUTH);
        
        // Right side - Cart and Customer Selection
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        
        // Customer Selection
        JPanel customerSelectionPanel = new JPanel(new FlowLayout());
        customerSelectionPanel.setBorder(new TitledBorder("Select Customer"));
        customerComboBox = new JComboBox<>();
        refreshCustomerComboBox();
        customerSelectionPanel.add(new JLabel("Customer:"));
        customerSelectionPanel.add(customerComboBox);
        
        // Cart Table
        cartModel = new DefaultTableModel(new String[]{"Product", "Quantity", "Price", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only quantity column editable
            }
        };
        cartTable = new JTable(cartModel);
        cartTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 1) { // Quantity column changed
                updateCartQuantity(e.getFirstRow());
            }
        });
        
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setBorder(new TitledBorder("Shopping Cart"));
        
        // Cart Controls
        JPanel cartControlsPanel = new JPanel(new BorderLayout());
        
        JPanel cartButtonsPanel = new JPanel(new FlowLayout());
        JButton removeFromCartBtn = new JButton("Remove Selected");
        removeFromCartBtn.addActionListener(e -> removeFromCart());
        JButton clearCartBtn = new JButton("Clear Cart");
        clearCartBtn.addActionListener(e -> clearCart());
        
        cartButtonsPanel.add(removeFromCartBtn);
        cartButtonsPanel.add(clearCartBtn);
        
        // Total and Process Order
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton processOrderBtn = new JButton("Process Order");
        processOrderBtn.setBackground(new Color(0, 100, 200));
        processOrderBtn.setForeground(Color.WHITE);
        processOrderBtn.setFont(new Font("Arial", Font.BOLD, 14));
        processOrderBtn.addActionListener(e -> processOrder());
        
        totalPanel.add(totalLabel, BorderLayout.NORTH);
        totalPanel.add(processOrderBtn, BorderLayout.SOUTH);
        
        cartControlsPanel.add(cartButtonsPanel, BorderLayout.NORTH);
        cartControlsPanel.add(totalPanel, BorderLayout.SOUTH);
        
        rightPanel.add(customerSelectionPanel, BorderLayout.NORTH);
        rightPanel.add(cartScrollPane, BorderLayout.CENTER);
        rightPanel.add(cartControlsPanel, BorderLayout.SOUTH);
        
        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(600);
        
        salesPanel.add(splitPane, BorderLayout.CENTER);
        
        return salesPanel;
    }
    
    private JPanel createOrderHistoryPanel() {
        JPanel orderPanel = new JPanel(new BorderLayout(10, 10));
        orderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Order History Table
        orderHistoryModel = new DefaultTableModel(new String[]{"Order ID", "Customer", "Date", "Total", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderHistoryTable = new JTable(orderHistoryModel);
        refreshOrderHistoryTable();
        
        JScrollPane orderScrollPane = new JScrollPane(orderHistoryTable);
        orderScrollPane.setBorder(new TitledBorder("Order History"));
        
        orderPanel.add(orderScrollPane, BorderLayout.CENTER);
        
        return orderPanel;
    }
    
    // Event Handlers
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(((JPasswordField) passwordField).getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        currentLogin = new Login(username, password);
        if (currentLogin.login()) {
            showMainScreen();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performLogout() {
        if (currentLogin != null) {
            currentLogin.logout();
            currentLogin = null;
        }
        showLoginScreen();
    }
    
    private void showLoginScreen() {
        getContentPane().removeAll();
        add(loginPanel, BorderLayout.CENTER);
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
        revalidate();
        repaint();
    }
    
    private void showMainScreen() {
        getContentPane().removeAll();
        add(mainPanel, BorderLayout.CENTER);
        refreshAllTables();
        revalidate();
        repaint();
    }
    
    private void toggleProductSpecificFields() {
        boolean isMedicine = "Medicine".equals(productTypeCombo.getSelectedItem());
        prescriptionRequiredBox.setEnabled(isMedicine);
        skinTypeField.setEnabled(!isMedicine);
        
        if (isMedicine) {
            skinTypeField.setText("");
        } else {
            prescriptionRequiredBox.setSelected(false);
        }
    }
    
    private void addProduct() {
        try {
            String name = productNameField.getText().trim();
            double price = Double.parseDouble(productPriceField.getText().trim());
            int quantity = Integer.parseInt(productQuantityField.getText().trim());
            String type = (String) productTypeCombo.getSelectedItem();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter product name!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int newId = getNextProductId();
            Product product;
            
            if ("Medicine".equals(type)) {
                boolean prescriptionRequired = prescriptionRequiredBox.isSelected();
                product = new Medicine(prescriptionRequired, newId, name, price, quantity);
            } else {
                String skinType = skinTypeField.getText().trim();
                if (skinType.isEmpty()) skinType = "All";
                product = new Cosmetic(skinType, newId, name, price, quantity);
            }
            
            inventory.addProduct(product);
            refreshProductTable();
            refreshAvailableProductsTable();
            clearProductFields();
            
            JOptionPane.showMessageDialog(this, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and quantity!", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addCustomer() {
        String name = customerNameField.getText().trim();
        String phone = customerPhoneField.getText().trim();
        
        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both name and phone!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Customer.initializeLastId("customers.txt");
        Customer customer = new Customer(name, phone);
        customers.add(customer);
        customer.saveToFile("customers.txt");
        
        refreshCustomerTable();
        refreshCustomerComboBox();
        clearCustomerFields();
        
        JOptionPane.showMessageDialog(this, "Customer added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void addToCart() {
        int selectedRow = availableProductsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product!", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (Integer) availableProductsModel.getValueAt(selectedRow, 0);
        Product product = inventory.getProductById(productId);
        
        if (product == null || product.getQuantity() <= 0) {
            JOptionPane.showMessageDialog(this, "Product not available!", "Stock Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:", "1");
        if (quantityStr == null) return;
        
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (quantity > product.getQuantity()) {
                JOptionPane.showMessageDialog(this, "Not enough stock! Available: " + product.getQuantity(), "Stock Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Check if product already in cart
            OrderItem existingItem = null;
            for (OrderItem item : currentCart) {
                if (item.getProduct().getProductId() == productId) {
                    existingItem = item;
                    break;
                }
            }
            
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
            } else {
                currentCart.add(new OrderItem(product, quantity));
            }
            
            refreshCartTable();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number!", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove!", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        currentCart.remove(selectedRow);
        refreshCartTable();
    }
    
    private void clearCart() {
        currentCart.clear();
        refreshCartTable();
    }
    
private void updateCartQuantity(int row) {
        try {
            int newQuantity = Integer.parseInt(cartModel.getValueAt(row, 1).toString());
            if (newQuantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            OrderItem item = currentCart.get(row);
            Product product = item.getProduct();
            
            if (newQuantity > product.getQuantity()) {
                JOptionPane.showMessageDialog(this, "Not enough stock! Available: " + product.getQuantity(), "Stock Error", JOptionPane.WARNING_MESSAGE);
                cartModel.setValueAt(item.getQuantity(), row, 1); // restore old value
                return;
            }
            
            item.setQuantity(newQuantity);
            refreshCartTable();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number!", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void processOrder() {
        if (currentCart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Order Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Customer customer = (Customer) customerComboBox.getSelectedItem();
        if (customer == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer!", "Order Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create new order
        Order order = new Order(customer);
        orders.add(order);
        
        // Deduct stock
        for (OrderItem item : currentCart) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
        }
        
        // Clear cart
        currentCart.clear();
        refreshCartTable();
        refreshProductTable();
        refreshAvailableProductsTable();
        refreshOrderHistoryTable();
        
        JOptionPane.showMessageDialog(this, "Order processed successfully!\nOrder ID: " + order.getOrderId(), "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ===================== Refresh Helpers =====================
    private void refreshAllTables() {
        refreshProductTable();
        refreshCustomerTable();
        refreshAvailableProductsTable();
        refreshCartTable();
        refreshOrderHistoryTable();
        refreshCustomerComboBox();
    }
    
private void refreshProductTable() {
    productTableModel.setRowCount(0);
    for (Product p : inventory.getProducts()) {
        String special = "";
        
        if (p instanceof Medicine) {
            special = ((Medicine) p).isPrescriptionRequired() ? "Prescription" : "OTC";
        } else if (p instanceof Cosmetic) {
            special = ((Cosmetic) p).getSuitableForSkinType();
        } else {
            special = "N/A"; // في حالة مش Medicine ولا Cosmetic
        }

        productTableModel.addRow(new Object[]{
            p.getProductId(),
            p.getName(),
            p.getClass().getSimpleName(),
            p.getPrice(),
            p.getQuantity(),
            special
        });
    }
}
    
    private void refreshCustomerTable() {
        customerTableModel.setRowCount(0);
        for (Customer c : customers) {
            customerTableModel.addRow(new Object[]{c.getCustomerid(),c.getName(), c.getPhone()});
        }
    }
    
    private void refreshAvailableProductsTable() {
        availableProductsModel.setRowCount(0);
        for (Product p : inventory.getProducts()) {
            if (p.getQuantity() > 0) {
                availableProductsModel.addRow(new Object[]{
                    p.getProductId(), p.getName(), p.getPrice(), p.getQuantity()
                });
            }
        }
    }
    
    private void refreshCartTable() {
        cartModel.setRowCount(0);
        double total = 0;
        for (OrderItem item : currentCart) {
            double subtotal = item.getQuantity() * item.getProduct().getPrice();
            cartModel.addRow(new Object[]{
                item.getProduct().getName(), item.getQuantity(), 
                item.getProduct().getPrice(), subtotal
            });
            total += subtotal;
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }
    
   private void refreshOrderHistoryTable() {
    // مسح البيانات القديمة
    orderHistoryModel.setRowCount(0);

    // التأكد إن فيه أوردرات
    if (orders == null || orders.isEmpty()) {
        return;
    }

    // إضافة كل الأوردرات
    for (Order o : orders) {
        String customerName = (o.getCustomer() != null) ? o.getCustomer().getName() : "Unknown";
        String orderDate = (o.getOrderDate() != null) ? o.getOrderDate().toString() : "N/A";
        String totalAmount = String.format("%.2f", o.getTotalAmount());
        String status = (o.getStatus() != null) ? o.getStatus() : "Pending";

        orderHistoryModel.addRow(new Object[]{
            o.getOrderId(),
            customerName,
            orderDate,
            totalAmount,
            status
        });
    }
}
    
    private void refreshCustomerComboBox() {
        customerComboBox.removeAllItems();
        for (Customer c : customers) {
            customerComboBox.addItem(c);
        }
    }
    
    // ===================== Utility Methods =====================
    private int getNextProductId() {
        int maxId = 0;
        for (Product p : inventory.getProducts()) {
            if (p.getProductId() > maxId) {
                maxId = p.getProductId();
            }
        }
        return maxId + 1;
    }
    
    private int getLowStockCount() {
        int count = 0;
        for (Product p : inventory.getProducts()) {
            if (p.getQuantity() < 10) {
                count++;
            }
        }
        return count;
    }
    
    private void clearProductFields() {
        productNameField.setText("");
        productPriceField.setText("");
        productQuantityField.setText("");
        prescriptionRequiredBox.setSelected(false);
        skinTypeField.setText("");
        productTypeCombo.setSelectedIndex(0);
    }
    
    private void clearCustomerFields() {
        customerNameField.setText("");
        customerPhoneField.setText("");
    }
    
    // ===================== Main Method =====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PharmacyGUI().setVisible(true);
        });
    }
}