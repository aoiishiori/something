package Client.VIEW;

import javafx.scene.control.Label;

/**
 * EmptyStateView — VIEW component for empty/placeholder states.
 *
 */
public class EmptyStateView extends Label {

    // Create an empty state label with default styling
    public EmptyStateView(String message) {
        super(message);
        applyDefaultStyle();
    }

    // Create an empty state label with custom color
    public EmptyStateView(String message, String textColor) {
        super(message);
        setStyle("-fx-font-size: 15; -fx-text-fill: " + textColor + ";");
    }

    private void applyDefaultStyle() {
        setStyle("-fx-font-size: 15; -fx-text-fill: #7f8c8d;");
    }

    public static EmptyStateView noProducts() {
        return new EmptyStateView("No products available.");
    }

    public static EmptyStateView sellerDashboard() {
        return new EmptyStateView("Use the sidebar to manage products or view orders.");
    }

    public static EmptyStateView noSearchResults() {
        return new EmptyStateView("No products match your search.");
    }

    public static EmptyStateView noOrders() {
        return new EmptyStateView("No orders yet.");
    }

    public static EmptyStateView noUsers() {
        return new EmptyStateView("No users found.");
    }

    public static EmptyStateView noLogs() {
        return new EmptyStateView("No activity logs available.");
    }

    public static EmptyStateView custom(String message) {
        return new EmptyStateView(message);
    }
}
