/* Originally written by Evashna Pillay, modified by Maqhobosheane Mohlerepe for current approach */

package org.rationalclosure;

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import org.tweetyproject.logics.pl.syntax.*;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;

public class App {

    public static void main(String[] args) throws IOException, ParserException {
        
        PlBeliefSet beliefSet = new PlBeliefSet();
        PlBeliefSet classicalSet = new PlBeliefSet();
        PlParser parser = new PlParser();
        
        // The kb file is assigned to the string variable fileName.
        String fileName = args[0];

        try {
            File file = new File(fileName);
            Scanner reader = new Scanner(file);

            // The file is read until the end of file.
            while (reader.hasNextLine()) {
                String stringFormula = reader.nextLine().trim();
                System.out.println("Read line: " + stringFormula); // Debugging output

                if (stringFormula.isEmpty()) {
                    continue;
                }

                if (stringFormula.contains("~>")) {
                    // Reformatting of the defeasible implications of the kb if necessary, as well as 
                    // the reformatting of the defeasible queries from ~> to =>.
                    stringFormula = reformatConnectives(reformatDefeasible(stringFormula));
                    System.out.println("Reformatted defeasible: " + stringFormula); // Debugging output
                    // All defeasible implications are added to the defeasible beliefset.
                    // Parse formula from string.
                    beliefSet.add((PlFormula) parser.parseFormula(stringFormula));
                
                } else {
                    // Reformatting of the classical implications of the kb if necessary.
                    stringFormula = reformatConnectives(stringFormula);
                    System.out.println("Reformatted classical: " + stringFormula); // Debugging output
                    // All classical implications are added to the classical beliefset.
                    // Parse formula from string.
                    classicalSet.add((PlFormula) parser.parseFormula(stringFormula));
                }
            }
            reader.close();

            // BaseRankThreaded object instantiated to allow the base ranking algorithm to run.
            //BaseRankThreaded baseRank = new BaseRankThreaded(beliefSet, classicalSet);
            BaseRankThreaded.setCkb(classicalSet);
            // Ranked knowledge base returned.
            ArrayList<PlBeliefSet> rankedKB = BaseRankThreaded.rank(beliefSet, new PlBeliefSet());

            // RationalReasoner object instantiated.
            RationalReasoner reasoner = new RationalReasoner(rankedKB);

            // Query file assigned to the string variable queryFile.
            String queryFile = args[1];
            File qfile = new File(queryFile);
            Scanner qreader = new Scanner(qfile);

            // Query file read until end of file.
            while (qreader.hasNextLine()) {
                String queryFormula = qreader.nextLine().trim();
                System.out.println("Read query: " + queryFormula); // Debugging output

                if (queryFormula.isEmpty()) {
                    continue;
                }

                queryFormula = reformatConnectives(reformatDefeasible(queryFormula));
                System.out.println("Reformatted query: " + queryFormula); // Debugging output
                PlFormula query = (PlFormula) parser.parseFormula(queryFormula);

                // Query the reasoner and print result.
                boolean result = reasoner.query(query);
                System.out.println("Query: " + queryFormula + " Result: " + result);
            }
            qreader.close();

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // The methods below allow the application to make any reformatting necessary possible,
    // and takes into consideration the logic that may be used by the end user, however this 
    // may not account for all possibilities. In that case, the user will be asked to 
    // reformat their defeasible implications.
    public static String reformatDefeasible(String formula) {
        int index = formula.indexOf("~>");
        formula = "(" + formula.substring(0, index).trim() + ") => (" + formula.substring(index + 2).trim() + ")";
        return formula;
    }

    public static String reformatConnectives(String formula) {
        formula = formula.replaceAll("¬", "!");
        formula = formula.replaceAll("~", "!");
        formula = formula.replaceAll("&", "&&");
        formula = formula.replaceAll("<->", "<=>");
        formula = formula.replaceAll("->", "=>");
        return formula;
    }
}
