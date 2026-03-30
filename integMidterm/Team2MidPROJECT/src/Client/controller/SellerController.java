package Client.controller;

import Client.model.CommonModel.ProductRow;
import Client.model.SellerModel;
import Client.VIEW.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * SellerController — MVC Controller for Seller_Dashboard.fxml
 */
public class SellerController extends BaseController implements Initializable {

    // FXML View Components
    @FXML private StackPane contentArea;
    @FXML private TableView<ProductRow> mainTable;
    @FXML private VBox settingsDropdown;
    @FXML private Circle userAvatar;
    @FXML private Label welcomeLabel;

    private final SellerModel sellerModel = new SellerModel();

    // Session State
    private ObservableList<ProductRow> productRows = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        welcomeLabel.setText("Welcome, " + username());
        setupTableColumns();
        mainTable.setItems(productRows);
        showDashboard(null);
        closeDropdown();
    }

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        mainTable.getColumns().clear();
        mainTable.getColumns().addAll(
                makeColumn("Product ID", "productId", 110),
                makeColumn("Name",       "name",      140),
                makeColumn("Category",   "category",  100),
                makeColumn("Price (₱)",  "price",     90),
                makeColumn("Qty",        "quantity",  55),
                makeColumn("Expires",    "expiryDate",110),
                makeColumn("Status",     "status",    90)
        );
    }

    private TableColumn<ProductRow, String> makeColumn(String title, String prop, int width) {
        TableColumn<ProductRow, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        return col;
    }

    private void loadMyProducts() {
        productRows.clear();
        for (String[] p : sellerModel.fetchMyProducts(username())) {
            // p = [productId, sellerUsername, name, category,
            //      originalPrice, discountedPrice, qty, expiryDate, status]
            productRows.add(new ProductRow(p[0], p[2], p[3], p[5], p[6], p[7], p[8]));
        }
    }

    // Navigation
    @FXML
    void showDashboard(ActionEvent event) {
        contentArea.getChildren().clear();
        // Use View component for empty state
        contentArea.getChildren().add(EmptyStateView.sellerDashboard());
    }

    @FXML
    void showProducts(ActionEvent event) {
        contentArea.getChildren().clear();

        // Use View component for toolbar
        SellerToolbarView toolbar = new SellerToolbarView();
        toolbar.setOnAddAction(e -> showAddDialog());
        toolbar.setOnEditAction(e -> showEditDialog());
        toolbar.setOnDeleteAction(e -> handleDelete());

        VBox view = new VBox(10);
        view.setStyle("-fx-padding: 20;");
        view.getChildren().addAll(toolbar, mainTable);
        VBox.setVgrow(mainTable, Priority.ALWAYS);
        contentArea.getChildren().add(view);
        loadMyProducts();
    }

    @FXML
    void showOrders(ActionEvent event) {
        contentArea.getChildren().clear();

        // Use View component for orders table
        OrdersTableView table = new OrdersTableView();
        table.setItems(FXCollections.observableArrayList(sellerModel.fetchMySales(username())));

        VBox view = new VBox(10);
        view.setStyle("-fx-padding: 20;");
        view.getChildren().addAll(new Label("My Sales"), table);
        VBox.setVgrow(table, Priority.ALWAYS);

        contentArea.getChildren().add(view);
    }

    // Product dialogs
    private void showAddDialog() {
        ProductDialogView dialog = new ProductDialogView(false);

        dialog.showAndWait().ifPresent(data -> {
            String[] result = sellerModel.addProduct(
                    username(),
                    data.getName(),
                    data.getCategory(),
                    parseDouble(data.getOriginalPrice()),
                    parseDouble(data.getDiscountedPrice()),
                    parseInt(data.getQuantity()),
                    data.getExpiryDate()
            );
            showOperationResult(result);
            loadMyProducts();
        });
    }

    private void showEditDialog() {
        ProductRow selected = mainTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a product to edit.");
            return;
        }

        ProductDialogView dialog = new ProductDialogView(true);

        // Pre-populate with existing data
        dialog.setProductData(
                selected.getName(),
                selected.getCategory(),
                null, // original price not shown in table
                selected.getPrice(),
                selected.getQuantity(),
                selected.getExpiryDate(),
                selected.getStatus()
        );

        dialog.showAndWait().ifPresent(data -> {
            String[] result = sellerModel.updateProduct(
                    username(),
                    selected.getProductId(),
                    data.getName(),
                    data.getCategory(),
                    parseDouble(data.getOriginalPrice()),
                    parseDouble(data.getDiscountedPrice()),
                    parseInt(data.getQuantity()),
                    data.getExpiryDate(),
                    data.getStatus()
            );
            showOperationResult(result);
            loadMyProducts();
        });
    }

    private void handleDelete() {
        ProductRow sel = mainTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Please select a product to delete.");
            return;
        }

        if (showConfirmation("Delete Product", "Delete " + sel.getName() + "?")) {
            String[] result = sellerModel.deleteProduct(username(), sel.getProductId());
            showOperationResult(result);
            loadMyProducts();
        }
    }

    private void showOperationResult(String[] result) {
        // result[0] = status, result[1] = message
        if (result != null && result.length >= 2) {
            if ("SUCCESS".equals(result[0])) {
                showSuccess(result[1]);
            } else {
                showError(result[1]);
            }
        } else {
            showError("Operation failed.");
        }
    }

    // Utility Methods
    private double parseDouble(String value) {
        try {
            return value != null && !value.isEmpty() ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(String value) {
        try {
            return value != null && !value.isEmpty() ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Settings dropdown
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

    // Settings Menu Actions

    @FXML
    void handleUpdateProfile(ActionEvent event) {
        showInfo("Profile update coming soon.");
        closeDropdown();
    }

    @FXML
    void handleShowNotifications(ActionEvent event) {
        closeDropdown();
        showOrders(null);
    }

    @FXML
    void handleChangePassword(ActionEvent event) {
        closeDropdown();
        showInfo("Log in as Buyer to change password.");
    }

    @FXML
    void switchToBuyer(ActionEvent event) {
        try {
            Stage stage = getStageFromComponent(welcomeLabel);
            navigateTo(stage, BUYER_DASHBOARD_FXML, "Marketplace");
        } catch (Exception e) {
            showError("Failed to switch to Buyer view: " + e.getMessage());
        }
    }

    // Logout
    @FXML
    public void handleLogout() {
        try {
            Stage stage = getStageFromComponent(welcomeLabel);
            navigateToLogin(stage);
        } catch (Exception e) {
            showError("Logout failed: " + e.getMessage());
        }
    }
}