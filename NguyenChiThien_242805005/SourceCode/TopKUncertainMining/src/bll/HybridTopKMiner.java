package bll;

import model.Itemset;
import model.Transaction;

import java.util.*;

public class HybridTopKMiner {

    private List<Transaction> db;
    private int topK;
    private double densityThreshold;
    private double minES = 0.0;
    private PriorityQueue<Itemset> topKQueue;
    private Set<String> visited = new HashSet<>();

    public HybridTopKMiner(List<Transaction> db, int topK, double densityThreshold) {
        this.db = db;
        this.topK = topK;
        this.densityThreshold = densityThreshold;
        this.topKQueue = new PriorityQueue<>(Comparator.comparingDouble(Itemset::getExpectedSupport));
    }

    public List<Itemset> mine() {
        double d = computeDensity();

        if (d >= densityThreshold) {
            UFPgrowth fpg = new UFPgrowth(db, topK, topKQueue);
            fpg.mine();
        } else {
            UHMine hm = new UHMine(db, topK, topKQueue);
            hm.mine();
        }

        List<Itemset> out = new ArrayList<>(topKQueue);
        out.sort((a, b) -> Double.compare(b.getExpectedSupport(), a.getExpectedSupport()));
        if (!out.isEmpty()) {
            minES = out.get(out.size() - 1).getExpectedSupport();
        }
        return out;
    }

    private double computeDensity() {
        double sum = 0.0;
        for (Transaction t : db) {
            sum += t.getItems().size();
        }
        return sum / db.size();
    }

    public void pushTopK(Itemset itemset) {
        String key = itemset.getItems().toString();
        if (visited.contains(key)) {
            return;
        }
        visited.add(key);

        if (topKQueue.size() < topK) {
            topKQueue.add(itemset);
        } else if (itemset.getExpectedSupport() > topKQueue.peek().getExpectedSupport()) {
            topKQueue.poll();
            topKQueue.add(itemset);
        }

        if (topKQueue.size() == topK) {
            minES = topKQueue.peek().getExpectedSupport();
        }
    }

    public double getMinES() {
        return minES;
    }

    public PriorityQueue<Itemset> getTopKQueue() {
        return topKQueue;
    }
}
