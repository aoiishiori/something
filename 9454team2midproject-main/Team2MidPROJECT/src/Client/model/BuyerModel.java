package Client.model;

import Client.util.RMIClient;
import Client.util.CommonUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * BuyerModel — MVC Model for the Buyer dashboard.
 *
 * Each product is returned as a String array:
 *   [0] productId        [1] sellerUsername  [2] name
 *   [3] category         [4] originalPrice   [5] discountedPrice
 *   [6] availableQuantity [7] expiryDate     [8] status
 *
 * All requests/responses now use JSON protocol.
 */
public class BuyerModel {

    public List<String[]> fetchAllProducts(String username) {
        String request  = RMIClient.buildRequest("FETCH_ALL_PRODUCTS", username);
        String response = RMIClient.sendRequest(request);
        return parseProducts(response);
    }

    public List<String[]> searchProducts(String username, String keyword) {
        String dataJson = "{\"keyword\":\"" + CommonUtils.escapeJson(keyword) + "\"}";
        String request  = RMIClient.buildRequest("SEARCH_PRODUCTS", username, dataJson);
        String response = RMIClient.sendRequest(request);
        return parseProducts(response);
    }

    public String[] buyProduct(String username, String productId, int quantity) {
        String dataJson = "{"
                + "\"productId\":\"" + CommonUtils.escapeJson(productId) + "\","
                + "\"quantity\":" + quantity
                + "}";
        String request  = RMIClient.buildRequest("BUY_PRODUCT", username, dataJson);
        String response = RMIClient.sendRequest(request);
        return new String[]{RMIClient.getStatus(response), RMIClient.getMessage(response)};
    }

    public List<String[]> fetchMyPurchases(String username) {
        String request  = RMIClient.buildRequest("FETCH_MY_PURCHASES", username);
        String response = RMIClient.sendRequest(request);
        return parseTransactions(response);
    }

    // -------------------------------------------------------
    // JSON Parsers
    // -------------------------------------------------------

    private List<String[]> parseProducts(String response) {
        List<String[]> products = new ArrayList<>();
        if (!RMIClient.isSuccess(response)) return products;

        String dataBlock = RMIClient.getDataBlock(response);
        parseJsonObjectArray(dataBlock, "products", products, new String[]{
                "productId", "sellerUsername", "name", "category",
                "originalPrice", "discountedPrice", "availableQuantity",
                "expiryDate", "status"
        });
        return products;
    }

    private List<String[]> parseTransactions(String response) {
        List<String[]> txList = new ArrayList<>();
        if (!RMIClient.isSuccess(response)) return txList;

        String dataBlock = RMIClient.getDataBlock(response);
        parseJsonObjectArray(dataBlock, "transactions", txList, new String[]{
                "transactionId", "productId", "sellerUsername", "quantity", "timestamp"
        });
        return txList;
    }

    /**
     * Parses all objects from a named JSON array and extracts the specified fields.
     */
    private void parseJsonObjectArray(String json, String arrayName,
                                      List<String[]> result, String[] fields) {
        if (json == null || json.isEmpty()) return;
        try {
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            tools.jackson.databind.JsonNode root = mapper.readTree(json);
            tools.jackson.databind.JsonNode arr = root.get(arrayName);

            if (arr != null && arr.isArray()) {
                for (tools.jackson.databind.JsonNode obj : arr) {
                    String[] row = new String[fields.length];
                    for (int i = 0; i < fields.length; i++) {
                        row[i] = obj.path(fields[i]).asText("");
                    }
                    result.add(row);
                }
            }
        } catch (Exception e) {
            System.err.println("[BuyerModel] JSON parse error: " + e.getMessage());
        }
    }
}