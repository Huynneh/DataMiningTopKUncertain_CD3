package main;

import bll.*;
import dal.*;
import java.io.*;
import java.util.*;
import model.*;

public class Main {

    // =====================================================================
    // Utility: đo thời gian chạy và bộ nhớ sử dụng cho một thuật toán
    // =====================================================================
    /**
     * Chạy một thuật toán bất kỳ và đo:
     *  - Thời gian thực thi (ms)
     *  - Bộ nhớ tăng thêm (MB)
     *
     * @param algorithm Runnable chứa việc thực thi thuật toán
     * @return Result đối tượng chứa timeMs và memMB
     */
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

    /**
     * Lưu kết quả đo thời gian và bộ nhớ.
     */
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


        // -----------------------------------------------------------------
        // CẤU HÌNH
        // -----------------------------------------------------------------
        String originFolder = "datasets/origin/";
        String probFolder   = "datasets/probability/";
        String outFolder    = "outputs/";

        int topK = 10;
        double densityThreshold = 0.1;

        new File(probFolder).mkdirs();
        new File(outFolder).mkdirs();

        // -----------------------------------------------------------------
        // Lấy danh sách file origin
        // -----------------------------------------------------------------
        File folder = new File(originFolder);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null) {
            System.out.println("ERROR: Origin folder does not exist: " + originFolder);
            return;
        }
        if (files.length == 0) {
            System.out.println("No .txt files found in: " + originFolder);
            return;
        }

        System.out.println("Detected " + files.length + " dataset(s).\n");

        // =====================================================================
        // DUYỆT TẤT CẢ DATASET
        // =====================================================================
        for (File file : files) {

            String datasetName = file.getName().replace(".txt", "");
            String originFile  = originFolder + file.getName();
            String probFile    = probFolder + datasetName + "_probability.txt";

            System.out.println("==================================================");
            System.out.println(">>> PROCESSING DATASET: " + datasetName);
            System.out.println("==================================================");

            // -------------------------------------------------------------
            // 1. SINH DATASET XÁC SUẤT + LẤY DANH SÁCH ITEM CHUẨN
            // -------------------------------------------------------------
            System.out.println("\n>>> Generating probability dataset...");
            List<String> allItems = DatasetGenerator.generateProbabilityDataset(originFile, probFile);
            System.out.println("Generated: " + probFile);
            System.out.println("Total items = " + allItems.size());

            // -------------------------------------------------------------
            // 2. ĐỌC DATASET XÁC SUẤT TRÙNG KHỚP VỚI allItems
            // -------------------------------------------------------------
            List<Transaction> transactions =
                    DataReader.readUncertainDataset(probFile, allItems);

            int itemCount = allItems.size();
            int transCount = transactions.size();
            System.out.println("Transactions: " + transCount);

            // =====================================================================
            // 4. U-APRIORI
            // =====================================================================
            System.out.println("\n--- Running U-Apriori ---");

            UAprioriTopK apriori = new UAprioriTopK(transactions, topK);
            List<Itemset> resultApriori = new ArrayList<>();

            Result rA = runAlgorithm(() -> resultApriori.addAll(apriori.findTopK()));

            DataWriter.writeResultToFile(
                    resultApriori,
                    outFolder + "uapriori_k" + topK + "_" + datasetName + ".txt",
                    topK, itemCount, transCount,
                    rA.memMB, (long) rA.timeMs
            );

            // =====================================================================
            // 5. U-FPGrowth
            // =====================================================================
            System.out.println("\n--- Running U-FPGrowth ---");

            PriorityQueue<Itemset> pqFP =
                    new PriorityQueue<>(Comparator.comparingDouble(Itemset::getExpectedSupport));

            UFPgrowth fp = new UFPgrowth(transactions, topK, pqFP);
            Result rFP = runAlgorithm(fp::mine);

            List<Itemset> resultFP = new ArrayList<>(pqFP);
            resultFP.sort((a, b) -> Double.compare(b.getExpectedSupport(), a.getExpectedSupport()));

            DataWriter.writeResultToFile(
                    resultFP,
                    outFolder + "ufpgrowth_k" + topK + "_" + datasetName + ".txt",
                    topK, itemCount, transCount,
                    rFP.memMB, (long) rFP.timeMs
            );

            // =====================================================================
            // 6. U-HMine
            // =====================================================================
            System.out.println("\n--- Running U-HMine ---");

            PriorityQueue<Itemset> pqHM =
                    new PriorityQueue<>(Comparator.comparingDouble(Itemset::getExpectedSupport));

            UHMine hm = new UHMine(transactions, topK, pqHM);
            Result rHM = runAlgorithm(hm::mine);

            List<Itemset> resultHM = new ArrayList<>(pqHM);
            resultHM.sort((a, b) -> Double.compare(b.getExpectedSupport(), a.getExpectedSupport()));

            DataWriter.writeResultToFile(
                    resultHM,
                    outFolder + "uhmine_k" + topK + "_" + datasetName + ".txt",
                    topK, itemCount, transCount,
                    rHM.memMB, (long) rHM.timeMs
            );

            // =====================================================================
            // 7. HybridTopKMiner
            // =====================================================================
            System.out.println("\n--- Running HybridTopKMiner ---");

            HybridTopKMiner hybrid = new HybridTopKMiner(transactions, topK, densityThreshold);
            List<Itemset> resultHybrid = new ArrayList<>();

            Result rHybrid = runAlgorithm(() -> resultHybrid.addAll(hybrid.mine()));

            DataWriter.writeResultToFile(
                    resultHybrid,
                    outFolder + "hybrid_k" + topK + "_" + datasetName + ".txt",
                    topK, itemCount, transCount,
                    rHybrid.memMB, (long) rHybrid.timeMs
            );

            System.out.println("\n>>> FINISHED DATASET: " + datasetName + "\n");
        }

        System.out.println("\n>>> ALL DATASETS COMPLETED.");
    }
}
