package Server.controller;

import Server.model.Transaction;
import Server.util.JsonUtils;
import Server.util.ServerLogger;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * TransactionController — handles retrieval of transaction history.
 *
 * Supported actions:
 *   FETCH_MY_PURCHASES  — buyer sees their own orders
 *   FETCH_MY_SALES      — seller sees their own sales
 *   FETCH_ALL_TRANSACTIONS — admin sees everything
 *
 * All responses are JSON strings.
 */
public class TransactionController {

    // -------------------------------------------------------
    // FETCH BUYER'S PURCHASES
    // -------------------------------------------------------
    public String fetchMyPurchases(String buyerUsername) {
        List<Transaction> purchases = Transaction.fetchBuyerPurchases(buyerUsername);

        ObjectNode dataNode = JsonUtils.getMapper().createObjectNode();
        ArrayNode txArr     = dataNode.putArray("transactions");
        for (Transaction t : purchases) {
            txArr.add(t.toJsonNode(JsonUtils.getMapper()));
        }

        ServerLogger.logTransaction(buyerUsername, "FETCH_MY_PURCHASES", "N/A");
        return JsonUtils.createResponseWithData("SUCCESS", "Your purchases.", dataNode);
    }

    // -------------------------------------------------------
    // FETCH SELLER'S SALES
    // -------------------------------------------------------
    public String fetchMySales(String sellerUsername) {
        List<Transaction> sales = Transaction.fetchSellerSales(sellerUsername);

        ObjectNode dataNode = JsonUtils.getMapper().createObjectNode();
        ArrayNode txArr     = dataNode.putArray("transactions");
        for (Transaction t : sales) {
            txArr.add(t.toJsonNode(JsonUtils.getMapper()));
        }

        ServerLogger.logTransaction(sellerUsername, "FETCH_MY_SALES", "N/A");
        return JsonUtils.createResponseWithData("SUCCESS", "Your sales.", dataNode);
    }

    // -------------------------------------------------------
    // FETCH ALL TRANSACTIONS (Admin)
    // -------------------------------------------------------
    public String fetchAllTransactions(String adminUsername) {
        List<Transaction> all = Transaction.fetchAllTransactions();

        ObjectNode dataNode = JsonUtils.getMapper().createObjectNode();
        ArrayNode txArr     = dataNode.putArray("transactions");

        for (Transaction t : all) {
            txArr.add(t.toJsonNode(JsonUtils.getMapper()));
        }

        ServerLogger.logTransaction(adminUsername, "FETCH_ALL_TRANSACTIONS",
                "count=" + all.size());
        return JsonUtils.createResponseWithData("SUCCESS", "All transactions.", dataNode);
    }
}