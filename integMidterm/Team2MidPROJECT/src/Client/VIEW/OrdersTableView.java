package Client.VIEW;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * OrdersTableView — VIEW component for displaying seller orders.
 *
 */
public class OrdersTableView extends TableView<String[]> {

    // Create a new orders table with predefined columns
    public OrdersTableView() {
        setupColumns();
    }

    // Create a new orders table with data
    public OrdersTableView(ObservableList<String[]> orders) {
        setupColumns();
        setItems(orders);
    }

    private void setupColumns() {
        // Transaction ID column
        TableColumn<String[], String> colTxId = new TableColumn<>("Tx ID");
        colTxId.setCellValueFactory(data -> new SimpleStringProperty(safeGet(data.getValue(), 0)));
        colTxId.setPrefWidth(110);

        // Product ID column
        TableColumn<String[], String> colProductId = new TableColumn<>("Product ID");
        colProductId.setCellValueFactory(data -> new SimpleStringProperty(safeGet(data.getValue(), 1)));
        colProductId.setPrefWidth(110);

        // Buyer column
        TableColumn<String[], String> colBuyer = new TableColumn<>("Buyer");
        colBuyer.setCellValueFactory(data -> new SimpleStringProperty(safeGet(data.getValue(), 2)));
        colBuyer.setPrefWidth(120);

        // Quantity column
        TableColumn<String[], String> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(data -> new SimpleStringProperty(safeGet(data.getValue(), 3)));
        colQty.setPrefWidth(60);

        // Date column
        TableColumn<String[], String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(data -> new SimpleStringProperty(safeGet(data.getValue(), 4)));
        colDate.setPrefWidth(150);

        getColumns().addAll(colTxId, colProductId, colBuyer, colQty, colDate);

        // Prevent column reordering
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private String safeGet(String[] array, int index) {
        if (array != null && index < array.length && array[index] != null) {
            return array[index];
        }
        return "";
    }
}
