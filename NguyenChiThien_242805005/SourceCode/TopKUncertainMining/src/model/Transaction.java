package model;

import java.util.Map;

/**
 * Dai dien cho 1 giao dich trong co so du lieu khong chac chan
 * Moi item co mot xac suat xuat hien (0.0 - 1.0)
 */
public class Transaction {
    private Map<String, Double> items; // itemID -> probability

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
