import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuerySetGeneratorWithMixedQueries {

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
        String outputFilePath = "mixed_repeated_100.txt";

        try {
            // Step 1: Read the files and store statements in a HashSet to ensure uniqueness
            HashSet<String> uniqueStatements = new HashSet<>();
            for (String inputFilePath : inputFilePaths) {
                BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
                String line;
                while ((line = reader.readLine()) != null) {
                    uniqueStatements.add(line.trim()); // Trim to remove leading/trailing spaces
                }
                reader.close();
            }

            // Step 2: Ensure uniqueness and select 50 unique statements
            List<String> uniqueList = new ArrayList<>(uniqueStatements);
            if (uniqueList.size() >= 50) {
                Random random = new Random();
                List<String> selectedStatements = new ArrayList<>();
                List<String> repeatedStatements = new ArrayList<>();

                // Select 50 unique statements
                for (int i = 0; i < 50; i++) {
                    int index = random.nextInt(uniqueList.size());
                    selectedStatements.add(uniqueList.get(index));
                    uniqueList.remove(index); // Remove to ensure no duplicates
                }

                // Select 50 repeated statements (could be any from the 50 unique ones)
                for (int i = 0; i < 50; i++) {
                    int index = random.nextInt(selectedStatements.size());
                    repeatedStatements.add(selectedStatements.get(index));
                }

                // Step 3: Write the selected statements (both unique and repeated) to a new file
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
                for (String statement : selectedStatements) {
                    writer.write(statement);
                    writer.newLine();
                }
                for (String statement : repeatedStatements) {
                    writer.write(statement);
                    writer.newLine();
                }
                writer.close();

                System.out.println("Mixed unique and repeated query set of 100 statements created successfully.");
            } else {
                System.out.println("Not enough unique statements available.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
