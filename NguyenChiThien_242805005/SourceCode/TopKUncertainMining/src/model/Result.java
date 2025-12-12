package model;

import java.util.List;

/**
 * Lưu trữ kết quả của quá trình khai thác Top-K tập mục thường xuyên
 * trong cơ sở dữ liệu không chắc chắn.
 * 
 * Gồm:
 *  - Danh sách Top-K itemsets thu được.
 *  - Thời gian chạy thuật toán (ms).
 */

public class Result {
    private List<Itemset> topKItemsets;
    private long runtimeMillis;

    public Result(List<Itemset> topKItemsets, long runtimeMillis) {
        this.topKItemsets = topKItemsets;
        this.runtimeMillis = runtimeMillis;
    }

    public List<Itemset> getTopKItemsets() {
        return topKItemsets;
    }

    public long getRuntimeMillis() {
        return runtimeMillis;
    }

    public void printResult() {
        System.out.println("=== TOP-K FREQUENT ITEMSETS ===");
        for (Itemset is : topKItemsets) {
            System.out.println(is);
        }
        System.out.println("Runtime: " + runtimeMillis + " ms");
    }
}
