package Server.util;

import Server.model.Account;
import Server.model.Product;
import Server.model.Transaction;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import java.io.File;
import java.util.List;

public class JsonWriter {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DATA_DIR = "data/json/";

    private static final Object accountsLock     = new Object();
    private static final Object productsLock     = new Object();
    private static final Object transactionsLock = new Object();

    public static void initializeDataFiles() {
        new File(DATA_DIR).mkdirs();
        createIfAbsent(DATA_DIR + "Accounts.json",
                "{\"Accounts\":[{\"accountId\":\"ACC-ADMIN0001\",\"username\":\"admin\","
                        + "\"password\":\"admin123\",\"role\":\"ADMIN\",\"status\":\"APPROVED\"}]}");
        createIfAbsent(DATA_DIR + "Products.json", "{\"Products\":[]}");
        createIfAbsent(DATA_DIR + "Transactions.json", "{\"transactions\":[]}");
    }

    private static void createIfAbsent(String path, String defaultContent) {
        File f = new File(path);
        if (!f.exists()) {
            try { mapper.readTree(defaultContent); // validate
                new java.io.FileWriter(f).write(defaultContent);
            } catch (Exception e) {
                System.err.println("[JsonWriter] init failed: " + e.getMessage());
            }
        }
    }

    public static void writeAccounts(List<Account> accounts) {
        synchronized (accountsLock) {
            try {
                ObjectNode root = mapper.createObjectNode();
                ArrayNode arr = root.putArray("Accounts");
                for (Account a : accounts) {
                    ObjectNode n = arr.addObject();
                    n.put("accountId", a.getAccountId());
                    n.put("username",  a.getUsername());
                    n.put("password",  a.getPassword());
                    n.put("role",      a.getRole());
                    n.put("status",    a.getStatus());
                }
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValue(new File(DATA_DIR + "Accounts.json"), root);
            } catch (Exception e) {
                System.err.println("[JsonWriter] writeAccounts: " + e.getMessage());
            }
        }
    }

    public static void writeProducts(List<Product> products) {
        synchronized (productsLock) {
            try {
                ObjectNode root = mapper.createObjectNode();
                ArrayNode arr = root.putArray("Products");
                for (Product p : products) {
                    ObjectNode n = arr.addObject();
                    n.put("productId",         p.getProductId());
                    n.put("sellerUsername",    p.getSellerUsername());
                    n.put("name",              p.getName());
                    n.put("category",          p.getCategory());
                    n.put("originalPrice",     p.getOriginalPrice());
                    n.put("discountedPrice",   p.getDiscountedPrice());
                    n.put("availableQuantity", p.getAvailableQuantity());
                    n.put("expiryDate",        p.getExpiryDate());
                    n.put("status",            p.getStatus());
                }
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValue(new File(DATA_DIR + "Products.json"), root);
            } catch (Exception e) {
                System.err.println("[JsonWriter] writeProducts: " + e.getMessage());
            }
        }
    }

    public static void appendTransaction(Transaction t) {
        synchronized (transactionsLock) {
            try {
                File f = new File(DATA_DIR + "Transactions.json");
                ObjectNode root = f.exists()
                        ? (ObjectNode) mapper.readTree(f)
                        : mapper.createObjectNode();

                ArrayNode arr = root.has("transactions")
                        ? (ArrayNode) root.get("transactions")
                        : root.putArray("transactions");

                ObjectNode n = arr.addObject();
                n.put("transactionId",  t.getTransactionId());
                n.put("productId",      t.getProductId());
                n.put("buyerUsername",  t.getBuyerUsername());
                n.put("sellerUsername", t.getSellerUsername());
                n.put("quantity",       t.getQuantity());
                n.put("timestamp",      t.getTimestamp());

                mapper.writerWithDefaultPrettyPrinter().writeValue(f, root);
            } catch (Exception e) {
                System.err.println("[JsonWriter] appendTransaction: " + e.getMessage());
            }
        }
    }
}