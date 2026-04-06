package Server.controller;

import Server.util.JsonReader;
import Server.util.JsonUtils;
import Server.util.JsonWriter;
import Server.util.ServerLogger;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * CartController — handles cart persistence for clients across different PCs.
 * Strictly uses JsonNode. NO data binding.
 */
public class CartController {

    // Fetches ONLY the specific user's cart file
    public String fetchCart(String username) {
        ServerLogger.logTransaction(username, "FETCH_CART", "N/A");

        ArrayNode cartItems = JsonReader.readUserCart(username);
        ObjectNode dataNode = JsonUtils.getMapper().createObjectNode();
        dataNode.set("cart", cartItems);

        return JsonUtils.createResponseWithData("SUCCESS", "Cart fetched.", dataNode);
    }

    // Overwrites ONLY the specific user's cart file
    public String saveCart(String username, JsonNode data) {
        JsonNode cartNode = data.get("cart");
        ArrayNode cartArray = cartNode.isArray() ? (ArrayNode) cartNode : JsonUtils.getMapper().createArrayNode();

        JsonWriter.writeUserCart(username, cartArray);
        ServerLogger.logTransaction(username, "SAVE_CART", "N/A");

        return JsonUtils.createResponse("SUCCESS", "Cart saved.");
    }

    // Clears ONLY the specific user's cart file
    public String clearCart(String username) {
        JsonWriter.writeUserCart(username, JsonUtils.getMapper().createArrayNode());
        ServerLogger.logTransaction(username, "CLEAR_CART", "N/A");

        return JsonUtils.createResponse("SUCCESS", "Cart cleared.");
    }
}