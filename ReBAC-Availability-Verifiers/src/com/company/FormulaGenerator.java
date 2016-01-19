package com.company;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Pooya on 15-02-10.
 */
public class FormulaGenerator {
    //Attributes
    protected Formula formula1;
    protected Formula formula2;
    protected Formula formula3;
    protected Formula positiveFormula1;
    protected Formula negativeFormula1;
    protected Formula positiveFormula2;
    protected Formula negativeFormula2;

    //Methods
    public Graph genRandomGraph(int numVertices, int numLabels, double probability, int anchor, int requester) {
        //Generating random graph pattern
        GraphGenerator gg = new GraphGenerator();
        Graph g = new Graph();
        boolean valid = false;
        while (!valid) {
            g = gg.genErdosRenyi(numVertices, numLabels, probability);
            valid = gg.validate(g, anchor, requester);
        }
        return g;
    }

    public Graph genRandomGraph(int numVertices, int numLabels, double probOrAvgDeg) {
        if (probOrAvgDeg > 1) {
            probOrAvgDeg = probOrAvgDeg / numVertices;
        }
        //Generating random graph pattern
        GraphGenerator gg = new GraphGenerator();
        Graph g = gg.genErdosRenyi(numVertices, numLabels, probOrAvgDeg);
        return g;

    }

    public Set<Integer> choose(int n , int k) {
        Set<Integer> result = new HashSet<Integer>();
        for (int i = 0; i < k; i++) {
            boolean flag = false;
            while (!flag) {
                int rand =  RandomUtil.random.nextInt(n);
                if (!result.contains(rand)) {
                    result.add(rand);
                    flag = true;
                }
            }
        }
        return result;
    }

    public void randPosNegGen(int numPosAtoms, int numNegAtoms, int gpnumVertices, int gpnumLabels, double gpprobOrAvgDeg, int numVertices) {
        formula1 = null;
        formula2 = null;
        formula3 = null;
        int anchor = 0;
        int requester = gpnumVertices - 1;
        //int anchor = RandomUtil.random.nextInt(gpnumVertices);
        //int requester = RandomUtil.random.nextInt(gpnumVertices);
        if (gpprobOrAvgDeg > 1) {
            gpprobOrAvgDeg = gpprobOrAvgDeg / gpnumVertices;
        }

        HashMap<Integer, FormulaAtom> atoms1 = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms2 = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms3 = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms4 = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms5 = new HashMap<Integer, FormulaAtom>();
        //Generating random atoms
        for (int i = 0; i < numPosAtoms + numNegAtoms; i++) {
            Graph g = genRandomGraph(gpnumVertices, gpnumLabels, gpprobOrAvgDeg, anchor, requester);
            BirootedGraph gp1 = new BirootedGraph(new Graph(g), anchor, requester);
            BirootedGraph gp2 = new BirootedGraph(new Graph(g), anchor, requester);
            BirootedGraph gp3 = new BirootedGraph(new Graph(g), anchor, requester);
            BirootedGraph gp4 = new BirootedGraph(new Graph(g), anchor, requester);
            BirootedGraph gp5 = new BirootedGraph(new Graph(g), anchor, requester);

            int a = RandomUtil.random.nextInt(numVertices);

            atoms1.put(i, new FormulaAtom(gp1, a));
            atoms2.put(i, new FormulaAtom(gp2, a));
            atoms3.put(i, new FormulaAtom(gp3, a));
            atoms4.put(i, new FormulaAtom(gp4, a));
            atoms5.put(i, new FormulaAtom(gp5, a));

        }

        Set<Integer> chosenAtoms = choose(numPosAtoms + numNegAtoms, numPosAtoms);
        for (int chosenAtom: chosenAtoms) {
            if (formula1 == null)
                formula1 = atoms1.get(chosenAtom);
            else
                formula1 = new FormulaOr(formula1, atoms1.get(chosenAtom));
            if (formula2 == null)
                formula2 = atoms2.get(chosenAtom);
            else
                formula2 = new FormulaOr(formula2, atoms2.get(chosenAtom));
            if (formula3 == null)
                formula3 = atoms3.get(chosenAtom);
            else
                formula3 = new FormulaOr(formula3, atoms3.get(chosenAtom));
            if (positiveFormula1 == null)
                positiveFormula1 = atoms4.get(chosenAtom);
            else
                positiveFormula1 = new FormulaOr(positiveFormula1, atoms4.get(chosenAtom));
            if (positiveFormula2 == null)
                positiveFormula2 = atoms5.get(chosenAtom);
            else
                positiveFormula2 = new FormulaOr(positiveFormula2, atoms5.get(chosenAtom));

        }

        for (int i = 0; i < numPosAtoms + numNegAtoms; i++) {
            if(!chosenAtoms.contains(i)) {
                formula1 = new FormulaAnd(formula1, new FormulaNot(atoms1.get(i)));
                formula2 = new FormulaAnd(formula2, new FormulaNot(atoms2.get(i)));
                formula3 = new FormulaAnd(formula3, new FormulaNot(atoms3.get(i)));
                if (negativeFormula1 == null)
                    negativeFormula1 = new FormulaNot(atoms4.get(i));
                else
                    negativeFormula1 = new FormulaAnd(negativeFormula1, new FormulaNot(atoms4.get(i)));
                if (negativeFormula2 == null)
                    negativeFormula2 = new FormulaNot(atoms5.get(i));
                else
                    negativeFormula2 = new FormulaAnd(negativeFormula2, new FormulaNot(atoms5.get(i)));
            }
        }
    }

    public void randPosNegGen(int numPosAtoms, int numNegAtoms, int gpPosNumVer, int gpNegNumVer, int gpnumLabels, double gpprobOrAvgDeg, int numVertices) {
        formula1 = null;
        formula2 = null;
        formula3 = null;
        int atomID = 0;
        int anchor = 0;
        int reqPos = gpPosNumVer - 1;
        int reqNeg = gpNegNumVer - 1;
        //int anchor = RandomUtil.random.nextInt(gpnumVertices);
        //int requester = RandomUtil.random.nextInt(gpnumVertices);
        double probPos = gpprobOrAvgDeg;
        double probNeg = gpprobOrAvgDeg;
        if (gpprobOrAvgDeg > 1) {
            probPos = gpprobOrAvgDeg / gpPosNumVer;
            probNeg = gpprobOrAvgDeg / gpNegNumVer;
        }

        HashMap<Integer, FormulaAtom> atoms1Pos = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms2Pos = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms3Pos = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms4Pos = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms5Pos = new HashMap<Integer, FormulaAtom>();
        //Generating random atoms
        for (int i = 0; i < numPosAtoms; i++) {
            Graph g = genRandomGraph(gpPosNumVer, gpnumLabels, probPos, anchor, reqPos);
            BirootedGraph gp1 = new BirootedGraph(new Graph(g), anchor, reqPos);
            BirootedGraph gp2 = new BirootedGraph(new Graph(g), anchor, reqPos);
            BirootedGraph gp3 = new BirootedGraph(new Graph(g), anchor, reqPos);
            BirootedGraph gp4 = new BirootedGraph(new Graph(g), anchor, reqPos);
            BirootedGraph gp5 = new BirootedGraph(new Graph(g), anchor, reqPos);

            int a = RandomUtil.random.nextInt(numVertices);

            atomID++;

            atoms1Pos.put(i, new FormulaAtom(gp1, a, atomID));
            atoms2Pos.put(i, new FormulaAtom(gp2, a, atomID));
            atoms3Pos.put(i, new FormulaAtom(gp3, a, atomID));
            atoms4Pos.put(i, new FormulaAtom(gp4, a, atomID));
            atoms5Pos.put(i, new FormulaAtom(gp5, a, atomID));
        }

        HashMap<Integer, FormulaAtom> atoms1Neg = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms2Neg = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms3Neg = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms4Neg = new HashMap<Integer, FormulaAtom>();
        HashMap<Integer, FormulaAtom> atoms5Neg = new HashMap<Integer, FormulaAtom>();
        //Generating random atoms
        for (int i = 0; i < numNegAtoms; i++) {
            Graph g = genRandomGraph(gpNegNumVer, gpnumLabels, probNeg, anchor, reqNeg);
            BirootedGraph gp1 = new BirootedGraph(new Graph(g), anchor, reqNeg);
            BirootedGraph gp2 = new BirootedGraph(new Graph(g), anchor, reqNeg);
            BirootedGraph gp3 = new BirootedGraph(new Graph(g), anchor, reqNeg);
            BirootedGraph gp4 = new BirootedGraph(new Graph(g), anchor, reqNeg);
            BirootedGraph gp5 = new BirootedGraph(new Graph(g), anchor, reqNeg);

            int a = RandomUtil.random.nextInt(numVertices);

            atomID++;

            atoms1Neg.put(i, new FormulaAtom(gp1, a, atomID));
            atoms2Neg.put(i, new FormulaAtom(gp2, a, atomID));
            atoms3Neg.put(i, new FormulaAtom(gp3, a, atomID));
            atoms4Neg.put(i, new FormulaAtom(gp4, a, atomID));
            atoms5Neg.put(i, new FormulaAtom(gp5, a, atomID));
        }

        for (int i = 0; i < numPosAtoms; i++) {
            if (formula1 == null)
                formula1 = atoms1Pos.get(i);
            else
                formula1 = new FormulaOr(formula1, atoms1Pos.get(i));
            if (formula2 == null)
                formula2 = atoms2Pos.get(i);
            else
                formula2 = new FormulaOr(formula2, atoms2Pos.get(i));
            if (formula3 == null)
                formula3 = atoms3Pos.get(i);
            else
                formula3 = new FormulaOr(formula3, atoms3Pos.get(i));
            if (positiveFormula1 == null)
                positiveFormula1 = atoms4Pos.get(i);
            else
                positiveFormula1 = new FormulaOr(positiveFormula1, atoms4Pos.get(i));
            if (positiveFormula2 == null)
                positiveFormula2 = atoms5Pos.get(i);
            else
                positiveFormula2 = new FormulaOr(positiveFormula2, atoms5Pos.get(i));
        }

        for (int i = 0; i < numNegAtoms; i++) {
            if (formula1 == null)
                formula1 = atoms1Neg.get(i);
            else
                formula1 = new FormulaAnd(formula1, new FormulaNot(atoms1Neg.get(i)));
            if (formula2 == null)
                formula2 = atoms2Neg.get(i);
            else
                formula2 = new FormulaAnd(formula2, new FormulaNot(atoms2Neg.get(i)));
            if (formula3 == null)
                formula3 = atoms3Neg.get(i);
            else
                formula3 = new FormulaAnd(formula3, new FormulaNot(atoms3Neg.get(i)));
            if (negativeFormula1 == null)
                negativeFormula1 = atoms4Neg.get(i);
            else
                negativeFormula1 = new FormulaAnd(negativeFormula1, new FormulaNot(atoms4Neg.get(i)));
            if (negativeFormula2 == null)
                negativeFormula2 = atoms5Neg.get(i);
            else
                negativeFormula2 = new FormulaAnd(negativeFormula2, new FormulaNot(atoms5Neg.get(i)));
        }
    }

    public void uniRandCNFGen(int numAtoms, int numClauses, int gpnumVertices, int gpnumLabels, double gpprobability, int numVertices){
        formula1 = null;
        formula2 = null;
        formula3 = null;
        if (numAtoms < 3)
        {
            int anchor = 0;
            int requester = gpnumVertices - 1;
            //int anchor = RandomUtil.random.nextInt(gpnumVertices);
            //int requester = RandomUtil.random.nextInt(gpnumVertices);

            Graph g = genRandomGraph(gpnumVertices, gpnumLabels, gpprobability, anchor, requester);
            BirootedGraph gp1 = new BirootedGraph(new Graph(g), anchor, requester);
            BirootedGraph gp2 = new BirootedGraph(new Graph(g), anchor, requester);
            BirootedGraph gp3 = new BirootedGraph(new Graph(g), anchor, requester);

            int n = RandomUtil.random.nextInt(numVertices);

            formula1 = new FormulaAtom(gp1, n);
            formula2 = new FormulaAtom(gp2, n);
            formula3 = new FormulaAtom(gp3, n);

        }
        else {
            HashMap<Integer, FormulaAtom> atoms1 = new HashMap<Integer, FormulaAtom>();
            HashMap<Integer, FormulaAtom> atoms2 = new HashMap<Integer, FormulaAtom>();
            HashMap<Integer, FormulaAtom> atoms3 = new HashMap<Integer, FormulaAtom>();
            //Generating random atoms
            for (int i = 0; i < numAtoms; i++) {
                int anchor = 0;
                int requester = gpnumVertices - 1;
                //int anchor = RandomUtil.random.nextInt(gpnumVertices);
                //int requester = RandomUtil.random.nextInt(gpnumVertices);
                Graph g = genRandomGraph(gpnumVertices, gpnumLabels, gpprobability, anchor, requester);
                BirootedGraph gp1 = new BirootedGraph(new Graph(g), anchor, requester);
                BirootedGraph gp2 = new BirootedGraph(new Graph(g), anchor, requester);
                BirootedGraph gp3 = new BirootedGraph(new Graph(g), anchor, requester);

                int a = RandomUtil.random.nextInt(numVertices);

                atoms1.put(i, new FormulaAtom(gp1, a));
                atoms2.put(i, new FormulaAtom(gp2, a));
                atoms3.put(i, new FormulaAtom(gp3, a));
            }

            //Generating random CNF
            Formula clauses1 = null;
            Formula clauses2 = null;
            Formula clauses3 = null;
            for (int i = 0; i < numClauses; i++) {
                Formula clause1 = null;
                Formula clause2 = null;
                Formula clause3 = null;
                Set<Integer> chosenAtoms = choose(numAtoms, 3);
                for (int chosenAtom : chosenAtoms){

                    //boolean sign = true;
                    boolean sign = RandomUtil.random.nextBoolean();

                    Formula literal1;
                    Formula literal2;
                    Formula literal3;

                    if (sign) {
                        literal1 = atoms1.get(chosenAtom);
                        literal2 = atoms2.get(chosenAtom);
                        literal3 = atoms3.get(chosenAtom);
                    }

                    else {
                        literal1 = new FormulaNot(atoms1.get(chosenAtom));
                        literal2 = new FormulaNot(atoms2.get(chosenAtom));
                        literal3 = new FormulaNot(atoms3.get(chosenAtom));
                    }

                    //first clause
                    if (clause1 == null) {

                        clause1 = literal1;
                    }
                    else {
                        clause1 = new FormulaOr(clause1, literal1);
                    }

                    //second clause
                    if (clause2 == null) {

                        clause2 = literal2;
                    }
                    else {
                        clause2 = new FormulaOr(clause2, literal2);
                    }

                    //third clause
                    if (clause3 == null) {

                        clause3 = literal3;
                    }
                    else {
                        clause3 = new FormulaOr(clause3, literal3);
                    }
                }

                //first CNF
                if (clauses1 == null) {
                    clauses1 = clause1;
                }
                else {
                    clauses1 = new FormulaAnd(clauses1, clause1);
                }

                //second CNF
                if (clauses2 == null) {
                    clauses2 = clause2;
                }
                else {
                    clauses2 = new FormulaAnd(clauses2, clause2);
                }

                //third CNF
                if (clauses3 == null) {
                    clauses3 = clause3;
                }
                else {
                    clauses3 = new FormulaAnd(clauses3, clause3);
                }
            }
            formula1 = clauses1;
            formula2 = clauses2;
            formula3 = clauses3;
        }
    }

}
