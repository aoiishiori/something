package Server.util;

import shared.ClientCallback;
import java.util.concurrent.ConcurrentHashMap;

public class CallbackManager {

    private static final ConcurrentHashMap<String, ClientCallback> callbacks
            = new ConcurrentHashMap<>();

    public static void register(String username, ClientCallback cb) {
        callbacks.put(username, cb);
    }

    public static void unregister(String username) {
        callbacks.remove(username);
    }

    public static void notifyAll(String message) {
        callbacks.forEach((user, cb) -> {
            try {
                cb.onServerMessage(message);
            } catch (Exception e) {
                callbacks.remove(user); // dead client, clean up
            }
        });
    }

    public static void notifyProductUpdated(String productId) {
        callbacks.forEach((user, cb) -> {
            try {
                cb.onProductUpdated(productId);
            } catch (Exception e) {
                callbacks.remove(user);
            }
        });
    }
}