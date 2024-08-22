/* Author: Maqhobosheane Mohlerepe, algorithm adopted from Joel Hamilton */

package org.rationalclosure;

import java.util.HashMap;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;

public class BCachedEntailment implements EntailmentInterface {

    // Cache to store previously computed query results
    private HashMap<String, Boolean> queryCache = new HashMap<>();
    
    // Cache to store the final filtered belief set for each negated antecedent
    private HashMap<String, PlBeliefSet> filteredKBCache = new HashMap<>();

    // Main method to check entailment using binary rational closure with caching
    @Override
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        //System.out.println("Starting binary entailment check for: " + formula.toString());

        // Generate a unique key for the query cache based on the formula and rankedKB
        String queryCacheKey = generateCacheKey(rankedKB, formula);

        // Check if the result for the full query is already in the cache
        if (queryCache.containsKey(queryCacheKey)) {
            return queryCache.get(queryCacheKey);
        }

        // Generate a unique key for the filtered KB cache based on the negated antecedent
        PlFormula negatedAntecedent = App.negateAntecedent(formula);
        String negatedAntecedentKey = negatedAntecedent.toString();

        // Check if the filtered knowledge base is already cached for this negated antecedent
        PlBeliefSet cachedFilteredKB = filteredKBCache.get(negatedAntecedentKey);
        if (cachedFilteredKB != null) {
            SatReasoner reasoner = new SatReasoner();
            boolean result = reasoner.query(cachedFilteredKB, formula);
            queryCache.put(queryCacheKey, result); // Cache the result
            return result;
        }

        // If not cached, proceed with the normal logic
        boolean infiniteRankEmpty = rankedKB.length == 0 || rankedKB[rankedKB.length - 1].isEmpty();
        int high = infiniteRankEmpty ? rankedKB.length - 1 : rankedKB.length;
        int low = 0;

        // Initialize the reasoner
        SatReasoner reasoner = new SatReasoner();

        int originalLow = low;
        int originalHigh = high;

        while (high > low) {
            int mid = low + (high - low) / 2;

            PlBeliefSet combinedBeliefSetMidToEnd = combineRanks(rankedKB, mid + 1, high - 1);
            if (!infiniteRankEmpty) {
                combinedBeliefSetMidToEnd.addAll(rankedKB[rankedKB.length - 1]); // Always add infinite rank
            }

            if (reasoner.query(combinedBeliefSetMidToEnd, App.negateAntecedent(formula))) {
                low = mid + 1;
            } else {
                PlBeliefSet combinedBeliefSetMinToMid = combineRanks(rankedKB, low, mid);
                if (!infiniteRankEmpty) {
                    combinedBeliefSetMinToMid.addAll(rankedKB[rankedKB.length - 1]); // Always add infinite rank
                }

                if (reasoner.query(combinedBeliefSetMinToMid, App.negateAntecedent(formula))) {
                    high = mid;
                } else {
                    int highestRank = mid;

                    // If no ranks were removed, use the entire original belief set
                    if (low == originalLow && high == originalHigh) {
                        boolean result = reasoner.query(combineRanks(rankedKB, 0, rankedKB.length - 1), formula);
                        queryCache.put(queryCacheKey, result); // Cache the result
                        return result;
                    }

                    PlBeliefSet finalFilteredBeliefSet = combineRanks(rankedKB, highestRank + 1, rankedKB.length - 1);
                    if (!infiniteRankEmpty) {
                        finalFilteredBeliefSet.addAll(rankedKB[rankedKB.length - 1]); // Always add infinite rank
                    }
                    filteredKBCache.put(negatedAntecedentKey, finalFilteredBeliefSet);

                    boolean finalResult = reasoner.query(finalFilteredBeliefSet, formula);
                    queryCache.put(queryCacheKey, finalResult); // Cache the result
                    return finalResult;
                }
            }
        }

        // Edge Case: No ranks were removed, use the entire original belief set
        if (low == originalLow && high == originalHigh) {
            boolean result = reasoner.query(combineRanks(rankedKB, 0, rankedKB.length - 1), formula);
            queryCache.put(queryCacheKey, result); // Cache the result
            return result;
        }

        // Final entailment check with the combined belief set from low to high-1
        PlBeliefSet finalCombinedBeliefSet = combineRanks(rankedKB, low, high - 1);
        if (!infiniteRankEmpty) {
            finalCombinedBeliefSet.addAll(rankedKB[rankedKB.length - 1]); // Always add infinite rank
        }

        boolean finalResult = reasoner.query(finalCombinedBeliefSet, formula);
        queryCache.put(queryCacheKey, finalResult); // Cache the result
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
