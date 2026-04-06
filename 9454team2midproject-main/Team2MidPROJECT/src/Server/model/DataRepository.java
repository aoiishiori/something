package Server.model;

import Server.util.JsonReader;
import Server.util.JsonWriter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.List;

public class DataRepository {
    // Global lock for purchase operations — prevents overselling
    private static final Object productLock = new Object();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DATA_DIR = "data/json/";

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
     * Prevent race conditions and avoid fetching/mapping all products.
     */
    public static String[] updateProductStock(String buyerUsername, String productId, int requestedQty) {
        synchronized (productLock) {
            try {
                File f = new File(DATA_DIR + "Products.json");
                if (!f.exists()) {
                    return new String[]{"FAILED", "Product data file missing."};
                }

                ObjectNode root = (ObjectNode) mapper.readTree(f);
                ArrayNode productsArr = (ArrayNode) root.get("Products");

                // Find and modify the specific node only
                for (JsonNode node : productsArr) {
                    if (productId.equals(node.path("productId").asText())) {

                        // Prevent purchasing own product [e.g. show = show_seller, reject buying own product; show != dave, accept buying (not own) product]
                        String sellerUsername = node.path("sellerUsername").asText();

                        String buyerBase = buyerUsername.endsWith("_seller") ? buyerUsername.substring(0, buyerUsername.length() - 7) : buyerUsername;
                        String sellerBase = sellerUsername.endsWith("_seller") ? sellerUsername.substring(0, sellerUsername.length() - 7) : sellerUsername;

                        if (buyerBase.equals(sellerBase)) {
                            return new String[]{"FAILED", "You cannot buy your own product."};
                        }

                        int currentQty = node.path("availableQuantity").asInt();
                        String status = node.path("status").asText();

                        if (!"AVAILABLE".equals(status)) {
                            return new String[]{"FAILED", "Product is no longer available."};
                        }
                        if (currentQty < requestedQty) {
                            return new String[]{"FAILED", "Not enough stock. Available: " + currentQty};
                        }

                        ObjectNode targetNode = (ObjectNode) node;
                        int newQty = currentQty - requestedQty;
                        targetNode.put("availableQuantity", newQty);

                        if (newQty == 0) {
                            targetNode.put("status", "UNAVAILABLE");
                        }

                        mapper.writerWithDefaultPrettyPrinter().writeValue(f, root);
                        return new String[]{"SUCCESS", sellerUsername};
                    }
                }
                return new String[]{"FAILED", "Product not found."};

            } catch (Exception e) {
                System.err.println("[DataRepository] Stock update error: " + e.getMessage());
                return new String[]{"FAILED", "Server data error."};
            }
        }
    }
}