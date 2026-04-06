package Server.util;

import Server.model.Account;
import Server.model.Product;
import Server.model.Transaction;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DATA_DIR = "data/json/";

    public static synchronized List<Account> readAccounts() {
        List<Account> list = new ArrayList<>();
        try {
            File f = new File(DATA_DIR + "Accounts.json");
            if (!f.exists()) return list;
            JsonNode root = mapper.readTree(f);
            for (JsonNode n : root.get("Accounts")) {
                Account a = new Account();
                a.setAccountId(n.path("accountId").asText());
                a.setUsername(n.path("username").asText());
                a.setPassword(n.path("password").asText());
                a.setRole(n.path("role").asText());
                a.setStatus(n.path("status").asText());
                list.add(a);
            }
        } catch (Exception e) {
            System.err.println("[JsonReader] readAccounts: " + e.getMessage());
        }
        return list;
    }

    public static synchronized List<Product> readProducts() {
        List<Product> list = new ArrayList<>();
        try {
            File f = new File(DATA_DIR + "Products.json");
            if (!f.exists()) return list;
            JsonNode root = mapper.readTree(f);
            for (JsonNode n : root.get("Products")) {
                Product p = new Product();
                p.setProductId(n.path("productId").asText());
                p.setSellerUsername(n.path("sellerUsername").asText());
                p.setName(n.path("name").asText());
                p.setCategory(n.path("category").asText());
                p.setOriginalPrice(n.path("originalPrice").asDouble());
                p.setDiscountedPrice(n.path("discountedPrice").asDouble());
                p.setAvailableQuantity(n.path("availableQuantity").asInt());
                p.setExpiryDate(n.path("expiryDate").asText());
                p.setStatus(n.path("status").asText());
                list.add(p);
            }
        } catch (Exception e) {
            System.err.println("[JsonReader] readProducts: " + e.getMessage());
        }
        return list;
    }

    public static ArrayNode readUserCart(String username) {
        try {
            String baseUser = getBaseUsername(username);
            String filePath = DATA_DIR + "carts/cart_" + baseUser + ".json";
            File f = new File(filePath);
            if (!f.exists()) {
                return new ObjectMapper().createArrayNode();
            }

            JsonNode root = mapper.readTree(f);
            JsonNode cartNode = root.get("cart");

            if (cartNode != null && cartNode.isArray()) {
                return (ArrayNode) cartNode;
            }
            return new ObjectMapper().createArrayNode();
        } catch (Exception e) {
            System.err.println("[JsonReader] readUserCart failed: " + e.getMessage());
            return new ObjectMapper().createArrayNode();
        }
    }

    private static String getBaseUsername(String username) {
        if (username != null && username.endsWith("_seller")) {
            return username.substring(0, username.length() - 7);
        }
        return username;
    }

    public static synchronized List<Transaction> readTransactions() {
        List<Transaction> list = new ArrayList<>();
        try {
            File f = new File(DATA_DIR + "Transactions.json");
            if (!f.exists()) return list;
            JsonNode root = mapper.readTree(f);
            for (JsonNode n : root.get("transactions")) {
                Transaction t = new Transaction();
                t.setTransactionId(n.path("transactionId").asText());
                t.setProductId(n.path("productId").asText());
                t.setBuyerUsername(n.path("buyerUsername").asText());
                t.setSellerUsername(n.path("sellerUsername").asText());
                t.setQuantity(n.path("quantity").asInt());
                t.setTimestamp(n.path("timestamp").asText());
                list.add(t);
            }
        } catch (Exception e) {
            System.err.println("[JsonReader] readTransactions: " + e.getMessage());
        }
        return list;
    }
}