package model;

import java.util.Set;

public class Itemset {

    private Set<String> items;
    private double expectedSupport;
    private double existProbability;

    public Itemset(Set<String> items) {
        this.items = items;
        this.expectedSupport = 0.0;
        this.existProbability = 0.0;
    }

    public Itemset(Set<String> items, double expectedSupport) {
        this.items = items;
        this.expectedSupport = expectedSupport;
        this.existProbability = 0.0;
    }

    public Itemset(Set<String> items, double expectedSupport, double existProbability) {
        this.items = items;
        this.expectedSupport = expectedSupport;
        this.existProbability = existProbability;
    }

    // --- Getter & Setter ---
    public Set<String> getItems() {
        return items;
    }

    public void setItems(Set<String> items) {
        this.items = items;
    }

    public double getExpectedSupport() {
        return expectedSupport;
    }

    public void setExpectedSupport(double expectedSupport) {
        this.expectedSupport = expectedSupport;
    }

    public double getExistProbability() {
        return existProbability;
    }

    public void setExistProbability(double existProbability) {
        this.existProbability = existProbability;
    }

    @Override
    public String toString() {
        return "Itemset " + items
                + " | ExpSup=" + String.format("%.4f", expectedSupport);
    }
}
