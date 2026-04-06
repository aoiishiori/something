package Server.controller;

import Server.model.Account;
import Server.model.DataRepository;
import Server.util.JsonUtils;
import Server.util.ServerLogger;
import Server.util.SessionManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.UUID;

/**
 * AccountController — handles all account-related requests from clients.
 *
 * Supported actions:
 *   LOGIN, REGISTER, FETCH_ALL_USERS,
 *   UPDATE_USER_STATUS, DELETE_USER, CHANGE_PASSWORD, FETCH_LOGS
 *
 * All responses are JSON strings.
 * All incoming data is parsed from a JsonNode "data" object.
 */
public class AccountController {

    // -------------------------------------------------------
    // LOGIN
    // data: { "password": "..." }
    // -------------------------------------------------------
    public String login(String username, JsonNode data) {
        String password = JsonUtils.getString(data, "password");
        Account account = DataRepository.findAccountByUsername(username);

        if (account != null && account.getPassword().equals(password)) {

            if ("PENDING".equals(account.getStatus())) {
                return JsonUtils.createResponse("PENDING",
                        "Your account is awaiting admin approval.");
            }
            if ("DENIED".equals(account.getStatus())) {
                return JsonUtils.createResponse("DENIED",
                        "Your account has been denied.");
            }

            // Block duplicate logins
            if (!SessionManager.login(username)) {
                return JsonUtils.createResponse("FAILED",
                        "This account is already logged in on another session.");
            }

            // Success — return role and accountId
            ServerLogger.logTransaction(username, "LOGIN", "role=" + account.getRole());

            ObjectNode dataNode = JsonUtils.getMapper().createObjectNode();
            dataNode.put("role", account.getRole());
            dataNode.put("accountId", account.getAccountId());

            return JsonUtils.createResponseWithData("SUCCESS", "Login successful.", dataNode);
        }
        return JsonUtils.createResponse("FAILED", "Invalid username or password.");
    }

    // -------------------------------------------------------
    // REGISTER
    // data: { "password": "...", "role": "BUYER" | "SELLER" }
    // -------------------------------------------------------
    public String register(String username, JsonNode data) {
        String password = JsonUtils.getString(data, "password");
        String role     = JsonUtils.getString(data, "role");
        if (role == null || role.isEmpty()) role = "BUYER";

        Account newAccount = Account.register(username, password, role);
        if (newAccount == null) {
            return JsonUtils.createResponse("FAILED", "Username already exists.");
        }

        ServerLogger.logTransaction(username, "REGISTER",
                "role=" + role + ", status=" + newAccount.getStatus());

        String message = "SELLER".equals(role)
                ? "Registration submitted. Awaiting admin approval."
                : "Account created successfully.";

        return JsonUtils.createResponse("SUCCESS", message);
    }

    // -------------------------------------------------------
    // FETCH ALL USERS (Admin only)
    // -------------------------------------------------------
    public String fetchAllUsers(String requestingUser) {
        List<Account> accounts = DataRepository.getAllAccounts();

        ObjectNode dataNode = JsonUtils.getMapper().createObjectNode();
        ArrayNode usersArr  = dataNode.putArray("users");

        for (Account a : accounts) {
            usersArr.add(a.toJsonNode(JsonUtils.getMapper()));
        }

        ServerLogger.logTransaction(requestingUser, "FETCH_ALL_USERS",
                "count=" + accounts.size());

        return JsonUtils.createResponseWithData("SUCCESS", "Users fetched.", dataNode);
    }

    // -------------------------------------------------------
    // UPDATE USER STATUS (Admin: APPROVE / DENY a seller)
    // data: { "targetUsername": "...", "newStatus": "APPROVED" | "DENIED" }
    // -------------------------------------------------------
    public String updateUserStatus(String adminUsername, JsonNode data) {
        String targetUsername = JsonUtils.getString(data, "targetUsername");
        String newStatus      = JsonUtils.getString(data, "newStatus");

        Account updated = Account.updateStatus(targetUsername, newStatus);
        if (updated == null) {
            return JsonUtils.createResponse("FAILED", "User not found: " + targetUsername);
        }

        ServerLogger.logTransaction(adminUsername, "UPDATE_USER_STATUS", "target=" + targetUsername + ", newStatus=" + newStatus);
        return JsonUtils.createResponse("SUCCESS", "User " + targetUsername + " status updated to " + newStatus + ".");
    }

    // -------------------------------------------------------
    // DELETE USER (Admin only)
    // data: { "targetUsername": "..." }
    // -------------------------------------------------------
    public String deleteUser(String adminUsername, JsonNode data) {
        String targetUsername = JsonUtils.getString(data, "targetUsername");

        boolean success = Account.deleteAccount(targetUsername);
        if (!success) {
            return JsonUtils.createResponse("FAILED", "User not found: " + targetUsername);
        }

        ServerLogger.logTransaction(adminUsername, "DELETE_USER", "deleted=" + targetUsername);
        return JsonUtils.createResponse("SUCCESS", "User " + targetUsername + " deleted.");
    }

    // -------------------------------------------------------
    // CHANGE PASSWORD
    // data: { "oldPassword": "...", "newPassword": "..." }
    // -------------------------------------------------------
    public String changePassword(String username, JsonNode data) {
        String oldPassword = JsonUtils.getString(data, "oldPassword");
        String newPassword = JsonUtils.getString(data, "newPassword");

        Account.PasswordChangeResult result = Account.changePassword(username, oldPassword, newPassword);
        ServerLogger.logTransaction(username, "CHANGE_PASSWORD", "N/A");

        switch (result) {
            case SUCCESS:
                return JsonUtils.createResponse("SUCCESS", "Password changed successfully.");
            case USER_NOT_FOUND:
                return JsonUtils.createResponse("FAILED", "User not found.");
            case INVALID_OLD_PASSWORD:
                return JsonUtils.createResponse("FAILED", "Old password is incorrect.");
            default:
                return JsonUtils.createResponse("FAILED", "Unknown error.");
        }
    }

    // -------------------------------------------------------
    // FETCH LOGS (Admin only)
    // Returns all log entries as a JSON array inside the data field
    // -------------------------------------------------------
    public String fetchLogs(String adminUsername) {
        ServerLogger.logTransaction(adminUsername, "FETCH_LOGS", "N/A");

        try {
            String logsJson = ServerLogger.readLogsAsJson();
            // Parse the array and wrap it in a data node
            JsonNode logsArray = JsonUtils.getMapper().readTree(logsJson);
            ObjectNode dataNode = JsonUtils.getMapper().createObjectNode();
            dataNode.set("logs", logsArray);
            return JsonUtils.createResponseWithData("SUCCESS", "Logs fetched.", dataNode);
        } catch (Exception e) {
            return JsonUtils.createResponse("FAILED", "Could not read logs: " + e.getMessage());
        }
    }
}