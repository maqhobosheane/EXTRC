/* Originally written by Evashna Pillay, modified by Maqhobosheane Mohlerepe for current approach */

package org.rationalclosure;

import java.util.Arrays;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;

public class TernaryEntailment implements EntailmentInterface {

    private static int rankRemove = -1;

    // Constructor to accept rankedKB and formula
    public TernaryEntailment() {
        // Initialize the SAT solver and reasoner
        SatSolver.setDefaultSolver(new Sat4jSolver());
    }

    @Override
    public boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula) {
        int rlength = rankedKB.length;
        PlFormula negation = App.negateAntecedent(formula);
        rankRemove = -1;

        return checkEntailmentRecursive(rankedKB, formula, 0, rlength, negation);
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
        }

        if (rankRemove + 1 < rankedKB.length) {
            return reasoner.query(combine(Arrays.copyOfRange(rankedKB, rankRemove + 1, rankedKB.length)), formula);
        } else {
            return true;
        }
    }

    private PlBeliefSet combine(PlBeliefSet[] ranks) {
        PlBeliefSet combined = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combined.addAll(rank);
        }
        return combined;
    }
}
