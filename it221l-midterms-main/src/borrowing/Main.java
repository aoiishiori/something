package borrowing;

import java.sql.*;
import java.util.Scanner;

/**
 * Main - Entry point for the CIS Facility/Equipment Borrowing System.
 * Provides a role-based console menu.
 *
 * IT 221L — Activity 4 (Midterms)
 * Team: ThesisIT
 */
public class Main {

    private static final String DIVIDER = "=".repeat(60);
    private static final String SYSTEM_NAME =
            "  CIS FACILITY / EQUIPMENT BORROWING SYSTEM";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Test DB connection at startup
        try {
            Database.getConnection();
        } catch (SQLException e) {
            System.err.println("[FATAL] Cannot connect to database: " + e.getMessage());
            System.err.println("  Check your DB credentials in Database.java");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            String role = showRoleMenu(scanner);
            if (role == null) {
                running = false;
                continue;
            }
            showRoleDashboard(scanner, role);
        }

        Database.closeConnection();
        System.out.println("\n  Thank you for using the CIS Borrowing System. Goodbye!");
        scanner.close();
    }

    // ----------------------------------------------------------------
    // Role selection (simulates login role context)
    // ----------------------------------------------------------------
    private static String showRoleMenu(Scanner scanner) {
        System.out.println("\n" + DIVIDER);
        System.out.println(SYSTEM_NAME);
        System.out.println(DIVIDER);
        System.out.println("  Select your role to continue:");
        System.out.println("  [1] Custodian");
        System.out.println("  [2] Borrower (Student / Faculty)");
        System.out.println("  [3] Head of Office / Admin");
        System.out.println("  [0] Exit System");
        System.out.println(DIVIDER);
        System.out.print("  Role: ");
        String input = scanner.nextLine().trim();
        return switch (input) {
            case "1" -> "CUSTODIAN";
            case "2" -> "BORROWER";
            case "3" -> "ADMIN";
            case "0" -> null;
            default -> {
                System.out.println("  [!] Invalid selection.");
                yield "";
            }
        };
    }

    // ----------------------------------------------------------------
    // Role-based dashboard — only shows UIs relevant to the actor
    // ----------------------------------------------------------------
    private static void showRoleDashboard(Scanner scanner, String role) {
        if (role.isEmpty()) return;

        boolean back = false;
        while (!back) {
            System.out.println("\n" + DIVIDER);
            System.out.printf("  DASHBOARD — %s%n", role);
            System.out.println(DIVIDER);

            switch (role) {
                case "CUSTODIAN" -> {
                    System.out.println("  [1] UI-01 | View Equipment Status");
                    System.out.println("  [2] UI-02 | View Borrow Records (by Class / Event)");
                    System.out.println("  [3] UI-03 | View Unreturned / Problematic Items");
                    System.out.println("  [4] UI-04 | View Borrower History");
                    System.out.println("  [5] UI-05 | View Lab Classes & Rosters");
                    System.out.println("  [6] UI-06 | View Students");
                    System.out.println("  [7] UI-07 | View Staff & Faculty");
                    System.out.println("  [0] Back to Role Selection");
                    System.out.println(DIVIDER);
                    System.out.print("  Choice: ");
                    String c = scanner.nextLine().trim();
                    switch (c) {
                        case "1" -> EquipmentStatus.show(scanner);
                        case "2" -> BorrowRecords.show(scanner);
                        case "3" -> UnreturnedItems.show(scanner);
                        case "4" -> BorrowerHistory.show(scanner);
                        case "5" -> LabClasses.show(scanner);
                        case "6" -> ViewStudents.show(scanner);
                        case "7" -> ViewStaffFaculty.show(scanner);
                        case "0" -> back = true;
                        default  -> System.out.println("  [!] Invalid option.");
                    }
                }
                case "BORROWER" -> {
                    System.out.println("  [1] UI-04 | View My Borrow History");
                    System.out.println("  [0] Back to Role Selection");
                    System.out.println(DIVIDER);
                    System.out.print("  Choice: ");
                    String c = scanner.nextLine().trim();
                    switch (c) {
                        case "1" -> BorrowerHistory.show(scanner);
                        case "0" -> back = true;
                        default  -> System.out.println("  [!] Invalid option.");
                    }
                }
                case "ADMIN" -> {
                    System.out.println("  [1] UI-01 | View Equipment Status");
                    System.out.println("  [2] UI-02 | View Borrow Records (by Class / Event)");
                    System.out.println("  [3] UI-03 | View Unreturned / Problematic Items");
                    System.out.println("  [4] UI-04 | View Borrower History");
                    System.out.println("  [0] Back to Role Selection");
                    System.out.println(DIVIDER);
                    System.out.print("  Choice: ");
                    String c = scanner.nextLine().trim();
                    switch (c) {
                        case "1" -> EquipmentStatus.show(scanner);
                        case "2" -> BorrowRecords.show(scanner);
                        case "3" -> UnreturnedItems.show(scanner);
                        case "4" -> BorrowerHistory.show(scanner);
                        case "0" -> back = true;
                        default  -> System.out.println("  [!] Invalid option.");
                    }
                }
            }
        }
    }
}
