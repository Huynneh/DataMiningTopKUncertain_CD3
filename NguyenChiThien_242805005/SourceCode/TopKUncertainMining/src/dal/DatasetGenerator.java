package dal;

import java.io.*;
import java.util.*;

public class DatasetGenerator {

    /**
     * Generate an uncertain dataset (probability dataset) from a classical transaction file.
     * Returns the list of all items (in sorted order) for later reference.
     *
     * @param inputFile  path to the original dataset file (space-separated items)
     * @param outputFile path to output probability file (optional)
     * @return list of all item names (Strings) in the dataset
     */
    public static List<String> generateProbabilityDataset(String inputFile, String outputFile) {
        if (outputFile == null || outputFile.isEmpty()) {
            File inFile = new File(inputFile);
            String name = inFile.getName();
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) name = name.substring(0, dotIndex);
            outputFile = inFile.getParent() + "/" + name + "_probability.txt";
        }

        List<List<String>> transactions = new ArrayList<>();
        Set<String> allItemsSet = new LinkedHashSet<>(); // giữ thứ tự xuất hiện

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] items = line.split("\\s+");
                transactions.add(Arrays.asList(items));
                allItemsSet.addAll(Arrays.asList(items));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        List<String> allItems = new ArrayList<>(allItemsSet);

        Random rand = new Random();

        // 2. Ghi file probability
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            for (List<String> transaction : transactions) {
                Set<String> transSet = new HashSet<>(transaction);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < allItems.size(); i++) {
                    String item = allItems.get(i);
                    double prob = transSet.contains(item) ? Math.round((rand.nextDouble() + 0.01) * 100.0) / 100.0 : 0.0;
                    sb.append(prob);
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

    // Test
    public static void main(String[] args) {
        List<String> allItems = generateProbabilityDataset("dataset/origin/example.txt", null);
        System.out.println("All items: " + allItems);
    }
}
