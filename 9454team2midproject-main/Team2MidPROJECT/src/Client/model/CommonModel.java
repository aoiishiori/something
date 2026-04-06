package Client.model;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import Client.util.RMIClient;
import java.util.ArrayList;
import java.util.List;

/**
 * CommonModel --- Consolidated data model classes used across multiple controllers.
 */
public class CommonModel {

    // =====================================================
    // CART PERSISTENCE (State Management)
    // =====================================================
    private static final ObjectMapper mapper = new ObjectMapper();

    // In-memory cart state
    private static final List<CartEntry> cart = new ArrayList<>();

    /**
     * Model layer: Loads cart state from the Server and loads into local memory.
     */
    public static void loadCart() {
        cart.clear();
        String request = RMIClient.buildRequest("FETCH_CART", SessionData.getUsername());
        String response = RMIClient.sendRequest(request);

        if (!RMIClient.isSuccess(response)) return;

        try {
            String dataBlock = RMIClient.getDataBlock(response);
            JsonNode root = mapper.readTree(dataBlock);
            JsonNode items = root.get("cart");

            if (items != null && items.isArray()) {
                for (JsonNode node : items) {
                    CartEntry entry = new CartEntry(
                        node.path("productId").asText(),
                        node.path("productName").asText(),
                        node.path("seller").asText(),
                        node.path("unitPrice").asDouble(),
                        node.path("quantity").asInt(),
                        node.path("maxAvailable").asInt()
                    );
                    cart.add(entry);
                }
            }
        } catch (Exception e) {
            System.err.println("[CommonModel] Failed to load cart: " + e.getMessage());
        }
    }

    /**
     * Model layer: Saves current memory state to the Server.
     */
    public static void saveCart() {
        ObjectNode dataNode = mapper.createObjectNode();
        ArrayNode cartArray = dataNode.putArray("cart");

        for (CartEntry c : cart) {
            ObjectNode node = cartArray.addObject();
            node.put("productId", c.getProductId());
            node.put("productName", c.getProductName());
            node.put("seller", c.getSeller());
            node.put("unitPrice", c.getUnitPrice());
            node.put("quantity", c.getQuantity());
            node.put("maxAvailable", c.getMaxAvailable());
        }

        String request = RMIClient.buildRequest("SAVE_CART", SessionData.getUsername(), dataNode.toString());
        RMIClient.sendRequest(request);
    }

    /**
     * Model layer: Tells server to delete the cart file, clears local memory.
     */
    public static void clearCart() {
        cart.clear();
        String request = RMIClient.buildRequest("CLEAR_CART", SessionData.getUsername());
        RMIClient.sendRequest(request);
    }

    public static List<CartEntry> getCart() {
        return cart;
    }

    // =====================================================
    // JSON UTILS
    // =====================================================
    public static String extractField(String json, String field) {
        if (json == null || json.isEmpty()) return "";
        try {
            JsonNode root = mapper.readTree(json);
            return root.path(field).asText("");
        } catch (Exception e) {
            return "";
        }
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

        public double getLineTotal() { return unitPrice * quantity; }

        public void addQuantity(int qty) { this.quantity += qty; }
    }

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
}