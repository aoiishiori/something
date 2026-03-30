package Client.controller;

import Client.model.AuthModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * SellerRegistrationController — MVC Controller for RegistrationForm.fxml
 * Handles seller registration requests which requires admin approval.
 */
public class SellerRegistrationController extends BaseController implements Initializable {

    // FXML View Components
    @FXML private TextField regUser;
    @FXML private PasswordField regPass;
    @FXML private PasswordField regRePass;
    @FXML private CheckBox showPass;
    @FXML private Button registerButton;
    @FXML private TextField visiblePass;
    @FXML private TextField visibleRePass;
    @FXML private Label idFileNameLabel;
    @FXML private Label permitFileNameLabel;

    private final AuthModel authModel = new AuthModel();

    // Uploaded File References
    private File idFile;
    private File permitFile;

    private static final int MIN_USERNAME_LENGTH = 5;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String SELLER_ROLE = "SELLER";

    // Initialization
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Bidirectional binding for password visibility toggle
        visiblePass.textProperty().bindBidirectional(regPass.textProperty());
        visibleRePass.textProperty().bindBidirectional(regRePass.textProperty());

        // Ensure visibility fields start hidden
        setFieldVisibility(regPass, visiblePass, false);
        setFieldVisibility(regRePass, visibleRePass, false);
    }

    // File Upload Handlers
    @FXML
    void handleUploadID(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select ID Document");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) registerButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            idFile = selectedFile;
            if (idFileNameLabel != null) {
                idFileNameLabel.setText(selectedFile.getName());
            }
        }
    }

    @FXML
    void handleUploadPermit(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Business Permit");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) registerButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            permitFile = selectedFile;
            if (permitFileNameLabel != null) {
                permitFileNameLabel.setText(selectedFile.getName());
            }
        }
    }

    // Form Submission
    @FXML
    void handleSubmit(ActionEvent event) {
        String username = regUser.getText().trim();
        String password = regPass.getText();
        String confirmPassword = regRePass.getText();

        if (!validateInput(username, password, confirmPassword)) {
            return;
        }

        AuthModel.RegisterResult result = authModel.register(username, password, SELLER_ROLE);

        if (result.isSuccess()) {
            showSuccess("Registration submitted. Awaiting admin approval.");
            closeWindow();
        } else {
            String message = result.getMessage();
            showError((message == null || message.isEmpty()) ? "Registration failed." : message);
        }
    }

    @FXML
    void handleRegister(ActionEvent event) {
        handleSubmit(event);
    }

    @FXML
    void showPassHandler(ActionEvent event) {
        boolean showPlainText = showPass.isSelected();
        setFieldVisibility(regPass, visiblePass, showPlainText);
        setFieldVisibility(regRePass, visibleRePass, showPlainText);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }

    // Input Validation
    private boolean validateInput(String username, String password, String confirmPassword) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Please fill in all fields.");
            return false;
        }

        if (username.length() < MIN_USERNAME_LENGTH) {
            showAlert("Username must be at least " + MIN_USERNAME_LENGTH + " characters.");
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            showAlert("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Passwords do not match.");
            return false;
        }

        return true;
    }

    // Field Visibility Helper
    private void setFieldVisibility(PasswordField passwordField, TextField textField, boolean showText) {
        passwordField.setVisible(!showText);
        passwordField.setManaged(!showText);
        textField.setVisible(showText);
        textField.setManaged(showText);

        if (showText && passwordField.isFocused()) {
            textField.requestFocus();
        } else if (!showText && textField.isFocused()) {
            passwordField.requestFocus();
        }
    }

    // Window Management
    private void closeWindow() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.close();
    }
}