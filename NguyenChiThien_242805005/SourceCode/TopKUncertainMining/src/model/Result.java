package model;

import java.util.List;

/**
 * Luu tru ket qua cua qua trinh khai thac Top-K tap mmuc thương xuyen
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
