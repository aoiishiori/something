package Server.model;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private String transactionId;
    private String productId;
    private String buyerUsername;
    private String sellerUsername;
    private int quantity;
    private String timestamp; // Format: YYYY-MM-DDTHH:MM:SS

    public Transaction() {
    }

    public Transaction(String transactionId, String productId, String buyerUsername,
                       String sellerUsername, int quantity, String timestamp) {
        this.transactionId = transactionId;
        this.productId = productId;
        this.buyerUsername = buyerUsername;
        this.sellerUsername = sellerUsername;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    // Getters and Setters...
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getBuyerUsername() {
        return buyerUsername;
    }

    public void setBuyerUsername(String buyerUsername) {
        this.buyerUsername = buyerUsername;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ObjectNode toJsonNode(ObjectMapper mapper) {
        ObjectNode n = mapper.createObjectNode();
        n.put("transactionId", this.transactionId);
        n.put("productId", this.productId);
        n.put("buyerUsername", this.buyerUsername);
        n.put("sellerUsername", this.sellerUsername);
        n.put("quantity", this.quantity);
        n.put("timestamp", this.timestamp);
        return n;
    }

    public static List<Transaction> fetchBuyerPurchases(String buyerUsername) {
        List<Transaction> purchases = new ArrayList<>();
        for (Transaction t : DataRepository.getAllTransactions()) {
            if (buyerUsername.equals(t.getBuyerUsername())) {
                purchases.add(t);
            }
        }
        return purchases;
    }

    public static List<Transaction> fetchSellerSales(String sellerUsername) {
        List<Transaction> sales = new ArrayList<>();
        for (Transaction t : DataRepository.getAllTransactions()) {
            if (sellerUsername.equals(t.getSellerUsername())) {
                sales.add(t);
            }
        }
        return sales;
    }

    public static List<Transaction> fetchAllTransactions() {
        return DataRepository.getAllTransactions();
    }
}