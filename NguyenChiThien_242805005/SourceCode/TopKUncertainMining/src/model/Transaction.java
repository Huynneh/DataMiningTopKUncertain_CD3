package model;

import java.util.Map;

/**
 * Đại diện cho một giao dịch (transaction) trong cơ sở dữ liệu không chắc chắn (uncertain database).
 * Mỗi item trong giao dịch được gán một xác suất xuất hiện (0.0 – 1.0).
 */
 
public class Transaction {

    private Map<String, Double> items;

    public Transaction(Map<String, Double> items) {
        this.items = items;
    }

    public Map<String, Double> getItems() {
        return items;
    }

    public Double getProbabilityOfItem(int itemId) {
        return items.getOrDefault(itemId, 0.0);
    }

    @Override
    public String toString() {
        return items.toString();
    }
}
