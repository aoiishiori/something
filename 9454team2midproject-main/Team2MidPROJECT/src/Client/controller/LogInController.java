package Client.controller;

import Client.model.AuthModel;
import Client.model.SessionData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * LogInController — MVC Controller for LoginForm.fxml
 */
public class LogInController extends BaseController {

    // FXML View Components
    @FXML private TextField UserNameInput;
    @FXML private PasswordField PasswordInput;
    @FXML private Button LogInButton;
    @FXML private Hyperlink CreateAccount;

    private final AuthModel authModel = new AuthModel();

    // Event Handlers
    @FXML
    void handleLogIn(ActionEvent event) {
        String username = UserNameInput.getText().trim();
        String password = PasswordInput.getText().trim();

        if (!validateInput(username, password)) {
            return;
        }

        AuthModel.LoginResult result = authModel.login(username, password);

        handleLoginResult(result, username);
    }

    @FXML
    void handleSignUp(ActionEvent event) {
        Stage stage = (Stage) CreateAccount.getScene().getWindow();
        navigateTo(stage, CREATE_ACCOUNT_FXML, "Create Account");
    }

    // Input Validation
    private boolean validateInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter both username and password.");
            return false;
        }
        return true;
    }

    // Login Result Handling
    private void handleLoginResult(AuthModel.LoginResult result, String username) {
        if (result.isSuccess()) {
            // Save session
            SessionData.setUsername(username);
            SessionData.setRole(result.getRole());

            // Navigate to appropriate dashboard
            openDashboard(result.getRole());

        } else if (result.isPending()) {
            showInfo(result.getMessage());

        } else if (result.isDenied()) {
            showError(result.getMessage());

        } else {
            // FAILED or ERROR
            String message = result.getMessage();
            showError((message == null || message.isEmpty())
                    ? "Login failed. Please try again."
                    : message);
        }
    }

    // Dashboard Navigation
    private void openDashboard(String role) {
        Stage stage = (Stage) LogInButton.getScene().getWindow();

        switch (role) {
            case "ADMIN":
                navigateTo(stage, ADMIN_DASHBOARD_FXML, "Admin Panel");
                break;
            case "SELLER":
                navigateTo(stage, SELLER_DASHBOARD_FXML, "Seller Dashboard");
                break;
            default:
                navigateTo(stage, BUYER_DASHBOARD_FXML, "Marketplace");
                break;
        }
    }
}
