/* Author: Maqhobosheane Mohlerepe */

package org.rationalclosure;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;

public class TimedReasonerComparison {

    public static void runTimedComparison(ArrayList<PlBeliefSet> rankedKB, PlParser parser, String queryFile) throws IOException, ParserException {
        // Read the query file and store the queries
        List<PlFormula> queries = new ArrayList<>();
        try (Scanner qreader = new Scanner(new File(queryFile))) {
            while (qreader.hasNextLine()) {
                String queryFormula = qreader.nextLine().trim();
                if (!queryFormula.isEmpty()) {
                    queryFormula = App.reformatConnectives(App.reformatDefeasible(queryFormula));
                    queries.add((PlFormula) parser.parseFormula(queryFormula));
                }
            }
        }

        // List of reasoners to compare
        List<EntailmentInterface> reasoners = new ArrayList<>();
        reasoners.add(new NaiveEntailment());
        reasoners.add(new BinaryEntailment());
        reasoners.add(new TernaryEntailment());
        reasoners.add(new NCachedEntailment()); // Cached Naive Entailment
        reasoners.add(new BCachedEntailment()); // Cached Binary Entailment
        reasoners.add(new TCachedEntailment()); // Cached Ternary Entailment

        // Prepare CSV writer
        try (FileWriter csvWriter = new FileWriter("timing_results.csv")) {
            csvWriter.append("KnowledgeBase,QuerySet,Algorithm,AverageTime(ms)\n");

            // Time each reasoner for the entire query set
            for (EntailmentInterface reasoner : reasoners) {
                long totalTime = 0;

                // Run the entire query set 5 times and calculate the average time
                for (int i = 0; i < 5; i++) {
                    long startTime = System.nanoTime();

                    // Check entailment for all queries in the set
                    for (PlFormula query : queries) {
                        reasoner.checkEntailment(rankedKB.toArray(new PlBeliefSet[0]), query);
                    }

                    long endTime = System.nanoTime();
                    long duration = (endTime - startTime) / 1000000; // Convert to milliseconds
                    totalTime += duration;

                    // Clear the cache after each iteration
                    if (reasoner instanceof NCachedEntailment) {
                        ((NCachedEntailment) reasoner).clearCache();
                    } else if (reasoner instanceof BCachedEntailment) {
                        ((BCachedEntailment) reasoner).clearCache();
                    } else if (reasoner instanceof TCachedEntailment) {
                        ((TCachedEntailment) reasoner).clearCache();
                    }
                }

                long averageTime = totalTime / 5;
                System.out.println(reasoner.getClass().getSimpleName() + " took " + averageTime + " ms on average for the entire query set");

                // Write the result to the CSV file
                csvWriter.append("YourKnowledgeBaseName," + queryFile + "," + reasoner.getClass().getSimpleName() + "," + averageTime + "\n");
            }
        }

        System.out.println("Timing results saved to timing_results.csv");
    }
}
