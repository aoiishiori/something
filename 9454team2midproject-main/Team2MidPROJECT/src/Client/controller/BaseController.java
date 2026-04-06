package Client.controller;

import Client.model.SessionData;
import Client.util.RMIClient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public abstract class BaseController {

    protected static final String FXML_PATH = "/Client/view/FXML/";
    protected static final String LOGIN_FXML = FXML_PATH + "LoginForm.fxml";
    protected static final String CREATE_ACCOUNT_FXML = FXML_PATH + "Create_Account.fxml";
    protected static final String REGISTRATION_FXML = FXML_PATH + "RegistrationForm.fxml";
    protected static final String ADMIN_DASHBOARD_FXML = FXML_PATH + "Admin_Dashboard.fxml";
    protected static final String SELLER_DASHBOARD_FXML = FXML_PATH + "Seller_Dashboard.fxml";
    protected static final String BUYER_DASHBOARD_FXML = FXML_PATH + "Buyer_Dashboard.fxml";

    protected String sendRequest(String requestXML) {
        return RMIClient.sendRequest(requestXML);
    }

    protected String username() { return SessionData.getUsername(); }
    protected String getCurrentRole() { return SessionData.getRole(); }
    protected void clearSession() { SessionData.clear(); }

    protected void navigateTo(Stage stage, String fxmlPath) {
        navigateTo(stage, fxmlPath, "");
    }

    protected void navigateTo(Stage stage, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource(fxmlPath)));
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            showError("Navigation failed: " + e.getMessage());
        }
    }

    protected void navigateToLogin(Stage stage) {
        RMIClient.sendRequest(RMIClient.buildRequest("LOGOUT", username()));
        clearSession();
        navigateTo(stage, LOGIN_FXML, "Login");
    }

    protected void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }
    protected void showAlert(String message) {
        new Alert(Alert.AlertType.WARNING, message, ButtonType.OK).showAndWait();
    }
    protected void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
    protected void showSuccess(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).showAndWait();
    }
    protected boolean showConfirmation(String title, String message) {
        return new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO)
                .showAndWait()
                .filter(btn -> btn == ButtonType.YES)
                .isPresent();
    }

    protected Stage getStageFromComponent(javafx.scene.Node node) {
        return (Stage) node.getScene().getWindow();
    }
}