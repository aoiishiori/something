package Client.controller;

import Client.model.AuthModel;
import Client.model.BuyerModel;
import Client.model.CommonModel.CartEntry;
import Client.VIEW.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * BuyerController — MVC Controller for Buyer_Dashboard.fxml
 */
public class BuyerController extends BaseController implements Initializable {

    // FXML View Components
    @FXML private FlowPane productContainer;
    @FXML private TextField searchBar;
    @FXML private VBox settingsDropdown;
    @FXML private Circle userAvatar;
    @FXML private Label welcomeLabel;

    private final BuyerModel buyerModel = new BuyerModel();
    private final AuthModel  authModel  = new AuthModel();

    // Session State
    private final List<CartEntry> cart = new ArrayList<>();
    private List<String[]> currentList;

    // Initialization
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        welcomeLabel.setText("Welcome back, " + username() + "!");
        loadAllProducts();
        closeDropdown();
    }

    private void loadAllProducts() {
        currentList = buyerModel.fetchAllProducts(username());
        renderProducts(currentList);
    }

    private void renderProducts(List<String[]> products) {
        productContainer.getChildren().clear();

        if (products == null || products.isEmpty()) {
            // Use View component for empty state
            productContainer.getChildren().add(EmptyStateView.noSearchResults());
            return;
        }

        for (String[] productData : products) {
            ProductCardView card = createProductCard(productData);
            productContainer.getChildren().add(card);
        }
    }

    private ProductCardView createProductCard(String[] p) {
        ProductCardView card = new ProductCardView();

        // Extract and pass data to View
        String productId = p[0];
        String seller = p.length > 1 ? p[1] : "Unknown";
        String name = p.length > 2 ? p[2] : "";
        String category = p.length > 3 ? p[3] : "";
        String origPrice = p.length > 4 ? p[4] : "0";
        String discPrice = p.length > 5 ? p[5] : "0";
        String quantity = p.length > 6 ? p[6] : "0";
        String expiry = p.length > 7 ? p[7] : "N/A";

        card.setProductData(productId, name, category, origPrice, discPrice, quantity, expiry, seller);

        // Bind event handlers (Controller logic, not View logic)
        card.getBuyButton().setOnAction(e -> handleBuy(card.getProductId(), card.getProductName()));
        card.getAddToCartButton().setOnAction(e -> handleAddToCart(p));

        return card;
    }

    // Event Handlers
    @FXML
    void handleSearchButton(ActionEvent event) {
        String keyword = searchBar.getText().trim();
        if (keyword.isEmpty()) {
            renderProducts(currentList);
        } else {
            renderProducts(buyerModel.searchProducts(username(), keyword));
        }
    }

    @FXML
    void handleSortLowToHigh(ActionEvent event) {
        if (currentList == null) return;
        currentList.sort(Comparator.comparingDouble(p -> parseDouble(p[5])));
        renderProducts(currentList);
    }

    @FXML
    void handleSortHighToLow(ActionEvent event) {
        if (currentList == null) return;
        currentList.sort((a, b) -> Double.compare(parseDouble(b[5]), parseDouble(a[5])));
        renderProducts(currentList);
    }

    // Purchase Operations
    private void handleBuy(String productId, String productName) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Buy Product");
        dialog.setHeaderText("Buying: " + productName);
        dialog.setContentText("Quantity:");
        dialog.showAndWait().ifPresent(input -> {
            try {
                int qty = Integer.parseInt(input.trim());
                if (qty <= 0) throw new NumberFormatException();

                String[] result = buyerModel.buyProduct(username(), productId, qty);
                showPurchaseResult(result);

                if ("SUCCESS".equals(result[0])) {
                    loadAllProducts();
                }
            } catch (NumberFormatException ex) {
                showAlert("Enter a valid quantity.");
            }
        });
    }

    private void handleAddToCart(String[] p) {
        String productId   = p[0];
        String productName = p.length > 2 && p[2] != null ? p[2] : "Unknown Product";
        String seller      = p.length > 1 ? p[1] : "Unknown";
        double unitPrice   = parseDouble(p.length > 5 ? p[5] : "0");
        int available = parseIntSafe(p.length > 6 ? p[6] : "0");

        if (available <= 0) {
            showAlert("This product is currently out of stock.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Adding: " + productName);
        dialog.setContentText("Quantity (max " + available + "):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int qty = Integer.parseInt(input.trim());
                if (qty <= 0) throw new NumberFormatException();

                int alreadyInCart = cart.stream()
                        .filter(c -> c.getProductId().equals(productId))
                        .mapToInt(CartEntry::getQuantity)
                        .sum();

                if (qty + alreadyInCart > available) {
                    int remaining = available - alreadyInCart;
                    showAlert("Cannot add that many. Remaining available: " + Math.max(remaining, 0));
                    return;
                }

                addToCart(productId, productName, seller, unitPrice, qty, available);
                showInfo("Added to cart: " + productName + " (x" + qty + ")");

            } catch (NumberFormatException ex) {
                showAlert("Enter a valid quantity.");
            }
        });
    }

    private void addToCart(String productId, String productName, String seller,
                           double unitPrice, int qty, int maxAvailable) {
        cart.stream()
                .filter(c -> c.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.addQuantity(qty),
                        () -> cart.add(new CartEntry(productId, productName, seller, unitPrice, qty, maxAvailable))
                );
    }

    // Cart Operations
    @FXML
    void handleViewCart(ActionEvent event) {
        if (cart.isEmpty()) {
            showInfo("Your cart is empty.");
            return;
        }

        StringBuilder summary = buildCartSummary();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, summary.toString(), ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("My Cart");
        alert.setHeaderText("Cart Overview");

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                checkoutCart();
            }
        });
    }

    private StringBuilder buildCartSummary() {
        StringBuilder sb = new StringBuilder("Items in your cart:\n\n");
        double total = 0.0;

        for (CartEntry c : cart) {
            double lineTotal = c.getLineTotal();
            total += lineTotal;
            sb.append("• ").append(c.getProductName())
                    .append(" (x").append(c.getQuantity()).append(")\n")
                    .append("  Seller: ").append(c.getSeller()).append("\n")
                    .append("  Price: ₱").append(c.getUnitPrice())
                    .append(" each, Subtotal: ₱")
                    .append(String.format("%.2f", lineTotal)).append("\n\n");
        }

        sb.append("Total: ₱").append(String.format("%.2f", total)).append("\n\n")
                .append("Proceed to checkout?");

        return sb;
    }

    private void checkoutCart() {
        if (cart.isEmpty()) {
            return;
        }

        StringBuilder summary = new StringBuilder();
        boolean anySuccess = false;
        boolean anyFailed  = false;

        for (CartEntry c : cart) {
            String[] result = buyerModel.buyProduct(username(), c.getProductId(), c.getQuantity());
            summary.append(c.getProductName())
                    .append(" (x").append(c.getQuantity()).append("): ")
                    .append(result[1]).append("\n\n");

            if ("SUCCESS".equals(result[0])) {
                anySuccess = true;
            } else {
                anyFailed = true;
            }
        }

        if (anySuccess) {
            cart.clear();
            loadAllProducts();
        }

        Alert.AlertType type = anySuccess && anyFailed ? Alert.AlertType.WARNING :
                anySuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;

        new Alert(type, summary.toString(), ButtonType.OK).showAndWait();
    }

    // User Settings
    @FXML
    void toggleSettings(MouseEvent event) {
        boolean v = settingsDropdown.isVisible();
        settingsDropdown.setVisible(!v);
        settingsDropdown.setManaged(!v);
    }

    private void closeDropdown() {
        settingsDropdown.setVisible(false);
        settingsDropdown.setManaged(false);
    }

    @FXML
    void handleUpdateProfile(ActionEvent event) {
        showInfo("Profile update coming soon.");
        closeDropdown();
    }

    @FXML
    void handleShowNotifications(ActionEvent event) {
        List<String[]> purchases = buyerModel.fetchMyPurchases(username());

        // Use View component for purchase history display
        PurchaseHistoryView purchaseHistoryView = new PurchaseHistoryView();
        purchaseHistoryView.setContent(purchases);
        purchaseHistoryView.show();

        closeDropdown();
    }

    @FXML
    void handleChangePassword(ActionEvent event) {
        closeDropdown();

        // Use View component for password dialog
        PasswordDialogView passwordDialog = new PasswordDialogView();

        passwordDialog.showAndWait().ifPresent(fields -> {
            if (!PasswordDialogView.passwordsMatch(fields)) {
                showAlert("Passwords don't match.");
                return;
            }

            AuthModel.ChangePasswordResult result = authModel.changePassword(username(), fields[0], fields[1]);
            showInfo(result.getMessage());
        });
    }

    @FXML
    void handleBecomeSeller(ActionEvent event) {
        closeDropdown();
        try {
            Stage s = new Stage();
            s.setTitle("Seller Registration");
            s.setScene(new Scene(FXMLLoader.load(
                    getClass().getResource(REGISTRATION_FXML))));
            s.show();
        } catch (Exception e) {
            showError("Cannot open form: " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        try {
            Stage stage = getStageFromComponent(welcomeLabel);
            navigateToLogin(stage);
        } catch (Exception e) {
            showError("Logout failed: " + e.getMessage());
        }
    }

    // Utility Methods
    private void showPurchaseResult(String[] result) {
        if ("SUCCESS".equals(result[0])) {
            showInfo(result[1]);
        } else {
            showError(result[1]);
        }
    }

    private double parseDouble(String v) {
        try {
            return v != null ? Double.parseDouble(v) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseIntSafe(String value) {
        try {
            return value != null && !value.isEmpty() ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}