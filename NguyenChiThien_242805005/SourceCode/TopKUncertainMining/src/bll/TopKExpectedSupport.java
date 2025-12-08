package bll;

import java.util.*;
import model.Transaction;
import model.Itemset;

public class TopKExpectedSupport {

    private final List<Transaction> transactions;
    private final int k;
    private final List<String> allItems;
    private final PriorityQueue<Itemset> topKQueue;
    private double minES = 0.0;

    public TopKExpectedSupport(List<Transaction> transactions, int k) {
        this.transactions = transactions;
        this.k = k;

        Map<String, Double> esMap = new HashMap<>();
        for (Transaction t : transactions) {
            for (Map.Entry<String, Double> e : t.getItems().entrySet()) {
                esMap.merge(e.getKey(), e.getValue(), Double::sum);
            }
        }

        List<String> items = new ArrayList<>(esMap.keySet());
        items.sort((a, b) -> Double.compare(esMap.get(b), esMap.get(a))); // sắp xếp giảm dần ES
        this.allItems = items;

        this.topKQueue = new PriorityQueue<>(Comparator.comparingDouble(Itemset::getExpectedSupport));
    }

    public List<Itemset> findTopK() {
        double[] probSoFar = new double[transactions.size()]; // xác suất tích lũy cho nhánh hiện tại
        Arrays.fill(probSoFar, 1.0);
        explore(0, new LinkedHashSet<>(), probSoFar);
        List<Itemset> result = new ArrayList<>(topKQueue);
        result.sort((a, b) -> Double.compare(b.getExpectedSupport(), a.getExpectedSupport()));
        return result;
    }

    private void explore(int index, Set<String> curr, double[] probSoFar) {
        for (int i = index; i < allItems.size(); i++) {
            String item = allItems.get(i);

            double[] newProb = new double[transactions.size()];
            double es = 0.0;
            for (int t = 0; t < transactions.size(); t++) {
                Double p = transactions.get(t).getItems().get(item);
                if (p == null) {
                    p = 0.0;
                }
                newProb[t] = probSoFar[t] * p;
                es += newProb[t];
            }

            if (es < minES) {
                continue; // cắt nhánh
            }
            curr.add(item);
            pushTopK(new Itemset(new LinkedHashSet<>(curr), es));

            explore(i + 1, curr, newProb);
            curr.remove(item);
        }
    }

    private void pushTopK(Itemset it) {
        if (topKQueue.size() < k) {
            topKQueue.add(it);
        } else if (it.getExpectedSupport() > topKQueue.peek().getExpectedSupport()) {
            topKQueue.poll();
            topKQueue.add(it);
        }

        if (topKQueue.size() == k) {
            minES = topKQueue.peek().getExpectedSupport();
        }
    }
}
