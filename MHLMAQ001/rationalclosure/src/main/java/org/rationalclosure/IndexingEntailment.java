/* Author: Maqhobosheane Mohlerepe */

package org.rationalclosure;

import java.util.ArrayList;
import java.util.Hashtable;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;

public class IndexingEntailment {
    private PlBeliefSet[] rankedKB;

    public IndexingEntailment(PlBeliefSet[] rankedKB) {
        this.rankedKB = rankedKB;
    }

    public void check(ArrayList<PlFormula> formulasToCheck, Hashtable<PlFormula, Integer> antecedents) {
        SatReasoner reasoner = new SatReasoner();

        for (PlFormula formula : formulasToCheck) {
            boolean result = reasoner.query(rankedKB[0], formula);
            System.out.println("IndexingEntailment check result for formula " + formula + ": " + result);
        }
    }
}
