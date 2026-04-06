package Client.view;

import Client.model.CommonModel.CartEntry;
import javafx.scene.control.*;

import java.util.List;

/**
 * CartView — VIEW component for shopping cart display.
 *
 */
public class CartView {

    // Confirmation alert for cart checkout
    public static Alert createCartConfirmationAlert(List<CartEntry> cartItems) {
        StringBuilder content = buildCartSummary(cartItems);

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                content.toString(),
                ButtonType.OK,
                ButtonType.CANCEL
        );
        alert.setTitle("My Cart");
        alert.setHeaderText("Cart Overview");

        return alert;
    }

    // Result alert after checkout
    public static Alert createCheckoutResultAlert(String summary, boolean anySuccess, boolean anyFailed) {
        Alert.AlertType type = anySuccess && anyFailed ? Alert.AlertType.WARNING :
                anySuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;

        Alert alert = new Alert(type, summary, ButtonType.OK);
        alert.setTitle("Checkout Result");
        alert.setHeaderText(anySuccess && anyFailed ? "Partial Success" :
                anySuccess ? "Checkout Complete" : "Checkout Failed");

        return alert;
    }

    private static StringBuilder buildCartSummary(List<CartEntry> cartItems) {
        StringBuilder sb = new StringBuilder("Items in your cart:\n\n");
        double total = 0.0;

        for (CartEntry item : cartItems) {
            double lineTotal = item.getLineTotal();
            total += lineTotal;

            sb.append("• ").append(item.getProductName())
                    .append(" (x").append(item.getQuantity()).append(")\n")
                    .append("  Seller: ").append(item.getSeller()).append("\n")
                    .append("  Price: ₱").append(item.getUnitPrice())
                    .append(" each, Subtotal: ₱")
                    .append(String.format("%.2f", lineTotal)).append("\n\n");
        }

        sb.append("Total: ₱").append(String.format("%.2f", total)).append("\n\n")
                .append("Proceed to checkout?");

        return sb;
    }
}
