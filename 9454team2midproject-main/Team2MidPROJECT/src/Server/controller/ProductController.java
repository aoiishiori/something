package Server.controller;

import Server.model.Product;
import Server.util.CallbackManager;
import Server.util.JsonUtils;
import Server.util.ServerLogger;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * ProductController — handles all product-related requests.
 *
 * Supported actions:
 *   ADD_PRODUCT, FETCH_ALL_PRODUCTS, FETCH_SELLER_PRODUCTS,
 *   UPDATE_PRODUCT, DELETE_PRODUCT, SEARCH_PRODUCTS, BUY_PRODUCT
 *
 * BUY_PRODUCT uses a static lock to prevent race conditions
 * when multiple clients try to buy the same item simultaneously.
 *
 * All responses are JSON strings.
 */
public class ProductController {

    // -------------------------------------------------------
    // ADD PRODUCT (Seller)
    // data: { "name", "category", "originalPrice",
    //         "discountedPrice", "availableQuantity", "expiryDate" }
    // -------------------------------------------------------
    public String addProduct(String sellerUsername, JsonNode data) {
        Product newProd = Product.addProduct(
                sellerUsername,
                JsonUtils.getString(data, "name"),
                JsonUtils.getString(data, "category"),
                JsonUtils.getDouble(data, "originalPrice"),
                JsonUtils.getDouble(data, "discountedPrice"),
                JsonUtils.getInt(data, "availableQuantity"),
                JsonUtils.getString(data, "expiryDate")
        );

        ServerLogger.logTransaction(sellerUsername, "ADD_PRODUCT",
                "productId=" + newProd.getProductId() + ", name=" + newProd.getName());
        // Notify all connected clients that products have changed
        CallbackManager.notifyProductUpdated(newProd.getProductId());

        return JsonUtils.createResponse("SUCCESS",
                "Product added: " + newProd.getName() + " [" + newProd.getProductId() + "]");
    }

    // -------------------------------------------------------
    // FETCH ALL PRODUCTS (Buyer — only AVAILABLE ones)
    // -------------------------------------------------------
    public String fetchAllProducts(String username) {
        ObjectNode dataNode  = JsonUtils.getMapper().createObjectNode();
        ArrayNode productsArr = dataNode.putArray("products");
        for (Product p : Product.fetchAvailableProducts()) {
            productsArr.add(p.toJsonNode(JsonUtils.getMapper()));
        }

        ServerLogger.logTransaction(username, "FETCH_ALL_PRODUCTS", "N/A");
        return JsonUtils.createResponseWithData("SUCCESS", "Products fetched.", dataNode);
    }

    // -------------------------------------------------------
    // FETCH SELLER'S OWN PRODUCTS (includes all statuses)
    // -------------------------------------------------------
    public String fetchSellerProducts(String sellerUsername) {
        ObjectNode dataNode   = JsonUtils.getMapper().createObjectNode();
        ArrayNode productsArr = dataNode.putArray("products");
        for (Product p : Product.fetchSellerProducts(sellerUsername)) {
            productsArr.add(p.toJsonNode(JsonUtils.getMapper()));
        }

        ServerLogger.logTransaction(sellerUsername, "FETCH_SELLER_PRODUCTS", "N/A");
        return JsonUtils.createResponseWithData("SUCCESS", "Your products fetched.", dataNode);
    }

    // -------------------------------------------------------
    // UPDATE PRODUCT (Seller)
    // data: { "productId", "name", "category", "originalPrice",
    //         "discountedPrice", "availableQuantity", "expiryDate", "status" }
    // -------------------------------------------------------
    public String updateProduct(String sellerUsername, JsonNode data) {
        String productId = JsonUtils.getString(data, "productId");

        Product updated = Product.updateProduct(
                sellerUsername, productId,
                JsonUtils.getString(data, "name"),
                JsonUtils.getString(data, "category"),
                JsonUtils.getDouble(data, "originalPrice"),
                JsonUtils.getDouble(data, "discountedPrice"),
                JsonUtils.getInt(data, "availableQuantity"),
                JsonUtils.getString(data, "expiryDate"),
                JsonUtils.getString(data, "status"),
                data.has("originalPrice"),
                data.has("discountedPrice"),
                data.has("availableQuantity")
        );

        // Only update fields that are actually provided
        if (updated == null) {
            return JsonUtils.createResponse("FAILED", "Product not found.");
        }

        ServerLogger.logTransaction(sellerUsername, "UPDATE_PRODUCT",
                "productId=" + productId);

        // Notify clients
        CallbackManager.notifyProductUpdated(productId);
        return JsonUtils.createResponse("SUCCESS", "Product updated.");
    }

    // -------------------------------------------------------
    // DELETE PRODUCT (Seller)
    // data: { "productId": "..." }
    // -------------------------------------------------------
    public String deleteProduct(String sellerUsername, JsonNode data) {
        String productId = JsonUtils.getString(data, "productId");
        boolean success = Product.deleteProduct(sellerUsername, productId);

        if (!success) {
            return JsonUtils.createResponse("FAILED", "Product not found or you don't own it.");
        }

        ServerLogger.logTransaction(sellerUsername, "DELETE_PRODUCT",
                "productId=" + productId);
        CallbackManager.notifyProductUpdated(productId);
        return JsonUtils.createResponse("SUCCESS", "Product deleted.");
    }

    // -------------------------------------------------------
    // SEARCH PRODUCTS (Buyer)
    // data: { "keyword": "..." }
    // -------------------------------------------------------
    public String searchProducts(String username, JsonNode data) {
        String keyword = JsonUtils.getString(data, "keyword").toLowerCase();

        ObjectNode dataNode   = JsonUtils.getMapper().createObjectNode();
        ArrayNode productsArr = dataNode.putArray("products");
        for (Product p : Product.searchAvailableProducts(keyword)) {
            productsArr.add(p.toJsonNode(JsonUtils.getMapper()));
        }

        ServerLogger.logTransaction(username, "SEARCH_PRODUCTS", "keyword=" + keyword);
        return JsonUtils.createResponseWithData("SUCCESS", "Search results.", dataNode);
    }

    // -------------------------------------------------------
    // BUY PRODUCT (Buyer) — THREAD-SAFE
    // data: { "productId": "...", "quantity": 1 }
    // -------------------------------------------------------
    public String buyProduct(String buyerUsername, JsonNode data) {
        String productId  = JsonUtils.getString(data, "productId");
        int    requested  = JsonUtils.getInt(data, "quantity");

        // Synchronized block — prevents two buyers from buying the last item simultaneously
        String[] result = Product.buyProduct(buyerUsername, productId, requested);

        if (!"SUCCESS".equals(result[0])) {
            return JsonUtils.createResponse(result[0], result[1]);
        }

        // Deduct stock
        String sellerUsername = result[2];
        // Record transaction
        ServerLogger.logTransaction(buyerUsername, "BUY_PRODUCT", "productId=" + productId + ", qty=" + requested + ", seller=" + sellerUsername);
        // Notify all clients that this product changed
        CallbackManager.notifyProductUpdated(productId);

        return JsonUtils.createResponse("SUCCESS", result[1]);
    }
}