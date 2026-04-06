package Server;

import Server.controller.AccountController;
import Server.controller.CartController;
import Server.controller.ProductController;
import Server.controller.TransactionController;
import Server.util.JsonUtils;
import Server.util.ServerLogger;
import Server.util.SessionManager;
import tools.jackson.databind.JsonNode;

/**
 * RequestHandler — parses each client JSON request and routes
 * to the correct controller method.
 *
 * Expected JSON request format:
 * {
 *   "action":   "ACTION_NAME",
 *   "username": "someuser",
 *   "data":     { ... }   <-- optional, action-specific fields
 * }
 */
public class RequestHandler {

    private final AccountController     accountController;
    private final ProductController     productController;
    private final TransactionController transactionController;
    private final CartController        cartController;

    public RequestHandler() {
        this.accountController     = new AccountController();
        this.productController     = new ProductController();
        this.transactionController = new TransactionController();
        this.cartController        = new CartController();
    }

    // -------------------------------------------------------
    // Main entry point — called by Server.processRequest() for every RMI call
    // -------------------------------------------------------
    public String processRequest(String jsonRequest) {
        try {
            JsonNode root = JsonUtils.parseRequest(jsonRequest);

            if (root == null) {
                return JsonUtils.createResponse("ERROR",
                        "Invalid JSON format in request.");
            }

            String action   = JsonUtils.getString(root, "action");
            String username = JsonUtils.getString(root, "username");
            JsonNode data   = JsonUtils.getData(root); // safe — returns empty node if absent

            if (action.isEmpty()) {
                return JsonUtils.createResponse("ERROR", "Missing 'action' in request.");
            }

            // -----------------------------------------------
            // Route to correct controller
            // -----------------------------------------------
            switch (action) {

                // --- Account operations ---
                case "LOGIN":
                    return accountController.login(username, data);

                case "REGISTER":
                    return accountController.register(username, data);

                case "FETCH_ALL_USERS":
                    return accountController.fetchAllUsers(username);

                case "UPDATE_USER_STATUS":
                    return accountController.updateUserStatus(username, data);

                case "DELETE_USER":
                    return accountController.deleteUser(username, data);

                case "CHANGE_PASSWORD":
                    return accountController.changePassword(username, data);

                case "FETCH_LOGS":
                    return accountController.fetchLogs(username);

                // --- Product operations ---
                case "ADD_PRODUCT":
                    return productController.addProduct(username, data);

                case "FETCH_ALL_PRODUCTS":
                    return productController.fetchAllProducts(username);

                case "FETCH_SELLER_PRODUCTS":
                    return productController.fetchSellerProducts(username);

                case "UPDATE_PRODUCT":
                    return productController.updateProduct(username, data);

                case "DELETE_PRODUCT":
                    return productController.deleteProduct(username, data);

                case "SEARCH_PRODUCTS":
                    return productController.searchProducts(username, data);

                case "BUY_PRODUCT":
                    return productController.buyProduct(username, data);

                // --- Cart operations ---

                case "FETCH_CART":
                    return cartController.fetchCart(username);

                case "SAVE_CART":
                    return cartController.saveCart(username, data);

                case "CLEAR_CART":
                    return cartController.clearCart(username);

                // --- Transaction operations ---
                case "FETCH_MY_PURCHASES":
                    return transactionController.fetchMyPurchases(username);

                case "FETCH_MY_SALES":
                    return transactionController.fetchMySales(username);

                case "FETCH_ALL_TRANSACTIONS":
                    return transactionController.fetchAllTransactions(username);

                // --- Session ---
                case "LOGOUT":
                    SessionManager.logout(username);
                    ServerLogger.logTransaction(username, "LOGOUT", "N/A");
                    return JsonUtils.createResponse("SUCCESS", "Logged out.");

                default:
                    ServerLogger.logError("Unknown action: " + action
                            + " from user: " + username);
                    return JsonUtils.createResponse("ERROR",
                            "Unknown action: " + action);
            }

        } catch (Exception e) {
            ServerLogger.logError("Failed to process request: " + e.getMessage());
            return JsonUtils.createResponse("ERROR",
                    "Request processing failed: " + e.getMessage());
        }
    }
}