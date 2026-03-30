package Client.VIEW;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.util.List;

/**
 * PurchaseHistoryView — VIEW component for displaying purchase history.
 *
 */
public class PurchaseHistoryView {

    private final Stage stage;
    private final TextArea textArea;

    // Create a new purchase history popup window
    public PurchaseHistoryView() {
        stage = new Stage();
        stage.setTitle("My Purchases");

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefSize(420, 300);
        textArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");

        stage.setScene(new Scene(textArea));
    }

    // Set the purchase history content
    public void setContent(List<String[]> purchases) {
        StringBuilder content = buildPurchaseHistoryText(purchases);
        textArea.setText(content.toString());
    }

    // Shows the purchase history window
    public void show() {
        stage.show();
    }

    // Closes the purchase history window
    public void close() {
        stage.close();
    }

    private StringBuilder buildPurchaseHistoryText(List<String[]> purchases) {
        StringBuilder sb = new StringBuilder("Your Recent Purchases:\n\n");

        if (purchases == null || purchases.isEmpty()) {
            sb.append("No purchases yet.");
        } else {
            for (String[] t : purchases) {
                sb.append("📦 ").append(safeGet(t, 0))
                        .append("\n Product: ").append(safeGet(t, 1))
                        .append("\n Seller: ").append(safeGet(t, 2))
                        .append("\n Qty: ").append(safeGet(t, 3))
                        .append("\n Date: ").append(safeGet(t, 4))
                        .append("\n\n");
            }
        }

        return sb;
    }

    private String safeGet(String[] array, int index) {
        if (array != null && index < array.length && array[index] != null) {
            return array[index];
        }
        return "N/A";
    }
}
