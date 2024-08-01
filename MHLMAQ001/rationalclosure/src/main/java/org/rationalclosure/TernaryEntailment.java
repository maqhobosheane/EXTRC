/* Originally written by Evashna Pillay, optimized by Maqhobosheane Mohlerepe */

package org.rationalclosure;

import java.util.HashMap;
import java.util.Map;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;

public class TernaryEntailment {
    // Cache to store results of previously computed entailment checks
    private Map<String, Boolean> memoizationCache = new HashMap<>();

    // Main method to check entailment using ternary search
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        System.out.println("Starting ternary entailment check for: " + formula.toString());
        int min = 0;
        int max = rankedKB.length - 1;

        // Ternary search loop
        while (min <= max) {
            int mid1 = min + (max - min) / 3;
            int mid2 = max - (max - min) / 3;

            System.out.println("Checking range: " + min + " to " + mid1 + " and " + mid2 + " to " + max);

            // Check entailment in the ranges defined by mid1 and mid2
            boolean resultMid1 = checkEntailmentForRange(rankedKB, formula, min, mid1);
            boolean resultMid2 = checkEntailmentForRange(rankedKB, formula, mid2, max);

            System.out.println("Result for mid1: " + resultMid1 + ", Result for mid2: " + resultMid2);

            // Adjust the search range based on the results
            if (resultMid1) {
                max = mid1 - 1;
            } else if (resultMid2) {
                min = mid2 + 1;
            } else {
                min = mid1 + 1;
                max = mid2 - 1;
            }
        }

        // Final check for the narrowed-down range
        boolean finalResult = checkEntailmentForRange(rankedKB, formula, min, max);
        System.out.println("Final entailment result: " + finalResult);
        return finalResult;
    }

    // Helper method to check entailment within a specific range of ranks
    private boolean checkEntailmentForRange(PlBeliefSet[] rankedKB, PlFormula formula, int min, int max) {
        String cacheKey = formula.toString() + "_" + min + "_" + max;

        // Return cached result if available
        if (memoizationCache.containsKey(cacheKey)) {
            System.out.println("Cache hit for key: " + cacheKey);
            return memoizationCache.get(cacheKey);
        }

        // Combine the ranks within the specified range
        PlBeliefSet combinedBeliefSet = new PlBeliefSet();
        for (int i = min; i <= max; i++) {
            combinedBeliefSet.addAll(rankedKB[i]);
        }

        System.out.println("Combined belief set for range " + min + " to " + max + ": " + combinedBeliefSet.toString());

        // Check entailment using the SAT reasoner from the TweetyProject
        SatReasoner reasoner = new SatReasoner();
        boolean result = reasoner.query(combinedBeliefSet, formula);

        // Cache the result
        memoizationCache.put(cacheKey, result);
        return result;
    }
}
