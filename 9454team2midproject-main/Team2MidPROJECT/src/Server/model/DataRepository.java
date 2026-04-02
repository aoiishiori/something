package Server.model;

import Server.util.JsonReader;
import Server.util.JsonWriter;

import java.util.List;

public class DataRepository {
    // Global lock for purchase operations — prevents overselling
    private static final Object productLock = new Object();

    public static List<Account> getAllAccounts() {
        return JsonReader.readAccounts();
    }

    public static void saveAllAccounts(List<Account> accounts) {
        JsonWriter.writeAccounts(accounts);
    }

    public static Account findAccountByUsername(String username) {
        List<Account> accounts = getAllAccounts();
        for (Account a : accounts) {
            if (a.getUsername().equals(username)) {
                return a;
            }
        }
        return null;
    }

    public static List<Product> getAllProducts() {
        return JsonReader.readProducts();
    }

    public static void saveAllProducts(List<Product> products) {
        JsonWriter.writeProducts(products);
    }

    public static Product findProductById(String productId) {
        List<Product> products = getAllProducts();
        for (Product p : products) {
            if (p.getProductId().equals(productId)) {
                return p;
            }
        }
        return null;
    }

    public static List<Transaction> getAllTransactions() {
        return JsonReader.readTransactions();
    }

    public static void addTransaction(Transaction tx) {
        JsonWriter.appendTransaction(tx);
    }

    /**
     * Reads products, updates stock, and saves back to file.
     * Prevent race conditions where two buyers buy the last item simultaneously.
     */
    public static String[] updateProductStock(String productId, int requestedQty) {
        synchronized (productLock) {
            List<Product> products = JsonReader.readProducts();
            Product target = null;

            for (Product p : products) {
                if (p.getProductId().equals(productId)) {
                    target = p;
                    break;
                }
            }

            if (target == null) {
                return new String[]{"FAILED", "Product not found."};
            }
            if (!"AVAILABLE".equals(target.getStatus())) {
                return new String[]{"FAILED", "Product is no longer available."};
            }
            if (target.getAvailableQuantity() < requestedQty) {
                return new String[]{"FAILED", "Not enough stock. Available: " + target.getAvailableQuantity()};
            }

            target.setAvailableQuantity(target.getAvailableQuantity() - requestedQty);
            if (target.getAvailableQuantity() == 0) {
                target.setStatus("UNAVAILABLE");
            }

            JsonWriter.writeProducts(products);

            return new String[]{"SUCCESS", target.getSellerUsername()};
        }
    }

}
