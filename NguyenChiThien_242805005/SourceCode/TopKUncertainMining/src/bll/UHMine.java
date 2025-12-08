package bll;

import model.Itemset;
import model.Transaction;

import java.util.*;

public class UHMine {

    private List<Transaction> db;
    private int topK;
    private PriorityQueue<Itemset> topKQueue;
    private double minES = 0.0;
    private Set<String> visited = new HashSet<>();

    public UHMine(List<Transaction> db, int topK, PriorityQueue<Itemset> sharedQueue) {
        this.db = db;
        this.topK = topK;
        this.topKQueue = sharedQueue;
    }

    private double exactES(Set<String> items) {
        double es = 0.0;
        for (Transaction t : db) {
            double p = 1.0;
            for (String it : items) {
                Double v = t.getItems().get(it);
                if (v == null) {
                    p = 0.0;
                    break;
                }
                p *= v;
            }
            es += p;
        }
        return es;
    }

    private Map<String, Double> computeES(List<Transaction> tdb) {
        Map<String, Double> map = new HashMap<>();
        for (Transaction t : tdb) {
            for (var e : t.getItems().entrySet()) {
                map.merge(e.getKey(), e.getValue(), Double::sum);
            }
        }
        return map;
    }

    private List<Transaction> condDB(List<Transaction> tdb, String item) {
        List<Transaction> ret = new ArrayList<>();
        for (Transaction t : tdb) {
            if (!t.getItems().containsKey(item)) {
                continue;
            }
            Map<String, Double> newMap = new HashMap<>();
            for (var e : t.getItems().entrySet()) {
                if (!e.getKey().equals(item)) {
                    newMap.put(e.getKey(), e.getValue());
                }
            }
            if (!newMap.isEmpty()) {
                ret.add(new Transaction(newMap));
            }
        }
        return ret;
    }

    private double singleES(String item, List<Transaction> tdb) {
        double sum = 0.0;
        for (Transaction t : tdb) {
            Double val = t.getItems().get(item);
            if (val != null) {
                sum += val;
            }
        }
        return sum;
    }

    public void mine() {
        Map<String, Double> es1 = computeES(db);
        List<String> items = new ArrayList<>(es1.keySet());
        items.sort((a, b) -> Double.compare(es1.get(b), es1.get(a)));
        explore(new TreeSet<>(), db, items);
    }

    private void explore(Set<String> prefix, List<Transaction> tdb, List<String> items) {
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            double ub = singleES(item, tdb);
            if (ub < minES) {
                continue;
            }

            Set<String> newPrefix = new TreeSet<>(prefix);
            newPrefix.add(item);

            double es = exactES(newPrefix);
            if (es < minES) {
                continue;
            }

            pushTopK(new Itemset(newPrefix, es));

            List<Transaction> cdb = condDB(tdb, item);
            if (cdb.isEmpty()) {
                continue;
            }

            Map<String, Double> ces = computeES(cdb);
            List<String> newItems = new ArrayList<>();
            for (int j = i + 1; j < items.size(); j++) {
                String it = items.get(j);
                if (ces.containsKey(it)) {
                    newItems.add(it);
                }
            }
            newItems.sort((a, b) -> Double.compare(ces.get(b), ces.get(a)));

            explore(newPrefix, cdb, newItems);
        }
    }

    private void pushTopK(Itemset itemset) {
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

    public PriorityQueue<Itemset> getTopK() {
        return topKQueue;
    }
}
