package borrowing;

import java.sql.*;
import java.util.Scanner;

/**
 * ViewStudents - UI-06
 * READ UI: Extract student data of currently enrolled students from the database.
 * Actors: Custodian
 */
public class ViewStudents {

    private static final String DIVIDER = "=".repeat(110);

    public static void show(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + DIVIDER);
            System.out.println("  UI-06 | STUDENT DATA");
            System.out.println(DIVIDER);
            System.out.println("  [1] View ALL students");
            System.out.println("  [2] Search by name or ID");
            System.out.println("  [3] View students enrolled in a specific class");
            System.out.println("  [0] Back to Main Menu");
            System.out.println(DIVIDER);
            System.out.print("  Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAll();
                case "2" -> searchStudents(scanner);
                case "3" -> viewByClass(scanner);
                case "0" -> back = true;
                default  -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ----------------------------------------------------------------
    // Option 1: All students
    // ----------------------------------------------------------------
    private static void viewAll() {
        String sql = """
                SELECT p.person_id, p.last_name, p.first_name, p.middle_name,
                       p.email, p.contact_no,
                       COUNT(DISTINCT ce.class_id) AS class_count
                FROM PERSON p
                LEFT JOIN CLASS_ENROLLMENT ce ON p.person_id = ce.student_id
                WHERE p.person_type = 'STUDENT'
                GROUP BY p.person_id, p.last_name, p.first_name,
                         p.middle_name, p.email, p.contact_no
                ORDER BY p.last_name, p.first_name
                """;
        runAndPrint(sql, null, "ALL STUDENTS");
    }

    // ----------------------------------------------------------------
    // Option 2: Search by name or ID
    // ----------------------------------------------------------------
    private static void searchStudents(Scanner scanner) {
        System.out.print("\n  Enter name or student ID (partial match OK): ");
        String keyword = "%" + scanner.nextLine().trim() + "%";

        String sql = """
                SELECT p.person_id, p.last_name, p.first_name, p.middle_name,
                       p.email, p.contact_no,
                       COUNT(DISTINCT ce.class_id) AS class_count
                FROM PERSON p
                LEFT JOIN CLASS_ENROLLMENT ce ON p.person_id = ce.student_id
                WHERE p.person_type = 'STUDENT'
                  AND (p.person_id  LIKE ?
                    OR p.last_name  LIKE ?
                    OR p.first_name LIKE ?)
                GROUP BY p.person_id, p.last_name, p.first_name,
                         p.middle_name, p.email, p.contact_no
                ORDER BY p.last_name, p.first_name
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
    // Option 3: Students in a specific class
    // ----------------------------------------------------------------
    private static void viewByClass(Scanner scanner) {
        // Show available classes for reference
        System.out.println("\n  Available Classes:");
        String classSql = "SELECT class_id, course_code, section FROM LAB_CLASS ORDER BY class_id";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(classSql)) {
            while (rs.next()) {
                System.out.printf("    %-12s %s %s%n",
                        rs.getString("class_id"),
                        rs.getString("course_code"),
                        rs.getString("section"));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }

        System.out.print("\n  Enter Class ID: ");
        String classId = scanner.nextLine().trim().toUpperCase();

        String sql = """
                SELECT p.person_id, p.last_name, p.first_name, p.middle_name,
                       p.email, p.contact_no,
                       COUNT(DISTINCT ce2.class_id) AS class_count
                FROM CLASS_ENROLLMENT ce
                JOIN PERSON p ON ce.student_id = p.person_id
                LEFT JOIN CLASS_ENROLLMENT ce2 ON p.person_id = ce2.student_id
                WHERE ce.class_id = ?
                GROUP BY p.person_id, p.last_name, p.first_name,
                         p.middle_name, p.email, p.contact_no
                ORDER BY p.last_name, p.first_name
                """;

        int count = 0;
        printHeader("STUDENTS IN CLASS: " + classId);
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classId);
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
        try (Connection conn = Database.getConnection()) {
            ResultSet rs;
            if (param == null) {
                Statement st = conn.createStatement();
                rs = st.executeQuery(sql);
            } else {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, param);
                rs = ps.executeQuery();
            }
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
        System.out.printf("  %-15s %-30s %-30s %-15s %s%n",
                "STUDENT ID", "NAME", "EMAIL", "CONTACT", "CLASSES ENROLLED");
        System.out.println("  " + "-".repeat(100));
    }

    private static void printRow(ResultSet rs) throws SQLException {
        String fullName = rs.getString("last_name") + ", "
                + rs.getString("first_name")
                + (rs.getString("middle_name") != null
                ? " " + rs.getString("middle_name").charAt(0) + "." : "");
        System.out.printf("  %-15s %-30s %-30s %-15s %d%n",
                rs.getString("person_id"),
                fullName,
                rs.getString("email")      != null ? rs.getString("email")      : "-",
                rs.getString("contact_no") != null ? rs.getString("contact_no") : "-",
                rs.getInt("class_count"));
    }

    private static void printFooter(int count) {
        System.out.println("  " + "-".repeat(100));
        if (count == 0) System.out.println("  No students found.");
        else System.out.printf("  %d student/s found.%n", count);
        System.out.println(DIVIDER);
    }
}
