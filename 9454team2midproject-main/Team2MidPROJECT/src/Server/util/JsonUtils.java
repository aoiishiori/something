package Server.util;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.JsonNode;

/**
 * JsonUtils — server-side utility for building and parsing JSON
 * request/response messages.
 *
 * Response format:
 * {
 *   "status":  "SUCCESS" | "FAILED" | "ERROR" | "PENDING" | "DENIED",
 *   "message": "Human-readable message",
 *   "data":    { ... }   <-- optional, only when there is data to return
 * }
 *
 * Request format:
 * {
 *   "action":   "LOGIN",
 *   "username": "john",
 *   "data":     { ... }  <-- optional
 * }
 */
public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    // -------------------------------------------------------
    // RESPONSE BUILDERS (Server → Client)
    // -------------------------------------------------------

    public static String createResponse(String status, String message) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("status", status);
            root.put("message", message);
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"status\":\"ERROR\",\"message\":\"Internal serialization error.\"}";
        }
    }

    public static String createResponseWithData(String status, String message, ObjectNode dataNode) {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("status", status);
            root.put("message", message);
            root.set("data", dataNode);
            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"status\":\"ERROR\",\"message\":\"Internal serialization error.\"}";
        }
    }

    // -------------------------------------------------------
    // REQUEST PARSERS (Client → Server)
    // -------------------------------------------------------

    public static JsonNode parseRequest(String jsonRequest) {
        try {
            return mapper.readTree(jsonRequest);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getString(JsonNode node, String field) {
        if (node == null || !node.has(field)) return "";
        JsonNode val = node.get(field);
        return val.isNull() ? "" : val.asText();
    }

    public static double getDouble(JsonNode node, String field) {
        if (node == null || !node.has(field)) return 0.0;
        return node.get(field).asDouble(0.0);
    }

    public static int getInt(JsonNode node, String field) {
        if (node == null || !node.has(field)) return 0;
        return node.get(field).asInt(0);
    }

    public static JsonNode getData(JsonNode requestNode) {
        if (requestNode == null || !requestNode.has("data")) {
            return mapper.createObjectNode();
        }
        return requestNode.get("data");
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }
}