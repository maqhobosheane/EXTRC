/* Code originally written by Evashna Pillay , that has been adjusted to run for our approaches. */

package org.rationalclosure;

import java.util.ArrayList;
import org.tweetyproject.logics.pl.syntax.*;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;

public class BaseRankThreaded {

    private static ArrayList<PlBeliefSet> rankedKB = new ArrayList<>();
    private static PlBeliefSet cStatements = new PlBeliefSet();
    private PlBeliefSet beliefSet;
    private PlBeliefSet classicalSet;

    public BaseRankThreaded(PlBeliefSet beliefSet, PlBeliefSet classicalSet) {
        this.beliefSet = beliefSet;
        this.classicalSet = classicalSet;
        rankedKB = rank(new PlBeliefSet(), beliefSet);
    }

    public ArrayList<PlBeliefSet> getRankedKB() {
        return rankedKB;
    }

    private ArrayList<PlBeliefSet> rank(PlBeliefSet curMaterial, PlBeliefSet prevMaterial) {
        prevMaterial = curMaterial;
        curMaterial = new PlBeliefSet();

        PlBeliefSet rank = new PlBeliefSet();       

        PlBeliefSet temp = new PlBeliefSet(prevMaterial);
        temp.addAll(cStatements);
        PlBeliefSet antecedents = new PlBeliefSet();

        for (PlFormula f : prevMaterial) {
            antecedents.add(((Implication) f).getFormulas().getFirst());
        }  

        // Threshold determined by Math.max(antecedents.size() / 6, 1)
        PlBeliefSet exceptionals = getExceptionals(antecedents, temp, Math.max(antecedents.size() / 6, 1));
        for (PlFormula f : prevMaterial) {
            PlFormula ante = ((Implication) f).getFormulas().getFirst();
            if (exceptionals.contains(ante))
                curMaterial.add(f);

            if (!cStatements.contains(f) && !curMaterial.contains(f)){
                rank.add(f);
            }
        }

        if (rank.size() != 0) {
            rankedKB.add(rank);
            int rSize = rankedKB.size() - 1;
            System.out.println("Rank " + rSize + ": " + rankedKB.get(rSize).toString());

        } else {
            // some statements that appear defeasible are in fact classical, and appear on the bottom rank. 
            // This is due to the fact that R ⊩ ¬α |∼ ⊥ if and only if R ⊩ α.
            cStatements.addAll(curMaterial);
        }

        if (!curMaterial.equals(prevMaterial)){
            // recursive call
            return rank(curMaterial, prevMaterial);
        } 

        rankedKB.add(cStatements);
        System.out.println("Final Ranked KB: " + rankedKB.toString());

        return rankedKB;
    }

    // Method to get exceptionals based on some threshold
    private PlBeliefSet getExceptionals(PlBeliefSet antecedents, PlBeliefSet temp, int threshold) {
        PlBeliefSet exceptionals = new PlBeliefSet();
        SatReasoner reasoner = new SatReasoner();

        for (PlFormula formula : antecedents) {
            if (!reasoner.query(temp, formula)) {
                exceptionals.add(formula);
                if (exceptionals.size() >= threshold) {
                    break;
                }
            }
        }
        return exceptionals;
    }

    // Setter method to set classical beliefset, used in App class.
    public static void setCkb(PlBeliefSet ckb){
        cStatements = ckb;
    }
}
