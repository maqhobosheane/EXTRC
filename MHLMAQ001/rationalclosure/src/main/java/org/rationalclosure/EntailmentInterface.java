//Author: Maqhobosheane Mohlerepe

package org.rationalclosure;

import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

public interface EntailmentInterface {
    boolean checkEntailment(PlBeliefSet[] rankedKB, PlFormula formula);
}
