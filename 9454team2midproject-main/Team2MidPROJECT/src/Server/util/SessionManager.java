package Server.util;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final ConcurrentHashMap<String, Long> activeSessions = new ConcurrentHashMap<>();

    // 5 minutes timeout in milliseconds (5 * 60 * 1000)
    private static final long SESSION_TIMEOUT_MS = 5 * 60 * 1000;

    public static boolean login(String username) {
        long now = System.currentTimeMillis();

        // Check if user is currently marked as "logged in"
        if (activeSessions.containsKey(username)) {
            long lastLoginTime = activeSessions.get(username);

            // If they logged in over 5 minutes ago, assume they crashed and evict the old session
            if (now - lastLoginTime > SESSION_TIMEOUT_MS) {
                activeSessions.remove(username);
            } else {
                return false;
            }
        }

        // Register the new session with the current timestamp
        activeSessions.put(username, now);
        return true;
    }

    // Remove user session when the user clicks logout
    public static void logout(String username) {
        activeSessions.remove(username);
    }
}