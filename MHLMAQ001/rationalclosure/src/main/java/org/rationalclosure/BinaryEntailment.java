/* Author: Maqhobosheane Mohlerepe, algorithm adopted from Joel Hamilton */

package org.rationalclosure;

import java.util.ArrayList;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;

public class BinaryEntailment implements EntailmentInterface {
  
    private PlBeliefSet[] rankedKB;
    private PlFormula formula;

    // Constructor that accepts rankedKB and formula
    public BinaryEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        this.rankedKB = rankedKB;
        this.formula = formula;
    }

    // Default constructor
    public BinaryEntailment() {
    }

    // Main method to check entailment using binary rational closure
    @Override
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        System.out.println("Starting binary entailment check for: " + formula.toString());

        // Check if the infinite rank is empty
        boolean infiniteRankEmpty = rankedKB.length == 0 || rankedKB[rankedKB.length - 1].isEmpty();

        // If infinite rank is not empty, treat it as an additional rank
        int high = infiniteRankEmpty ? rankedKB.length - 1 : rankedKB.length;
        int low = 0;

        // Initialize the reasoner
        SatReasoner reasoner = new SatReasoner();
        PlFormula negatedAntecedent = App.negateAntecedent(formula);

        int originalLow = low;
        int originalHigh = high;

        while (high > low) {
            int mid = low + (high - low) / 2;
            System.out.println("Low: " + low + ", High: " + high + ", Midpoint: " + mid);

            // Check if removing ranks from mid+1 to high results in consistency with the negated antecedent
            PlBeliefSet combinedBeliefSetMidToEnd = combineRanks(rankedKB, mid + 1, high - 1);
            System.out.println("Combined belief set (mid+1 to high-1): " + combinedBeliefSetMidToEnd.toString());

            if (reasoner.query(combinedBeliefSetMidToEnd, negatedAntecedent)) {
                low = mid + 1; // If consistent, update low to search the upper half
                System.out.println("Negated antecedent is consistent with combined belief set from mid+1 to high-1. Updating low to " + low);
            } else {
                // Otherwise, check if adding rank mid results in consistency with the negated antecedent
                PlBeliefSet combinedBeliefSetMinToMid = combineRanks(rankedKB, low, mid);
                System.out.println("Combined belief set (low to mid): " + combinedBeliefSetMinToMid.toString());

                if (reasoner.query(combinedBeliefSetMinToMid, negatedAntecedent)) {
                    high = mid; // If consistent, update high to search the lower half
                    System.out.println("Negated antecedent is consistent with combined belief set from low to mid. Updating high to " + high);
                } else {
                    // Final belief set identified, highest rank to remove: mid
                    int highestRank = mid;
                    System.out.println("Final belief set identified, highest rank to remove: " + highestRank);

                    // Check if no ranks were removed, i.e., low and high still encompass the entire range
                    if (low == originalLow && high == originalHigh) {
                        System.out.println("No ranks removed; using the entire original belief set.");
                        return reasoner.query(combineRanks(rankedKB, 0, rankedKB.length - 1), formula);
                    }

                    // Combine ranks greater than the identified rank
                    PlBeliefSet finalBeliefSet = combineRanks(rankedKB, highestRank + 1, rankedKB.length - 1);
                    System.out.println("Final combined belief set after removing higher ranks: " + finalBeliefSet.toString());

                    // Perform the final entailment check
                    boolean finalResult = reasoner.query(finalBeliefSet, formula);
                    System.out.println("Final entailment check result: " + finalResult);
                    return finalResult;
                }
            }
        }

        // Final entailment check with the combined belief set from low to high-1
        PlBeliefSet finalCombinedBeliefSet = combineRanks(rankedKB, low, high - 1);
        System.out.println("Final combined belief set (low to high-1): " + finalCombinedBeliefSet.toString());

        // Check if no ranks were removed, i.e., low and high still encompass the entire range
        if (low == originalLow && high == originalHigh) {
            System.out.println("No ranks removed; using the entire original belief set.");
            return reasoner.query(combineRanks(rankedKB, 0, rankedKB.length - 1), formula);
        }

        boolean finalResult = reasoner.query(finalCombinedBeliefSet, formula);
        System.out.println("Final entailment result: " + finalResult);
        return finalResult;
    }

    // Combine the belief sets from the specified range (start to end)
    private PlBeliefSet combineRanks(PlBeliefSet[] rankedKB, int start, int end) {
        PlBeliefSet combinedBeliefSet = new PlBeliefSet();
        for (int i = start; i <= end; i++) {
            if (rankedKB[i] != null) {  // Ensure we're not adding null belief sets
                combinedBeliefSet.addAll(rankedKB[i]);
            }
        }
        System.out.println("Combined belief set for range " + start + " to " + end + ": " + combinedBeliefSet.toString());
        return combinedBeliefSet;
    }
}

