package borrowing;

import java.sql.*;
import java.util.Scanner;

/**
 * LabClasses - UI-05
 * READ UI: Extract and view CIS laboratory classes with their student rosters.
 * Actors: Custodian
 */
public class LabClasses {

    private static final String DIVIDER = "=".repeat(110);

    public static void show(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + DIVIDER);
            System.out.println("  UI-05 | LABORATORY CLASSES & STUDENT ROSTERS");
            System.out.println(DIVIDER);
            System.out.println("  [1] View ALL lab classes");
            System.out.println("  [2] View student roster for a specific class");
            System.out.println("  [3] Search by course code or school year");
            System.out.println("  [0] Back to Main Menu");
            System.out.println(DIVIDER);
            System.out.print("  Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllClasses();
                case "2" -> viewClassRoster(scanner);
                case "3" -> searchClasses(scanner);
                case "0" -> back = true;
                default  -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ----------------------------------------------------------------
    // Option 1: All classes (summary list)
    // ----------------------------------------------------------------
    private static void viewAllClasses() {
        String sql = """
                SELECT lc.class_id, lc.course_code, lc.section,
                       lc.semester, lc.school_year, lc.room, lc.schedule,
                       CONCAT(p.last_name, ', ', p.first_name) AS faculty_name,
                       COUNT(ce.student_id) AS enrolled_count
                FROM LAB_CLASS lc
                JOIN PERSON p ON lc.faculty_id = p.person_id
                LEFT JOIN CLASS_ENROLLMENT ce ON lc.class_id = ce.class_id
                GROUP BY lc.class_id, lc.course_code, lc.section,
                         lc.semester, lc.school_year, lc.room, lc.schedule, faculty_name
                ORDER BY lc.school_year DESC, lc.semester, lc.course_code, lc.section
                """;

        System.out.println("\n" + DIVIDER);
        System.out.println("  ALL LABORATORY CLASSES");
        System.out.println(DIVIDER);
        System.out.printf("  %-12s %-10s %-8s %-15s %-10s %-12s %-20s %-25s %s%n",
                "CLASS ID", "COURSE", "SECT", "SEMESTER", "SY", "ROOM",
                "SCHEDULE", "FACULTY", "ENROLLED");
        System.out.println("  " + "-".repeat(120));

        int count = 0;
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                count++;
                System.out.printf("  %-12s %-10s %-8s %-15s %-10s %-12s %-20s %-25s %d%n",
                        rs.getString("class_id"),
                        rs.getString("course_code"),
                        rs.getString("section"),
                        rs.getString("semester"),
                        rs.getString("school_year"),
                        rs.getString("room")     != null ? rs.getString("room")     : "-",
                        rs.getString("schedule") != null ? rs.getString("schedule") : "-",
                        rs.getString("faculty_name"),
                        rs.getInt("enrolled_count"));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }

        System.out.println(DIVIDER);
        System.out.printf("  %d class/es found.%n", count);
    }

    // ----------------------------------------------------------------
    // Option 2: Student roster for a specific class
    // ----------------------------------------------------------------
    private static void viewClassRoster(Scanner scanner) {
        // Show class list first for reference
        showClassList();

        System.out.print("\n  Enter Class ID (e.g., IT221L-A): ");
        String classId = scanner.nextLine().trim().toUpperCase();

        // Verify class exists and get class details
        String classInfo = getClassInfo(classId);
        if (classInfo == null) {
            System.out.println("  [!] Class ID not found: " + classId);
            return;
        }

        String sql = """
                SELECT p.person_id, p.last_name, p.first_name, p.middle_name,
                       p.email, p.contact_no
                FROM CLASS_ENROLLMENT ce
                JOIN PERSON p ON ce.student_id = p.person_id
                WHERE ce.class_id = ?
                ORDER BY p.last_name, p.first_name
                """;

        System.out.println("\n" + DIVIDER);
        System.out.printf("  STUDENT ROSTER — %s%n", classInfo);
        System.out.println(DIVIDER);
        System.out.printf("  %-15s %-30s %-30s %-15s%n",
                "STUDENT ID", "NAME", "EMAIL", "CONTACT");
        System.out.println("  " + "-".repeat(95));

        int count = 0;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                count++;
                String fullName = rs.getString("last_name") + ", "
                        + rs.getString("first_name")
                        + (rs.getString("middle_name") != null
                        ? " " + rs.getString("middle_name").charAt(0) + "."
                        : "");
                System.out.printf("  %-15s %-30s %-30s %-15s%n",
                        rs.getString("person_id"),
                        fullName,
                        rs.getString("email")      != null ? rs.getString("email")      : "-",
                        rs.getString("contact_no") != null ? rs.getString("contact_no") : "-");
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
            return;
        }

        System.out.println(DIVIDER);
        if (count == 0) {
            System.out.println("  No students enrolled in this class.");
        } else {
            System.out.printf("  %d student/s enrolled.%n", count);
        }
    }

    // ----------------------------------------------------------------
    // Option 3: Search classes
    // ----------------------------------------------------------------
    private static void searchClasses(Scanner scanner) {
        System.out.println("\n  Search by:");
        System.out.println("  [1] Course code (e.g., IT 221L)");
        System.out.println("  [2] School year (e.g., 2025-2026)");
        System.out.print("  Choice: ");
        String choice = scanner.nextLine().trim();

        String sql;
        String keyword;

        if ("1".equals(choice)) {
            System.out.print("  Enter course code (partial match OK): ");
            keyword = "%" + scanner.nextLine().trim() + "%";
            sql = """
                    SELECT lc.class_id, lc.course_code, lc.section,
                           lc.semester, lc.school_year, lc.room, lc.schedule,
                           CONCAT(p.last_name, ', ', p.first_name) AS faculty_name,
                           COUNT(ce.student_id) AS enrolled_count
                    FROM LAB_CLASS lc
                    JOIN PERSON p ON lc.faculty_id = p.person_id
                    LEFT JOIN CLASS_ENROLLMENT ce ON lc.class_id = ce.class_id
                    WHERE lc.course_code LIKE ?
                    GROUP BY lc.class_id, lc.course_code, lc.section,
                             lc.semester, lc.school_year, lc.room, lc.schedule, faculty_name
                    ORDER BY lc.school_year DESC, lc.course_code, lc.section
                    """;
        } else if ("2".equals(choice)) {
            System.out.print("  Enter school year (e.g., 2025-2026): ");
            keyword = scanner.nextLine().trim();
            sql = """
                    SELECT lc.class_id, lc.course_code, lc.section,
                           lc.semester, lc.school_year, lc.room, lc.schedule,
                           CONCAT(p.last_name, ', ', p.first_name) AS faculty_name,
                           COUNT(ce.student_id) AS enrolled_count
                    FROM LAB_CLASS lc
                    JOIN PERSON p ON lc.faculty_id = p.person_id
                    LEFT JOIN CLASS_ENROLLMENT ce ON lc.class_id = ce.class_id
                    WHERE lc.school_year = ?
                    GROUP BY lc.class_id, lc.course_code, lc.section,
                             lc.semester, lc.school_year, lc.room, lc.schedule, faculty_name
                    ORDER BY lc.course_code, lc.section
                    """;
        } else {
            System.out.println("  [!] Invalid choice.");
            return;
        }

        System.out.println("\n" + DIVIDER);
        System.out.printf("  %-12s %-10s %-8s %-15s %-10s %-12s %-20s %-25s %s%n",
                "CLASS ID", "COURSE", "SECT", "SEMESTER", "SY", "ROOM",
                "SCHEDULE", "FACULTY", "ENROLLED");
        System.out.println("  " + "-".repeat(120));

        int count = 0;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                count++;
                System.out.printf("  %-12s %-10s %-8s %-15s %-10s %-12s %-20s %-25s %d%n",
                        rs.getString("class_id"),
                        rs.getString("course_code"),
                        rs.getString("section"),
                        rs.getString("semester"),
                        rs.getString("school_year"),
                        rs.getString("room")     != null ? rs.getString("room")     : "-",
                        rs.getString("schedule") != null ? rs.getString("schedule") : "-",
                        rs.getString("faculty_name"),
                        rs.getInt("enrolled_count"));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }

        System.out.println(DIVIDER);
        System.out.printf("  %d class/es found.%n", count);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private static void showClassList() {
        System.out.println("\n  Available Classes:");
        String sql = "SELECT class_id, course_code, section, semester, school_year FROM LAB_CLASS ORDER BY class_id";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            System.out.printf("  %-12s %-12s %-8s %-15s %-10s%n",
                    "CLASS ID","COURSE","SECT","SEMESTER","SY");
            System.out.println("  " + "-".repeat(60));
            while (rs.next()) {
                System.out.printf("  %-12s %-12s %-8s %-15s %-10s%n",
                        rs.getString("class_id"),
                        rs.getString("course_code"),
                        rs.getString("section"),
                        rs.getString("semester"),
                        rs.getString("school_year"));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
    }

    private static String getClassInfo(String classId) {
        String sql = """
                SELECT CONCAT(lc.course_code, ' ', lc.section, ' | ',
                              lc.semester, ' SY ', lc.school_year, ' | ',
                              lc.room, ' | ', lc.schedule, ' | Faculty: ',
                              p.last_name, ', ', p.first_name) AS info
                FROM LAB_CLASS lc
                JOIN PERSON p ON lc.faculty_id = p.person_id
                WHERE lc.class_id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, classId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("info");
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        return null;
    }
}
