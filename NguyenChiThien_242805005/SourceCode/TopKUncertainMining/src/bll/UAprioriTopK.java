package bll;

import java.util.*;
import model.Itemset;
import model.Transaction;

/**
 * Lớp UAprioriTopK triển khai thuật toán U-Apriori để tìm Top-K
 * tập mục phổ biến trong cơ sở dữ liệu không chắc chắn
 *
 * Thuật toán sử dụng:
 *  - Expected Support (ES) cho từng tập mục
 *  - Ngưỡng động Top-K (minES) để cắt tỉa ứng viên
 *  - Phát sinh ứng viên theo kiểu Apriori (join + prune)
 *  - Ước lượng cận trên UB = tổng ES của từng item đơn lẻ để cắt nhánh sớm
 *
 * Kết quả cuối cùng là Top-K itemset có ES cao nhất, sắp xếp giảm dần
 */

public class UAprioriTopK {

    /** Danh sách giao dịch không chắc chắn */
    private final List<Transaction> transactions;

    /** Số lượng Top-K cần tìm */
    private final int k;

    /** Danh sách item sắp theo ES đơn lẻ giảm dần */
    private final List<String> allItems;

    /** Hàng đợi Top-K lưu itemset theo ES tăng dần */
    private final PriorityQueue<Itemset> topKQueue;

    /** Ngưỡng ES động của Top-K (dùng để cắt tỉa) */
    private double minES = 0.0; 

    /**
     * Khởi tạo thuật toán U-Apriori Top-K
     *
     * @param transactions cơ sở dữ liệu không chắc chắn
     * @param k số tập mục cần tìm
     */
    public UAprioriTopK(List<Transaction> transactions, int k) {
        this.transactions = transactions;
        this.k = k;

        // Tính ES đơn lẻ cho từng item
        Map<String, Double> esMap = new HashMap<>();
        for (Transaction t : transactions) {
            for (Map.Entry<String, Double> e : t.getItems().entrySet()) {
                esMap.merge(e.getKey(), e.getValue(), Double::sum);
            }
        }

        // Sắp xếp item theo ES giảm dần – hỗ trợ cắt tỉa hiệu quả
        List<String> items = new ArrayList<>(esMap.keySet());
        items.sort((a, b) -> Double.compare(esMap.get(b), esMap.get(a)));
        this.allItems = items;

        this.topKQueue = new PriorityQueue<>(Comparator.comparingDouble(Itemset::getExpectedSupport));
    }

    /**
     * Thực thi U-Apriori Top-K với cắt tỉa theo ngưỡng động
     *
     * @return danh sách Top-K tập mục theo ES giảm dần
     */
    public List<Itemset> findTopK() {

        List<Set<String>> currentLevel = new ArrayList<>();
        Map<Set<String>, Double> freqMap = new HashMap<>();

        // ------------------- L1: tạo tập mục 1 phần tử -------------------
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

        // ------------------- L2, L3, ...: Apriori join + prune ---------
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

    /**
     * Thêm vào Top-K nếu đủ điều kiện, đồng thời cập nhật ngưỡng động minES
     */
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

    /**
     * Join-step Apriori: chỉ join được nếu hai tập mục có prefix giống nhau (trừ phần tử cuối)
     */
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

    /**
     * Apriori prune: tất cả tập con (k-1) phải nằm trong freqMap
     */
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
