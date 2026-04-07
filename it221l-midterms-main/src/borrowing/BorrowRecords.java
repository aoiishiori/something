package borrowing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * BorrowRecords - UI-02
 * READ UI: View borrow records for a specific class or activity/event.
 * Actors: Custodian, Admin/Head of Office
 */
public class BorrowRecords {

    private static final String DIVIDER = "=".repeat(110);

    public static void show(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + DIVIDER);
            System.out.println("  UI-02 | BORROW RECORDS — BY CLASS OR EVENT");
            System.out.println(DIVIDER);
            System.out.println("  [1] View records by LAB CLASS");
            System.out.println("  [2] View records by ACTIVITY / EVENT");
            System.out.println("  [3] View ALL borrow records");
            System.out.println("  [0] Back to Main Menu");
            System.out.println(DIVIDER);
            System.out.print("  Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewByClass(scanner);
                case "2" -> viewByActivity(scanner);
                case "3" -> viewAll();
                case "0" -> back = true;
                default  -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ----------------------------------------------------------------
    // Option 1: By class
    // ----------------------------------------------------------------
    private static void viewByClass(Scanner scanner) {
        // First show available classes
        showAvailableClasses();
        System.out.print("\n  Enter Class ID (e.g., IT221L-A): ");
        String classId = scanner.nextLine().trim().toUpperCase();

        String sql = """
                SELECT bt.transaction_id,
                       CONCAT(p.last_name, ', ', p.first_name) AS borrower_name,
                       bt.borrower_id,
                       bt.class_id,
                       NULL AS activity_name,
                       DATE_FORMAT(bt.borrow_date, '%Y-%m-%d %H:%i')    AS borrow_date,
                       bt.expected_return,
                       DATE_FORMAT(bt.return_date, '%Y-%m-%d %H:%i')    AS return_date,
                       bt.transaction_status
                FROM BORROW_TRANSACTION bt
                JOIN PERSON p ON bt.borrower_id = p.person_id
                WHERE bt.class_id = ?
                ORDER BY bt.borrow_date DESC
                """;
        List<DataClasses.BorrowRecord> records = fetchWithSingleParam(sql, classId);
        printRecords(records, "BORROW RECORDS — Class: " + classId);

        // Show items per transaction
        if (!records.isEmpty()) {
            showItemsForTransactions(records);
        }
    }

    // ----------------------------------------------------------------
    // Option 2: By activity
    // ----------------------------------------------------------------
    private static void viewByActivity(Scanner scanner) {
        showAvailableActivities();
        System.out.print("\n  Enter Activity/Request ID (number): ");
        String requestId = scanner.nextLine().trim();

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
                WHERE bt.request_id = ?
                ORDER BY bt.borrow_date DESC
                """;
        List<DataClasses.BorrowRecord> records = fetchWithSingleParam(sql, requestId);
        printRecords(records, "BORROW RECORDS — Activity Request #" + requestId);

        if (!records.isEmpty()) {
            showItemsForTransactions(records);
        }
    }

    // ----------------------------------------------------------------
    // Option 3: All records
    // ----------------------------------------------------------------
    private static void viewAll() {
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
                ORDER BY bt.borrow_date DESC
                """;
        List<DataClasses.BorrowRecord> records = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        printRecords(records, "ALL BORROW RECORDS");
        if (!records.isEmpty()) {
            showItemsForTransactions(records);
        }
    }

    // ----------------------------------------------------------------
    // Show items for a list of transactions
    // ----------------------------------------------------------------
    private static void showItemsForTransactions(List<DataClasses.BorrowRecord> records) {
        System.out.println("\n  --- ITEMS PER TRANSACTION ---");
        for (DataClasses.BorrowRecord rec : records) {
            System.out.printf("%n  TXN #%d — %s:%n", rec.transactionId, rec.borrowerName);
            String sql = """
                    SELECT bi.transaction_id, bi.barcode, e.item_name,
                           bi.item_condition_out, bi.item_condition_in,
                           IFNULL(bi.damage_notes, '-') AS damage_notes
                    FROM BORROW_ITEM bi
                    JOIN EQUIPMENT e ON bi.barcode = e.barcode
                    WHERE bi.transaction_id = ?
                    """;
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, rec.transactionId);
                ResultSet rs = ps.executeQuery();
                boolean hasItems = false;
                while (rs.next()) {
                    hasItems = true;
                    System.out.println("  " + new DataClasses.BorrowItem(
                            rs.getInt("transaction_id"),
                            rs.getString("barcode"),
                            rs.getString("item_name"),
                            rs.getString("item_condition_out"),
                            rs.getString("item_condition_in"),
                            rs.getString("damage_notes")
                    ));
                }
                if (!hasItems) System.out.println("    (no items logged)");
            } catch (SQLException e) {
                System.err.println("  [DB ERROR] " + e.getMessage());
            }
        }
        System.out.println("\n" + DIVIDER);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private static void showAvailableClasses() {
        System.out.println("\n  Available Lab Classes:");
        String sql = "SELECT class_id, course_code, section, semester, school_year, schedule FROM LAB_CLASS ORDER BY class_id";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            System.out.printf("  %-12s %-12s %-8s %-15s %-10s %-20s%n",
                    "CLASS ID","COURSE","SECTION","SEMESTER","SY","SCHEDULE");
            System.out.println("  " + "-".repeat(80));
            while (rs.next()) {
                System.out.printf("  %-12s %-12s %-8s %-15s %-10s %-20s%n",
                        rs.getString("class_id"),
                        rs.getString("course_code"),
                        rs.getString("section"),
                        rs.getString("semester"),
                        rs.getString("school_year"),
                        rs.getString("schedule"));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
    }

    private static void showAvailableActivities() {
        System.out.println("\n  Available Activity Requests:");
        String sql = "SELECT request_id, activity_name, activity_type, activity_date, status FROM ACTIVITY_REQUEST ORDER BY activity_date";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            System.out.printf("  %-5s %-35s %-15s %-12s %-10s%n",
                    "ID","ACTIVITY NAME","TYPE","DATE","STATUS");
            System.out.println("  " + "-".repeat(80));
            while (rs.next()) {
                System.out.printf("  %-5d %-35s %-15s %-12s %-10s%n",
                        rs.getInt("request_id"),
                        rs.getString("activity_name"),
                        rs.getString("activity_type"),
                        rs.getString("activity_date"),
                        rs.getString("status"));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
    }

    private static List<DataClasses.BorrowRecord> fetchWithSingleParam(String sql, String param) {
        List<DataClasses.BorrowRecord> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        return list;
    }

    private static DataClasses.BorrowRecord mapRecord(ResultSet rs) throws SQLException {
        return new DataClasses.BorrowRecord(
                rs.getInt("transaction_id"),
                rs.getString("borrower_name"),
                rs.getString("borrower_id"),
                rs.getString("class_id"),
                rs.getString("activity_name"),
                rs.getString("borrow_date"),
                rs.getString("expected_return"),
                rs.getString("return_date"),
                rs.getString("transaction_status")
        );
    }

    private static void printRecords(List<DataClasses.BorrowRecord> list, String title) {
        System.out.println("\n" + DIVIDER);
        System.out.printf("  %s  (%d record/s found)%n", title, list.size());
        System.out.println(DIVIDER);
        if (list.isEmpty()) {
            System.out.println("  No records found.");
        } else {
            for (DataClasses.BorrowRecord r : list) {
                System.out.println("  " + r);
            }
        }
    }
}
