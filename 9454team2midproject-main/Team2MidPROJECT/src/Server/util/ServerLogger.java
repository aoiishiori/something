package Server.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ServerLogger — logs all server events to:
 *   - The console (System.out / System.err)
 *   - data/json/server_log.json
 *
 * Strictly uses Jackson Tree Model (JsonNode). NO data binding.
 */
public class ServerLogger {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String LOG_FILE = "data/json/server_log.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    // -------------------------------------------------------
    // Initialize log file at server startup
    // -------------------------------------------------------
    public static synchronized void initLogFile() {
        File f = new File(LOG_FILE);
        new File("data/json").mkdirs();
        if (!f.exists()) {
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(f, mapper.createArrayNode());
            } catch (Exception e) {
                System.err.println("[ServerLogger] Could not create log file: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------
    // Public logging methods
    // -------------------------------------------------------

    public static void logServerStart(int port) {
        String msg = String.format("[%s] SERVER STARTED on port %d", now(), port);
        System.out.println(msg);
        appendEntry("SYSTEM", "SERVER_START", "port=" + port, "SUCCESS");
    }

    public static void logServerShutdown() {
        String msg = String.format("[%s] SERVER SHUTDOWN", now());
        System.out.println(msg);
        appendEntry("SYSTEM", "SERVER_SHUTDOWN", "N/A", "SUCCESS");
    }

    public static void logTransaction(String user, String action, String dataAffected) {
        String msg = String.format("[%s] USER=%s | ACTION=%s | DATA=%s",
                now(), user, action, dataAffected);
        System.out.println(msg);
        appendEntry(user, action, dataAffected, "SUCCESS");
    }

    public static void logError(String message) {
        String msg = String.format("[%s] ERROR: %s", now(), message);
        System.err.println(msg);
        appendEntry("SYSTEM", "ERROR", message, "FAILED");
    }

    public static void logClientConnect(String identifier) {
        System.out.println(String.format("[%s] CLIENT CONNECTED: %s", now(), identifier));
        appendEntry(identifier, "CLIENT_CONNECT", "N/A", "N/A");
    }

    public static void logClientDisconnect(String identifier) {
        System.out.println(String.format("[%s] CLIENT DISCONNECTED: %s", now(), identifier));
        appendEntry(identifier, "CLIENT_DISCONNECT", "N/A", "N/A");
    }

    // -------------------------------------------------------
    // Internal — append one entry to the JSON log array
    // -------------------------------------------------------
    private static synchronized void appendEntry(String user, String action,
                                                 String dataAffected, String result) {
        try {
            File f = new File(LOG_FILE);
            ArrayNode logsArray;

            // Read existing array without data binding
            if (f.exists() && f.length() > 0) {
                JsonNode root = mapper.readTree(f);
                logsArray = root.isArray() ? (ArrayNode) root : mapper.createArrayNode();
            } else {
                logsArray = mapper.createArrayNode();
            }

            ObjectNode entry = logsArray.addObject();
            entry.put("timestamp", now());
            entry.put("user", user != null ? user : "SYSTEM");
            entry.put("action", action != null ? action : "UNKNOWN");
            entry.put("dataAffected", dataAffected != null ? dataAffected : "N/A");
            entry.put("result", result != null ? result : "N/A");

            mapper.writerWithDefaultPrettyPrinter().writeValue(f, logsArray);

        } catch (Exception e) {
            System.err.println("[ServerLogger] Failed to write log entry: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Read all logs as a JSON string (for FETCH_LOGS action)
    // -------------------------------------------------------
    public static synchronized String readLogsAsJson() {
        File f = new File(LOG_FILE);
        if (!f.exists()) return "[]";
        try {
            JsonNode root = mapper.readTree(f);
            if (root.isArray()) {
                return mapper.writeValueAsString(root);
            }
            return "[]";
        } catch (Exception e) {
            System.err.println("[ServerLogger] Failed to read logs: " + e.getMessage());
            return "[]";
        }
    }

    private static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}