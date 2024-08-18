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
        System.out.println("Starting binary entailment check for: " + formula.toString());

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

        // Check if the filtered knowledge base is already cached for this negated antecedent
        PlBeliefSet cachedFilteredKB = filteredKBCache.get(negatedAntecedentKey);
        if (cachedFilteredKB != null) {
            System.out.println("Filtered KB cache hit for negated antecedent: " + negatedAntecedent);
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
            //System.out.println("Low: " + low + ", High: " + high + ", Midpoint: " + mid);

            PlBeliefSet combinedBeliefSetMidToEnd = combineRanks(rankedKB, mid + 1, high - 1);
            //System.out.println("Combined belief set (mid+1 to high-1): " + combinedBeliefSetMidToEnd.toString());

            if (reasoner.query(combinedBeliefSetMidToEnd, App.negateAntecedent(formula))) {
                low = mid + 1;
                //System.out.println("Negated antecedent is consistent with combined belief set from mid+1 to high-1. Updating low to " + low);
            } else {
                PlBeliefSet combinedBeliefSetMinToMid = combineRanks(rankedKB, low, mid);
                //System.out.println("Combined belief set (low to mid): " + combinedBeliefSetMinToMid.toString());

                if (reasoner.query(combinedBeliefSetMinToMid, App.negateAntecedent(formula))) {
                    high = mid;
                    //System.out.println("Negated antecedent is consistent with combined belief set from low to mid. Updating high to " + high);
                } else {
                    // Final belief set identified, highest rank to remove: mid
                    int highestRank = mid;
                    //System.out.println("Final belief set identified, highest rank to remove: " + highestRank);

                    // Check if no ranks were removed, i.e., low and high still encompass the entire range
                    if (low == originalLow && high == originalHigh) {
                        //System.out.println("No ranks removed; using the entire original belief set.");
                        boolean result = reasoner.query(combineRanks(rankedKB, 0, rankedKB.length - 1), formula);
                        queryCache.put(queryCacheKey, result); // Cache the result
                        return result;
                    }

                    // If this is the first time we're encountering this negated antecedent, cache the filtered belief set
                    PlBeliefSet finalFilteredBeliefSet = combineRanks(rankedKB, highestRank + 1, rankedKB.length - 1);
                    //System.out.println("Final combined belief set after removing higher ranks (before caching): " + finalFilteredBeliefSet.toString());
                    filteredKBCache.put(negatedAntecedentKey, finalFilteredBeliefSet);
                    //System.out.println("Caching filtered belief set for negated antecedent: " + negatedAntecedentKey);

                    boolean finalResult = reasoner.query(finalFilteredBeliefSet, formula);
                    queryCache.put(queryCacheKey, finalResult); // Cache the result
                    return finalResult;
                }
            }
        }

        // Edge Case: No ranks were removed, use the entire original belief set
        if (low == originalLow && high == originalHigh) {
            //System.out.println("No ranks removed; using the entire original belief set.");
            boolean result = reasoner.query(combineRanks(rankedKB, 0, rankedKB.length - 1), formula);
            queryCache.put(queryCacheKey, result); // Cache the result
            return result;
        }

        // Final entailment check with the combined belief set from low to high-1
        PlBeliefSet finalCombinedBeliefSet = combineRanks(rankedKB, low, high - 1);
        //System.out.println("Final combined belief set (low to high-1): " + finalCombinedBeliefSet.toString());

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
        //System.out.println("Combined belief set for range " + start + " to " + end + ": " + combinedBeliefSet.toString());
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
