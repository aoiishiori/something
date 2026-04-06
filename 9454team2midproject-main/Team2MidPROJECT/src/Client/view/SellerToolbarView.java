package Client.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * SellerToolbarView — VIEW component for seller product management toolbar.
 *
 */
public class SellerToolbarView extends HBox {

    private final Button addButton;
    private final Button editButton;
    private final Button deleteButton;

    // Create a new seller toolbar with Add, Edit, and Delete buttons.
    public SellerToolbarView() {
        super(10);
        setStyle("-fx-padding: 0 0 10 0;");

        // Create buttons
        addButton = createButton("➕ Add", "#27ae60");
        editButton = createButton("✏️ Edit", "#2980b9");
        deleteButton = createButton("🗑️ Delete", "#c0392b");

        getChildren().addAll(addButton, editButton, deleteButton);
    }

    private Button createButton(String text, String backgroundColor) {
        Button button = new Button(text);
        button.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold;",
                backgroundColor
        ));
        return button;
    }

    // Event Handlers
    public void setOnAddAction(EventHandler<ActionEvent> handler) {
        addButton.setOnAction(handler);
    }

    public void setOnEditAction(EventHandler<ActionEvent> handler) {
        editButton.setOnAction(handler);
    }

    public void setOnDeleteAction(EventHandler<ActionEvent> handler) {
        deleteButton.setOnAction(handler);
    }

    // Button Access
    public Button getAddButton() { return addButton; }
    public Button getEditButton() { return editButton; }
    public Button getDeleteButton() { return deleteButton; }
}
