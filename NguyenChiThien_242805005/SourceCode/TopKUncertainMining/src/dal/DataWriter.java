package dal;

import java.io.*;
import java.util.*;
import model.Itemset;

public class DataWriter {

    public static void writeResultToFile(
            List<Itemset> itemsets,
            String outputFile,
            int topK,
            int itemCount,
            int transCount,
            double maxMemoryMB,
            long totalTimeMS
    ) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

            // ===== HEADER =====
            bw.write("======PRINT TOP-K FREQUENT ITEMSETS FROM UNCERTAIN DATABASES=====\n\n");

            // ===== TOP-K RESULTS =====
            for (Itemset itemset : itemsets) {
                bw.write(itemset.toString() + "\n");
            }

            bw.write("\n");
            bw.write("============= TOP-K FREQUENT FROM UNCERTAIN DATABASES=============\n");

            // ===== STATISTICS =====
            bw.write(" Top K = " + topK + "\n");
            bw.write(" Items count from dataset: " + itemCount + "\n");
            bw.write(" Transactions count from dataset : " + transCount + "\n");
            bw.write(" Maximum memory usage : " + maxMemoryMB + " mb\n");
            bw.write(" Total time ~ " + totalTimeMS + " ms\n");

            bw.write("===================================================\n");

            bw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
