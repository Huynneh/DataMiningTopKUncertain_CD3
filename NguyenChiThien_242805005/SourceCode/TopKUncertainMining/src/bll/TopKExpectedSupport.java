package bll;

import java.util.*;
import model.Itemset;
import model.Transaction;

/**
 * Lớp TopKExpectedSupport dùng để tìm Top-K tập mục có
 * Hỗ trợ kỳ vọng (Expected Support – ES) cao nhất trong cơ sở dữ liệu không chắc chắn
 *
 * Thuật toán duyệt theo dạng nhánh (branch-and-bound) dựa trên:
 * - Danh sách item được sắp xếp giảm dần theo ES đơn lẻ
 * - Tính ES tích lũy theo từng mức mở rộng tập mục
 * - Cắt tỉa nhánh khi ES < minES (ngưỡng ES nhỏ nhất trong Top-K hiện tại)
 */

public class TopKExpectedSupport {

    /** Danh sách giao dịch đầu vào */
    private final List<Transaction> transactions;

    /** Số lượng Top-K cần tìm */
    private final int k;

    /** Danh sách item sắp xếp giảm dần theo ES đơn lẻ */
    private final List<String> allItems;

    /** Hàng đợi Top-K itemset (sắp theo ES tăng dần) */
    private final PriorityQueue<Itemset> topKQueue;

    /** ES nhỏ nhất trong Top-K (dùng cho cắt tỉa) */
    private double minES = 0.0;


    /**
     * Khởi tạo bộ tìm kiếm Top-K ES
     *
     * @param transactions cơ sở dữ liệu không chắc chắn
     * @param k số lượng tập mục cần tìm
     */
    public TopKExpectedSupport(List<Transaction> transactions, int k) {
        this.transactions = transactions;
        this.k = k;

        // Tính ES của từng item đơn lẻ
        Map<String, Double> esMap = new HashMap<>();
        for (Transaction t : transactions) {
            for (Map.Entry<String, Double> e : t.getItems().entrySet()) {
                esMap.merge(e.getKey(), e.getValue(), Double::sum);
            }
        }

        // Sắp xếp item theo ES giảm dần để tăng hiệu quả cắt tỉa
        List<String> items = new ArrayList<>(esMap.keySet());
        items.sort((a, b) -> Double.compare(esMap.get(b), esMap.get(a))); // sắp xếp giảm dần ES
        this.allItems = items;

        this.topKQueue = new PriorityQueue<>(Comparator.comparingDouble(Itemset::getExpectedSupport));
    }

    /**
     * Tìm Top-K itemset bằng duyệt nhánh và cắt tỉ
     *
     * @return danh sách Top-K itemset theo ES giảm dần
     */
    public List<Itemset> findTopK() {
        double[] probSoFar = new double[transactions.size()]; // xác suất tích lũy cho nhánh hiện tại
        Arrays.fill(probSoFar, 1.0);
        explore(0, new LinkedHashSet<>(), probSoFar);
        List<Itemset> result = new ArrayList<>(topKQueue);
        result.sort((a, b) -> Double.compare(b.getExpectedSupport(), a.getExpectedSupport()));
        return result;
    }

    /**
     * Hàm đệ quy mở rộng tập mục và tính ES
     *
     * @param index vị trí item bắt đầu duyệt
     * @param curr tập mục hiện tại
     * @param probSoFar xác suất tích lũy cho từng giao dịch
     */
    private void explore(int index, Set<String> curr, double[] probSoFar) {
        for (int i = index; i < allItems.size(); i++) {
            String item = allItems.get(i);

            double[] newProb = new double[transactions.size()];
            double es = 0.0;

            // Tính ES của tập mục mới
            for (int t = 0; t < transactions.size(); t++) {
                Double p = transactions.get(t).getItems().get(item);
                if (p == null) {
                    p = 0.0;
                }
                newProb[t] = probSoFar[t] * p;
                es += newProb[t];
            }

            // Cắt nhánh nếu ES không đủ tốt
            if (es < minES) {
                continue; // cắt nhánh
            }
            curr.add(item);
            pushTopK(new Itemset(new LinkedHashSet<>(curr), es));

            explore(i + 1, curr, newProb);
            curr.remove(item);
        }
    }

    /**
     * Đưa itemset vào Top-K nếu đạt yêu cầu
     *
     * @param it itemset cần xét
     */
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
