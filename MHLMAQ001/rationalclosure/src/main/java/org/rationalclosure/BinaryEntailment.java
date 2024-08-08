/* Author: Maqhobosheane Mohlerepe, algorithm adopted from Joel Hamilton */

package org.rationalclosure;

import java.util.ArrayList;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;

public class BinaryEntailment implements EntailmentInterface {
    // Main method to check entailment using binary rational closure
    @Override
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        System.out.println("Starting binary entailment check for: " + formula.toString());

        // Check if the infinite rank is empty
        boolean infiniteRankEmpty = rankedKB.length == 0 || rankedKB[rankedKB.length - 1].isEmpty();

        // If infinite rank is not empty, treat it as an additional rank
        int high = infiniteRankEmpty ? rankedKB.length - 1 : rankedKB.length;
        int low = 0;

        while (high > low) {
            int mid = low + (high - low) / 2;
            System.out.println("Low: " + low + ", High: " + high + ", Midpoint: " + mid);

            // Check if removing ranks from mid+1 to high results in consistency with the negated antecedent
            PlBeliefSet combinedBeliefSet = combineRanks(rankedKB, mid + 1, high - 1);
            PlFormula negatedAntecedent = App.negateAntecedent(formula);

            SatReasoner reasoner = new SatReasoner();
            if (reasoner.query(combinedBeliefSet, negatedAntecedent)) {
                low = mid + 1; // If consistent, update low to search the upper half
                System.out.println("Negated antecedent is consistent with combined belief set from mid+1 to high-1. Updating low to " + low);
            } else {
                // Otherwise, check if adding rank mid results in consistency with the negated antecedent
                combinedBeliefSet = combineRanks(rankedKB, mid, high - 1);
                if (reasoner.query(combinedBeliefSet, negatedAntecedent)) {
                    // If consistent, update high to search the lower half
                    high = mid;
                    System.out.println("Negated antecedent is consistent with combined belief set from mid to high-1. Updating high to " + high);
                } else {
                    // If not, check the final entailment result with the combined belief set
                    combinedBeliefSet = combineRanks(rankedKB, mid + 1, high - 1);
                    boolean finalResult = reasoner.query(combinedBeliefSet, formula);
                    System.out.println("Final entailment check result: " + finalResult);
                    return finalResult;
                }
            }
        }

        // Final entailment check with the combined belief set from low to high
        PlBeliefSet finalCombinedBeliefSet = combineRanks(rankedKB, low, high - 1);
        boolean finalResult = new SatReasoner().query(finalCombinedBeliefSet, formula);
        System.out.println("Final combined belief set: " + finalCombinedBeliefSet);
        System.out.println("Final entailment result: " + finalResult);
        return finalResult;
    }

    // Combine the belief sets from the specified range (start to end)
    private PlBeliefSet combineRanks(PlBeliefSet[] rankedKB, int start, int end) {
        PlBeliefSet combinedBeliefSet = new PlBeliefSet();
        for (int i = start; i <= end; i++) {
            combinedBeliefSet.addAll(rankedKB[i]);
        }
        System.out.println("Combined belief set for range " + start + " to " + end + ": " + combinedBeliefSet.toString());
        return combinedBeliefSet;
    }
}
