package borrowing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * BorrowerHistory - UI-04
 * READ UI: View one's own borrow history (Borrower), or any borrower's history (Custodian/Admin).
 * Actors: Borrower (own records), Custodian, Admin/Head of Office
 */
public class BorrowerHistory {

    private static final String DIVIDER = "=".repeat(110);

    public static void show(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + DIVIDER);
            System.out.println("  UI-04 | BORROWER HISTORY");
            System.out.println(DIVIDER);
            System.out.println("  [1] View MY borrow history (enter your Person ID)");
            System.out.println("  [2] View history of a SPECIFIC borrower (by Person ID)");
            System.out.println("  [3] View history filtered by STATUS");
            System.out.println("  [0] Back to Main Menu");
            System.out.println(DIVIDER);
            System.out.print("  Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1", "2" -> viewByPerson(scanner);
                case "3"      -> viewByStatusFilter(scanner);
                case "0"      -> back = true;
                default       -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ----------------------------------------------------------------
    // Option 1 & 2: By person ID
    // ----------------------------------------------------------------
    private static void viewByPerson(Scanner scanner) {
        // Show list of known persons for easy reference
        showPersonList();
        System.out.print("\n  Enter Person ID: ");
        String personId = scanner.nextLine().trim();

        // First verify the person exists and get their name
        String personName = getPersonName(personId);
        if (personName == null) {
            System.out.println("  [!] Person ID not found: " + personId);
            return;
        }

        String sql = """
                SELECT bt.transaction_id,
                       CONCAT(p.last_name, ', ', p.first_name) AS borrower_name,
                       bt.borrower_id,
                       bt.class_id,
                       ar.activity_name,
                       DATE_FORMAT(bt.borrow_date, '%Y-%m-%d %H:%i')  AS borrow_date,
                       bt.expected_return,
                       DATE_FORMAT(bt.return_date, '%Y-%m-%d %H:%i')  AS return_date,
                       bt.transaction_status
                FROM BORROW_TRANSACTION bt
                JOIN PERSON p ON bt.borrower_id = p.person_id
                LEFT JOIN ACTIVITY_REQUEST ar ON bt.request_id = ar.request_id
                WHERE bt.borrower_id = ?
                ORDER BY bt.borrow_date DESC
                """;

        List<DataClasses.BorrowRecord> records = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, personId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(new DataClasses.BorrowRecord(
                        rs.getInt("transaction_id"),
                        rs.getString("borrower_name"),
                        rs.getString("borrower_id"),
                        rs.getString("class_id"),
                        rs.getString("activity_name"),
                        rs.getString("borrow_date"),
                        rs.getString("expected_return"),
                        rs.getString("return_date"),
                        rs.getString("transaction_status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
            return;
        }

        // Print summary header
        System.out.println("\n" + DIVIDER);
        System.out.printf("  BORROW HISTORY — %s (%s)  |  %d transaction/s%n",
                personName, personId, records.size());
        System.out.println(DIVIDER);

        if (records.isEmpty()) {
            System.out.println("  No borrow history found for this person.");
            return;
        }

        // Print summary stats
        long borrowed   = records.stream().filter(r -> r.status.equals("BORROWED")).count();
        long returned   = records.stream().filter(r -> r.status.equals("RETURNED")).count();
        long overdue    = records.stream().filter(r -> r.status.equals("OVERDUE")).count();
        long withIssue  = records.stream().filter(r -> r.status.equals("RETURNED_WITH_ISSUE")).count();
        System.out.printf("  Summary — Currently Borrowed: %d | Returned: %d | Overdue: %d | Returned w/ Issue: %d%n%n",
                borrowed, returned, overdue, withIssue);

        for (DataClasses.BorrowRecord r : records) {
            System.out.println("  " + r);
            // Show items for each transaction
            printItemsForTransaction(r.transactionId);
            System.out.println();
        }
        System.out.println(DIVIDER);
    }

    // ----------------------------------------------------------------
    // Option 3: Filter all borrowers by transaction status
    // ----------------------------------------------------------------
    private static void viewByStatusFilter(Scanner scanner) {
        System.out.println("\n  Status options: BORROWED | RETURNED | RETURNED_WITH_ISSUE | OVERDUE");
        System.out.print("  Enter status: ");
        String status = scanner.nextLine().trim().toUpperCase();

        String sql = """
                SELECT bt.transaction_id,
                       CONCAT(p.last_name, ', ', p.first_name) AS borrower_name,
                       bt.borrower_id,
                       bt.class_id,
                       ar.activity_name,
                       DATE_FORMAT(bt.borrow_date, '%Y-%m-%d %H:%i')  AS borrow_date,
                       bt.expected_return,
                       DATE_FORMAT(bt.return_date, '%Y-%m-%d %H:%i')  AS return_date,
                       bt.transaction_status
                FROM BORROW_TRANSACTION bt
                JOIN PERSON p ON bt.borrower_id = p.person_id
                LEFT JOIN ACTIVITY_REQUEST ar ON bt.request_id = ar.request_id
                WHERE bt.transaction_status = ?
                ORDER BY bt.borrow_date DESC
                """;

        List<DataClasses.BorrowRecord> records = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records.add(new DataClasses.BorrowRecord(
                        rs.getInt("transaction_id"),
                        rs.getString("borrower_name"),
                        rs.getString("borrower_id"),
                        rs.getString("class_id"),
                        rs.getString("activity_name"),
                        rs.getString("borrow_date"),
                        rs.getString("expected_return"),
                        rs.getString("return_date"),
                        rs.getString("transaction_status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
            return;
        }

        System.out.println("\n" + DIVIDER);
        System.out.printf("  BORROW HISTORY — Status: %s  (%d record/s)%n", status, records.size());
        System.out.println(DIVIDER);

        if (records.isEmpty()) {
            System.out.println("  No records found with status: " + status);
            return;
        }

        for (DataClasses.BorrowRecord r : records) {
            System.out.println("  " + r);
            printItemsForTransaction(r.transactionId);
            System.out.println();
        }
        System.out.println(DIVIDER);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private static void printItemsForTransaction(int transactionId) {
        String sql = """
                SELECT bi.barcode, e.item_name,
                       bi.item_condition_out, bi.item_condition_in,
                       IFNULL(bi.damage_notes, '-') AS damage_notes
                FROM BORROW_ITEM bi
                JOIN EQUIPMENT e ON bi.barcode = e.barcode
                WHERE bi.transaction_id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("    -> %-12s %-22s | Out: %-8s | In: %-15s | Damage: %s%n",
                        rs.getString("barcode"),
                        rs.getString("item_name"),
                        rs.getString("item_condition_out"),
                        rs.getString("item_condition_in") != null
                                ? rs.getString("item_condition_in") : "NOT RETURNED",
                        rs.getString("damage_notes"));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
    }

    private static String getPersonName(String personId) {
        String sql = "SELECT CONCAT(last_name, ', ', first_name) AS full_name FROM PERSON WHERE person_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, personId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("full_name");
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        return null;
    }

    private static void showPersonList() {
        System.out.println("\n  Known Persons (for reference):");
        String sql = """
                SELECT p.person_id, CONCAT(p.last_name, ', ', p.first_name) AS full_name,
                       p.person_type,
                       COUNT(bt.transaction_id) AS total_borrows
                FROM PERSON p
                LEFT JOIN BORROW_TRANSACTION bt ON p.person_id = bt.borrower_id
                GROUP BY p.person_id, full_name, p.person_type
                HAVING total_borrows > 0
                ORDER BY p.person_type, p.last_name
                """;
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            System.out.printf("  %-15s %-30s %-10s %-8s%n",
                    "PERSON ID","NAME","TYPE","BORROWS");
            System.out.println("  " + "-".repeat(65));
            while (rs.next()) {
                System.out.printf("  %-15s %-30s %-10s %-8d%n",
                        rs.getString("person_id"),
                        rs.getString("full_name"),
                        rs.getString("person_type"),
                        rs.getInt("total_borrows"));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
    }
}
