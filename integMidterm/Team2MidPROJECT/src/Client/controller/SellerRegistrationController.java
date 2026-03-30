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

public class SellerRegistrationController extends BaseController implements Initializable {

    // These match RegistrationForm.fxml exactly
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Label idFileName;
    @FXML private TextField storeNameField;
    @FXML private TextArea storeDescArea;
    @FXML private Label permitFileName;
    @FXML private TextField gcashField;

    // We need a reference node to get the Stage — use storeNameField
    private File idFile;
    private File permitFile;

    private final AuthModel authModel = new AuthModel();
    private static final String SELLER_ROLE = "SELLER";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // nothing to bind here — no password fields in this form
    }

    @FXML
    void handleUploadID(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Government ID");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image/PDF", "*.png","*.jpg","*.jpeg","*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        Stage stage = (Stage) storeNameField.getScene().getWindow();
        File f = fc.showOpenDialog(stage);
        if (f != null) {
            idFile = f;
            idFileName.setText(f.getName());
        }
    }

    @FXML
    void handleUploadPermit(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Business Permit");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image/PDF", "*.png","*.jpg","*.jpeg","*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        Stage stage = (Stage) storeNameField.getScene().getWindow();
        File f = fc.showOpenDialog(stage);
        if (f != null) {
            permitFile = f;
            permitFileName.setText(f.getName());
        }
    }

    @FXML
    void handleSubmit(ActionEvent event) {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String storeName = storeNameField.getText().trim();
        String gcash     = gcashField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || storeName.isEmpty()) {
            showAlert("Please fill in First Name, Last Name, and Store Name.");
            return;
        }

        // Username = firstName + "_" + lastName lowercased, store name as extra data
        String username = (firstName + "_" + lastName).toLowerCase().replace(" ", "_");

        // Register as SELLER — server marks PENDING, admin must approve
        AuthModel.RegisterResult result = authModel.registerSeller(username,
                "changeme123"); // default password — user must change after approval

        if (result.isSuccess()) {
            showSuccess("Application submitted for: " + storeName
                    + "\nUsername: " + username
                    + "\nDefault password: changeme123"
                    + "\nAwaiting admin approval.");
            closeWindow();
        } else {
            showError(result.getMessage() != null ? result.getMessage() : "Registration failed.");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) storeNameField.getScene().getWindow();
        stage.close();
    }
}