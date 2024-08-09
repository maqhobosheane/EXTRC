/* Author: Maqhobosheane Mohlerepe */

package org.rationalclosure;

import java.util.HashMap;
import java.util.ArrayList;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;  // Import the SatReasoner class
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

public class NCachedEntailment implements EntailmentInterface {

    // Cache to store previously computed entailment results
    private HashMap<String, Boolean> cache = new HashMap<>();

    @Override
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        // Generate a unique key for the cache based on the formula and rankedKB
        String cacheKey = generateCacheKey(rankedKB, formula);
        
        // Check if the result is already in the cache
        if (cache.containsKey(cacheKey)) {
            System.out.println("Cache hit for formula: " + formula);
            return cache.get(cacheKey);
        }

        System.out.println("Cache miss for formula: " + formula);
        
        // Perform the entailment check
        boolean result = performEntailmentCheck(rankedKB, formula);

        // Store the result in the cache
        cache.put(cacheKey, result);

        return result;
    }

    // Helper method to generate a unique cache key
    private String generateCacheKey(PlBeliefSet[] rankedKB, PlFormula formula) {
        // Create a string that uniquely identifies the combination of the rankedKB and the formula
        StringBuilder sb = new StringBuilder(formula.toString());
        for (PlBeliefSet set : rankedKB) {
            sb.append(set.toString());
        }
        return sb.toString();
    }

    // Helper method to perform the actual entailment check
    private boolean performEntailmentCheck(PlBeliefSet[] rankedKB, PlFormula formula) {
        // Implement the entailment logic similar to NaiveEntailment
        ArrayList<PlBeliefSet> filteredKB = removeContradictions(rankedKB, formula);

        if (filteredKB.isEmpty()) {
            return false;
        }

        PlBeliefSet[] filteredKBArray = new PlBeliefSet[filteredKB.size()];
        filteredKBArray = filteredKB.toArray(filteredKBArray);
        return checkEntailmentForRange(filteredKBArray, formula, 0, filteredKBArray.length - 1);
    }

    // Method to remove ranks that cause contradictions and return the filtered knowledge base
    private ArrayList<PlBeliefSet> removeContradictions(PlBeliefSet[] rankedKB, PlFormula formula) {
        ArrayList<PlBeliefSet> filteredKB = new ArrayList<>();
        SatReasoner reasoner = new SatReasoner();  // Use the SatReasoner from TweetyProject

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
        SatReasoner reasoner = new SatReasoner();  // Use the SatReasoner from TweetyProject
        return reasoner.query(combinedBeliefSet, formula);
    }
}
