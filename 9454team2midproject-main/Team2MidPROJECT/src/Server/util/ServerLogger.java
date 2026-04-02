package Server.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ServerLogger — logs all server events to:
 *   - The console (System.out / System.err)
 *   - data/json/server_log.json
 *
 * Log entry format:
 * {
 *   "timestamp":    "2025-01-01 12:00:00",
 *   "user":         "john",
 *   "action":       "LOGIN",
 *   "dataAffected": "role=BUYER",
 *   "result":       "SUCCESS"
 * }
 *
 * REPLACES: ServerLogger (XML version)
 */
public class ServerLogger {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String LOG_FILE = "data/json/server_log.jsonl";
    private static final ObjectMapper mapper = new ObjectMapper();

    // -------------------------------------------------------
    // Initialize log file at server startup
    // -------------------------------------------------------
    public static synchronized void initLogFile() {
        File f = new File(LOG_FILE);
        new File("data/json").mkdirs();
        if (!f.exists()) {
            try {
                f.createNewFile();
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

            String jsonLine = String.format(
                    "{\"timestamp\":\"%s\",\"user\":\"%s\",\"action\":\"%s\",\"dataAffected\":\"%s\",\"result\":\"%s\"}",
                    now(),
                    user != null ? user : "SYSTEM",
                    action != null ? action : "UNKNOWN",
                    dataAffected != null ? dataAffected : "N/A",
                    result != null ? result : "N/A"
            ) + System.lineSeparator(); // Add a newline at the end

            // Append to file. Does NOT fetch or read existing data.
            Files.write(new File(LOG_FILE).toPath(), jsonLine.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

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
            // Read all lines from the JSONL file
            List<String> lines = Files.readAllLines(f.toPath());
            ArrayNode logsArray = mapper.createArrayNode();

            // Parse each line individually into a JSON array for the admin
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    JsonNode node = mapper.readTree(line);
                    logsArray.add(node);
                }
            }
            return mapper.writeValueAsString(logsArray);
        } catch (Exception e) {
            System.err.println("[ServerLogger] Failed to read logs: " + e.getMessage());
            return "[]";
        }
    }

    private static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}