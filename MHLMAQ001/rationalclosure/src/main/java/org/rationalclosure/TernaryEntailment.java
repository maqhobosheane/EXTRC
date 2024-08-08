package org.rationalclosure;

import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;

public class TernaryEntailment implements EntailmentInterface {
    @Override
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        System.out.println("Starting ternary entailment check for: " + formula.toString());

        // Remove contradictory ranks
        int max = removeContradictions(rankedKB, formula);

        if (max == -1) {
            return false;
        }

        // Final entailment check on the contradiction-free knowledge base
        boolean finalResult = checkEntailmentForRange(rankedKB, formula, 0, max);
        System.out.println("Final entailment result: " + finalResult);
        return finalResult;
    }

    // Remove ranks that cause contradictions and return the index of the last valid rank
    private int removeContradictions(PlBeliefSet[] rankedKB, PlFormula formula) {
        int max = rankedKB.length - 1;

        for (int i = 0; i <= max; i++) {
            // Create a combined belief set from rank i to max
            PlBeliefSet combinedBeliefSet = new PlBeliefSet();
            for (int j = i; j <= max; j++) {
                combinedBeliefSet.addAll(rankedKB[j]);
            }

            // Negate the antecedent of the formula
            PlFormula negatedAntecedent = App.negateAntecedent(formula);

            // Check if the combined belief set entails the negation of the antecedent
            SatReasoner reasoner = new SatReasoner();
            if (reasoner.query(combinedBeliefSet, negatedAntecedent)) {
                // If it entails the negation, remove the most typical rank (i)
                System.out.println("Removing contradictory rank: " + i);
                max--;
                i = -1; // Restart checking from the beginning
            }
        }

        return max;
    }

    // Helper method to check entailment within a specific range of ranks
    private boolean checkEntailmentForRange(PlBeliefSet[] rankedKB, PlFormula formula, int min, int max) {
        // Combine the ranks within the specified range
        PlBeliefSet combinedBeliefSet = new PlBeliefSet();
        for (int i = min; i <= max; i++) {
            combinedBeliefSet.addAll(rankedKB[i]);
        }

        System.out.println("Combined belief set for range " + min + " to " + max + ": " + combinedBeliefSet.toString());

        // Check entailment using the SAT reasoner from the TweetyProject
        SatReasoner reasoner = new SatReasoner();
        boolean result = reasoner.query(combinedBeliefSet, formula);

        return result;
    }
}
