package Server.util;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final Set<String> activeSessions =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static boolean login(String username) {
        return activeSessions.add(username); // returns false if already logged in
    }

    public static void logout(String username) {
        activeSessions.remove(username);
    }

    public static boolean isLoggedIn(String username) {
        return activeSessions.contains(username);
    }
}