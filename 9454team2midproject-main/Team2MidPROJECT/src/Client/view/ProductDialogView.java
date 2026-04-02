package Client.view;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.*;

/**
 * ProductDialogView — VIEW component for Add/Edit Product dialogs.
 *
 */
public class ProductDialogView {

    private Dialog<ProductData> dialog;
    private TextField nameField;
    private TextField categoryField;
    private TextField origPriceField;
    private TextField discPriceField;
    private TextField qtyField;
    private TextField expiryField;
    private ComboBox<String> statusCombo;

    private final boolean isEditMode;

    public ProductDialogView(boolean isEditMode) {
        this.isEditMode = isEditMode;
        initialize();
    }

    private void initialize() {
        dialog = new Dialog<>();
        dialog.setTitle(isEditMode ? "Edit Product" : "Add New Product");
        dialog.setHeaderText(isEditMode ? "Update product details" : "Enter product details");

        // Create form fields
        nameField = createTextField("Product Name");
        categoryField = createTextField("Category");
        origPriceField = createTextField("Original Price");
        discPriceField = createTextField("Discounted Price");
        qtyField = createTextField("Quantity");
        expiryField = createTextField("Expiry Date (YYYY-MM-DD)");

        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("AVAILABLE", "UNAVAILABLE");
        statusCombo.setValue("AVAILABLE");
        statusCombo.setMaxWidth(Double.MAX_VALUE);

        // Build grid layout
        GridPane grid = createFormGrid();
        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Result converter
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return new ProductData(
                        nameField.getText().trim(),
                        categoryField.getText().trim(),
                        origPriceField.getText().trim(),
                        discPriceField.getText().trim(),
                        qtyField.getText().trim(),
                        expiryField.getText().trim(),
                        statusCombo.getValue()
                );
            }
            return null;
        });
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        int row = 0;
        grid.add(new Label("Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryField, 1, row++);

        grid.add(new Label("Original Price:"), 0, row);
        grid.add(origPriceField, 1, row++);

        grid.add(new Label("Discounted Price:"), 0, row);
        grid.add(discPriceField, 1, row++);

        grid.add(new Label("Quantity:"), 0, row);
        grid.add(qtyField, 1, row++);

        grid.add(new Label("Expiry Date:"), 0, row);
        grid.add(expiryField, 1, row++);

        if (isEditMode) {
            grid.add(new Label("Status:"), 0, row);
            grid.add(statusCombo, 1, row);
        }

        return grid;
    }

    public void setProductData(String name, String category, String origPrice,
                               String discPrice, String qty, String expiry, String status) {
        if (name != null) nameField.setText(name);
        if (category != null) categoryField.setText(category);
        if (origPrice != null) origPriceField.setText(origPrice);
        if (discPrice != null) discPriceField.setText(discPrice);
        if (qty != null) qtyField.setText(qty);
        if (expiry != null) expiryField.setText(expiry);
        if (status != null) statusCombo.setValue(status);
    }

    // Show dialog and return result
    public Optional<ProductData> showAndWait() {
        return dialog.showAndWait();
    }

    public static class ProductData {
        private final String name;
        private final String category;
        private final String originalPrice;
        private final String discountedPrice;
        private final String quantity;
        private final String expiryDate;
        private final String status;

        public ProductData(String name, String category, String originalPrice,
                           String discountedPrice, String quantity,
                           String expiryDate, String status) {
            this.name = name;
            this.category = category;
            this.originalPrice = originalPrice;
            this.discountedPrice = discountedPrice;
            this.quantity = quantity;
            this.expiryDate = expiryDate;
            this.status = status;
        }

        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getOriginalPrice() { return originalPrice; }
        public String getDiscountedPrice() { return discountedPrice; }
        public String getQuantity() { return quantity; }
        public String getExpiryDate() { return expiryDate; }
        public String getStatus() { return status; }
    }

    public static Optional<Integer> promptQuantity(String title, String header, int maxQty) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText("Quantity (max " + maxQty + "):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int qty = Integer.parseInt(result.get().trim());
                if (qty <= 0) throw new NumberFormatException();
                return Optional.of(qty);
            } catch (NumberFormatException e) {
                return promptQuantity(title, "Please enter a valid whole number.", maxQty);
            }
        }
        return Optional.empty(); // User clicked cancel
    }
}
