/* Originally written by Evashna Pillay, optimized by Maqhobosheane Mohlerepe */

package org.rationalclosure;

import java.util.ArrayList;
import java.util.Hashtable;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

public class RationalReasoner {
    // Instance of TernaryEntailment for ternary search with memoization and dynamic programming
    TernaryEntailment tObject = new TernaryEntailment();
    IndexingEntailment iObject;
    ArrayList<PlBeliefSet> rankedKB = new ArrayList<PlBeliefSet>();
    PlBeliefSet[] rankedKBArray;

    // Constructor for the reasoner with ternary approach
    public RationalReasoner(ArrayList<PlBeliefSet> rankedKB) {
        this.rankedKB = rankedKB;
        rankedKBArray = new PlBeliefSet[rankedKB.size()];
        this.rankedKBArray = rankedKB.toArray(rankedKBArray);
        this.iObject = new IndexingEntailment(rankedKBArray);  // Pass rankedKB to IndexingEntailment
    }

    // Method to query entailment using ternary search with optimizations
    public boolean query(PlFormula formula) {
        System.out.println("Querying: " + formula.toString());
        boolean result = tObject.checkEntailment(rankedKBArray, formula);
        System.out.println("Query result: " + result);
        return result;
    }

    // Method to check entailment using indexing approach
    public void check(ArrayList<PlFormula> formulastoCheck, Hashtable<PlFormula, Integer> antecedents) {
        iObject.check(formulastoCheck, antecedents);
    }
}
