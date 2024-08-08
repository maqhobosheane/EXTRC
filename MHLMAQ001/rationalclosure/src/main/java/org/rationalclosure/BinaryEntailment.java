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

        int low = 0;
        int high = rankedKB.length - 1;

        while (high > low) {
            int mid = low + (high - low) / 2;
            System.out.println("Midpoint: " + mid);

            PlBeliefSet combinedBeliefSet = new PlBeliefSet();
            for (int j = mid + 1; j <= high; j++) {
                combinedBeliefSet.addAll(rankedKB[j]);
            }
            System.out.println("Combined belief set (mid+1 to end): " + combinedBeliefSet.toString());

            PlFormula negatedAntecedent = App.negateAntecedent(formula);
            SatReasoner reasoner = new SatReasoner();

            if (reasoner.query(combinedBeliefSet, negatedAntecedent)) {
                low = mid + 1;
            } else {
                combinedBeliefSet = new PlBeliefSet();
                for (int j = mid; j <= high; j++) {
                    combinedBeliefSet.addAll(rankedKB[j]);
                }
                System.out.println("Combined belief set (mid to end): " + combinedBeliefSet.toString());

                if (reasoner.query(combinedBeliefSet, negatedAntecedent)) {
                    combinedBeliefSet = new PlBeliefSet();
                    for (int j = mid + 1; j < rankedKB.length; j++) {
                        combinedBeliefSet.addAll(rankedKB[j]);
                    }
                    boolean finalResult = reasoner.query(combinedBeliefSet, formula);
                    System.out.println("Final entailment check result: " + finalResult);
                    return finalResult;
                } else {
                    high = mid;
                }
            }
        }

        PlBeliefSet finalCombinedBeliefSet = new PlBeliefSet();
        for (int j = low; j <= high; j++) {
            finalCombinedBeliefSet.addAll(rankedKB[j]);
        }
        boolean finalResult = new SatReasoner().query(finalCombinedBeliefSet, formula);
        System.out.println("Final entailment check result: " + finalResult);
        return finalResult;
    }
}