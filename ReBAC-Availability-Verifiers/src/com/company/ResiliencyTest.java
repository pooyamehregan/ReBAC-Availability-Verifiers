package com.company;

import com.google.common.collect.HashBiMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.*;

public class ResiliencyTest {
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

        int numTest = 20;
        int distance = 0;
        int numLabels = 2;
        Graph.labelSetSize = numLabels;
        double probability = 0.3;
        GraphGenerator gg = new GraphGenerator();
        int anchor = 0;
        Random rand = new Random();
        for (int i = 0; i < numTest; i++) {
            //Generating random graph
            //int numVertices = rand.nextInt(50);
            int numVertices = 10;
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

            //Generate Feasibility
            Feasibility2 feasibility = new Feasibility2(bg, atom1, distance);

            boolean result = feasibility.checkSat_Sat4J();
            assertEquals(true, result);

            reset();

            Graph h = new Graph(g);
            gg.removeEdges(h, 0.5);

            bg = new BirootedGraph(h, anchor, requester);
            feasibility = new Feasibility2(bg, atom2, distance);
            result = feasibility.checkSat_Sat4J();
            assertEquals(false, result);

            reset();
            System.out.println(i);
        }

    }
}