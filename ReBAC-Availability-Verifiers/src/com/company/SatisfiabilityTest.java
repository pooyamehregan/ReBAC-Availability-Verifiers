package com.company;

import com.google.common.collect.HashBiMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class SatisfiabilityTest {
    private void init() {
        Formula.atomVarsMap = HashBiMap.create();
        Formula.atomQBFsMap = new HashMap<FormulaAtom, QBF>();
        Formula.atomIsoVarsMap = new HashMap<FormulaAtom, Integer[][]>();
    }

    private void reset() {
        Formula.atomVarsMap.clear();
        Formula.atomQBFsMap.clear();
        Formula.atomIsoVarsMap.clear();
        Formula.reqVars.clear();
        Formula.mutatedGraphVars = null;
    }

    @Test
    public void testCheckSat_Sat4J() {
        init();

        int numTest = 10;
        Graph.labelSetSize = 1;
        int numLabels = 1;
        double probability = 0.1;
        GraphGenerator gg = new GraphGenerator();
        int anchor = 0;
        Random rand = new Random();
        for (int i = 0; i < numTest; i ++) {
            //Generating random graph
            //int numVertices = rand.nextInt(50);
            int numVertices = 5;
            int requester = numVertices - 1;
            boolean valid = false;
            Graph g = new Graph();
            while (!valid) {
                g = gg.genErdosRenyi(numVertices, numLabels, probability);
                valid = gg.validate(g, anchor, requester);
            }
            BirootedGraph bg = new BirootedGraph(g, anchor, requester);
            //Generate Atom
            FormulaAtom atom1 = new FormulaAtom(bg, anchor);
            FormulaAtom atom2 = new FormulaAtom(bg, anchor);

            //Generate Satisfiability
            Satisfiability satisfiability = new Satisfiability(bg, atom1);

            boolean result = satisfiability.checkSat_Sat4J();
            assertEquals(true, result);

            reset();

            Graph h = new Graph(g);
            gg.removeEdges(h, 0.5);

            bg = new BirootedGraph(h, anchor, requester);
            satisfiability = new Satisfiability(bg, atom2);
            result = satisfiability.checkSat_Sat4J();
            assertEquals(false, result);

            reset();
            System.out.println(i);
        }

    }
}