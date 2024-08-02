/* Author: Maqhobosheane Mohlerepe */

package org.rationalclosure;

import java.util.ArrayList;
import org.tweetyproject.logics.pl.syntax.*;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

public class TimedReasonerComparison {

    public static void main(String[] args) {
        // Setup the belief sets and reasoners for comparison
        PlBeliefSet beliefSet = new PlBeliefSet();
        PlBeliefSet classicalSet = new PlBeliefSet();

        // Example belief set (these should be read from a file or another source)
        beliefSet.add(new Implication(new Proposition("A"), new Proposition("B")));
        classicalSet.add(new Implication(new Proposition("B"), new Proposition("C")));

        // Create ranked knowledge base
        BaseRankThreaded.setCkb(classicalSet);
            
        ArrayList<PlBeliefSet> rankedKB = BaseRankThreaded.rank(beliefSet, new PlBeliefSet());

        // Create reasoners
        RationalReasoner reasoner = new RationalReasoner(rankedKB);

        // Query to test
        PlFormula query = new Implication(new Proposition("A"), new Proposition("C"));

        // Measure time for querying
        long startTime = System.nanoTime();
        boolean result = reasoner.query(query);
        long endTime = System.nanoTime();

        // Output result and time taken
        System.out.println("Query result: " + result);
        System.out.println("Time taken: " + (endTime - startTime) + " ns");
    }
}

