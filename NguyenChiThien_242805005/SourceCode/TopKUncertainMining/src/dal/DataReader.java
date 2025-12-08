package dal;

import java.io.*;
import java.util.*;
import model.Transaction;

public class DataReader {

    public static List<Transaction> readUncertainDataset(String probabilityFile, List<String> allItems) {
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(probabilityFile))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] probs = line.split("\\s+");
                Map<String, Double> items = new LinkedHashMap<>();

                for (int i = 0; i < probs.length; i++) {
                    double p = Double.parseDouble(probs[i]);
                    if (p > 0) {
                        items.put(allItems.get(i), p); 
                    }
                }

                transactions.add(new Transaction(items));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactions;
    }
}
