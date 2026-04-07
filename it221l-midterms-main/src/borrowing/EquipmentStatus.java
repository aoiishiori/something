package borrowing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * EquipmentStatus - UI-01
 * READ UI: View current equipment status.
 * Actors: Custodian, Admin/Head of Office
 */
public class EquipmentStatus {

    private static final String DIVIDER =
            "=".repeat(110);

    // ----------------------------------------------------------------
    // Entry point called from Main menu
    // ----------------------------------------------------------------
    public static void show(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + DIVIDER);
            System.out.println("  UI-01 | EQUIPMENT STATUS");
            System.out.println(DIVIDER);
            System.out.println("  [1] View ALL equipment");
            System.out.println("  [2] Filter by STATUS (Available / Borrowed / Damaged / Decommissioned)");
            System.out.println("  [3] Filter by CATEGORY (Equipment / Peripheral / Accessory)");
            System.out.println("  [4] Search by ITEM NAME or BARCODE");
            System.out.println("  [0] Back to Main Menu");
            System.out.println(DIVIDER);
            System.out.print("  Choice: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAll();
                case "2" -> filterByStatus(scanner);
                case "3" -> filterByCategory(scanner);
                case "4" -> searchByNameOrBarcode(scanner);
                case "0" -> back = true;
                default  -> System.out.println("  [!] Invalid option. Try again.");
            }
        }
    }

    // ----------------------------------------------------------------
    // Option 1: View all
    // ----------------------------------------------------------------
    private static void viewAll() {
        String sql = """
                SELECT barcode, item_name, category, brand, model, status,
                       IFNULL(remarks, '-') AS remarks
                FROM EQUIPMENT
                ORDER BY category, item_name, barcode
                """;
        List<DataClasses.Equipment> list = fetchEquipment(sql);
        printEquipmentTable(list, "ALL EQUIPMENT");
    }

    // ----------------------------------------------------------------
    // Option 2: Filter by status
    // ----------------------------------------------------------------
    private static void filterByStatus(Scanner scanner) {
        System.out.println("\n  Status options: AVAILABLE | BORROWED | DAMAGED | DECOMMISSIONED");
        System.out.print("  Enter status: ");
        String status = scanner.nextLine().trim().toUpperCase();

        String sql = """
                SELECT barcode, item_name, category, brand, model, status,
                       IFNULL(remarks, '-') AS remarks
                FROM EQUIPMENT
                WHERE status = ?
                ORDER BY category, item_name, barcode
                """;
        List<DataClasses.Equipment> list = fetchEquipmentWithParam(sql, status);
        printEquipmentTable(list, "EQUIPMENT — Status: " + status);
    }

    // ----------------------------------------------------------------
    // Option 3: Filter by category
    // ----------------------------------------------------------------
    private static void filterByCategory(Scanner scanner) {
        System.out.println("\n  Category options: EQUIPMENT | PERIPHERAL | ACCESSORY");
        System.out.print("  Enter category: ");
        String category = scanner.nextLine().trim().toUpperCase();

        String sql = """
                SELECT barcode, item_name, category, brand, model, status,
                       IFNULL(remarks, '-') AS remarks
                FROM EQUIPMENT
                WHERE category = ?
                ORDER BY item_name, barcode
                """;
        List<DataClasses.Equipment> list = fetchEquipmentWithParam(sql, category);
        printEquipmentTable(list, "EQUIPMENT — Category: " + category);
    }

    // ----------------------------------------------------------------
    // Option 4: Search by name or barcode
    // ----------------------------------------------------------------
    private static void searchByNameOrBarcode(Scanner scanner) {
        System.out.print("\n  Enter item name or barcode (partial match OK): ");
        String keyword = "%" + scanner.nextLine().trim() + "%";

        String sql = """
                SELECT barcode, item_name, category, brand, model, status,
                       IFNULL(remarks, '-') AS remarks
                FROM EQUIPMENT
                WHERE item_name LIKE ? OR barcode LIKE ?
                ORDER BY category, item_name
                """;
        List<DataClasses.Equipment> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DataClasses.Equipment(
                        rs.getString("barcode"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("status"),
                        rs.getString("remarks")
                ));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        printEquipmentTable(list, "SEARCH RESULTS");
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------
    private static List<DataClasses.Equipment> fetchEquipment(String sql) {
        List<DataClasses.Equipment> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new DataClasses.Equipment(
                        rs.getString("barcode"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("status"),
                        rs.getString("remarks")
                ));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        return list;
    }

    private static List<DataClasses.Equipment> fetchEquipmentWithParam(String sql, String param) {
        List<DataClasses.Equipment> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DataClasses.Equipment(
                        rs.getString("barcode"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getString("status"),
                        rs.getString("remarks")
                ));
            }
        } catch (SQLException e) {
            System.err.println("  [DB ERROR] " + e.getMessage());
        }
        return list;
    }

    private static void printEquipmentTable(List<DataClasses.Equipment> list, String title) {
        System.out.println("\n" + DIVIDER);
        System.out.printf("  %s  (%d record/s found)%n", title, list.size());
        System.out.println(DIVIDER);
        System.out.printf("  %-12s %-22s %-12s %-10s %-16s %-14s %s%n",
                "BARCODE", "ITEM NAME", "CATEGORY", "BRAND", "MODEL", "STATUS", "REMARKS");
        System.out.println("-".repeat(110));
        if (list.isEmpty()) {
            System.out.println("  No records found.");
        } else {
            for (DataClasses.Equipment e : list) {
                System.out.println("  " + e);
            }
        }
        System.out.println(DIVIDER);
    }
}
