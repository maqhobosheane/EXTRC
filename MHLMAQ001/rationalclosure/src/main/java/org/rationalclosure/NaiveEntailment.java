/* Author: Maqhobosheane Mohlerepe */

package org.rationalclosure;

import java.util.ArrayList;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;

public class NaiveEntailment implements EntailmentInterface {

    private PlBeliefSet[] rankedKB;
    private PlFormula formula;

    // Constructor that accepts rankedKB and formula
    public NaiveEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        this.rankedKB = rankedKB;
        this.formula = formula;
    }

    // Default constructor
    public NaiveEntailment() {
    }

    // Main method to check entailment using naive rational closure
    @Override
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        System.out.println("Starting naive entailment check for: " + formula.toString());

        // Remove contradictory ranks
        ArrayList<PlBeliefSet> filteredKB = removeContradictions(rankedKB, formula);

        if (filteredKB.isEmpty()) {
            return false;
        }

        // Final entailment check on the contradiction-free knowledge base
        PlBeliefSet[] filteredKBArray = new PlBeliefSet[filteredKB.size()];
        filteredKBArray = filteredKB.toArray(filteredKBArray);
        boolean finalResult = checkEntailmentForRange(filteredKBArray, formula, 0, filteredKBArray.length - 1);
        System.out.println("Final entailment result: " + finalResult);
        return finalResult;
    }

    // Remove ranks that cause contradictions and return the filtered knowledge base
    private ArrayList<PlBeliefSet> removeContradictions(PlBeliefSet[] rankedKB, PlFormula formula) {
        ArrayList<PlBeliefSet> filteredKB = new ArrayList<>();
        SatReasoner reasoner = new SatReasoner();

        for (int i = 0; i < rankedKB.length; i++) {
            // Create a combined belief set from rank i to the end
            PlBeliefSet combinedBeliefSet = new PlBeliefSet();
            for (int j = i; j < rankedKB.length; j++) {
                combinedBeliefSet.addAll(rankedKB[j]);
            }

            // Negate the antecedent of the formula
            PlFormula negatedAntecedent = App.negateAntecedent(formula);

            // Check if the combined belief set entails the negation of the antecedent
            if (!reasoner.query(combinedBeliefSet, negatedAntecedent)) {
                // If it does not entail the negation, include this rank
                filteredKB.add(rankedKB[i]);
            } else {
                //System.out.println("Removing contradictory rank: " + i);
            }
        }

        return filteredKB;
    }

    // Helper method to check entailment within a specific range of ranks
    private boolean checkEntailmentForRange(PlBeliefSet[] rankedKB, PlFormula formula, int min, int max) {
        // Combine the ranks within the specified range
        PlBeliefSet combinedBeliefSet = new PlBeliefSet();
        for (int i = min; i <= max; i++) {
            combinedBeliefSet.addAll(rankedKB[i]);
        }

        //System.out.println("Combined belief set for range " + min + " to " + max + ": " + combinedBeliefSet.toString());

        // Check entailment using the SAT reasoner from the TweetyProject
        SatReasoner reasoner = new SatReasoner();
        return reasoner.query(combinedBeliefSet, formula);
    }
}

