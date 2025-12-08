package main;

import dal.*;
import model.*;
import bll.*;

import java.util.*;
import java.io.*;

public class Main {

    // ============================================================
    // 
    // ============================================================
    public static Result runAlgorithm(Runnable algorithm) {
        Runtime rt = Runtime.getRuntime();
        rt.gc();

        long memBefore = rt.totalMemory() - rt.freeMemory();
        long start = System.nanoTime();

        algorithm.run();

        long end = System.nanoTime();
        long memAfter = rt.totalMemory() - rt.freeMemory();

        double timeMs = (end - start) / 1_000_000.0;
        double memMB = (memAfter - memBefore) / (1024.0 * 1024.0);

        return new Result(timeMs, memMB);
    }

    public static class Result {

        public double timeMs;
        public double memMB;

        public Result(double t, double m) {
            this.timeMs = t;
            this.memMB = m;
        }
    }

    // ============================================================
    // MAIN
    // ============================================================
    public static void main(String[] args) {

        // String originFile = "dataset/origin/example.txt";
        // String probFile = "dataset/probability/example_probability.txt";
        String originFile = "dataset/origin/chess.txt";
        String probFile = "dataset/probability/chess_probability.txt";
        // String originFile = "dataset/origin/foodmart.txt";
        // String probFile = "dataset/probability/foodmart_probability.txt";
        // String originFile = "dataset/origin/retail.txt";
        // String probFile = "dataset/probability/retail_probability.txt";
        // String originFile = "dataset/origin/t20i6d100k.txt";
        // String probFile = "dataset/probability/t20i6d100k_probability.txt";
        String baseName = new File(originFile).getName();

        // ------------------------------------------------------------
        // 1. 
        // ------------------------------------------------------------
        System.out.println(">>> Generating probability dataset...");
        DatasetGenerator.generateProbabilityDataset(originFile, probFile);
        System.out.println("Generated: " + probFile);

        // ------------------------------------------------------------
        // 2. 
        // ------------------------------------------------------------
        List<String> allItems = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(originFile))) {

            Set<String> unique = new TreeSet<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    unique.addAll(Arrays.asList(line.trim().split("\\s+")));
                }
            }
            allItems.addAll(unique);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return;
        }

        // ------------------------------------------------------------
        // 3. 
        // ------------------------------------------------------------
        List<Transaction> transactions
                = DataReader.readUncertainDataset(probFile, allItems);

        int itemCount = allItems.size();
        int transCount = transactions.size();

        int k = 50;

        // ============================================================
        // 4. TOP-K EXPECTED SUPPORT
        // ============================================================
        // System.out.println("\n--- Top-K Itemsets (Expected Support) ---");
        // TopKExpectedSupport topk = new TopKExpectedSupport(transactions, k);
        // List<Itemset> result1 = new ArrayList<>();
        // Result r1 = runAlgorithm(() -> {
        //     result1.addAll(topk.findTopK());
        // });
        // result1.forEach(System.out::println);
        // DataWriter.writeResultToFile(
        //         result1,
        //         "testcase/output_" + "k" + k + "_" + baseName,
        //         k,
        //         itemCount,
        //         transCount,
        //         r1.memMB,
        //         (long) r1.timeMs
        // );
        // ============================================================
        // 5. U-APRIORI
        // ============================================================
        System.out.println("\n--- Top-K Frequent Itemsets (U-Apriori) ---");

        UAprioriTopK apr = new UAprioriTopK(transactions, k);

        List<Itemset> result2 = new ArrayList<>();
        Result r2 = runAlgorithm(() -> {
            result2.addAll(apr.findTopK());
        });

        result2.forEach(System.out::println);

        DataWriter.writeResultToFile(
                result2,
                "testcase/uapriori_output_" + "k" + k + "_" + baseName,
                k,
                itemCount,
                transCount,
                r2.memMB,
                (long) r2.timeMs
        );

        // ============================================================
        // 6. U-FPGrowth
        // ============================================================
        System.out.println("\n--- Top-K Itemsets (U-FPGrowth) ---");
        PriorityQueue<Itemset> pqFP
                = new PriorityQueue<>(Comparator.comparingDouble(Itemset::getExpectedSupport));
        UFPgrowth fp = new UFPgrowth(transactions, k, pqFP);
        Result r3 = runAlgorithm(fp::mine);
        List<Itemset> result3 = new ArrayList<>(pqFP);
        result3.sort((a, b) -> Double.compare(b.getExpectedSupport(), a.getExpectedSupport()));
        result3.forEach(System.out::println);
        DataWriter.writeResultToFile(
                result3,
                "testcase/ufpgrowth_output_" + "k" + k + "_" + baseName,
                k,
                itemCount,
                transCount,
                r3.memMB,
                (long) r3.timeMs
        );
        // ============================================================
        // 7. U-HMine
        // ============================================================
        System.out.println("\n--- Top-K Itemsets (U-HMine) ---");
        PriorityQueue<Itemset> pqHM
                = new PriorityQueue<>(Comparator.comparingDouble(Itemset::getExpectedSupport));
        UHMine hm = new UHMine(transactions, k, pqHM);
        Result r4 = runAlgorithm(hm::mine);
        List<Itemset> result4 = new ArrayList<>(pqHM);
        result4.sort((a, b) -> Double.compare(b.getExpectedSupport(), a.getExpectedSupport()));
        result4.forEach(System.out::println);
        DataWriter.writeResultToFile(
                result4,
                "testcase/uhmine_output_" + "k" + k + "_" + baseName,
                k,
                itemCount,
                transCount,
                r4.memMB,
                (long) r4.timeMs
        );
        // ============================================================
        // 8. Hybrid FP-tree + H-Mine
        // ============================================================
        System.out.println("\n--- Top-K Itemsets (Hybrid) ---");

        HybridTopKMiner hybrid = new HybridTopKMiner(transactions, k, 0.5);

        List<Itemset> result5 = new ArrayList<>();
        Result r5 = runAlgorithm(() -> {
            result5.addAll(hybrid.mine());
        });

        result5.forEach(System.out::println);

        DataWriter.writeResultToFile(
                result5,
                "testcase/hybrid_output_" + "k" + k + "_" + baseName,
                k,
                itemCount,
                transCount,
                r5.memMB,
                (long) r5.timeMs
        );

        System.out.println("\n>>> DONE.");
    }
}
