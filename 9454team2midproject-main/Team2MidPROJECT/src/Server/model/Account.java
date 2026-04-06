package Server.model;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.UUID;

public class Account {
    private String accountId;
    private String username;
    private String password;
    private String role; // ADMIN, BUYER, SELLER
    private String status; // PENDING, APPROVED, DENIED

    // Enum for clean business logic returns
    public enum PasswordChangeResult { SUCCESS, USER_NOT_FOUND, INVALID_OLD_PASSWORD }

    public Account() {
    }

    public Account(String accountId, String username, String password, String role, String status) {
        this.accountId = accountId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    // Getters and Setters...
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ObjectNode toJsonNode(ObjectMapper mapper) {
        ObjectNode n = mapper.createObjectNode();
        n.put("accountId", this.accountId);
        n.put("username", this.username);
        n.put("role", this.role);
        n.put("status", this.status);
        return n;
    }

    public static Account register(String username, String password, String role) {
        if (DataRepository.findAccountByUsername(username) != null) {
            return null; // Username taken
        }

        String status = "SELLER".equals(role) ? "PENDING" : "APPROVED";
        Account newAccount = new Account(
                "ACC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                username, password, role, status
        );

        List<Account> accounts = DataRepository.getAllAccounts();
        accounts.add(newAccount);
        DataRepository.saveAllAccounts(accounts);

        return newAccount;
    }

    public static Account updateStatus(String targetUsername, String newStatus) {
        Account target = DataRepository.findAccountByUsername(targetUsername);
        if (target == null) return null;

        target.setStatus(newStatus);
        DataRepository.saveAllAccounts(DataRepository.getAllAccounts());
        return target;
    }

    public static boolean deleteAccount(String targetUsername) {
        List<Account> accounts = DataRepository.getAllAccounts();
        boolean removed = accounts.removeIf(a -> a.getUsername().equals(targetUsername));

        if (removed) {
            DataRepository.saveAllAccounts(accounts);
        }
        return removed;
    }

    public static PasswordChangeResult changePassword(String username, String oldPassword, String newPassword) {
        Account account = DataRepository.findAccountByUsername(username);
        if (account == null) return PasswordChangeResult.USER_NOT_FOUND;

        if (!account.getPassword().equals(oldPassword)) return PasswordChangeResult.INVALID_OLD_PASSWORD;

        account.setPassword(newPassword);
        DataRepository.saveAllAccounts(DataRepository.getAllAccounts());
        return PasswordChangeResult.SUCCESS;
    }
}