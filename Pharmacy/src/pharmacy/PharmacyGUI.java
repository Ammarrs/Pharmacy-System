package pharmacy;
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
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Enhanced Pharmacy Management System GUI with Morning/Evening Shift Management
 * @author PharmacySystem
 */
public class PharmacyGUI extends JFrame {
    
    // Shift Types
    public enum ShiftType {
        MORNING("Morning Shift", "morning"),
        EVENING("Evening Shift", "evening");
        
        private final String displayName;
        private final String fileName;
        
        ShiftType(String displayName, String fileName) {
            this.displayName = displayName;
            this.fileName = fileName;
        }
        
        public String getDisplayName() { return displayName; }
        public String getFileName() { return fileName; }
    }
    
    // Core components
    private Login currentLogin;
    private Inventory inventory;
    private List<Customer> customers;
    private List<Order> orders;
    private Order currentOrder;
    private Product currentEditingProduct;
    // GUI Components
    private JTabbedPane mainTabbedPane;
    private JPanel loginPanel, mainPanel;
    private JTextField usernameField, passwordField;
    private JButton loginButton, logoutButton;
    private JButton endShiftButton;
    private JLabel headerLabel; // Make it a field for dynamic updates
    
    // Shift management
    private ShiftType currentShift = ShiftType.MORNING;
    private List<Order> allHistoricalOrders;
    private Date shiftStartTime;
    private Map<String, String> userCredentials; // username -> password
    private static final String USERS_FILE = "users.txt";
    private static final String SHIFT_STATE_FILE = "current_shift.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // Dashboard Components - Added for dynamic refresh
    private JLabel totalProductsLabel, totalCustomersLabel, ordersTodayLabel, lowStockLabel;
    
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
        initializeUserCredentials();
        initializeData();
        initializeGUI();
        showLoginScreen();
    }
    
    /**
     * Initialize user credentials from file
     */
    private void initializeUserCredentials() {
        userCredentials = new HashMap<>();
        
        // Load users from file
        loadUsersFromFile();
        
        // If no users exist, create default users
        if (userCredentials.isEmpty()) {
            System.out.println("No users found in users.txt file");
        }
    }
    
    /**
     * Load users from users.txt file
     */
    private void loadUsersFromFile() {
        File usersFile = new File(USERS_FILE);
        if (usersFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue; // Skip empty lines and comments
                    
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) {
                        userCredentials.put(parts[0].trim(), parts[1].trim());
                    }
                }
                System.out.println("Loaded " + userCredentials.size() + " users from file");
            } catch (IOException e) {
                System.err.println("Error loading users from file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Create default users
     */
    private void createDefaultUsers() {
        userCredentials.put("morning_user", "morning123");
        userCredentials.put("evening_user", "evening123");
        userCredentials.put("admin", "admin123");
        System.out.println("Created default users");
    }
    
    /**
     * Save users to file
     */
    private void saveUsersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }
            
            System.out.println("Saved " + userCredentials.size() + " users to file");
        } catch (IOException e) {
            System.err.println("Error saving users to file: " + e.getMessage());
        }
    }
    
    /**
     * Determine shift type based on username
     */
    private ShiftType determineShiftFromUsername(String username) {
        if (username.toLowerCase().contains("evening")) {
            return ShiftType.EVENING;
        } else if (username.toLowerCase().contains("morning")) {
            return ShiftType.MORNING;
        } else {
            // For admin or other users, determine based on current time
            int hour = new Date().getHours();
            return (hour >= 6 && hour < 18) ? ShiftType.MORNING : ShiftType.EVENING;
        }
    }
    
    /**
     * Load current shift state from file
     */
    private void loadShiftState() {
        File shiftFile = new File(SHIFT_STATE_FILE);
        if (shiftFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(shiftFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("SHIFT_TYPE=")) {
                        String shiftTypeStr = line.substring("SHIFT_TYPE=".length());
                        try {
                            currentShift = ShiftType.valueOf(shiftTypeStr);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid shift type in file: " + shiftTypeStr);
                            currentShift = ShiftType.MORNING;
                        }
                    } else if (line.startsWith("SHIFT_START_TIME=")) {
                        String timeStr = line.substring("SHIFT_START_TIME=".length());
                        try {
                            shiftStartTime = DATE_FORMAT.parse(timeStr);
                        } catch (ParseException e) {
                            System.err.println("Error parsing shift start time: " + e.getMessage());
                            shiftStartTime = new Date();
                        }
                    }
                }
                System.out.println("Loaded existing shift state: " + currentShift.getDisplayName() + 
                                 ", Started: " + shiftStartTime);
            } catch (IOException e) {
                System.err.println("Error loading shift state: " + e.getMessage());
                initializeNewShift();
            }
        } else {
            initializeNewShift();
        }
    }
    
    /**
     * Save current shift state to file
     */
    private void saveShiftState() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SHIFT_STATE_FILE))) {
            writer.println("SHIFT_TYPE=" + currentShift.name());
            writer.println("SHIFT_START_TIME=" + DATE_FORMAT.format(shiftStartTime));
            System.out.println("Saved shift state: " + currentShift.getDisplayName());
        } catch (IOException e) {
            System.err.println("Error saving shift state: " + e.getMessage());
        }
    }
    
    /**
     * Initialize a new shift
     */
    private void initializeNewShift() {
        currentShift = ShiftType.MORNING;
        shiftStartTime = new Date();
        saveShiftState();
    }
    
    /**
     * Load current shift orders
     */
    private void loadCurrentShiftOrders() {
        // Load all orders from file
        List<Order> allOrders = Order.loadOrdersFromFile(customers);
        allHistoricalOrders = new ArrayList<>(allOrders);
        
        // Filter orders for current shift by loading shift-specific order file
        String shiftOrdersFile = currentShift.getFileName() + "_shift_orders.txt";
        File file = new File(shiftOrdersFile);
        
        orders = new ArrayList<>();
        
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("ORDER_ID=")) {
                        int orderId = Integer.parseInt(line.substring("ORDER_ID=".length()));
                        // Find this order in all historical orders and add to current shift
                        for (Order order : allHistoricalOrders) {
                            if (order.getOrderId() == orderId) {
                                orders.add(order);
                                break;
                            }
                        }
                    }
                }
                System.out.println("Loaded " + orders.size() + " orders for " + currentShift.getDisplayName());
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading current shift orders: " + e.getMessage());
            }
        }
    }
    
    /**
     * Save current shift orders to shift-specific file
     */
    private void saveCurrentShiftOrders() {
        String shiftOrdersFile = currentShift.getFileName() + "_shift_orders.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(shiftOrdersFile))) {
            for (Order order : orders) {
                writer.println("ORDER_ID=" + order.getOrderId());
            }
            System.out.println("Saved " + orders.size() + " orders for " + currentShift.getDisplayName());
        } catch (IOException e) {
            System.err.println("Error saving current shift orders: " + e.getMessage());
        }
    }
    
    private void initializeShiftData() {
    File shiftStateFile = new File(SHIFT_STATE_FILE);
    
    if (shiftStateFile.exists()) {
        // Load existing shift state
        loadShiftState();
    } else {
        // Initialize with Morning shift as default
        currentShift = ShiftType.MORNING;
        shiftStartTime = new Date();
        saveShiftState();
    }
    
    // Load current shift orders
    loadCurrentShiftOrders();
    
    System.out.println(currentShift.getDisplayName() + " initialized with " + 
                     orders.size() + " orders");
}
    private void initializeData() {
        // Initialize core data structures
        inventory = new Inventory();
        currentCart = new ArrayList<>();
        // Load existing customers from file
        Customer.initializeLastId("customers.txt");
        customers = Customer.loadCustomersFromFile("customers.txt");
        
        // Initialize shift data (this will load current shift state and orders)
        initializeShiftData();
        
        // Add some sample data if files are empty
        if (customers.isEmpty()) {
            customers.add(new Customer("Farida", "01012345678"));
            customers.add(new Customer("Haneen", "01012345679"));
            customers.add(new Customer("Ahmed", "01012345680"));
            // Save sample customers to file
            for (Customer c : customers) {
                c.saveToFile("customers.txt");
            }
        }
        // Only initialize sample inventory if completely empty AND no file exists
        if (inventory.getProducts().isEmpty()) {
            File inventoryFile = new File("inventory.txt");
            if (!inventoryFile.exists()) {
                inventory.addProduct(new Medicine(false, 1, "Panadol", 15.50, 100));
                inventory.addProduct(new Medicine(true, 2, "Insulin", 120.00, 25));
                inventory.addProduct(new Cosmetic("Normal", 3, "Face Cream", 45.00, 30));
                inventory.addProduct(new Medicine(false, 4, "Aspirin", 12.00, 75));
            }
        }
    }
    
    private void initializeGUI() {
        setTitle("Pharmacy Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Add window closing listener to save shift state
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Save current shift state before closing
                saveShiftState();
                saveCurrentShiftOrders();
                System.exit(0);
            }
        });
        
        // Create main container
        setLayout(new BorderLayout());
        
        createLoginPanel();
        createMainPanel();
        
        // Initially show login panel
        add(loginPanel, BorderLayout.CENTER);
    }
    
    private void createLoginPanel() {
    loginPanel = new JPanel(new GridBagLayout());
    loginPanel.setBackground(new Color(170, 200, 225));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    
    // Title
    ImageIcon originalIcon = new ImageIcon(getClass().getResource("/Pharmacy/f1.png"));
    Image scaledImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
    ImageIcon scaledIcon = new ImageIcon(scaledImage);
    JLabel titleLabel = new JLabel("Pharmacy Management System", scaledIcon, JLabel.CENTER);
    titleLabel.setFont(new Font("Palatino Linotype", Font.BOLD, 40));
    titleLabel.setForeground(new Color(50, 50, 225));
    titleLabel.setHorizontalTextPosition(JLabel.CENTER);
    titleLabel.setVerticalTextPosition(JLabel.BOTTOM);
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
    loginPanel.add(titleLabel, gbc);
    
    // Current shift indicator - always show the current shift
    JLabel shiftIndicatorLabel = new JLabel("Current Shift: " + currentShift.getDisplayName(), JLabel.CENTER);
    shiftIndicatorLabel.setFont(new Font("Arial", Font.BOLD, 18));
    shiftIndicatorLabel.setForeground(new Color(50, 50, 225));
    gbc.gridy = 1;
    loginPanel.add(shiftIndicatorLabel, gbc);
    
    // Username
    gbc.gridwidth = 1; gbc.gridy = 2;
    JLabel usernameLabel = new JLabel("Username :");
    usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
    usernameLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Pharmacy/user1.png")));
    loginPanel.add(usernameLabel, gbc);
    gbc.gridx = 1;
    usernameField = new JTextField(15);
    loginPanel.add(usernameField, gbc);
    
    // Password
    gbc.gridx = 0; gbc.gridy = 3;
    JLabel passwordLabel = new JLabel("Password :");
    passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
    passwordLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Pharmacy/lock.png")));
    loginPanel.add(passwordLabel, gbc);
    gbc.gridx = 1;
    passwordField = new JPasswordField(15);
    loginPanel.add(passwordField, gbc);
    
    // Login Button
    gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
    loginButton = new JButton("Login");
    loginButton.setBackground(new Color(0, 0, 139));
    loginButton.setForeground(Color.WHITE);
    loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
    loginButton.addActionListener(e -> performLogin());
    loginPanel.add(loginButton, gbc);
    
    // Enter key support
    getRootPane().setDefaultButton(loginButton);
}
    private void endCurrentShift() {
    // Determine the NEXT shift (opposite of current)
    ShiftType nextShift = (currentShift == ShiftType.MORNING) ? ShiftType.EVENING : ShiftType.MORNING;
    
    // Show confirmation dialog
    int choice = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to end " + currentShift.getDisplayName() + "?\n\n" +
        "This will:\n" +
        "• Save current shift data\n" +
        "• Switch to " + nextShift.getDisplayName() + "\n" +
        "• Require re-login for the new shift\n" +
        "• Clear current shift orders from display\n\n" +
        "Current shift has " + orders.size() + " orders",
        "End Shift Confirmation",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );
    
    if (choice != JOptionPane.YES_OPTION) {
        return;
    }
    
    try {
        // 1. Save current shift summary
        saveShiftSummary();
        
        // 2. Add current shift orders to all historical orders (if not already there)
        for (Order order : orders) {
            if (!allHistoricalOrders.contains(order)) {
                allHistoricalOrders.add(order);
            }
        }
        
        // 3. Archive current shift orders file
        String currentShiftFile = currentShift.getFileName() + "_shift_orders.txt";
        String archivedShiftFile = "archived_" + currentShift.getFileName() + "_" + 
                                 DATE_FORMAT.format(shiftStartTime).replace(":", "-").replace(" ", "_") + "_orders.txt";
        File currentFile = new File(currentShiftFile);
        if (currentFile.exists()) {
            currentFile.renameTo(new File(archivedShiftFile));
        }
        
        // 4. Switch to next shift - THIS IS THE KEY FIX
        currentShift = nextShift;
        shiftStartTime = new Date();
        
        // 5. Clear orders for new shift and load any existing orders for the new shift
        orders.clear();
        loadCurrentShiftOrders(); // Load orders for the new shift (if any)
        
        // 6. Save new shift state
        saveShiftState();
        saveCurrentShiftOrders();
        
        // 7. Clear current cart if any
        currentCart.clear();
        
        // 8. Update UI to reflect new shift
        updateHeaderLabel();
        updateEndShiftButtonText();
        
        // 9. Show success message
        JOptionPane.showMessageDialog(
            this,
            "Shift switched successfully!\n\n" +
            "Switched from " + ((nextShift == ShiftType.MORNING) ? "Evening" : "Morning") + " to " + nextShift.getDisplayName() + "\n" +
            "Please log in again for the new shift",
            "Shift Changed",
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // 10. Force logout and return to login screen
        performLogout();
        
        System.out.println("Switched to " + currentShift.getDisplayName() + " at: " + shiftStartTime);
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(
            this,
            "Error switching shift: " + e.getMessage(),
            "Shift Error",
            JOptionPane.ERROR_MESSAGE
        );
        e.printStackTrace();
    }
}
    // Add method to save shift summary:
    private void saveShiftSummary() {
        try {
            File file = new File("shift_summaries.txt");
            FileWriter writer = new FileWriter(file, true); // Append mode
            PrintWriter printWriter = new PrintWriter(writer);
            
            // Calculate shift totals
            double shiftTotal = 0;
            int totalItems = 0;
            
            for (Order order : orders) {
                shiftTotal += order.getTotalAmount();
                totalItems += order.getItems().size();
            }
            
            // Write shift summary
            printWriter.println("=== " + currentShift.getDisplayName().toUpperCase() + " SUMMARY ===");
            printWriter.println("Start Time: " + shiftStartTime);
            printWriter.println("End Time: " + new Date());
            printWriter.println("Total Orders: " + orders.size());
            printWriter.println("Total Items Sold: " + totalItems);
            printWriter.println("Total Revenue: $" + String.format("%.2f", shiftTotal));
            printWriter.println("Cashier: " + (currentLogin != null ? currentLogin.getUsername() : "Unknown"));
            printWriter.println("Orders in this shift:");
            
            for (Order order : orders) {
                printWriter.println("  - Order #" + order.getOrderId() + 
                                 " | Customer: " + order.getCustomer().getName() + 
                                 " | Total: $" + String.format("%.2f", order.getTotalAmount()));
            }
            
            printWriter.println("=====================================");
            printWriter.println(); // Empty line
            
            printWriter.close();
            writer.close();
            
        } catch (IOException e) {
            System.err.println("Error saving shift summary: " + e.getMessage());
        }
    }
    
    // Update header with shift information
    private void updateHeaderLabel() {
        if (headerLabel != null) {
            headerLabel.setText("Pharmacy Management System - " + currentShift.getDisplayName());
        }
    }
    
   private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(170, 200, 225));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Left side - Logo and title with shift info
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/Pharmacy/f1.png"));
        Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        headerLabel = new JLabel("Pharmacy Management System - " + currentShift.getDisplayName(), 
                                scaledIcon, JLabel.CENTER);
        headerLabel.setFont(new Font("Lucida Sans", Font.BOLD, 20));
        headerLabel.setForeground(new Color(50, 50, 225));
        
        // Right side - Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(170, 200, 225));
        
        // End Shift Button
        endShiftButton = new JButton();
        updateEndShiftButtonText(); // Set initial text
        endShiftButton.setBackground(new Color(255, 140, 0)); // Orange color
        endShiftButton.setForeground(Color.WHITE);
        endShiftButton.setFont(new Font("Arial", Font.BOLD, 12));
        endShiftButton.addActionListener(e -> endCurrentShift());
        
        // Logout Button
        logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(200, 50, 50));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(e -> performLogout());
        
        buttonPanel.add(endShiftButton);
        buttonPanel.add(logoutButton);
        
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Rest of the method remains the same...
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.addTab("Sales", createSalesPanel());
        mainTabbedPane.addTab("Customers", createCustomerPanel());
        mainTabbedPane.addTab("Products", createProductPanel());
        mainTabbedPane.addTab("Order History", createOrderHistoryPanel());
        mainTabbedPane.addTab("Dashboard", createDashboardPanel());
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
        mainPanel.setBackground(new Color(170, 200, 225));
    }
    
    private JPanel createDashboardPanel() {
        JPanel dashboard = new JPanel(new GridLayout(2, 2, 10, 10));
        dashboard.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        dashboard.setBackground(new Color(170, 200, 225));
        
        // Create statistics cards with stored label references for dynamic updates
        dashboard.add(createStatCardWithReference("Total Products", 
            String.valueOf(inventory.getProducts().size()), 
            new Color(52, 152, 219), 
            totalProductsLabel = new JLabel()));
            
        dashboard.add(createStatCardWithReference("Total Customers", 
            String.valueOf(customers.size()), 
            new Color(46, 204, 113), 
            totalCustomersLabel = new JLabel()));
            
        dashboard.add(createStatCardWithReference("Orders This Shift", 
            String.valueOf(orders.size()), 
            new Color(155, 89, 182), 
            ordersTodayLabel = new JLabel()));
            
        dashboard.add(createStatCardWithReference("Low Stock Items", 
            String.valueOf(getLowStockCount()), 
            new Color(231, 76, 60), 
            lowStockLabel = new JLabel()));
        
        return dashboard;
    }
    
    private JPanel createStatCardWithReference(String title, String value, Color color, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(color);
        
        valueLabel.setText(value);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 36));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createProductPanel() {
        JPanel productPanel = new JPanel(new BorderLayout(10, 10));
        productPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        productPanel.setBackground(new Color(170, 200, 225));
                
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(170, 200, 225));
        searchPanel.add(new JLabel("Search Products:"));
        JTextField productSearchField = new JTextField(20);
        searchPanel.add(productSearchField);
        // Product Table
        productTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Type", "Price", "Quantity", "Special"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setBackground(new Color(200, 220, 255));
        productTable.setForeground(Color.BLACK);
        productTable.setGridColor(new Color(120, 150, 200));
        productTable.getTableHeader().setBackground(new Color(70, 130, 180));
        productTable.getTableHeader().setForeground(Color.BLACK);
        JScrollPane productScrollPane = new JScrollPane(productTable);
        productScrollPane.getViewport().setBackground(new Color(200, 220, 255));
        productScrollPane.setBorder(BorderFactory.createTitledBorder("Product Inventory"));
        refreshProductTable();
        // Add search functionality
        productSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterProducts(); }
            public void filterProducts() {
                String searchText = productSearchField.getText().toLowerCase();
                productTableModel.setRowCount(0);
                for (Product p : inventory.getProducts()) {
                    if (p.getName().toLowerCase().startsWith(searchText)) {
                        String special = "";
                        if (p instanceof Medicine) {
                            Medicine med = (Medicine) p;
                            special = med.isPrescriptionRequired() ? "Prescription Required" : "OTC";
                        } else if (p instanceof Cosmetic) {
                            Cosmetic cos = (Cosmetic) p;
                            special = "Skin: " + cos.getSuitableForSkinType();
                        } else {
                            special = "N/A";
                        }
                        productTableModel.addRow(new Object[]{
                            p.getProductId(), p.getName(), p.getClass().getSimpleName(),
                            String.format("$%.2f", p.getPrice()), p.getQuantity(), special
                        });
                    }
                }
            }
        });
        // Product Form Panel
        JPanel productFormPanel = createProductFormPanel();
        // Combine search and form panels
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(productFormPanel, BorderLayout.CENTER);
        productPanel.add(topPanel, BorderLayout.NORTH);
        productPanel.add(productScrollPane, BorderLayout.CENTER);
        return productPanel;
    }
    
private JPanel createProductFormPanel() {
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(new TitledBorder("Add/Update Product"));
    formPanel.setBackground(new Color(170, 200, 225));
    
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
    
    // Buttons Row 1 - Add and Update buttons
    gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
    JButton addProductBtn = new JButton("Add Product");
    addProductBtn.setBackground(new Color(0, 150, 0));
    addProductBtn.setForeground(Color.WHITE);
    addProductBtn.addActionListener(e -> addProduct());
    formPanel.add(addProductBtn, gbc);
    
    gbc.gridx = 1;
    JButton updateProductBtn = new JButton("Update Product");
    updateProductBtn.setBackground(new Color(255, 140, 0)); // Orange color
    updateProductBtn.setForeground(Color.WHITE);
    updateProductBtn.addActionListener(e -> updateProduct());
    formPanel.add(updateProductBtn, gbc);
    
    gbc.gridx = 2; gbc.gridwidth = 2;
    JButton clearBtn = new JButton("Clear Fields");
    clearBtn.addActionListener(e -> clearProductFields());
    formPanel.add(clearBtn, gbc);
    
    // Buttons Row 2 - Delete and Load functionality
    gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
    JButton deleteProductBtn = new JButton("Delete Selected Product");
    deleteProductBtn.setBackground(new Color(200, 50, 50));
    deleteProductBtn.setForeground(Color.WHITE);
    deleteProductBtn.addActionListener(e -> deleteSelectedProduct());
    formPanel.add(deleteProductBtn, gbc);
    
    gbc.gridx = 2; gbc.gridwidth = 2;
    JButton loadProductBtn = new JButton("Load Selected to Form");
    loadProductBtn.setBackground(new Color(70, 130, 180));
    loadProductBtn.setForeground(Color.WHITE);
    loadProductBtn.addActionListener(e -> loadSelectedProductToForm());
    formPanel.add(loadProductBtn, gbc);
    
    toggleProductSpecificFields();
    return formPanel;
}
private void updateProduct() {
    if (currentEditingProduct == null) {
        JOptionPane.showMessageDialog(this, 
            "No product loaded for editing!\n\n" +
            "To update a product:\n" +
            "1. Select a product from the table\n" +
            "2. Click 'Load Selected to Form'\n" +
            "3. Modify the fields as needed\n" +
            "4. Click 'Update Product'", 
            "Update Error", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    try {
        String name = productNameField.getText().trim();
        double price = Double.parseDouble(productPriceField.getText().trim());
        int quantity = Integer.parseInt(productQuantityField.getText().trim());
        String type = (String) productTypeCombo.getSelectedItem();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter product name!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (price <= 0) {
            JOptionPane.showMessageDialog(this, "Price must be greater than zero!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (quantity < 0) {
            JOptionPane.showMessageDialog(this, "Quantity cannot be negative!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if the product type matches
        boolean typeMatches = (type.equals("Medicine") && currentEditingProduct instanceof Medicine) ||
                             (type.equals("Cosmetic") && currentEditingProduct instanceof Cosmetic);
        
        if (!typeMatches) {
            JOptionPane.showMessageDialog(this, 
                "Cannot change product type!\n\n" +
                "Current product is: " + currentEditingProduct.getClass().getSimpleName() + "\n" +
                "Selected type: " + type + "\n\n" +
                "To change type, delete this product and create a new one.", 
                "Type Change Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Store old values for comparison
        String oldName = currentEditingProduct.getName();
        double oldPrice = currentEditingProduct.getPrice();
        int oldQuantity = currentEditingProduct.getQuantity();
        
        // Update basic product properties
        currentEditingProduct.setName(name);
        currentEditingProduct.setPrice(price);
        currentEditingProduct.setQuantity(quantity);
        
        // Update type-specific properties
        if (currentEditingProduct instanceof Medicine) {
            Medicine medicine = (Medicine) currentEditingProduct;
            boolean oldPrescriptionReq = medicine.isPrescriptionRequired();
            medicine.setPrescriptionRequired(prescriptionRequiredBox.isSelected());
            System.out.println("Updated Medicine - Prescription required: " + prescriptionRequiredBox.isSelected());
        } else if (currentEditingProduct instanceof Cosmetic) {
            Cosmetic cosmetic = (Cosmetic) currentEditingProduct;
            String skinType = skinTypeField.getText().trim();
            if (skinType.isEmpty()) skinType = "All";
            String oldSkinType = cosmetic.getSuitableForSkinType();
            cosmetic.setSuitableForSkinType(skinType);
            System.out.println("Updated Cosmetic - Skin type: " + skinType);
        }
        
        // Save to file
        inventory.saveToFile();
        
        // Refresh all tables and dashboard
        refreshProductTable();
        refreshAvailableProductsTable();
        refreshDashboard();
        
        // Store product ID before clearing (to avoid null pointer exception)
        int productId = currentEditingProduct.getProductId();
        
        // Clear form and editing state
        clearProductFields();
        
        JOptionPane.showMessageDialog(this, 
            "Product updated successfully!\n\n" +
            "Product ID: " + productId + "\n" +
            "Name: " + name + " (was: " + oldName + ")\n" +
            "Price: $" + String.format("%.2f", price) + " (was: $" + String.format("%.2f", oldPrice) + ")\n" +
            "Quantity: " + quantity + " (was: " + oldQuantity + ")", 
            "Update Successful", 
            JOptionPane.INFORMATION_MESSAGE);
        
        System.out.println("Product updated successfully: ID=" + productId + ", Name=" + name);
        
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, 
            "Please enter valid numbers for price and quantity!\n\n" +
            "Price: Must be a decimal number (e.g., 15.50)\n" +
            "Quantity: Must be a whole number (e.g., 100)", 
            "Input Error", 
            JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, 
            "Error updating product: " + e.getMessage(), 
            "Update Error", 
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

// Add this new method to handle product deletion:
private void deleteSelectedProduct() {
    int selectedRow = productTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, 
            "Please select a product to delete!", 
            "Selection Error", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Get the product ID from the selected row
    int productId = (Integer) productTableModel.getValueAt(selectedRow, 0);
    String productName = (String) productTableModel.getValueAt(selectedRow, 1);
    
    // Find the product in inventory
    Product productToDelete = inventory.getProductById(productId);
    if (productToDelete == null) {
        JOptionPane.showMessageDialog(this, 
            "Product not found in inventory!", 
            "Delete Error", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Confirm deletion
    int choice = JOptionPane.showConfirmDialog(this,
        "Are you sure you want to delete this product?\n\n" +
        "Product: " + productName + "\n" +
        "ID: " + productId + "\n" +
        "Quantity: " + productToDelete.getQuantity() + "\n" +
        "Price: $" + String.format("%.2f", productToDelete.getPrice()) + "\n\n" +
        "This action cannot be undone!",
        "Confirm Product Deletion",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
    
    if (choice == JOptionPane.YES_OPTION) {
        try {
            // Remove product from inventory
            boolean removed = inventory.removeProduct(productId);
            
            if (removed) {
                // Save updated inventory to file
                inventory.saveToFile();
                
                // Refresh all relevant tables and UI
                refreshProductTable();
                refreshAvailableProductsTable();
                refreshDashboard();
                
                // Clear form fields
                clearProductFields();
                
                // Show success message
                JOptionPane.showMessageDialog(this, 
                    "Product '" + productName + "' deleted successfully!", 
                    "Delete Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                System.out.println("Product deleted: ID=" + productId + ", Name=" + productName);
                
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to delete product from inventory!", 
                    "Delete Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error deleting product: " + e.getMessage(), 
                "Delete Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
private void loadSelectedProductToForm() {
    int selectedRow = productTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, 
            "Please select a product to load!", 
            "Selection Error", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Get product data from table
    int productId = (Integer) productTableModel.getValueAt(selectedRow, 0);
    Product product = inventory.getProductById(productId);
    
    if (product != null) {
        // THIS IS THE KEY FIX: Set the currentEditingProduct
        currentEditingProduct = product;
        
        // Load basic product data
        productNameField.setText(product.getName());
        productPriceField.setText(String.valueOf(product.getPrice()));
        productQuantityField.setText(String.valueOf(product.getQuantity()));
        
        // Load type-specific data
        if (product instanceof Medicine) {
            productTypeCombo.setSelectedItem("Medicine");
            Medicine medicine = (Medicine) product;
            prescriptionRequiredBox.setSelected(medicine.isPrescriptionRequired());
            skinTypeField.setText("");
        } else if (product instanceof Cosmetic) {
            productTypeCombo.setSelectedItem("Cosmetic");
            Cosmetic cosmetic = (Cosmetic) product;
            skinTypeField.setText(cosmetic.getSuitableForSkinType());
            prescriptionRequiredBox.setSelected(false);
        }
        
        // Toggle fields based on type
        toggleProductSpecificFields();
        
        JOptionPane.showMessageDialog(this, 
            "Product data loaded into form!\n" +
            "Product: " + product.getName() + " (ID: " + product.getProductId() + ")\n" +
            "You can now modify and click 'Update Product'.", 
            "Data Loaded for Editing", 
            JOptionPane.INFORMATION_MESSAGE);
            
        System.out.println("Loaded product for editing: ID=" + product.getProductId() + ", Name=" + product.getName());
    } else {
        JOptionPane.showMessageDialog(this, 
            "Product not found in inventory!", 
            "Load Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}
    private JPanel createCustomerPanel() {
    JPanel customerPanel = new JPanel(new BorderLayout(10, 10));
    customerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    customerPanel.setBackground(new Color(170, 200, 225));
    
    // Customer Table
    customerTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Phone"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    customerTable = new JTable(customerTableModel);
    customerTable.setBackground(new Color(200, 220, 255));
    customerTable.setForeground(Color.BLACK);
    customerTable.setGridColor(new Color(120, 150, 200));
    customerTable.getTableHeader().setBackground(new Color(70, 130, 180));
    customerTable.getTableHeader().setForeground(Color.BLACK);
    JScrollPane customerScrollPane = new JScrollPane(customerTable);
    customerScrollPane.getViewport().setBackground(new Color(200, 220, 255));
    customerPanel.setBackground(new Color(180, 200, 240));
    customerPanel.setBorder(BorderFactory.createTitledBorder("Customer List"));
    customerPanel.add(customerScrollPane, BorderLayout.CENTER);
    refreshCustomerTable();
    
    // Customer Form
    JPanel customerFormPanel = createCustomerFormPanel();
    customerPanel.add(customerFormPanel, BorderLayout.NORTH);
    customerPanel.add(customerScrollPane, BorderLayout.CENTER);
    
    return customerPanel;
}
    
    private JPanel createCustomerFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Add Customer"));
        formPanel.setBackground(new Color(170, 200, 225));
        
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
        salesPanel.setBackground(new Color(170, 200, 225));
        // Left side - Available Products
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(170, 200, 225));
        // Search Panel for products
        JPanel productSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productSearchPanel.setBackground(new Color(170, 200, 225));
        productSearchPanel.add(new JLabel("Search Products:"));
        JTextField salesProductSearchField = new JTextField(20);
        productSearchPanel.add(salesProductSearchField);
        availableProductsModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        availableProductsTable = new JTable(availableProductsModel);
        availableProductsTable.setBackground(new Color(200, 220, 255));
        availableProductsTable.setForeground(Color.BLACK);
        availableProductsTable.setGridColor(new Color(120, 150, 200));
        availableProductsTable.getTableHeader().setBackground(new Color(70, 130, 180));
        availableProductsTable.getTableHeader().setForeground(Color.BLACK);
        refreshAvailableProductsTable();
        // Add search functionality for sales products
        salesProductSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterSalesProducts(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterSalesProducts(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterSalesProducts(); }
            public void filterSalesProducts() {
                String searchText = salesProductSearchField.getText().toLowerCase();
                availableProductsModel.setRowCount(0);
                for (Product p : inventory.getProducts()) {
                    if (p.getQuantity() > 0 && p.getName().toLowerCase().startsWith(searchText)) {
                        availableProductsModel.addRow(new Object[]{
                            p.getProductId(), p.getName(), 
                            String.format("$%.2f", p.getPrice()), p.getQuantity()
                        });
                    }
                }
            }
        });
        JScrollPane availableProductsScrollPane = new JScrollPane(availableProductsTable);
        availableProductsScrollPane.getViewport().setBackground(new Color(200, 220, 255));
        availableProductsScrollPane.setBorder(new TitledBorder("Available Products"));
        leftPanel.add(productSearchPanel, BorderLayout.NORTH);
            JButton addToCartBtn = new JButton("Add to Cart");
            addToCartBtn.setBackground(new Color(0, 150, 0));
            addToCartBtn.setForeground(Color.WHITE);
            addToCartBtn.addActionListener(e -> addToCart());
            leftPanel.add(availableProductsScrollPane, BorderLayout.CENTER);
            leftPanel.add(addToCartBtn, BorderLayout.SOUTH);
            // Right side - Cart and Customer Selection
            JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
            rightPanel.setBackground(new Color(170, 200, 225));
            // Customer Selection
            JPanel customerSelectionPanel = new JPanel(new FlowLayout());
            customerSelectionPanel.setBorder(new TitledBorder("Select Customer"));
            customerSelectionPanel.setBackground(new Color(170, 200, 225));
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
        // تنسيق جدول الكارت
        cartTable.setBackground(new Color(200, 220, 255));
        cartTable.setForeground(Color.BLACK);
        cartTable.setGridColor(new Color(120, 150, 200));
        cartTable.getTableHeader().setBackground(new Color(70, 130, 180));
        cartTable.getTableHeader().setForeground(Color.BLACK);
        cartTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 1) { // Quantity column changed
                updateCartQuantity(e.getFirstRow());
            }
        });
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.getViewport().setBackground(new Color(200, 220, 255));
        cartScrollPane.setBorder(new TitledBorder("Shopping Cart"));
        // Cart Controls
        JPanel cartControlsPanel = new JPanel(new BorderLayout());
        JPanel cartButtonsPanel = new JPanel(new FlowLayout());
        cartButtonsPanel.setBackground(new Color(170, 200, 225));
        JButton removeFromCartBtn = new JButton("Remove Selected");
        removeFromCartBtn.addActionListener(e -> removeFromCart());
        JButton clearCartBtn = new JButton("Clear Cart");
        clearCartBtn.addActionListener(e -> clearCart());
        cartButtonsPanel.add(removeFromCartBtn);
        cartButtonsPanel.add(clearCartBtn);
            // Total and Process Order
            JPanel totalPanel = new JPanel(new BorderLayout());
            totalPanel.setBackground(new Color(170, 200, 225));
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
        orderPanel.setBackground(new Color(170, 200, 225));
        // Order History Table
        orderHistoryModel = new DefaultTableModel(new String[]{"Order ID", "Customer", "Date", "Total", "Status", "Sold By", "Products"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderHistoryTable = new JTable(orderHistoryModel);
        orderHistoryTable.setBackground(new Color(200, 220, 255));
        orderHistoryTable.setForeground(Color.BLACK);
        orderHistoryTable.setGridColor(new Color(120, 150, 200));
        orderHistoryTable.getTableHeader().setBackground(new Color(70, 130, 180));
        orderHistoryTable.getTableHeader().setForeground(Color.BLACK);
        // Add double-click listener to show full order details
        orderHistoryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = orderHistoryTable.getSelectedRow();
                    if (row != -1) {
                        showOrderDetails(row);
                    }
                }
            }
        });
        JScrollPane orderScrollPane = new JScrollPane(orderHistoryTable);
        orderScrollPane.getViewport().setBackground(new Color(200, 220, 255));
        orderScrollPane.setBorder(new TitledBorder("Order History - " + currentShift.getDisplayName() + " (Double-click for details)"));
        orderPanel.add(orderScrollPane, BorderLayout.CENTER);
        refreshOrderHistoryTable();
        return orderPanel;
    }
    
    private void showOrderDetails(int orderIndex) {
        if (orderIndex >= 0 && orderIndex < orders.size()) {
            Order order = orders.get(orderIndex);
            order.loadOrderItems(inventory.getProducts());
            StringBuilder details = new StringBuilder();
            details.append("Order ID: ").append(order.getOrderId()).append("\n");
            details.append("Customer: ").append(order.getCustomer().getName()).append("\n");
            details.append("Phone: ").append(order.getCustomer().getPhone()).append("\n");
            details.append("Date: ").append(order.getOrderDate()).append("\n");
            details.append("Status: ").append(order.getStatus()).append("\n");
            details.append("Shift: ").append(currentShift.getDisplayName()).append("\n\n");
            details.append("Products:\n");
            details.append("----------------------------------------\n");
            for (OrderItem item : order.getItems()) {
                details.append(String.format("• %s\n  Qty: %d | Price: $%.2f | Subtotal: $%.2f\n\n", 
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getPrice(),
                    item.calculateSubtotal()));
            }
            details.append("----------------------------------------\n");
            details.append(String.format("Total Amount: $%.2f", order.getTotalAmount()));
            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            JOptionPane.showMessageDialog(this, scrollPane, "Order Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // ===================== Event Handlers =====================
    private void performLogin() {
    String username = usernameField.getText().trim();
    String password = new String(((JPasswordField) passwordField).getPassword());
    
    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter both username and password!", "Login Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Check credentials against loaded users
    if (!userCredentials.containsKey(username) || !userCredentials.get(username).equals(password)) {
        JOptionPane.showMessageDialog(this, "Invalid username or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Load existing shift state or initialize default (Morning)
    File shiftStateFile = new File(SHIFT_STATE_FILE);
    if (shiftStateFile.exists()) {
        // Load existing shift state - DON'T change it based on username
        loadShiftState();
        loadCurrentShiftOrders();
        System.out.println("Loaded existing " + currentShift.getDisplayName() + " started at " + shiftStartTime);
    } else {
        // If no shift state exists, start with Morning shift
        currentShift = ShiftType.MORNING;
        shiftStartTime = new Date();
        orders = new ArrayList<>();
        saveShiftState();
        System.out.println("Started new " + currentShift.getDisplayName() + " at " + shiftStartTime);
    }
    
    // Create login instance
    currentLogin = new Login(username, password);
    currentLogin.login();
    
    updateHeaderLabel();
    updateEndShiftButtonText();
    showMainScreen();
}
//    private void updateEndShiftButton() {
//        if (endShiftButton != null) {
//            endShiftButton.setText("End " + currentShift.name().substring(0, 1) + 
//                                  currentShift.name().substring(1).toLowerCase() + " Shift");
//        }
//    }
//    private void updateEndShiftButton() {
//        updateEndShiftButtonText();
//    }
    
    private void updateEndShiftButtonText() {
        if (endShiftButton != null) {
            String shiftName = currentShift.getDisplayName();
            endShiftButton.setText("End " + shiftName);
        }
    }
    
    private void performLogout() {
        // Save shift state before logout
        saveShiftState();
        saveCurrentShiftOrders();
        
        if (currentLogin != null) {
            currentLogin.logout();
            currentLogin = null;
        }
        showLoginScreen();
    }
    
    private void showLoginScreen() {
    getContentPane().removeAll();
    // Update login panel to show current shift
    createLoginPanel();
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
            refreshDashboard(); // Added for dynamic dashboard update
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
        
        Customer customer = new Customer(name, phone);
        customers.add(customer);
        customer.saveToFile("customers.txt");
        
        refreshCustomerTable();
        refreshCustomerComboBox();
        refreshDashboard(); // Added for dynamic dashboard update
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
            // Check current quantity in cart for this product
            int currentCartQuantity = 0;
            OrderItem existingItem = null;
            for (OrderItem item : currentCart) {
                if (item.getProduct().getProductId() == productId) {
                    existingItem = item;
                    currentCartQuantity = item.getQuantity();
                    break;
                }
            }
            // Check if total quantity (cart + new) exceeds available stock
            int totalQuantityNeeded = currentCartQuantity + quantity;
            if (totalQuantityNeeded > product.getQuantity()) {
                JOptionPane.showMessageDialog(this, 
                    String.format("Not enough stock! Available: %d, Already in cart: %d, Requesting: %d", 
                                 product.getQuantity(), currentCartQuantity, quantity), 
                    "Stock Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (existingItem != null) {
                existingItem.setQuantity(totalQuantityNeeded);
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
    
    // ===================== ENHANCED PROCESS ORDER WITH FILE SAVING =====================
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
        
        try {
            // Create new order
            Order order = new Order(customer, currentLogin.getUsername());
            
            // Add items to order and calculate total
            for (OrderItem item : currentCart) {
                order.addItem(item.getProduct(), item.getQuantity());
            }
            
            // Complete the order (this saves to files)
            order.completeOrder();
            
            // Add to current shift orders list
            orders.add(order);
            
            // Save current shift orders to file
            saveCurrentShiftOrders();
            
            // Deduct stock from inventory
            for (OrderItem item : currentCart) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() - item.getQuantity());
            }
            
            // Save inventory changes to file
            inventory.saveToFile();
            
            // Refresh all displays including dashboard
            refreshCartTable();
            refreshProductTable();
            refreshAvailableProductsTable();
            refreshOrderHistoryTable();
            refreshDashboard(); // Added for dynamic dashboard update
            
            // Clear cart
            currentCart.clear();
            
            // Show success message
            JOptionPane.showMessageDialog(this, 
                String.format("Order processed successfully!\nOrder ID: %d\nTotal: $%.2f\nShift: %s\nSaved to files & current shift", 
                             order.getOrderId(), order.getTotalAmount(), currentShift.getDisplayName()), 
                "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing order: " + e.getMessage(), "Order Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // ===================== Dynamic Dashboard Refresh Method =====================
    private void refreshDashboard() {
        if (totalProductsLabel != null) {
            totalProductsLabel.setText(String.valueOf(inventory.getProducts().size()));
        }
        if (totalCustomersLabel != null) {
            totalCustomersLabel.setText(String.valueOf(customers.size()));
        }
        if (ordersTodayLabel != null) {
           ordersTodayLabel.setText(String.valueOf(orders.size()));
        }
        if (lowStockLabel != null) {
            lowStockLabel.setText(String.valueOf(getLowStockCount()));
        }
    }
    
    // ===================== Refresh Methods =====================
    private void refreshAllTables() {
        refreshProductTable();
        refreshCustomerTable();
        refreshAvailableProductsTable();
        refreshCartTable();
        refreshOrderHistoryTable();
        refreshCustomerComboBox();
        refreshDashboard(); // Added for dynamic dashboard update
    }
    
    private void refreshProductTable() {
        productTableModel.setRowCount(0);
        for (Product p : inventory.getProducts()) {
            String special = "";
            
            if (p instanceof Medicine) {
                Medicine med = (Medicine) p;
                special = med.isPrescriptionRequired() ? "Prescription Required" : "OTC";
            } else if (p instanceof Cosmetic) {
                Cosmetic cos = (Cosmetic) p;
                special = "Skin: " + cos.getSuitableForSkinType();
            } else {
                special = "N/A";
            }
            productTableModel.addRow(new Object[]{
                p.getProductId(),
                p.getName(),
                p.getClass().getSimpleName(),
                String.format("$%.2f", p.getPrice()),
                p.getQuantity(),
                special
            });
        }
    }
    
    private void refreshCustomerTable() {
        customerTableModel.setRowCount(0);
        for (Customer c : customers) {
            customerTableModel.addRow(new Object[]{
                c.getCustomerid(),
                c.getName(), 
                c.getPhone()
            });
        }
    }
    
    private void refreshAvailableProductsTable() {
        availableProductsModel.setRowCount(0);
        for (Product p : inventory.getProducts()) {
            if (p.getQuantity() > 0) {
                availableProductsModel.addRow(new Object[]{
                    p.getProductId(), 
                    p.getName(), 
                    String.format("$%.2f", p.getPrice()), 
                    p.getQuantity()
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
                item.getProduct().getName(), 
                item.getQuantity(), 
                String.format("$%.2f", item.getProduct().getPrice()), 
                String.format("$%.2f", subtotal)
            });
            total += subtotal;
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }
    
    private void refreshOrderHistoryTable() {
        orderHistoryModel.setRowCount(0);
        if (orders == null || orders.isEmpty()) {
            return;
        }
        for (Order o : orders) {
            String customerName = (o.getCustomer() != null) ? o.getCustomer().getName() : "Unknown";
            String orderDate = (o.getOrderDate() != null) ? o.getOrderDate().toString() : "N/A";
            String totalAmount = String.format("$%.2f", o.getTotalAmount());
            String status = (o.getStatus() != null) ? o.getStatus() : "Pending";
            // Only load order items if not already loaded (check if items list is empty)
            if (o.getItems().isEmpty()) {
                o.loadOrderItems(inventory.getProducts());
            }
            StringBuilder productSummary = new StringBuilder();
            for (OrderItem item : o.getItems()) {
                if (productSummary.length() > 0) productSummary.append(", ");
                productSummary.append(item.getProduct().getName()).append(" (").append(item.getQuantity()).append(")");
            }
            // Truncate if too long
            String products = productSummary.toString();
            if (products.length() > 50) {
                products = products.substring(0, 47) + "...";
            }
            orderHistoryModel.addRow(new Object[]{
                o.getOrderId(),
                customerName,
                orderDate,
                totalAmount,
                status,
                o.getSoldBy(),
                products
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
    
    // Clear editing state - THIS IS IMPORTANT
    currentEditingProduct = null;
    
    // Reset field states
    toggleProductSpecificFields();
    
    System.out.println("Product fields cleared and editing state reset");
}
    
    
    private void clearCustomerFields() {
        customerNameField.setText("");
        customerPhoneField.setText("");
    }
    
    // ===================== Main Method =====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set Nimbus look and feel for better appearance
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // If Nimbus is not available, use default look and feel
                System.err.println("Could not set Nimbus look and feel, using default: " + e.getMessage());
            }
            
            new PharmacyGUI().setVisible(true);
        });
    }
}