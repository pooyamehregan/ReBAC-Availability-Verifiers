package com.company;

import com.google.common.collect.HashBiMap;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class FormulaAtomTest {

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
    public void equalityTest() {
        Graph.labelSetSize = 1;
        GraphGenerator gg = new GraphGenerator();
        Graph graph = gg.genErdosRenyi(10, 1, 0.1);
        Formula.graph = graph;
        FormulaGenerator fg = new FormulaGenerator();
        fg.uniRandCNFGen(3, 1, 5, 1, 0.1, 10);

        init();
        Satisfiability2 satisfiability = new Satisfiability2(graph, fg.formula1);
        Counters counters1 = new Counters();
        satisfiability.genReqVars(counters1);
        Formula.reqVars = satisfiability.reqVars;
        StringBuilder sb1 = new StringBuilder();
        String str1 = fg.formula2.eval(satisfiability, counters1). evalQDIMACS(sb1).toString();

        reset();

        Feasibility2 feasibility = new Feasibility2(graph, fg.formula2, 0);
        Counters counters2 = new Counters();
        feasibility.genReqVars(counters2);
        Formula.reqVars = feasibility.reqVars;
        StringBuilder sb2 = new StringBuilder();
        String str2 = fg.formula1.eval(feasibility, counters2).evalQDIMACS(sb2).toString();
        assertEquals(str1, str2);
    }
}