/* Originally written by Evashna Pillay, modified by Maqhobosheane Mohlerepe for current approach */

package org.rationalclosure;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.tweetyproject.logics.pl.syntax.*;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;

public class App {

    public static void main(String[] args) throws IOException, ParserException {
        if (args.length < 2) {
            System.out.println("Please provide the knowledge base file and query file as arguments.");
            return;
        }

        PlBeliefSet beliefSet = new PlBeliefSet();
        PlBeliefSet classicalSet = new PlBeliefSet();
        PlParser parser = new PlParser();

        // The kb file is assigned to the string variable fileName.
        String fileName = args[0];
        String querySetName = args[1]; // The query set file is assigned to the string variable querySetName.
        try {
            File file = new File(fileName);
            Scanner reader = new Scanner(file);

            // The file is read until the end of file.
            while (reader.hasNextLine()) {
                String stringFormula = reader.nextLine().trim();
                //System.out.println("Read line: " + stringFormula); // Debugging output

                if (stringFormula.isEmpty()) {
                    continue;
                }

                if (stringFormula.contains("~>")) {
                    // Reformatting of the defeasible implications of the kb if necessary, as well as 
                    // the reformatting of the defeasible queries from ~> to =>.
                    stringFormula = reformatConnectives(reformatDefeasible(stringFormula));
                    //System.out.println("Reformatted defeasible: " + stringFormula); // Debugging output
                    // All defeasible implications are added to the defeasible beliefset.
                    // Parse formula from string.
                    beliefSet.add((PlFormula) parser.parseFormula(stringFormula));
                
                } else {
                    // Reformatting of the classical implications of the kb if necessary.
                    stringFormula = reformatConnectives(stringFormula);
                    //System.out.println("Reformatted classical: " + stringFormula); // Debugging output
                    // All classical implications are added to the classical beliefset.
                    // Parse formula from string.
                    classicalSet.add((PlFormula) parser.parseFormula(stringFormula));
                }
            }
            reader.close();

            // BaseRankThreaded object instantiated to allow the base ranking algorithm to run.
            BaseRankThreaded.setCkb(classicalSet);
            // Ranked knowledge base returned.
            ArrayList<PlBeliefSet> rankedKB = BaseRankThreaded.rank(beliefSet, new PlBeliefSet());

            // Prompt user to choose running mode
            System.out.println("Choose running mode:");
            System.out.println("1. Run timed reasoner comparison");
            System.out.println("2. Run reasoners independently");
            Scanner scanner = new Scanner(System.in);
            int modeChoice = scanner.nextInt();

            if (modeChoice == 1) {
                // Run timed reasoner comparison
                runTimedReasonerComparison(rankedKB, parser, fileName, querySetName);
            } else if (modeChoice == 2) {
                // Run reasoners independently
                runReasonersIndependently(rankedKB, parser, querySetName);
            } else {
                System.out.println("Invalid choice. Exiting.");
            }

        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runReasonersIndependently(ArrayList<PlBeliefSet> rankedKB, PlParser parser, String queryFile) throws IOException, ParserException {
        // Prompt user to choose entailment method
        System.out.println("Choose entailment method:");
        System.out.println("1. Naive Entailment");
        System.out.println("2. Binary Entailment");
        System.out.println("3. Ternary Entailment");
        System.out.println("4. Cached Naive Entailment");
        System.out.println("5. Cached Binary Entailment");
        System.out.println("6. Cached Ternary Entailment");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        EntailmentInterface entailment;

        switch (choice) {
            case 1:
                entailment = new NaiveEntailment();
                break;
            case 2:
                entailment = new BinaryEntailment();
                break;
            case 3:
                entailment = new TernaryEntailment();
                break;
            case 4:
                entailment = new NCachedEntailment();
                break;
            case 5:
                entailment = new BCachedEntailment();
                break;
            case 6:
                entailment = new TCachedEntailment();
                break;
            default:
                System.out.println("Invalid choice. Defaulting to Naive Entailment.");
                entailment = new NaiveEntailment();
                break;
        }

        // Query file assigned to the string variable queryFile.
        File qfile = new File(queryFile);
        Scanner qreader = new Scanner(qfile);

        // Query file read until end of file.
        while (qreader.hasNextLine()) {
            String queryFormula = qreader.nextLine().trim();
            //System.out.println("Read query: " + queryFormula); // Debugging output

            if (queryFormula.isEmpty()) {
                continue;
            }

            queryFormula = reformatConnectives(reformatDefeasible(queryFormula));
            //System.out.println("Reformatted query: " + queryFormula); // Debugging output
            PlFormula query = (PlFormula) parser.parseFormula(queryFormula);

            // Query the reasoner and print result.
            boolean result = entailment.checkEntailment(rankedKB.toArray(new PlBeliefSet[0]), query);
            System.out.println("Query: " + queryFormula + " Result: " + result);
        }
        qreader.close();
    }

    private static void runTimedReasonerComparison(ArrayList<PlBeliefSet> rankedKB, PlParser parser, String kbFile, String querySetName) throws IOException, ParserException {
        // Pass the ranked knowledge base, knowledge base file name, and query set name to the timed reasoner
        TimedReasonerComparison.runTimedComparison(rankedKB, parser, kbFile, querySetName);
    }

    // Method to negate the antecedent of an implication
    public static PlFormula negateAntecedent(PlFormula formula) {
        if (formula instanceof Implication) {
            Implication implication = (Implication) formula;
            PlFormula antecedent = (PlFormula) implication.getFormulas().getFirst();
            return new Negation(antecedent);
        }
        throw new IllegalArgumentException("Provided formula is not an implication.");
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
        formula.replaceAll("&", "&&");
        formula = formula.replaceAll("<->", "<=>");
        formula = formula.replaceAll("->", "=>");
        return formula;
    }
}
