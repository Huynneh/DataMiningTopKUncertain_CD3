package bll;

import java.util.*;
import model.Itemset;
import model.Transaction;

/**
 * Thuật toán UFP-growth để khai thác Top-K itemset có Expected Support cao nhất
 * trên cơ sở dữ liệu không chắc chắn. Thuật toán mở rộng theo hướng FP-growth:
 * - Sử dụng cơ sở dữ liệu điều kiện (conditional DB)
 * - Sắp xếp item theo ES giảm dần
 * - Cắt tỉa bằng cận trên ES và ngưỡng động minES
 * - Tránh lặp bằng visited
 */

public class UFPgrowth {

    private List<Transaction> db;
    private int topK;
    private PriorityQueue<Itemset> topKQueue;
    private double minES = 0.0;
    private Set<String> visited = new HashSet<>();

    /**
     * Khởi tạo thuật toán UFP-growth
     *
     * @param db           Cơ sở dữ liệu giao dịch
     * @param topK         Số lượng itemset cần lấy
     * @param sharedQueue  Hàng đợi Top-K dùng chung giữa các thuật toán (nếu có)
     */
    public UFPgrowth(List<Transaction> db, int topK, PriorityQueue<Itemset> sharedQueue) {
        this.db = db;
        this.topK = topK;
        this.topKQueue = sharedQueue;
    }

    /**
     * Tính Expected Support chính xác của một itemset
     * ES(X) = tổng tích xác suất của từng item trong tất cả các giao dịch
     */
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

    /**
     * Điểm bắt đầu của thuật toán
     * - Tính ES của 1-itemset
     * - Sắp xếp giảm dần theo ES
     * - Gọi hàm explore để đệ quy mở rộng theo prefix
     */
    public void mine() {
        Map<String, Double> es1 = computeES(db);
        List<String> items = new ArrayList<>(es1.keySet());
        items.sort((a, b) -> Double.compare(es1.get(b), es1.get(a)));
        explore(new TreeSet<>(), db, items);
    }

    /**
     * Tính ES cho tất cả item đơn trong một cơ sở dữ liệu con
     * Đây là bước dùng cho việc sắp xếp item và cận trên
     */
    private Map<String, Double> computeES(List<Transaction> tdb) {
        Map<String, Double> map = new HashMap<>();
        for (Transaction t : tdb) {
            for (var e : t.getItems().entrySet()) {
                map.merge(e.getKey(), e.getValue(), Double::sum);
            }
        }
        return map;
    }

    /**
     * Xây dựng cơ sở dữ liệu điều kiện (Conditional Database)
     * cho một item cụ thể:
     * - Lọc những giao dịch chứa item
     * - Loại bỏ chính item đó
     */
    private List<Transaction> buildCondDB(List<Transaction> tdb, String item) {
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

    /**
     * Tính cận trên Expected Support cho một item đơn trong DB con
     * Dùng để prune khi ub < minES
     */
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

    /**
     * Hàm đệ quy chính của UFP-growth.
     * - Kiểm tra cận trên để prune
     * - Tính ES của prefix item
     * - Thêm vào Top-K
     * - Xây dựng DB điều kiện
     * - Đệ quy mở rộng tiếp
     */
    private void explore(Set<String> prefix, List<Transaction> tdb, List<String> items) {
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);

            // 1. Prune bằng cận trên ES
            double ub = singleES(item, tdb);
            if (ub < minES) {
                continue;
            }

            // 2. Mở rộng prefix
            Set<String> newPrefix = new TreeSet<>(prefix);
            newPrefix.add(item);

            // 3. Tính ES chính xác
            double es = exactES(newPrefix);
            if (es < minES) {
                continue;
            }

            // 4. Cập nhật Top-K
            pushTopK(new Itemset(newPrefix, es));

            // 5. Tạo DB điều kiện cho lần đệ quy tiếp theo
            List<Transaction> cdb = buildCondDB(tdb, item);
            if (cdb.isEmpty()) {
                continue;
            }

            // 6. Lọc và sắp xếp item phù hợp với DB điều kiện
            Map<String, Double> ces = computeES(cdb);
            List<String> newItems = new ArrayList<>();
            for (int j = i + 1; j < items.size(); j++) {
                String it = items.get(j);
                if (ces.containsKey(it)) {
                    newItems.add(it);
                }
            }
            newItems.sort((a, b) -> Double.compare(ces.get(b), ces.get(a)));

            // 7. Đệ quy mở rộng
            explore(newPrefix, cdb, newItems);
        }
    }

    /**
     * Thêm itemset vào Top-K (nếu đủ điều kiện),
     * đồng thời cập nhật minES
     */
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
}
