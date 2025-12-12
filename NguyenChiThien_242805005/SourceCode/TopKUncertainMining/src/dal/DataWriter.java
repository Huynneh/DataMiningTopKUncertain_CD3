package dal;

import java.io.*;
import java.util.*;
import model.Itemset;

/**
 * Lớp hỗ trợ ghi kết quả khai thác Top-K frequent itemsets
 * từ cơ sở dữ liệu không chắc chắn ra file văn bản.
 */

public class DataWriter {

    /**
     * @param itemsets     danh sách Top-K frequent itemsets
     * @param outputFile   đường dẫn file đầu ra
     * @param topK         số lượng K cần lấy
     * @param itemCount    tổng số item trong dataset
     * @param transCount   tổng số giao dịch trong dataset
     * @param maxMemoryMB  lượng bộ nhớ tối đa sử dụng (MB)
     * @param totalTimeMS  tổng thời gian chạy thuật toán (ms)
     */
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

            bw.write("======PRINT TOP-K FREQUENT ITEMSETS FROM UNCERTAIN DATABASES=====\n\n");
            for (Itemset itemset : itemsets) {
                bw.write(itemset.toString() + "\n");
            }
            bw.write("\n");
            bw.write("============= TOP-K FREQUENT FROM UNCERTAIN DATABASES=============\n");
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
