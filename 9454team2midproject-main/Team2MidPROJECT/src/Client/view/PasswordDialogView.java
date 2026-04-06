package Client.view;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * PasswordDialogView — VIEW component for password change dialog.
 *
 */
public class PasswordDialogView {

    private Dialog<String[]> dialog;
    private PasswordField oldPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;

    // Create a new password change dialog
    public PasswordDialogView() {
        initialize();
    }

    private void initialize() {
        dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your current password and choose a new one");

        // Create form fields
        oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Current password");

        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password");

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");

        // Build grid layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        grid.add(new Label("Old Password:"), 0, 0);
        grid.add(oldPasswordField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Change Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Result converter
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                return new String[]{
                        oldPasswordField.getText(),
                        newPasswordField.getText(),
                        confirmPasswordField.getText()
                };
            }
            return null;
        });

        // Focus on old password field when dialog opens
        dialog.setOnShown(e -> oldPasswordField.requestFocus());
    }

    // Shows the dialog and returns the entered passwords
    public Optional<String[]> showAndWait() {
        return dialog.showAndWait();
    }

    // Checks if passwords match
    public static boolean passwordsMatch(String[] fields) {
        return fields != null && fields.length >= 3 && fields[1].equals(fields[2]);
    }
}
