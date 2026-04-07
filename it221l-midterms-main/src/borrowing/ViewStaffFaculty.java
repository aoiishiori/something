package borrowing;

import java.sql.*;
import java.util.Scanner;

/**
 * ViewStaffFaculty - UI-07
 * READ UI: Extract CIS staff and faculty data.
 * Actors: Custodian
 */
public class ViewStaffFaculty {

    private static final String DIVIDER = "=".repeat(110);

    public static void show(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + DIVIDER);
            System.out.println("  UI-07 | STAFF & FACULTY DATA");
            System.out.println(DIVIDER);
            System.out.println("  [1] View ALL staff and faculty");
            System.out.println("  [2] View FACULTY only");
            System.out.println("  [3] View STAFF only");
            System.out.println("  [4] Search by name or employee ID");
            System.out.println("  [0] Back to Main Menu");
            System.out.println(DIVIDER);
            System.out.print("  Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAll();
                case "2" -> viewByType("FACULTY");
                case "3" -> viewByType("STAFF");
                case "4" -> searchByName(scanner);
                case "0" -> back = true;
                default  -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ----------------------------------------------------------------
    // Option 1: All staff and faculty
    // ----------------------------------------------------------------
    private static void viewAll() {
        String sql = """
                SELECT p.person_id, p.last_name, p.first_name, p.middle_name,
                       p.person_type, p.email, p.contact_no,
                       COUNT(DISTINCT lc.class_id) AS classes_handled
                FROM PERSON p
                LEFT JOIN LAB_CLASS lc ON p.person_id = lc.faculty_id
                WHERE p.person_type IN ('FACULTY', 'STAFF')
                GROUP BY p.person_id, p.last_name, p.first_name,
                         p.middle_name, p.person_type, p.email, p.contact_no
                ORDER BY p.person_type, p.last_name, p.first_name
                """;
        runAndPrint(sql, null, "ALL STAFF & FACULTY");
    }

    // ----------------------------------------------------------------
    // Option 2 & 3: Filter by type
    // ----------------------------------------------------------------
    private static void viewByType(String type) {
        String sql = """
                SELECT p.person_id, p.last_name, p.first_name, p.middle_name,
                       p.person_type, p.email, p.contact_no,
                       COUNT(DISTINCT lc.class_id) AS classes_handled
                FROM PERSON p
                LEFT JOIN LAB_CLASS lc ON p.person_id = lc.faculty_id
                WHERE p.person_type = ?
                GROUP BY p.person_id, p.last_name, p.first_name,
                         p.middle_name, p.person_type, p.email, p.contact_no
                ORDER BY p.last_name, p.first_name
                """;

        int count = 0;
        printHeader(type + " LIST");
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                count++;
                printRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        printFooter(count);
    }

    // ----------------------------------------------------------------
    // Option 4: Search
    // ----------------------------------------------------------------
    private static void searchByName(Scanner scanner) {
        System.out.print("\n  Enter name or ID (partial match OK): ");
        String keyword = "%" + scanner.nextLine().trim() + "%";

        String sql = """
                SELECT p.person_id, p.last_name, p.first_name, p.middle_name,
                       p.person_type, p.email, p.contact_no,
                       COUNT(DISTINCT lc.class_id) AS classes_handled
                FROM PERSON p
                LEFT JOIN LAB_CLASS lc ON p.person_id = lc.faculty_id
                WHERE p.person_type IN ('FACULTY', 'STAFF')
                  AND (p.person_id  LIKE ?
                    OR p.last_name  LIKE ?
                    OR p.first_name LIKE ?)
                GROUP BY p.person_id, p.last_name, p.first_name,
                         p.middle_name, p.person_type, p.email, p.contact_no
                ORDER BY p.person_type, p.last_name
                """;

        int count = 0;
        printHeader("SEARCH RESULTS");
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            ps.setString(3, keyword);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                count++;
                printRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        printFooter(count);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private static void runAndPrint(String sql, String param, String title) {
        int count = 0;
        printHeader(title);
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                count++;
                printRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        printFooter(count);
    }

    private static void printHeader(String title) {
        System.out.println("\n" + DIVIDER);
        System.out.println("  " + title);
        System.out.println(DIVIDER);
        System.out.printf("  %-12s %-30s %-8s %-30s %-15s %s%n",
                "ID", "NAME", "TYPE", "EMAIL", "CONTACT", "CLASSES HANDLED");
        System.out.println("  " + "-".repeat(105));
    }

    private static void printRow(ResultSet rs) throws SQLException {
        String fullName = rs.getString("last_name") + ", "
                + rs.getString("first_name")
                + (rs.getString("middle_name") != null
                ? " " + rs.getString("middle_name").charAt(0) + "." : "");
        System.out.printf("  %-12s %-30s %-8s %-30s %-15s %d%n",
                rs.getString("person_id"),
                fullName,
                rs.getString("person_type"),
                rs.getString("email")      != null ? rs.getString("email")      : "-",
                rs.getString("contact_no") != null ? rs.getString("contact_no") : "-",
                rs.getInt("classes_handled"));
    }

    private static void printFooter(int count) {
        System.out.println("  " + "-".repeat(105));
        if (count == 0) System.out.println("  No records found.");
        else System.out.printf("  %d record/s found.%n", count);
        System.out.println(DIVIDER);
    }
}
