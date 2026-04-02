package Client.model;

import Client.util.RMIClient;
import Client.util.CommonUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * SellerModel — MVC Model for the Seller dashboard.
 *
 * Each product is a String array:
 *   [0] productId        [1] sellerUsername  [2] name
 *   [3] category         [4] originalPrice   [5] discountedPrice
 *   [6] availableQuantity [7] expiryDate     [8] status
 *
 * All requests/responses now use JSON protocol.
 */
public class SellerModel {

    public List<String[]> fetchMyProducts(String username) {
        String request  = RMIClient.buildRequest("FETCH_SELLER_PRODUCTS", username);
        String response = RMIClient.sendRequest(request);
        return parseProducts(response);
    }

    public String[] addProduct(String username, String name, String category,
                               double originalPrice, double discountedPrice,
                               int quantity, String expiryDate) {
        String dataJson = "{"
                + "\"name\":\""              + CommonUtils.escapeJson(name)       + "\","
                + "\"category\":\""          + CommonUtils.escapeJson(category)   + "\","
                + "\"originalPrice\":"       + originalPrice                       + ","
                + "\"discountedPrice\":"     + discountedPrice                     + ","
                + "\"availableQuantity\":"   + quantity                            + ","
                + "\"expiryDate\":\""        + CommonUtils.escapeJson(expiryDate) + "\""
                + "}";
        String request  = RMIClient.buildRequest("ADD_PRODUCT", username, dataJson);
        String response = RMIClient.sendRequest(request);
        return new String[]{RMIClient.getStatus(response), RMIClient.getMessage(response)};
    }

    public String[] updateProduct(String username, String productId,
                                  String name, String category,
                                  double originalPrice, double discountedPrice,
                                  int quantity, String expiryDate, String status) {
        String dataJson = "{"
                + "\"productId\":\""         + CommonUtils.escapeJson(productId)  + "\","
                + "\"name\":\""              + CommonUtils.escapeJson(name)       + "\","
                + "\"category\":\""          + CommonUtils.escapeJson(category)   + "\","
                + "\"originalPrice\":"       + originalPrice                       + ","
                + "\"discountedPrice\":"     + discountedPrice                     + ","
                + "\"availableQuantity\":"   + quantity                            + ","
                + "\"expiryDate\":\""        + CommonUtils.escapeJson(expiryDate) + "\","
                + "\"status\":\""            + CommonUtils.escapeJson(status)     + "\""
                + "}";
        String request  = RMIClient.buildRequest("UPDATE_PRODUCT", username, dataJson);
        String response = RMIClient.sendRequest(request);
        return new String[]{RMIClient.getStatus(response), RMIClient.getMessage(response)};
    }

    public String[] deleteProduct(String username, String productId) {
        String dataJson = "{\"productId\":\"" + CommonUtils.escapeJson(productId) + "\"}";
        String request  = RMIClient.buildRequest("DELETE_PRODUCT", username, dataJson);
        String response = RMIClient.sendRequest(request);
        return new String[]{RMIClient.getStatus(response), RMIClient.getMessage(response)};
    }

    public List<String[]> fetchMySales(String username) {
        String request  = RMIClient.buildRequest("FETCH_MY_SALES", username);
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
                "transactionId", "productId", "buyerUsername", "quantity", "timestamp"
        });
        return txList;
    }

    /**
     * Parses all objects from a named JSON array and extracts the specified fields.
     */
    private void parseJsonObjectArray(String json, String arrayName,
                                      List<String[]> result, String[] fields) {
        if (json == null || json.isEmpty()) return;

        String arrayKey = "\"" + arrayName + "\"";
        int keyIdx = json.indexOf(arrayKey);
        if (keyIdx == -1) return;

        int arrStart = json.indexOf("[", keyIdx);
        if (arrStart == -1) return;

        int i = arrStart + 1;
        while (i < json.length()) {
            int objStart = json.indexOf("{", i);
            if (objStart == -1) break;

            int depth = 0, objEnd = objStart;
            for (int j = objStart; j < json.length(); j++) {
                char c = json.charAt(j);
                if (c == '{') depth++;
                else if (c == '}') { depth--; if (depth == 0) { objEnd = j; break; } }
            }

            String obj = json.substring(objStart, objEnd + 1);
            String[] row = new String[fields.length];
            for (int f = 0; f < fields.length; f++) {
                row[f] = CommonUtils.extractField(obj, fields[f]);
            }
            result.add(row);
            i = objEnd + 1;
        }
    }
}