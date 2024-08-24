/* Author: Maqhobosheane Mohlerepe */

package org.rationalclosure;

import java.util.Arrays;
import java.util.HashMap;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;

public class TCachedEntailment implements EntailmentInterface {

    private static int rankRemove = -1;

    // Cache to store previously computed query results
    private HashMap<String, Boolean> queryCache = new HashMap<>();
    
    // Cache to store the final filtered belief set for each negated antecedent
    private HashMap<String, PlBeliefSet> filteredKBCache = new HashMap<>();

    // Cache hit counter
    private int cacheHitCounter = 0;

    // Constructor to accept rankedKB and formula
    public TCachedEntailment() {
        // Initialize the SAT solver and reasoner
        SatSolver.setDefaultSolver(new Sat4jSolver());
    }

    @Override
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        int rlength = rankedKB.length;
        PlFormula negation = App.negateAntecedent(formula);
        rankRemove = -1;

        // Generate a unique key for the query cache based on the formula and rankedKB
        String queryCacheKey = generateCacheKey(rankedKB, formula);

        // Check if the result for the full query is already in the cache
        if (queryCache.containsKey(queryCacheKey)) {
            cacheHitCounter++;  // Increment the cache hit counter
            return queryCache.get(queryCacheKey);
        }

        // Generate a unique key for the filtered KB cache based on the negated antecedent
        String negatedAntecedentKey = negation.toString();

        // Check if the filtered knowledge base is already cached for this negated antecedent
        PlBeliefSet cachedFilteredKB = filteredKBCache.get(negatedAntecedentKey);
        if (cachedFilteredKB != null) {
            cacheHitCounter++;  // Increment the cache hit counter
            SatReasoner reasoner = new SatReasoner();
            boolean result = reasoner.query(cachedFilteredKB, formula);
            queryCache.put(queryCacheKey, result); // Cache the result
            return result;
        }

        boolean result = checkEntailmentRecursive(rankedKB, formula, 0, rlength, negation);

        // Cache the result after the recursive check
        queryCache.put(queryCacheKey, result);

        return result;
    }

    private boolean checkEntailmentRecursive(PlBeliefSet[] rankedKB, PlFormula formula, int left, int right, PlFormula negation) {
        SatReasoner reasoner = new SatReasoner();

        if (right > left) {
            int mid = left + (right - left) / 3;
            int mid2 = right - (right - left) / 3;

            if (reasoner.query(combine(Arrays.copyOfRange(rankedKB, mid + 1, rankedKB.length)), negation)) {
                if (mid2 < rankedKB.length) {
                    if (reasoner.query(combine(Arrays.copyOfRange(rankedKB, mid2 + 1, rankedKB.length)), negation)) {
                        return checkEntailmentRecursive(rankedKB, formula, mid2 + 1, right, negation);
                    } else {
                        if (reasoner.query(combine(Arrays.copyOfRange(rankedKB, mid2, rankedKB.length)), negation)) {
                            rankRemove = mid2;
                            cacheFilteredKB(rankedKB, negation, rankRemove);
                        } else {
                            return checkEntailmentRecursive(rankedKB, formula, mid + 1, mid2 - 1, negation);
                        }
                    }
                } else if (mid2 == rankedKB.length) {
                    return checkEntailmentRecursive(rankedKB, formula, mid + 1, mid2 - 1, negation);
                }
            } else {
                if (reasoner.query(combine(Arrays.copyOfRange(rankedKB, mid, rankedKB.length)), negation)) {
                    rankRemove = mid;
                    cacheFilteredKB(rankedKB, negation, rankRemove);
                } else {
                    return checkEntailmentRecursive(rankedKB, formula, left, mid, negation);
                }
            }
        } else {
            if (right == left) {
                rankRemove = right;
            }
            if (!reasoner.query(combine(Arrays.copyOfRange(rankedKB, rankRemove, rankedKB.length)), negation)) {
                rankRemove -= 1;
            }
            cacheFilteredKB(rankedKB, negation, rankRemove);
        }

        if (rankRemove + 1 < rankedKB.length) {
            return reasoner.query(combine(Arrays.copyOfRange(rankedKB, rankRemove + 1, rankedKB.length)), formula);
        } else {
            return true;
        }
    }

    private void cacheFilteredKB(PlBeliefSet[] rankedKB, PlFormula negation, int rankRemove) {
        String negatedAntecedentKey = negation.toString();
        PlBeliefSet finalFilteredBeliefSet = combine(Arrays.copyOfRange(rankedKB, rankRemove + 1, rankedKB.length));
        filteredKBCache.put(negatedAntecedentKey, finalFilteredBeliefSet);
    }

    private PlBeliefSet combine(PlBeliefSet[] ranks) {
        PlBeliefSet combined = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combined.addAll(rank);
        }
        return combined;
    }

    // Helper method to generate a unique cache key
    private String generateCacheKey(PlBeliefSet[] rankedKB, PlFormula formula) {
        StringBuilder sb = new StringBuilder(formula.toString());
        for (PlBeliefSet set : rankedKB) {
            sb.append(set.toString());
        }
        return sb.toString();
    }

    // Method to clear cache and reset cache hit counter
    public void clearCache() {
        queryCache.clear();
        filteredKBCache.clear();
        cacheHitCounter = 0;  // Reset the cache hit counter
    }

    // Getter method for cache hit counter
    public int getCacheHitCounter() {
        return cacheHitCounter;
    }
}
