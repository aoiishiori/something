package Client.model;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * CommonModel --- Consolidated data model classes used across multiple controllers.
 */
public class CommonModel {

    // =====================================================
    // CART PERSISTENCE (State Management)
    // =====================================================
    private static final String CART_FILE = "data/client_cart.json";
    private static final ObjectMapper cartMapper = new ObjectMapper();
    private static final List<CartEntry> persistentCart = new ArrayList<>();

    /**
     * Model layer: Provides the single source of truth for the cart list.
     */
    public static List<CartEntry> getCart() {
        return persistentCart;
    }

    /**
     * Model layer: Loads cart state from file into memory (called once at Buyer startup).
     */
    public static void loadCart() {
        persistentCart.clear();
        File f = new File(CART_FILE);
        if (!f.exists()) return;

        try {
            ObjectNode root = (ObjectNode) cartMapper.readTree(f);
            ArrayNode arr = root.withArray("cart");
            for (JsonNode node : arr) {
                persistentCart.add(new CartEntry(
                        node.path("productId").asText(),
                        node.path("productName").asText(),
                        node.path("seller").asText(),
                        node.path("unitPrice").asDouble(),
                        node.path("quantity").asInt(),
                        node.path("maxAvailable").asInt()
                ));
            }
        } catch (Exception e) {
            System.err.println("[CommonModel] Failed to load cart: " + e.getMessage());
        }
    }

    /**
     * Model layer: Saves current memory state to the local JSON file.
     */
    public static void saveCart() {
        try {
            ObjectNode root = cartMapper.createObjectNode();
            ArrayNode arr = root.putArray("cart");
            for (CartEntry c : persistentCart) {
                ObjectNode node = arr.addObject();
                node.put("productId", c.getProductId());
                node.put("productName", c.getProductName());
                node.put("seller", c.getSeller());
                node.put("unitPrice", c.getUnitPrice());
                node.put("quantity", c.getQuantity());
                node.put("maxAvailable", c.getMaxAvailable());
            }
            new File("data").mkdirs();
            cartMapper.writerWithDefaultPrettyPrinter().writeValue(new File(CART_FILE), root);
        } catch (Exception e) {
            System.err.println("[CommonModel] Failed to save cart: " + e.getMessage());
        }
    }

    /**
     * Model layer: Empties cart in memory and updates file.
     */
    public static void clearCart() {
        persistentCart.clear();
        saveCart();
    }

    // =====================================================
    // UserRow - Used by AdminController
    // Represents a user account in the admin user management table
    // =====================================================
    public static class UserRow {
        private final String accountId;
        private final String username;
        private final String role;
        private final String status;

        /**
         * Creates a new UserRow.
         */
        public UserRow(String accountId, String username, String role, String status) {
            this.accountId = accountId;
            this.username = username;
            this.role = role;
            this.status = status;
        }

        public String getAccountId() { return accountId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public String getStatus() { return status; }
    }

    // =====================================================
    // LogRow - Used by AdminController
    // Represents a server activity log entry
    // =====================================================
    public static class LogRow {
        private final String timestamp;
        private final String user;
        private final String action;
        private final String dataAffected;
        private final String result;

        /**
         * Creates a new LogRow.
         */
        public LogRow(String timestamp, String user, String action,
                      String dataAffected, String result) {
            this.timestamp = timestamp;
            this.user = user;
            this.action = action;
            this.dataAffected = dataAffected;
            this.result = result;
        }

        public String getTimestamp() { return timestamp; }
        public String getUser() { return user; }
        public String getAction() { return action; }
        public String getDataAffected() { return dataAffected; }
        public String getResult() { return result; }
    }

    public static class ProductRow {
        private final String productId;
        private final String name;
        private final String category;
        private final String price;
        private final String quantity;
        private final String expiryDate;
        private final String status;

        /**
         * Creates a new ProductRow.
         */
        public ProductRow(String productId, String name, String category,
                          String price, String quantity, String expiryDate, String status) {
            this.productId = productId;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.expiryDate = expiryDate;
            this.status = status;
        }

        public String getProductId() { return productId; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getPrice() { return price; }
        public String getQuantity() { return quantity; }
        public String getExpiryDate() { return expiryDate; }
        public String getStatus() { return status; }
    }

    public static class CartEntry {
        private final String productId;
        private final String productName;
        private final String seller;
        private final double unitPrice;
        private int quantity;
        private final int maxAvailable;

        public CartEntry(String productId, String productName, String seller,
                         double unitPrice, int quantity, int maxAvailable) {
            this.productId = productId;
            this.productName = productName;
            this.seller = seller;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.maxAvailable = maxAvailable;
        }

        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getSeller() { return seller; }
        public double getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public int getMaxAvailable() { return maxAvailable; }

        public void addQuantity(int qty) { this.quantity += qty; }
        public double getLineTotal() { return this.unitPrice * this.quantity; }
    }
}