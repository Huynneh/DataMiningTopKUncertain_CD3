package dal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter {

    /**
     * Xuất dữ liệu ra file CSV dạng bảng.
     *
     * @param headers  Danh sách tiêu đề cột
     * @param rows     Danh sách các dòng (mỗi dòng là List<String>)
     * @param filePath Đường dẫn file CSV đầu ra
     */
    public static void exportToCSV(List<String> headers,
                                   List<List<String>> rows,
                                   String filePath) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            // --- Ghi header ---
            writer.write(String.join(",", headers));
            writer.newLine();

            // --- Ghi từng dòng ---
            for (List<String> row : rows) {
                // Escape dấu phẩy và dấu nháy nếu có
                String line = convertRowToCSV(row);
                writer.write(line);
                writer.newLine();
            }

            System.out.println("CSV exported to: " + filePath);

        } catch (IOException e) {
            System.err.println("ERROR writing CSV: " + e.getMessage());
        }
    }

    /**
     * Xử lý 1 dòng để đảm bảo chuẩn CSV.
     */
    private static String convertRowToCSV(List<String> row) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (String col : row) {
            if (!first) sb.append(",");
            first = false;

            if (col == null) col = "";

            // Nếu có dấu phẩy hoặc dấu nháy, phải thêm dấu nháy kép
            if (col.contains(",") || col.contains("\"")) {
                col = col.replace("\"", "\"\"");
                sb.append("\"").append(col).append("\"");
            } else {
                sb.append(col);
            }
        }

        return sb.toString();
    }
}
