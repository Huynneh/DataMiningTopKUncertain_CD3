package bll;

import java.util.*;
import model.Itemset;
import model.Transaction;

/**
 * Lớp HybridTopKMiner cài đặt chiến lược lai (hybrid) để khai thác
 * tập mục phổ biến Top-K trong cơ sở dữ liệu không chắc chắn (uncertain database).
 * Thuật toán tự động lựa chọn giữa hai phương pháp:
 *      + UFPgrowth - phù hợp với các tập dữ liệu có mật độ cao
 *      + UHMine – hiệu quả hơn với dữ liệu thưa
 *
 * Việc lựa chọn thuật toán dựa trên giá trị mật độ trung bình của cơ sở dữ liệu,
 * so sánh với ngưỡng densityThreshold. Cách tiếp cận này giúp tận dụng ưu điểm
 * của từng thuật toán trong từng bối cảnh dữ liệu khác nhau, đảm bảo hiệu quả
 * cả về thời gian chạy và khả năng mở rộng.
 */

public class HybridTopKMiner {

     /** Cơ sở dữ liệu giao dịch không chắc chắn đầu vào */
    private List<Transaction> db;

    /** Số lượng tập mục phổ biến cần trích xuất (Top-K) */
    private int topK;

    /** Ngưỡng mật độ dùng để quyết định phương pháp khai thác */
    private double densityThreshold;

    /** Giá trị ES nhỏ nhất hiện tại trong Top-K itemset */
    private double minES = 0.0;

    /**
     * Hàng đợi ưu tiên lưu trữ Top-K itemset, được sắp xếp tăng dần theo ES
     * Phần tử có ES nhỏ nhất nằm ở đầu hàng đợi để dễ dàng thay thế khi cần
     */
    private PriorityQueue<Itemset> topKQueue;

    /** Tập dùng để theo dõi các itemset đã xử lý, tránh thêm trùng lặp */
    private Set<String> visited = new HashSet<>();

    /**
     * Khởi tạo đối tượng HybridTopKMiner
     *
     * @param db                cơ sở dữ liệu giao dịch không chắc chắn
     * @param topK              số lượng tập mục cần tìm trong Top-K
     * @param densityThreshold  ngưỡng mật độ để lựa chọn thuật toán
     */
    public HybridTopKMiner(List<Transaction> db, int topK, double densityThreshold) {
        this.db = db;
        this.topK = topK;
        this.densityThreshold = densityThreshold;
        this.topKQueue = new PriorityQueue<>(Comparator.comparingDouble(Itemset::getExpectedSupport));
    }

    /**
     * Thực thi khai thác Top-K. Chọn U-FPGrowth hoặc UH-Mine tùy theo mật độ.
     *
     * @return danh sách Top-K itemset theo ES giảm dần
     */
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

    /** Tính mật độ = tổng số item / số giao dịch */
    private double computeDensity() {
        double sum = 0.0;
        for (Transaction t : db) {
            sum += t.getItems().size();
        }
        return sum / db.size();
    }

    /**
     * Thêm itemset vào Top-K nếu tốt hơn hoặc nếu chưa đủ K phần tử
     *
     * @param itemset itemset cần xét
     */
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

    /** Trả về ES nhỏ nhất trong Top-K */
    public double getMinES() {
        return minES;
    }

    /** Trả về hàng đợi Top-K */
    public PriorityQueue<Itemset> getTopKQueue() {
        return topKQueue;
    }
}
