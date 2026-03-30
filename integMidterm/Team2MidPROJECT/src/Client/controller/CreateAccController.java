package Client.controller;

import Client.model.AuthModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * CreateAccController — MVC Controller for Create_Account.fxml
 */
public class CreateAccController extends BaseController implements Initializable {

    // FXML View Components
    @FXML private TextField regUser;
    @FXML private PasswordField regPass;
    @FXML private PasswordField regRePass;
    @FXML private CheckBox showPass;
    @FXML private Button createButton;
    @FXML private TextField visiblePass;
    @FXML private TextField visibleRePass;

    private final AuthModel authModel = new AuthModel();

    private static final int MIN_USERNAME_LENGTH = 5;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String DEFAULT_ROLE = "BUYER";

    // Initialization
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Bidirectional binding for password visibility toggle
        visiblePass.textProperty().bindBidirectional(regPass.textProperty());
        visibleRePass.textProperty().bindBidirectional(regRePass.textProperty());

        // Ensure visibility fields start hidden
        toggleFields(regPass, visiblePass, false);
        toggleFields(regRePass, visibleRePass, false);
    }

    // Event Handlers
    @FXML
    void createHandler(ActionEvent event) {
        String username = regUser.getText().trim();
        String password = regPass.getText();
        String rePassword = regRePass.getText();

        if (!validateInput(username, password, rePassword)) {
            return;
        }

        AuthModel.RegisterResult result = authModel.register(username, password, DEFAULT_ROLE);

        if (result.isSuccess()) {
            showSuccess("Account created! You can now log in.");
            navigateToLoginScreen();
        } else {
            String message = result.getMessage();
            showError((message == null || message.isEmpty()) ? "Registration failed." : message);
        }
    }

    @FXML
    void showPassHandler(ActionEvent event) {
        boolean show = showPass.isSelected();
        toggleFields(regPass, visiblePass, show);
        toggleFields(regRePass, visibleRePass, show);
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

    private void toggleFields(PasswordField pField, TextField tField, boolean showText) {
        pField.setVisible(!showText);
        pField.setManaged(!showText);
        tField.setVisible(showText);
        tField.setManaged(showText);

        // Maintain focus: if user is typing and clicks show, keep cursor in the box
        if (showText && pField.isFocused()) tField.requestFocus();
        else if (!showText && tField.isFocused()) pField.requestFocus();
    }

    // Navigation
    private void navigateToLoginScreen() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        navigateToLogin(stage);
    }
}