package dal;

import java.io.*;
import java.util.*;
import model.Transaction;

public class DataReader {
    /**
     * Đọc dữ liệu giao dịch không chắc chắn từ file xác suất.
     * Mỗi dòng tương ứng một giao dịch, mỗi giá trị là xác suất xuất hiện của item.
     *
     * @param probabilityFile  đường dẫn file xác suất
     * @param allItems         danh sách tất cả item theo đúng thứ tự cột
     * @return danh sách giao dịch dưới dạng Transaction
     */
    public static List<Transaction> readUncertainDataset(String probabilityFile, List<String> allItems) {
        // Tạo danh sách giao dịch để lưu kết quả đọc từ file
        List<Transaction> transactions = new ArrayList<>();

        // Mở file bằng BufferedReader (tự đóng sau khi dùng nhờ try-with-resources)
        try (BufferedReader br = new BufferedReader(new FileReader(probabilityFile))) {
            String line;
            // Đọc từng dòng cho đến khi hết file
            while ((line = br.readLine()) != null) {
                // Xóa khoảng trắng ở đầu/cuối dòng
                line = line.trim();
                // Bỏ qua dòng trống
                if (line.isEmpty()) continue;

                // Tách dòng thành các chuỗi xác suất (phân cách bởi khoảng trắng)
                String[] probs = line.split("\\s+");
                // Map item -> xác suất, dùng LinkedHashMap để giữ thứ tự
                Map<String, Double> items = new LinkedHashMap<>();

                // Duyệt qua từng xác suất trong dòng
                for (int i = 0; i < probs.length; i++) {
                    double p = Double.parseDouble(probs[i]);
                    // Chỉ thêm vào giao dịch nếu xác suất > 0
                    if (p > 0) {
                        items.put(allItems.get(i), p); 
                    }
                }
                // Tạo giao dịch và thêm vào danh sách kết quả
                transactions.add(new Transaction(items));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public static List<String> readItemList(String itemFile) {
    List<String> items = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(itemFile))) {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty()) {
                items.add(line);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    return items;
}
}
