package Client.model;

import Client.util.RMIClient;
import Client.util.CommonUtils;

/**
 * AuthModel — MVC Model for authentication.
 * Handles LOGIN, REGISTER, and CHANGE_PASSWORD requests.
 *
 * All requests/responses now use JSON protocol.
 */
public class AuthModel {

    // -------------------------------------------------------
    // LOGIN
    // -------------------------------------------------------
    public LoginResult login(String username, String password) {
        String dataJson = "{\"password\":\"" + CommonUtils.escapeJson(password) + "\"}";
        String request  = RMIClient.buildRequest("LOGIN", username, dataJson);
        String response = RMIClient.sendRequest(request);

        String status  = RMIClient.getStatus(response);
        String message = RMIClient.getMessage(response);

        if ("SUCCESS".equals(status)) {
            String dataBlock = RMIClient.getDataBlock(response);
            String role      = CommonUtils.extractField(dataBlock, "role");
            String accountId = CommonUtils.extractField(dataBlock, "accountId");
            return new LoginResult(status, message, role, accountId);
        }

        return new LoginResult(status, message, null, null);
    }

    // -------------------------------------------------------
    // REGISTER
    // -------------------------------------------------------
    public RegisterResult register(String username, String password, String role) {
        String dataJson = "{"
                + "\"password\":\"" + CommonUtils.escapeJson(password) + "\","
                + "\"role\":\"" + CommonUtils.escapeJson(role) + "\""
                + "}";
        String request  = RMIClient.buildRequest("REGISTER", username, dataJson);
        String response = RMIClient.sendRequest(request);

        return new RegisterResult(RMIClient.getStatus(response), RMIClient.getMessage(response));
    }

    /**
     * Registers as a SELLER — server will mark status as PENDING.
     */
    public RegisterResult registerSeller(String username, String password) {
        return register(username, password, "SELLER");
    }

    // -------------------------------------------------------
    // CHANGE PASSWORD
    // -------------------------------------------------------
    public ChangePasswordResult changePassword(String username,
                                               String oldPassword, String newPassword) {
        String dataJson = "{"
                + "\"oldPassword\":\"" + CommonUtils.escapeJson(oldPassword) + "\","
                + "\"newPassword\":\"" + CommonUtils.escapeJson(newPassword) + "\""
                + "}";
        String request  = RMIClient.buildRequest("CHANGE_PASSWORD", username, dataJson);
        String response = RMIClient.sendRequest(request);

        return new ChangePasswordResult(
                RMIClient.getStatus(response),
                RMIClient.getMessage(response));
    }

    // =====================================================
    // RESULT CLASSES
    // =====================================================

    public static class LoginResult {
        private final String status;
        private final String message;
        private final String role;
        private final String accountId;

        public LoginResult(String status, String message, String role, String accountId) {
            this.status = status;
            this.message = message;
            this.role = role;
            this.accountId = accountId;
        }

        public String getStatus()    { return status; }
        public String getMessage()   { return message; }
        public String getRole()      { return role; }
        public String getAccountId() { return accountId; }

        public boolean isSuccess() { return "SUCCESS".equals(status); }
        public boolean isPending() { return "PENDING".equals(status); }
        public boolean isDenied()  { return "DENIED".equals(status); }
        public boolean isFailed()  { return "FAILED".equals(status); }
    }

    public static class RegisterResult {
        private final String status;
        private final String message;

        public RegisterResult(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus()  { return status; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return "SUCCESS".equals(status); }
    }

    public static class ChangePasswordResult {
        private final String status;
        private final String message;

        public ChangePasswordResult(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus()  { return status; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return "SUCCESS".equals(status); }
    }
}