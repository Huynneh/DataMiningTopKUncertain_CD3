package bll;

import java.util.*;
import model.Itemset;
import model.Transaction;

/**
 * Thuật toán UHMine dùng để khai thác Top-K frequent itemsets 
 * trong cơ sở dữ liệu giao dịch không chắc chắn (uncertain database),
 * dựa trên giá trị Hỗ trợ Kỳ vọng (Expected Support - ES).
 *
 * Thuật toán duyệt sâu (DFS) để sinh các itemset ứng viên theo từng prefix,
 * sử dụng ngưỡng cận (upper bound) và ngưỡng minES động để cắt tỉa không gian tìm kiếm.
 * Kết quả được lưu trong priority queue dùng để quản lý Top-K itemset có ES cao nhất
 */

public class UHMine {

    private List<Transaction> db;
    private int topK;
    private PriorityQueue<Itemset> topKQueue;
    private double minES = 0.0;
    private Set<String> visited = new HashSet<>();

    /**
     * Khởi tạo UHMine
     *
     * @param db         danh sách các giao dịch (uncertain transactions)
     * @param topK       số lượng itemset cần lấy
     * @param sharedQueue queue dùng để lưu trữ Top-K itemset
     */
    public UHMine(List<Transaction> db, int topK, PriorityQueue<Itemset> sharedQueue) {
        this.db = db;
        this.topK = topK;
        this.topKQueue = sharedQueue;
    }

    /**
     * Tính ES chính xác cho một itemset bằng cách nhân xác suất xuất hiện
     * của các item trong từng giao dịch
     *
     * @param items tập các item
     * @return giá trị Expected Support
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
     * Tính ES của từng item đơn trong một tập giao dịch con
     *
     * @param tdb tập giao dịch
     * @return map item → ES
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
     * Sinh cơ sở dữ liệu điều kiện (conditional database) cho một item
     *
     * @param tdb  tập giao dịch hiện tại
     * @param item item để điều kiện hóa
     * @return danh sách giao dịch con sau khi loại bỏ item
     */
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

    /**
     * ES của một item đơn
     *
     * @param item item cần tính
     * @param tdb  tập giao dịch
     * @return tổng ES
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
     * Điểm bắt đầu thuật toán: tính ES cho item đơn, sắp xếp,
     * sau đó gọi đệ quy explore()
     */
    public void mine() {
        Map<String, Double> es1 = computeES(db);
        List<String> items = new ArrayList<>(es1.keySet());
        items.sort((a, b) -> Double.compare(es1.get(b), es1.get(a)));
        explore(new TreeSet<>(), db, items);
    }

    /**
     * Hàm đệ quy chính của UHMine: sinh itemset theo prefix, 
     * tính ES, cắt tỉa bằng minES, mở rộng cơ sở dữ liệu điều kiện
     *
     * @param prefix prefix itemset hiện tại
     * @param tdb    tập giao dịch con ứng với prefix
     * @param items  tập item có thể mở rộng
     */
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

    /**
     * Đẩy một itemset vào Top-K queue
     * Tự động cập nhật ngưỡng minES khi Top-K đủ phần tử.
     *
     * @param itemset itemset cần xét
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

    /**
     * Trả về danh sách Top-K itemset đã khai thác.
     *
     * @return priority queue chứa itemset theo thứ tự ES tăng dần
     */
    public PriorityQueue<Itemset> getTopK() {
        return topKQueue;
    }
}
