package Client.util;

import shared.RemoteService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMIClient — handles all communication with the RMI server.
 *
 * Builds JSON request strings, sends them over RMI,
 * and provides helpers to parse JSON responses.
 *
 * JSON Request format:
 * {
 *   "action":   "LOGIN",
 *   "username": "john",
 *   "data":     { "password": "pass123" }
 * }
 *
 * JSON Response format:
 * {
 *   "status":  "SUCCESS",
 *   "message": "Login successful.",
 *   "data":    { "role": "BUYER", "accountId": "ACC-XXXX" }
 * }
 */
public class RMIClient {

    private static final String SERVER_HOST =
            System.getProperty("server.host", "localhost");
    private static final int    RMI_PORT     = 1099;
    private static final String SERVICE_NAME = "FoodWasteService";

    // -------------------------------------------------------
    // Send request to server via RMI
    // -------------------------------------------------------
    public static String sendRequest(String jsonRequest) {
        try {
            Registry registry = LocateRegistry.getRegistry(SERVER_HOST, RMI_PORT);
            RemoteService service = (RemoteService) registry.lookup(SERVICE_NAME);
            return service.processRequest(jsonRequest);
        } catch (Exception e) {
            return buildErrorResponse("Connection failed: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Request builders
    // -------------------------------------------------------

    /**
     * Builds a JSON request with no data payload.
     * e.g. LOGOUT, FETCH_ALL_PRODUCTS
     */
    public static String buildRequest(String action, String username) {
        return "{"
                + "\"action\":\"" + escapeJson(action) + "\","
                + "\"username\":\"" + escapeJson(username) + "\""
                + "}";
    }

    /**
     * Builds a JSON request with a data payload string.
     * The dataJson parameter must be a valid JSON object string,
     * e.g. "{\"password\":\"pass123\"}"
     */
    public static String buildRequest(String action, String username, String dataJson) {
        return "{"
                + "\"action\":\"" + escapeJson(action) + "\","
                + "\"username\":\"" + escapeJson(username) + "\","
                + "\"data\":" + dataJson
                + "}";
    }

    // -------------------------------------------------------
    // Response parsers
    // -------------------------------------------------------

    /**
     * Extracts the "status" field from a JSON response string.
     */
    public static String getStatus(String responseJson) {
        return extractJsonString(responseJson, "status");
    }

    /**
     * Extracts the "message" field from a JSON response string.
     */
    public static String getMessage(String responseJson) {
        return extractJsonString(responseJson, "message");
    }

    /**
     * Returns true if status is "SUCCESS".
     */
    public static boolean isSuccess(String responseJson) {
        return "SUCCESS".equals(getStatus(responseJson));
    }

    /**
     * Extracts the entire "data" block as a raw JSON string.
     * Returns "{}" if not present.
     */
    public static String getDataBlock(String responseJson) {
        if (responseJson == null) return "{}";
        int start = responseJson.indexOf("\"data\":");
        if (start == -1) return "{}";
        // Move past "data":
        int objStart = responseJson.indexOf("{", start + 7);
        int arrStart = responseJson.indexOf("[", start + 7);

        // Could be object or array
        if (objStart == -1 && arrStart == -1) return "{}";

        boolean isArray = arrStart != -1 && (objStart == -1 || arrStart < objStart);

        if (isArray) {
            int depth = 0;
            for (int i = arrStart; i < responseJson.length(); i++) {
                char c = responseJson.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) return responseJson.substring(arrStart, i + 1);
                }
            }
        } else {
            int depth = 0;
            for (int i = objStart; i < responseJson.length(); i++) {
                char c = responseJson.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return responseJson.substring(objStart, i + 1);
                }
            }
        }
        return "{}";
    }

    /**
     * Extracts a string field value from inside the "data" block.
     * e.g. getDataField(response, "role") → "BUYER"
     */
    public static String getDataField(String responseJson, String field) {
        String dataBlock = getDataBlock(responseJson);
        return extractJsonString(dataBlock, field);
    }

    // -------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------

    /**
     * Simple JSON string field extractor.
     * Handles: "field":"value"  and  "field": "value"
     * Does NOT use a full JSON parser — keeps the client lightweight.
     */
    private static String extractJsonString(String json, String field) {
        if (json == null || json.isEmpty()) return "";
        String key = "\"" + field + "\"";
        int keyIdx = json.indexOf(key);
        if (keyIdx == -1) return "";

        int colonIdx = json.indexOf(":", keyIdx + key.length());
        if (colonIdx == -1) return "";

        // Skip whitespace after colon
        int valueStart = colonIdx + 1;
        while (valueStart < json.length() && json.charAt(valueStart) == ' ') valueStart++;

        if (valueStart >= json.length()) return "";

        char first = json.charAt(valueStart);
        if (first == '"') {
            // String value
            int end = valueStart + 1;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                end++;
            }
            return json.substring(valueStart + 1, end)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\t", "\t");
        } else {
            // Number, boolean, null
            int end = valueStart;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == ',' || c == '}' || c == ']' || c == '\n') break;
                end++;
            }
            return json.substring(valueStart, end).trim();
        }
    }

    private static String buildErrorResponse(String message) {
        return "{\"status\":\"ERROR\",\"message\":\""
                + escapeJson(message) + "\"}";
    }

    /**
     * Escapes a string for safe inclusion in a JSON value.
     */
    public static String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}