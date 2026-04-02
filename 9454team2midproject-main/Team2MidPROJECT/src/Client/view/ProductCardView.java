package Client.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * ProductCardView — VIEW component for displaying a product card.
 *
 */
public class ProductCardView extends VBox {

    private final Label nameLabel;
    private final Label categoryLabel;
    private final Label priceLabel;
    private final Label origPriceLabel;
    private final Label quantityLabel;
    private final Label expiryLabel;
    private final Label sellerLabel;
    private final Button buyButton;
    private final Button addToCartButton;

    private String productId;
    private String productName;

    public ProductCardView() {
        this(8);
    }

    public ProductCardView(double spacing) {
        super(spacing);
        setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; "
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        setPrefWidth(200);

        // Initialize components
        nameLabel = createNameLabel();
        categoryLabel = createCategoryLabel();
        sellerLabel = createSellerLabel();
        priceLabel = createPriceLabel();
        origPriceLabel = createOrigPriceLabel();
        quantityLabel = createQuantityLabel();
        expiryLabel = createExpiryLabel();
        buyButton = createBuyButton();
        addToCartButton = createAddToCartButton();

        getChildren().addAll(nameLabel, categoryLabel, sellerLabel, priceLabel,
                origPriceLabel, quantityLabel, expiryLabel, buyButton, addToCartButton);
    }

    // Factory methods for creating styled components
    private Label createNameLabel() {
        Label label = new Label("Unknown Product");
        label.setFont(new Font("System Bold", 14));
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #2c3e50;");
        return label;
    }

    private Label createCategoryLabel() {
        Label label = new Label();
        label.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        return label;
    }

    private Label createPriceLabel() {
        Label label = new Label("₱0");
        label.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 16;");
        return label;
    }

    private Label createOrigPriceLabel() {
        Label label = new Label("Was: ₱0");
        label.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11; -fx-strikethrough: true;");
        return label;
    }

    private Label createQuantityLabel() {
        Label label = new Label("Quantity Available: 0");
        label.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2980b9;");
        return label;
    }

    private Label createExpiryLabel() {
        Label label = new Label("Expires: N/A");
        label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11;");
        return label;
    }

    private Label createSellerLabel() {
        Label label = new Label("By: Unknown");
        label.setStyle("-fx-font-size: 11; -fx-font-style: italic;");
        return label;
    }

    private Button createBuyButton() {
        Button button = new Button("BUY NOW");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-background-radius: 5;");
        return button;
    }

    private Button createAddToCartButton() {
        Button button = new Button("ADD TO CART");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: #2c3e50; "
                + "-fx-font-weight: bold; -fx-background-radius: 5;");
        return button;
    }

    // Setters
    public void setProductData(String productId, String productName, String category,
                               String originalPrice, String discountedPrice,
                               String quantity, String expiryDate, String seller) {
        this.productId = productId;
        this.productName = productName != null && !productName.isEmpty() ? productName : "Unknown Product";

        nameLabel.setText(this.productName);
        categoryLabel.setText("📦 " + (category != null ? category : ""));
        priceLabel.setText("₱" + (discountedPrice != null ? discountedPrice : "0"));
        origPriceLabel.setText("Was: ₱" + (originalPrice != null ? originalPrice : "0"));
        quantityLabel.setText("Quantity Available: " + (quantity != null && !quantity.isEmpty() ? quantity : "0"));
        expiryLabel.setText("Expires: " + (expiryDate != null ? expiryDate : "N/A"));
        sellerLabel.setText("By: " + (seller != null ? seller : "Unknown"));
    }

    // Getters
    public Button getBuyButton() {
        return buyButton;
    }

    public Button getAddToCartButton() {
        return addToCartButton;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }
}