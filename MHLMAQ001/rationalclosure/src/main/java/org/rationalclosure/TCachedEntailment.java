/* Author: Maqhobosheane Mohlerepe */

package org.rationalclosure;

import java.util.HashMap;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

public class TCachedEntailment implements EntailmentInterface {

    // Cache to store previously computed query results
    private HashMap<String, Boolean> queryCache = new HashMap<>();

    // Cache to store the filtered knowledge base for each negated antecedent
    private HashMap<String, PlBeliefSet[]> filteredKBCache = new HashMap<>();

    // Default constructor
    public TCachedEntailment() {
    }

    @Override
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        System.out.println("Starting ternary entailment check for: " + formula.toString());

        // Generate a unique key for the query cache based on the formula and rankedKB
        String queryCacheKey = generateCacheKey(rankedKB, formula);

        // Check if the result for the full query is already in the cache
        if (queryCache.containsKey(queryCacheKey)) {
            System.out.println("Query cache hit for formula: " + formula);
            return queryCache.get(queryCacheKey);
        }

        // Generate a unique key for the filtered KB cache based on the negated antecedent
        PlFormula negatedAntecedent = App.negateAntecedent(formula);
        String negatedAntecedentKey = negatedAntecedent.toString();

        PlBeliefSet[] filteredKB;
        if (filteredKBCache.containsKey(negatedAntecedentKey)) {
            System.out.println("Filtered KB cache hit for negated antecedent: " + negatedAntecedent);
            filteredKB = filteredKBCache.get(negatedAntecedentKey);
        } else {
            System.out.println("Cache miss for negated antecedent: " + negatedAntecedent);
            // Compute the filtered knowledge base by removing inconsistent ranks
            filteredKB = filterKnowledgeBase(rankedKB, negatedAntecedent);

            // Cache the filtered knowledge base for the negated antecedent
            filteredKBCache.put(negatedAntecedentKey, filteredKB);
        }

        // Check the full query entailment using the cached filtered knowledge base
        boolean result = checkEntailmentForFilteredKB(filteredKB, formula);

        // Store the result in the query cache
        queryCache.put(queryCacheKey, result);

        return result;
    }

    // Method to filter the knowledge base by removing inconsistent ranks based on the negated antecedent
    private PlBeliefSet[] filterKnowledgeBase(PlBeliefSet[] rankedKB, PlFormula negatedAntecedent) {
        SatReasoner reasoner = new SatReasoner();
        int max = rankedKB.length;
        int min = 0;

        // Ternary search to find the rank at which negated antecedent becomes consistent
        while (max > min) {
            int mid1 = min + (max - min) / 3;
            int mid2 = max - (max - min) / 3;

            System.out.println("min: " + min + ", max: " + max + ", mid1: " + mid1 + ", mid2: " + mid2);

            PlBeliefSet combinedBeliefSetMid1 = combineRanks(rankedKB, mid1 + 1, max - 1);
            if (reasoner.query(combinedBeliefSetMid1, negatedAntecedent)) {
                min = mid1 + 1;
                System.out.println("Negated antecedent consistent after mid1, updating min: " + min);
            } else {
                PlBeliefSet combinedBeliefSetMid1AndBelow = combineRanks(rankedKB, min, mid1);
                if (reasoner.query(combinedBeliefSetMid1AndBelow, negatedAntecedent)) {
                    max = mid1;
                    System.out.println("Negated antecedent consistent up to mid1, updating max: " + max);
                } else {
                    max = mid2;
                }
            }

            if (mid2 < rankedKB.length) {
                PlBeliefSet combinedBeliefSetMid2 = combineRanks(rankedKB, mid2 + 1, max - 1);
                if (reasoner.query(combinedBeliefSetMid2, negatedAntecedent)) {
                    min = mid2 + 1;
                    System.out.println("Negated antecedent consistent after mid2, updating min: " + min);
                } else {
                    PlBeliefSet combinedBeliefSetMid2AndBelow = combineRanks(rankedKB, min, mid2);
                    if (reasoner.query(combinedBeliefSetMid2AndBelow, negatedAntecedent)) {
                        max = mid2;
                        System.out.println("Negated antecedent consistent up to mid2, updating max: " + max);
                    } else {
                        max = mid1;
                        min = mid1 + 1;
                    }
                }
            }
        }

        // The filtered knowledge base is composed of ranks from min to rankedKB.length - 1
        PlBeliefSet[] filteredKB = new PlBeliefSet[rankedKB.length - min];
        System.arraycopy(rankedKB, min, filteredKB, 0, filteredKB.length);

        return filteredKB;
    }

    // Method to check entailment using a cached filtered knowledge base
    private boolean checkEntailmentForFilteredKB(PlBeliefSet[] filteredKB, PlFormula formula) {
        SatReasoner reasoner = new SatReasoner();
        PlBeliefSet combinedFilteredKB = combineRanks(filteredKB, 0, filteredKB.length - 1);
        return reasoner.query(combinedFilteredKB, formula);
    }

    // Method to combine ranks within a specific range
    private PlBeliefSet combineRanks(PlBeliefSet[] rankedKB, int start, int end) {
        PlBeliefSet combinedBeliefSet = new PlBeliefSet();
        for (int i = start; i <= end; i++) {
            combinedBeliefSet.addAll(rankedKB[i]);
        }
        return combinedBeliefSet;
    }

    // Helper method to generate a unique cache key
    private String generateCacheKey(PlBeliefSet[] rankedKB, PlFormula formula) {
        StringBuilder sb = new StringBuilder(formula.toString());
        for (PlBeliefSet set : rankedKB) {
            sb.append(set.toString());
        }
        return sb.toString();
    }

    // Method to clear cache
    public void clearCache() {
        queryCache.clear();
        filteredKBCache.clear();
    }
}
