package Server.model;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Product {
    private String productId;
    private String sellerUsername;
    private String name;
    private String category;
    private double originalPrice;
    private double discountedPrice;
    private int availableQuantity;
    private String expiryDate; // Format: YYYY-MM-DD
    private String status; // AVAILABLE, UNAVAILABLE
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public Product() {
    }

    public Product(String productId, String sellerUsername, String name, String category,
                   double originalPrice, double discountedPrice, int availableQuantity,
                   String expiryDate, String status) {
        this.productId = productId;
        this.sellerUsername = sellerUsername;
        this.name = name;
        this.category = category;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.availableQuantity = availableQuantity;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    // Getters and Setters...
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ObjectNode toJsonNode(ObjectMapper mapper) {
        ObjectNode n = mapper.createObjectNode();
        n.put("productId", this.productId);
        n.put("sellerUsername", this.sellerUsername);
        n.put("name", this.name);
        n.put("category", this.category);
        n.put("originalPrice", this.originalPrice);
        n.put("discountedPrice", this.discountedPrice);
        n.put("availableQuantity", this.availableQuantity);
        n.put("expiryDate", this.expiryDate);
        n.put("status", this.status);
        return n;
    }

    public void applyUpdates(String name, String category, String expiry, String status,
                             double origPrice, double discPrice, int qty, boolean hasOrigPrice,
                             boolean hasDiscPrice, boolean hasQty) {
        if (name != null && !name.isEmpty()) this.name = name;
        if (category != null && !category.isEmpty()) this.category = category;
        if (expiry != null && !expiry.isEmpty()) this.expiryDate = expiry;
        if (status != null && !status.isEmpty()) this.status = status;
        if (hasOrigPrice) this.originalPrice = origPrice;
        if (hasDiscPrice) this.discountedPrice = discPrice;
        if (hasQty) this.availableQuantity = qty;
    }

    public static Product addProduct(String sellerUsername, String name, String category,
                                     double origPrice, double discPrice, int qty, String expiry) {
        Product p = new Product();
        p.setProductId("PRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        p.setSellerUsername(sellerUsername);
        p.setName(name);
        p.setCategory(category);
        p.setOriginalPrice(origPrice);
        p.setDiscountedPrice(discPrice);
        p.setAvailableQuantity(qty);
        p.setExpiryDate(expiry);
        p.setStatus("AVAILABLE");

        List<Product> products = DataRepository.getAllProducts();
        products.add(p);
        DataRepository.saveAllProducts(products);
        return p;
    }

    public static List<Product> fetchAvailableProducts() {
        List<Product> available = new ArrayList<>();
        for (Product p : DataRepository.getAllProducts()) {
            if ("AVAILABLE".equals(p.getStatus()) && p.getAvailableQuantity() > 0) {
                available.add(p);
            }
        }
        return available;
    }

    public static List<Product> fetchSellerProducts(String sellerUsername) {
        List<Product> sellerProds = new ArrayList<>();
        for (Product p : DataRepository.getAllProducts()) {
            if (sellerUsername.equals(p.getSellerUsername())) {
                sellerProds.add(p);
            }
        }
        return sellerProds;
    }

    public static Product updateProduct(String sellerUsername, String productId, String name, String category,
                                        double origPrice, double discPrice, int qty, String expiry, String status,
                                        boolean hasOrigPrice, boolean hasDiscPrice, boolean hasQty) {
        List<Product> products = DataRepository.getAllProducts();
        for (Product p : products) {
            if (p.getProductId().equals(productId) && p.getSellerUsername().equals(sellerUsername)) {
                p.applyUpdates(name, category, expiry, status, origPrice, discPrice, qty, hasOrigPrice, hasDiscPrice, hasQty);
                DataRepository.saveAllProducts(products);
                return p;
            }
        }
        return null; // Not found
    }

    public static boolean deleteProduct(String sellerUsername, String productId) {
        List<Product> products = DataRepository.getAllProducts();
        boolean removed = products.removeIf(p -> p.getProductId().equals(productId) && p.getSellerUsername().equals(sellerUsername));
        if (removed) {
            DataRepository.saveAllProducts(products);
        }
        return removed;
    }

    public static List<Product> searchAvailableProducts(String keyword) {
        String kw = keyword.toLowerCase();
        List<Product> results = new ArrayList<>();
        for (Product p : DataRepository.getAllProducts()) {
            if ("AVAILABLE".equals(p.getStatus()) && p.getAvailableQuantity() > 0) {
                if (p.getName().toLowerCase().contains(kw) || p.getCategory().toLowerCase().contains(kw)) {
                    results.add(p);
                }
            }
        }
        return results;
    }

    public static String[] buyProduct(String buyerUsername, String productId, int requested) {
        if (requested <= 0) requested = 1;
        String[] stockResult = DataRepository.updateProductStock(buyerUsername, productId, requested);
        if (!"SUCCESS".equals(stockResult[0])) {
            return stockResult; // Returns failure array
        }

        String sellerUsername = stockResult[1];

        // Create Transaction Record
        Transaction tx = new Transaction(
                "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                productId, buyerUsername, sellerUsername, requested,
                LocalDateTime.now().format(TS_FORMAT)
        );
        DataRepository.addTransaction(tx);

        return new String[]{"SUCCESS", "Purchase successful! Transaction ID: " + tx.getTransactionId(), sellerUsername};
    }
}