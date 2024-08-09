/* Author: Maqhobosheane Mohlerepe, algorithm adopted from Evashna Pillay */

package org.rationalclosure;

import java.util.ArrayList;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;

public class TernaryEntailment implements EntailmentInterface {

    private PlBeliefSet[] rankedKB;
    private PlFormula formula;

    // Constructor that accepts rankedKB and formula
    public TernaryEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        this.rankedKB = rankedKB;
        this.formula = formula;
    }

    // Default constructor
    public TernaryEntailment() {
    }

    @Override
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        System.out.println("Starting ternary entailment check for: " + formula.toString());

        // Check if the infinite rank is empty
        boolean infiniteRankEmpty = rankedKB.length == 0 || rankedKB[rankedKB.length - 1].isEmpty();

        // If infinite rank is not empty, treat it as an additional rank
        int max = infiniteRankEmpty ? rankedKB.length - 1 : rankedKB.length;
        int min = 0;

        // Continue the search until min and max converge
        while (max > min) {
            // Calculate the first and second midpoints
            int mid1 = min + (max - min) / 3;
            int mid2 = max - (max - min) / 3;

            System.out.println("Checking min: " + min + ", max: " + max + ", mid1: " + mid1 + ", mid2: " + mid2);

            // Check if removing ranks from mid1+1 to max results in consistency with the negated antecedent
            PlBeliefSet combinedBeliefSetMid1 = combineRanks(rankedKB, mid1 + 1, max - 1);
            PlFormula negatedAntecedent = App.negateAntecedent(formula);
            
            SatReasoner reasoner = new SatReasoner();
            if (reasoner.query(combinedBeliefSetMid1, negatedAntecedent)) {
                // If consistent, update min to search the upper half
                min = mid1 + 1;
                System.out.println("Negated antecedent is consistent with combined belief set from mid1+1 to max-1. Updating min to " + min);
            } else {
                // Otherwise, check if adding rank mid1 results in consistency with the negated antecedent
                PlBeliefSet combinedBeliefSetMid1AndBelow = combineRanks(rankedKB, min, mid1);
                if (reasoner.query(combinedBeliefSetMid1AndBelow, negatedAntecedent)) {
                    // If consistent, update max to search the lower half
                    max = mid1;
                    System.out.println("Negated antecedent is consistent with combined belief set from min to mid1. Updating max to " + max);
                } else {
                    // If not, check the final entailment result with the combined belief set
                    return reasoner.query(combinedBeliefSetMid1AndBelow, formula);
                }
            }

            // Check if mid2 is within the valid range
            if (mid2 < rankedKB.length) {
                // Check if removing ranks from mid2+1 to max results in consistency with the negated antecedent
                PlBeliefSet combinedBeliefSetMid2 = combineRanks(rankedKB, mid2 + 1, max - 1);
                if (reasoner.query(combinedBeliefSetMid2, negatedAntecedent)) {
                    // If consistent, update min to search the upper half
                    min = mid2 + 1;
                    System.out.println("Negated antecedent is consistent with combined belief set from mid2+1 to max-1. Updating min to " + min);
                } else {
                    // Otherwise, check if adding rank mid2 results in consistency with the negated antecedent
                    PlBeliefSet combinedBeliefSetMid2AndBelow = combineRanks(rankedKB, min, mid2);
                    if (reasoner.query(combinedBeliefSetMid2AndBelow, negatedAntecedent)) {
                        // If consistent, update max to search the lower half
                        max = mid2;
                        System.out.println("Negated antecedent is consistent with combined belief set from min to mid2. Updating max to " + max);
                    } else {
                        // If not, check the final entailment result with the combined belief set
                        return reasoner.query(combinedBeliefSetMid2AndBelow, formula);
                    }
                }
            } else if (mid2 == rankedKB.length) {
                // If mid2 is out of range, adjust min and max accordingly
                max = mid1;
                min = mid1 + 1;
                System.out.println("mid2 equals rankedKB length, adjust min and max.");
            }
        }

        // Final entailment check with the combined belief set from 0 to max
        PlBeliefSet finalCombinedBeliefSet = combineRanks(rankedKB, 0, max - 1);
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
