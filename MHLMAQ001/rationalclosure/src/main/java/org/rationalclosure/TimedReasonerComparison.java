/* Author: Maqhobosheane Mohlerepe */

package org.rationalclosure;

import java.io.File;
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
        // Read the query file
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
        reasoners.add(new NaiveEntailment(rankedKB.toArray(new PlBeliefSet[0]), null));
        reasoners.add(new BinaryEntailment(rankedKB.toArray(new PlBeliefSet[0]), null));
        reasoners.add(new TernaryEntailment(rankedKB.toArray(new PlBeliefSet[0]), null));

        // Time each reasoner for each query
        for (PlFormula query : queries) {
            System.out.println("\nProcessing query: " + query);

            for (EntailmentInterface reasoner : reasoners) {
                long startTime = System.nanoTime();
                boolean result = reasoner.checkEntailment(rankedKB.toArray(new PlBeliefSet[0]), query);
                long endTime = System.nanoTime();

                long duration = (endTime - startTime) / 1000000; // Convert to milliseconds
                System.out.println(reasoner.getClass().getSimpleName() + " took " + duration + " ms, Result: " + result);
            }
        }
    }
}
