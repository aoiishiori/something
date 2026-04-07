package borrowing;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DataClasses - Plain Old Java Objects (POJOs) for the borrowing system entities.
 */
public class DataClasses {

    // ----------------------------------------------------------------
    // Equipment
    // ----------------------------------------------------------------
    public static class Equipment {
        public String barcode;
        public String itemName;
        public String category;
        public String brand;
        public String model;
        public String status;
        public String remarks;

        public Equipment(String barcode, String itemName, String category,
                         String brand, String model, String status, String remarks) {
            this.barcode  = barcode;
            this.itemName = itemName;
            this.category = category;
            this.brand    = brand;
            this.model    = model;
            this.status   = status;
            this.remarks  = remarks;
        }

        @Override
        public String toString() {
            return String.format("%-12s %-22s %-12s %-10s %-16s %-12s %s",
                    barcode, itemName, category, brand, model, status,
                    (remarks != null ? remarks : "-"));
        }
    }

    // ----------------------------------------------------------------
    // BorrowTransaction (summary for listing)
    // ----------------------------------------------------------------
    public static class BorrowRecord {
        public int    transactionId;
        public String borrowerName;
        public String borrowerId;
        public String classId;
        public String activityName;
        public String borrowDate;
        public String expectedReturn;
        public String returnDate;
        public String status;

        public BorrowRecord(int transactionId, String borrowerName, String borrowerId,
                            String classId, String activityName,
                            String borrowDate, String expectedReturn,
                            String returnDate, String status) {
            this.transactionId  = transactionId;
            this.borrowerName   = borrowerName;
            this.borrowerId     = borrowerId;
            this.classId        = classId != null ? classId : "-";
            this.activityName   = activityName != null ? activityName : "-";
            this.borrowDate     = borrowDate;
            this.expectedReturn = expectedReturn;
            this.returnDate     = returnDate != null ? returnDate : "NOT RETURNED";
            this.status         = status;
        }

        @Override
        public String toString() {
            return String.format("TXN#%-4d | %-25s | Class: %-12s | Activity: %-25s | Borrowed: %-20s | Return Due: %-12s | Returned: %-20s | Status: %s",
                    transactionId, borrowerName + " (" + borrowerId + ")",
                    classId, activityName, borrowDate, expectedReturn, returnDate, status);
        }
    }

    // ----------------------------------------------------------------
    // BorrowItem (individual item within a transaction)
    // ----------------------------------------------------------------
    public static class BorrowItem {
        public int    transactionId;
        public String barcode;
        public String itemName;
        public String conditionOut;
        public String conditionIn;
        public String damageNotes;

        public BorrowItem(int transactionId, String barcode, String itemName,
                          String conditionOut, String conditionIn, String damageNotes) {
            this.transactionId = transactionId;
            this.barcode       = barcode;
            this.itemName      = itemName;
            this.conditionOut  = conditionOut;
            this.conditionIn   = conditionIn != null ? conditionIn  : "NOT RETURNED";
            this.damageNotes   = damageNotes != null ? damageNotes  : "-";
        }

        @Override
        public String toString() {
            return String.format("  Barcode: %-12s | Item: %-22s | Out: %-8s | In: %-15s | Damage: %s",
                    barcode, itemName, conditionOut, conditionIn, damageNotes);
        }
    }

    // ----------------------------------------------------------------
    // Unreturned / Problematic summary
    // ----------------------------------------------------------------
    public static class UnreturnedRecord {
        public int    transactionId;
        public String borrowerName;
        public String borrowerId;
        public String borrowDate;
        public String expectedReturn;
        public String status;
        public String items; // comma-separated item names

        public UnreturnedRecord(int transactionId, String borrowerName, String borrowerId,
                                String borrowDate, String expectedReturn,
                                String status, String items) {
            this.transactionId  = transactionId;
            this.borrowerName   = borrowerName;
            this.borrowerId     = borrowerId;
            this.borrowDate     = borrowDate;
            this.expectedReturn = expectedReturn;
            this.status         = status;
            this.items          = items;
        }

        @Override
        public String toString() {
            return String.format("TXN#%-4d | %-25s | Due: %-12s | Status: %-22s | Items: %s",
                    transactionId,
                    borrowerName + " (" + borrowerId + ")",
                    expectedReturn, status, items);
        }
    }
}
