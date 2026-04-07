package borrowing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * UnreturnedItems - UI-03
 * READ UI: View borrowers with unreturned items or returns with issues/damage.
 * Actors: Custodian, Admin/Head of Office
 */
public class UnreturnedItems {

    private static final String DIVIDER = "=".repeat(110);

    public static void show(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + DIVIDER);
            System.out.println("  UI-03 | UNRETURNED / PROBLEMATIC ITEMS");
            System.out.println(DIVIDER);
            System.out.println("  [1] View OVERDUE transactions (not yet returned past due date)");
            System.out.println("  [2] View currently BORROWED items (all active borrows)");
            System.out.println("  [3] View RETURNED WITH ISSUES (damage/problems reported)");
            System.out.println("  [4] View ALL problematic records (Overdue + Issues)");
            System.out.println("  [0] Back to Main Menu");
            System.out.println(DIVIDER);
            System.out.print("  Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewByStatus("OVERDUE", "OVERDUE TRANSACTIONS");
                case "2" -> viewByStatus("BORROWED", "CURRENTLY BORROWED");
                case "3" -> viewByStatus("RETURNED_WITH_ISSUE", "RETURNED WITH ISSUES");
                case "4" -> viewAllProblematic();
                case "0" -> back = true;
                default  -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ----------------------------------------------------------------
    // Filter by a single status
    // ----------------------------------------------------------------
    private static void viewByStatus(String status, String title) {
        String sql = """
                SELECT bt.transaction_id,
                       CONCAT(p.last_name, ', ', p.first_name) AS borrower_name,
                       bt.borrower_id,
                       DATE_FORMAT(bt.borrow_date, '%Y-%m-%d %H:%i') AS borrow_date,
                       bt.expected_return,
                       bt.transaction_status,
                       GROUP_CONCAT(e.item_name ORDER BY e.item_name SEPARATOR ', ') AS items
                FROM BORROW_TRANSACTION bt
                JOIN PERSON p ON bt.borrower_id = p.person_id
                JOIN BORROW_ITEM bi ON bt.transaction_id = bi.transaction_id
                JOIN EQUIPMENT e   ON bi.barcode = e.barcode
                WHERE bt.transaction_status = ?
                GROUP BY bt.transaction_id, borrower_name, bt.borrower_id,
                         borrow_date, bt.expected_return, bt.transaction_status
                ORDER BY bt.expected_return ASC
                """;
        List<DataClasses.UnreturnedRecord> list = fetchWithStatus(sql, status);
        printTable(list, title);

        // Show damage details for RETURNED_WITH_ISSUE
        if (status.equals("RETURNED_WITH_ISSUE") && !list.isEmpty()) {
            showDamageDetails(list);
        }
    }

    // ----------------------------------------------------------------
    // All problematic: OVERDUE + RETURNED_WITH_ISSUE
    // ----------------------------------------------------------------
    private static void viewAllProblematic() {
        String sql = """
                SELECT bt.transaction_id,
                       CONCAT(p.last_name, ', ', p.first_name) AS borrower_name,
                       bt.borrower_id,
                       DATE_FORMAT(bt.borrow_date, '%Y-%m-%d %H:%i') AS borrow_date,
                       bt.expected_return,
                       bt.transaction_status,
                       GROUP_CONCAT(e.item_name ORDER BY e.item_name SEPARATOR ', ') AS items
                FROM BORROW_TRANSACTION bt
                JOIN PERSON p ON bt.borrower_id = p.person_id
                JOIN BORROW_ITEM bi ON bt.transaction_id = bi.transaction_id
                JOIN EQUIPMENT e    ON bi.barcode = e.barcode
                WHERE bt.transaction_status IN ('OVERDUE', 'RETURNED_WITH_ISSUE')
                GROUP BY bt.transaction_id, borrower_name, bt.borrower_id,
                         borrow_date, bt.expected_return, bt.transaction_status
                ORDER BY bt.transaction_status, bt.expected_return ASC
                """;
        List<DataClasses.UnreturnedRecord> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        printTable(list, "ALL PROBLEMATIC RECORDS (Overdue + Issues)");
        if (!list.isEmpty()) {
            showDamageDetails(list);
        }
    }

    // ----------------------------------------------------------------
    // Show damage details for problematic items
    // ----------------------------------------------------------------
    private static void showDamageDetails(List<DataClasses.UnreturnedRecord> records) {
        System.out.println("\n  --- DAMAGE / ISSUE DETAILS ---");
        for (DataClasses.UnreturnedRecord rec : records) {
            String sql = """
                    SELECT bi.barcode, e.item_name,
                           bi.item_condition_out, bi.item_condition_in,
                           IFNULL(bi.damage_notes, 'No notes') AS damage_notes
                    FROM BORROW_ITEM bi
                    JOIN EQUIPMENT e ON bi.barcode = e.barcode
                    WHERE bi.transaction_id = ?
                      AND (bi.item_condition_in IS NULL
                           OR bi.item_condition_in != 'Good'
                           OR bi.damage_notes IS NOT NULL)
                    """;
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, rec.transactionId);
                ResultSet rs = ps.executeQuery();
                boolean printed = false;
                while (rs.next()) {
                    if (!printed) {
                        System.out.printf("%n  TXN #%d — %s (%s):%n",
                                rec.transactionId, rec.borrowerName, rec.borrowerId);
                        printed = true;
                    }
                    System.out.printf("    %-12s %-22s | Out: %-8s | In: %-12s | Notes: %s%n",
                            rs.getString("barcode"),
                            rs.getString("item_name"),
                            rs.getString("item_condition_out"),
                            rs.getString("item_condition_in") != null
                                    ? rs.getString("item_condition_in") : "N/A",
                            rs.getString("damage_notes"));
                }
            } catch (SQLException e) {
                System.err.println("  [DB ERROR] " + e.getMessage());
            }
        }
        System.out.println("\n" + DIVIDER);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private static List<DataClasses.UnreturnedRecord> fetchWithStatus(String sql, String status) {
        List<DataClasses.UnreturnedRecord> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        return list;
    }

    private static DataClasses.UnreturnedRecord mapRow(ResultSet rs) throws SQLException {
        return new DataClasses.UnreturnedRecord(
                rs.getInt("transaction_id"),
                rs.getString("borrower_name"),
                rs.getString("borrower_id"),
                rs.getString("borrow_date"),
                rs.getString("expected_return"),
                rs.getString("transaction_status"),
                rs.getString("items")
        );
    }

    private static void printTable(List<DataClasses.UnreturnedRecord> list, String title) {
        System.out.println("\n" + DIVIDER);
        System.out.printf("  %s  (%d record/s found)%n", title, list.size());
        System.out.println(DIVIDER);
        if (list.isEmpty()) {
            System.out.println("  No records found.");
        } else {
            for (DataClasses.UnreturnedRecord r : list) {
                System.out.println("  " + r);
            }
        }
    }
}
