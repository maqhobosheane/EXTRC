import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuerySetWithSingleAntecedent {

      public static void main(String[] args) {
        // Array of input file paths
        String[] inputFilePaths = {
            "kbsets\\1k50.txt", // Add more file paths as needed
            "kbsets\\500statements.txt",
            "kbsets\\2k50.txt",
            "kbsets\\10ranks.txt",
            "kbsets\\100ranks.txt",
            "kbsets\\exp10.txt",
            "kbsets\\uniform10.txt",
            "kbsets\\normal10.txt"
        };
        String outputFilePath = "repeated_antecedent.txt";

        try {
            // Step 1: Read the files and organize statements by their antecedent
            HashSet<String> uniqueConsequents = new HashSet<>();
            String chosenAntecedent = null;

            for (String inputFilePath : inputFilePaths) {
                BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("~>");
                    if (parts.length == 2) {
                        String antecedent = parts[0];
                        String consequent = parts[1];

                        // If chosenAntecedent is not set, set it to the first antecedent found
                        if (chosenAntecedent == null) {
                            chosenAntecedent = antecedent;
                        }

                        // Add only consequents that belong to the chosen antecedent
                        if (chosenAntecedent.equals(antecedent)) {
                            uniqueConsequents.add(consequent);
                        }
                    }
                }
                reader.close();
            }

            // Step 2: Ensure we have 100 unique consequents
            List<String> consequentList = new ArrayList<>(uniqueConsequents);
            Random random = new Random();

            while (consequentList.size() < 100) {
                // Generate a random numeric consequent and ensure it's unique
                String generatedConsequent = String.valueOf(random.nextInt(1000));
                if (!uniqueConsequents.contains(generatedConsequent)) {
                    consequentList.add(generatedConsequent);
                    uniqueConsequents.add(generatedConsequent);
                }
            }

            // Shuffle the list to ensure randomness
            List<String> querySet = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                querySet.add(chosenAntecedent + "~>" + consequentList.get(i));
            }

            // Step 3: Write the selected queries to a new file
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
            for (String query : querySet) {
                writer.write(query);
                writer.newLine();
            }
            writer.close();

            System.out.println("Query set with a single antecedent and 100 unique numeric consequents (including generated ones) created successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}