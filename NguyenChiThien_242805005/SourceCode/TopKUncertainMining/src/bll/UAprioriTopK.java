package bll;

import java.util.*;
import model.Transaction;
import model.Itemset;

/**
 * U-Apriori Top-K with dynamic threshold and pruning
 */
public class UAprioriTopK {

    private final List<Transaction> transactions;
    private final int k;
    private final List<String> allItems;
    private final PriorityQueue<Itemset> topKQueue;

    private double minES = 0.0; // Dynamic threshold

    public UAprioriTopK(List<Transaction> transactions, int k) {
        this.transactions = transactions;
        this.k = k;

        Map<String, Double> esMap = new HashMap<>();
        for (Transaction t : transactions) {
            for (Map.Entry<String, Double> e : t.getItems().entrySet()) {
                esMap.merge(e.getKey(), e.getValue(), Double::sum);
            }
        }

        List<String> items = new ArrayList<>(esMap.keySet());
        items.sort((a, b) -> Double.compare(esMap.get(b), esMap.get(a)));
        this.allItems = items;

        this.topKQueue = new PriorityQueue<>(Comparator.comparingDouble(Itemset::getExpectedSupport));
    }

    public List<Itemset> findTopK() {

        List<Set<String>> currentLevel = new ArrayList<>();
        Map<Set<String>, Double> freqMap = new HashMap<>();

        // --------------------- L1: Frequent 1-itemsets ---------------------
        for (String item : allItems) {
            Set<String> itemset = new LinkedHashSet<>();
            itemset.add(item);

            double es = computeExpectedSupport(itemset);

            if (es >= minES) {
                pushTopK(new Itemset(itemset, es));
                currentLevel.add(itemset);
                freqMap.put(itemset, es);
            }
        }

        // --------------------- Generate L2, L3, ... ------------------------
        while (!currentLevel.isEmpty()) {

            List<Set<String>> nextLevel = new ArrayList<>();

            for (int i = 0; i < currentLevel.size(); i++) {
                for (int j = i + 1; j < currentLevel.size(); j++) {

                    Set<String> candidate = tryJoin(currentLevel.get(i), currentLevel.get(j));
                    if (candidate == null) {
                        continue;
                    }

                    // Apriori prune
                    if (!allSubsetsFrequent(candidate, freqMap)) {
                        continue;
                    }

                    double ub = candidate.stream().mapToDouble(this::singleES).sum();
                    if (ub < minES) {
                        continue;
                    }

                    double es = computeExpectedSupport(candidate);

                    // prune by Top-K dynamic threshold
                    if (es < minES) {
                        continue;
                    }

                    pushTopK(new Itemset(candidate, es));
                    nextLevel.add(candidate);
                    freqMap.put(candidate, es);
                }
            }

            currentLevel = nextLevel;
        }

        // sort descending by ES
        List<Itemset> result = new ArrayList<>(topKQueue);
        result.sort((a, b) -> Double.compare(b.getExpectedSupport(), a.getExpectedSupport()));
        return result;
    }

    private double computeExpectedSupport(Set<String> itemset) {
        double sum = 0.0;
        for (Transaction t : transactions) {
            double p = 1.0;
            for (String item : itemset) {
                Double val = t.getItems().get(item);
                if (val == null) {
                    p = 0.0;
                    break;
                }
                p *= val;
            }
            sum += p;
        }
        return sum;
    }

    private double singleES(String item) {
        double sum = 0.0;
        for (Transaction t : transactions) {
            Double val = t.getItems().get(item);
            if (val != null) {
                sum += val;
            }
        }
        return sum;
    }

    private void pushTopK(Itemset is) {
        if (topKQueue.size() < k) {
            topKQueue.add(is);
        } else if (is.getExpectedSupport() > topKQueue.peek().getExpectedSupport()) {
            topKQueue.poll();
            topKQueue.add(is);
        }

        if (topKQueue.size() == k) {
            minES = topKQueue.peek().getExpectedSupport();
        }
    }

    private Set<String> tryJoin(Set<String> a, Set<String> b) {
        List<String> A = new ArrayList<>(a);
        List<String> B = new ArrayList<>(b);
        for (int i = 0; i < A.size() - 1; i++) {
            if (!A.get(i).equals(B.get(i))) {
                return null;
            }
        }
        Set<String> joined = new LinkedHashSet<>(a);
        joined.add(B.get(B.size() - 1));
        return joined;
    }

    private boolean allSubsetsFrequent(Set<String> candidate, Map<Set<String>, Double> freqMap) {
        for (String item : candidate) {
            Set<String> subset = new LinkedHashSet<>(candidate);
            subset.remove(item);
            if (!freqMap.containsKey(subset)) {
                return false;
            }
        }
        return true;
    }
}
