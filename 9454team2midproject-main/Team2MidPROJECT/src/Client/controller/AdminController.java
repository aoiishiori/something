package Client.controller;

import Client.model.AdminModel;
import Client.model.CommonModel.LogRow;
import Client.model.CommonModel.UserRow;
import Client.util.CommonUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * AdminController — MVC Controller for Admin_Dashboard.fxml
 */
public class AdminController extends BaseController implements Initializable {

    // FXML View Components
    @FXML private StackPane adminContentArea;
    @FXML private TextField adminSearchField;
    @FXML private TableView<UserRow> userTable;
    @FXML private TableColumn<UserRow, String> colId;
    @FXML private TableColumn<UserRow, String> colUsername;
    @FXML private TableColumn<UserRow, String> colRole;
    @FXML private TableColumn<UserRow, String> colStatus;
    @FXML private ComboBox<String> roleFilter;
    @FXML private VBox serverControlView;
    @FXML private VBox userMgmtView;
    @FXML private VBox logViewerView;
    @FXML private TableView<LogRow> logTable;
    @FXML private TableColumn<LogRow, String> colTimestamp;
    @FXML private TableColumn<LogRow, String> colUser;
    @FXML private TableColumn<LogRow, String> colAction;
    @FXML private TableColumn<LogRow, String> colData;
    @FXML private TableColumn<LogRow, String> colResult;
    @FXML private Label serverStatusLabel;
    @FXML private Label viewTitle;

    // Model References
    private final AdminModel adminModel  = new AdminModel();

    // Session State
    private ObservableList<UserRow> allUsers = FXCollections.observableArrayList();
    private ObservableList<LogRow> allLogs = FXCollections.observableArrayList();

    // Initialization
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupRoleFilter();
        loadUsers();
        showUserManagement(null);
    }

    private void setupTableColumns() {
        // User table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("accountId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Log table columns
        if (logTable != null) {
            colTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
            colUser.setCellValueFactory(new PropertyValueFactory<>("user"));
            colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
            colData.setCellValueFactory(new PropertyValueFactory<>("dataAffected"));
            colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        }
    }

    private void setupRoleFilter() {
        roleFilter.setItems(FXCollections.observableArrayList("All", "ADMIN", "BUYER", "SELLER"));
        roleFilter.setValue("All");
        roleFilter.setOnAction(e -> applyFilter());
        adminSearchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    // Data Loading
    private void loadUsers() {
        allUsers.clear();
        for (String[] u : adminModel.fetchAllUsers(username())) {
            allUsers.add(new UserRow(u[0], u[1], u[2], u[3]));
        }
        userTable.setItems(allUsers);
    }

    private void applyFilter() {
        String kw   = adminSearchField.getText().toLowerCase();
        String role = roleFilter.getValue();
        ObservableList<UserRow> filtered = FXCollections.observableArrayList();
        for (UserRow r : allUsers) {
            boolean matchKw   = r.getUsername().toLowerCase().contains(kw)
                    || r.getAccountId().toLowerCase().contains(kw);
            boolean matchRole = "All".equals(role) || role.equals(r.getRole());
            if (matchKw && matchRole) filtered.add(r);
        }
        userTable.setItems(filtered);
    }

    // View Navigation
    @FXML
    void showUserManagement(ActionEvent event) {
        viewTitle.setText("User Management");
        setViewVisibility(userMgmtView, true);
        setViewVisibility(serverControlView, false);
        setViewVisibility(logViewerView, false);
        loadUsers();
    }

    @FXML
    void showSellerRequests(ActionEvent event) {
        viewTitle.setText("Seller Requests — Pending Accounts");
        roleFilter.setValue("SELLER");
        applyFilter();
        setViewVisibility(userMgmtView, true);
        setViewVisibility(serverControlView, false);
        setViewVisibility(logViewerView, false);
    }

    @FXML
    void showServerControl(ActionEvent event) {
        viewTitle.setText("Server Control");
        setViewVisibility(userMgmtView, false);
        setViewVisibility(serverControlView, true);
        setViewVisibility(logViewerView, false);
    }

    @FXML
    public void handleViewLogs() {
        viewTitle.setText("Server Activity Logs");
        setViewVisibility(userMgmtView, false);
        setViewVisibility(serverControlView, false);
        setViewVisibility(logViewerView, true);
        loadLogs();
    }

    private void setViewVisibility(VBox view, boolean visible) {
        if (view != null) {
            view.setVisible(visible);
            view.setManaged(visible);
        }
    }

    // Log Management
    private void loadLogs() {
        allLogs.clear();
        allLogs.addAll(adminModel.fetchLogs(username()));

        if (logTable != null) {
            logTable.setItems(allLogs);
        }
    }

    // User Management Actions
    @FXML
    void handleAcceptSeller(ActionEvent event) {
        UserRow sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Please select a seller first.");
            return;
        }
        if (!"SELLER".equals(sel.getRole())) {
            showAlert("Selected user is not a seller.");
            return;
        }

        boolean ok = adminModel.updateUserStatus(username(), sel.getUsername(), "APPROVED");
        if (ok) {
            showSuccess(sel.getUsername() + " has been approved as a seller.");
            loadUsers();
        } else {
            showError("Failed to approve seller.");
        }
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        UserRow sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Please select a user first.");
            return;
        }

        String newStatus = showStatusChoiceDialog(sel);
        if (newStatus != null) {
            boolean success = adminModel.updateUserStatus(username(), sel.getUsername(), newStatus);
            if (success) {
                showSuccess(sel.getUsername() + " is now " + newStatus + ".");
                loadUsers();
            } else {
                showError("Failed to update status.");
            }
        }
    }

    private String showStatusChoiceDialog(UserRow sel) {
        Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
        choice.setTitle("Update Account Status");
        choice.setHeaderText("User: " + sel.getUsername()
                + "  |  Role: " + sel.getRole()
                + "  |  Current Status: " + sel.getStatus());
        choice.setContentText("What action do you want to perform?");

        ButtonType btnApprove = new ButtonType("✅ APPROVE");
        ButtonType btnDeny    = new ButtonType("❌ DENY");
        ButtonType btnCancel  = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        choice.getButtonTypes().setAll(btnApprove, btnDeny, btnCancel);

        return choice.showAndWait().map(btn -> {
            if (btn == btnApprove) return "APPROVED";
            if (btn == btnDeny) return "DENIED";
            return null;
        }).orElse(null);
    }

    @FXML
    void handleDeleteAccount(ActionEvent event) {
        UserRow sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Select a user to delete.");
            return;
        }

        if (showConfirmation("Delete User", "Delete user: " + sel.getUsername() + "?")) {
            boolean success = adminModel.deleteUser(username(), sel.getUsername());
            if (success) {
                showSuccess("User deleted.");
                loadUsers();
            } else {
                showError("Failed to delete user.");
            }
        }
    }

    // Server control
    @FXML
    void handleRestartServer(ActionEvent event) {
        showInfo("Go to the Server console and type 'stop', then relaunch Server.java.");
    }

    // Logout
    @FXML
    public void handleLogout() {
        try {
            Stage stage = getStageFromComponent(viewTitle);
            navigateToLogin(stage);
        } catch (Exception e) {
            showError("Logout failed: " + e.getMessage());
        }
    }
}