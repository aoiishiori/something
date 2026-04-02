package Client.model;

import Client.util.RMIClient;
import Client.util.CommonUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminModel — MVC Model for the Admin dashboard.
 *
 * REPLACES: old SocketClient-based AdminModel
 * NOW USES: RMIClient with JSON protocol
 */
public class AdminModel {

    // -------------------------------------------------------
    // Fetch all user accounts
    // Returns a list of String arrays: [accountId, username, role, status]
    // -------------------------------------------------------
    public List<String[]> fetchAllUsers(String adminUsername) {
        List<String[]> users = new ArrayList<>();

        String request  = RMIClient.buildRequest("FETCH_ALL_USERS", adminUsername);
        String response = RMIClient.sendRequest(request);

        if (!RMIClient.isSuccess(response)) return users;

        // Parse the "users" array from the data block
        // Response data: { "users": [ { "accountId":..., "username":..., "role":..., "status":... }, ... ] }
        String dataBlock = RMIClient.getDataBlock(response);
        parseJsonArray(dataBlock, "users", users,
                new String[]{"accountId", "username", "role", "status"});

        return users;
    }

    // -------------------------------------------------------
    // Approve or Deny a user (newStatus = "APPROVED" or "DENIED")
    // -------------------------------------------------------
    public boolean updateUserStatus(String adminUsername,
                                    String targetUsername, String newStatus) {
        String dataJson = "{"
                + "\"targetUsername\":\"" + CommonUtils.escapeJson(targetUsername) + "\","
                + "\"newStatus\":\"" + CommonUtils.escapeJson(newStatus) + "\""
                + "}";
        String request  = RMIClient.buildRequest("UPDATE_USER_STATUS", adminUsername, dataJson);
        String response = RMIClient.sendRequest(request);
        return RMIClient.isSuccess(response);
    }

    // -------------------------------------------------------
    // Delete a user account
    // -------------------------------------------------------
    public boolean deleteUser(String adminUsername, String targetUsername) {
        String dataJson = "{\"targetUsername\":\"" + CommonUtils.escapeJson(targetUsername) + "\"}";
        String request  = RMIClient.buildRequest("DELETE_USER", adminUsername, dataJson);
        String response = RMIClient.sendRequest(request);
        return RMIClient.isSuccess(response);
    }

    // -------------------------------------------------------
    // Fetch server activity logs
    // Returns the raw JSON string of the logs array for AdminController to parse
    // -------------------------------------------------------
    public List<CommonModel.LogRow> fetchLogs(String adminUsername) {
        List<CommonModel.LogRow> logs = new ArrayList<>();
        String request  = RMIClient.buildRequest("FETCH_LOGS", adminUsername);
        String response = RMIClient.sendRequest(request);

        if (!RMIClient.isSuccess(response)) return logs;

        try {
            String dataBlock = RMIClient.getDataBlock(response);
            tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
            tools.jackson.databind.JsonNode rootNode = mapper.readTree(dataBlock);
            tools.jackson.databind.JsonNode logsArray = rootNode.get("logs");

            if (logsArray != null && logsArray.isArray()) {
                for (tools.jackson.databind.JsonNode entry : logsArray) {
                    logs.add(new CommonModel.LogRow(
                            entry.path("timestamp").asText(),
                            entry.path("user").asText(),
                            entry.path("action").asText(),
                            entry.path("dataAffected").asText(),
                            entry.path("result").asText()
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse logs: " + e.getMessage());
        }
        return logs;
    }

    // -------------------------------------------------------
    // Fetch all transactions (admin view)
    // Returns list of String arrays:
    //   [transactionId, productId, buyerUsername, sellerUsername, quantity, timestamp]
    // -------------------------------------------------------
    public List<String[]> fetchAllTransactions(String adminUsername) {
        List<String[]> txList = new ArrayList<>();

        String request  = RMIClient.buildRequest("FETCH_ALL_TRANSACTIONS", adminUsername);
        String response = RMIClient.sendRequest(request);

        if (!RMIClient.isSuccess(response)) return txList;

        String dataBlock = RMIClient.getDataBlock(response);
        parseJsonArray(dataBlock, "transactions", txList,
                new String[]{"transactionId", "productId", "buyerUsername",
                        "sellerUsername", "quantity", "timestamp"});

        return txList;
    }

    // -------------------------------------------------------
    // JSON array parser helper
    // Finds all objects inside a named JSON array and extracts the specified fields
    // -------------------------------------------------------
    private void parseJsonArray(String json, String arrayName,
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
            System.err.println("[AdminModel] JSON parse error: " + e.getMessage());
        }
    }
}