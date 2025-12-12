package dal;

import java.io.*;
import java.util.*;

public class DatasetGenerator {

    /**
     * Sinh dataset không chắc chắn (probabilistic dataset) từ dataset giao dịch truyền thống.
     * Mỗi item trong giao dịch sẽ được gán một xác suất ngẫu nhiên > 0.
     *
     * @param inputFile  đường dẫn dataset gốc (các item cách nhau bởi khoảng trắng)
     * @param outputFile đường dẫn file output probability (nếu null → tự sinh)
     * @return danh sách tất cả item (giữ nguyên thứ tự xuất hiện)
     */
    public static List<String> generateProbabilityDataset(String inputFile, String outputFile) {
        // Nếu không chỉ định file output -> tự tạo file "_probability.txt"
        if (outputFile == null || outputFile.isEmpty()) {
            File inFile = new File(inputFile);
            // lấy tên file
            String name = inFile.getName();
            // vị trí dấu .
            int dotIndex = name.lastIndexOf('.');
            // bỏ phần mở rộng
            if (dotIndex > 0) name = name.substring(0, dotIndex);
            // tạo file _probability
            outputFile = inFile.getParent() + "/" + name + "_probability.txt";
        }

        // Lưu toàn bộ giao dịch dưới dạng list item
        List<List<String>> transactions = new ArrayList<>();

        // Lưu tất cả item, dùng LinkedHashSet để giữ nguyên thứ tự xuất hiện
        Set<String> allItemsSet = new LinkedHashSet<>();

        // Đọc file dữ liệu gốc
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            // Đọc từng dòng giao dịch
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // bỏ dòng rỗng
                if (line.isEmpty()) continue;

                // Tách item trong giao dịch
                String[] items = line.split("\\s+");
                // Lưu giao dịch vào danh sách
                transactions.add(Arrays.asList(items));
                // Thêm item vào tập allItems
                allItemsSet.addAll(Arrays.asList(items));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Chuyển Set -> List để dùng index
        List<String> allItems = new ArrayList<>(allItemsSet);

        // random để sinh xác suất
        Random rand = new Random();

        // Ghi file probability
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            // Duyệt từng giao dịch
            for (List<String> transaction : transactions) {
                // Dùng Set để kiểm tra item có xuất hiện hay không
                Set<String> transSet = new HashSet<>(transaction);
                StringBuilder sb = new StringBuilder();

                // Duyệt toàn bộ item trong allItems
                for (int i = 0; i < allItems.size(); i++) {
                    String item = allItems.get(i);

                    // Nếu item xuất hiện -> gán xác suất random > 0, làm tròn 2 chữ số // không xuất hiện -> xác suất 0
                    double prob = transSet.contains(item) ? Math.round((rand.nextDouble() + 0.01) * 100.0) / 100.0 : 0.0;
                    sb.append(prob);
                    // Thêm dấu cách giữa các cột
                    if (i < allItems.size() - 1) sb.append(" ");
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Probability dataset generated at: " + outputFile);
        return allItems;
    }
}
