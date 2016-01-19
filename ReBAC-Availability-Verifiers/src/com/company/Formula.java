package com.company;

import com.google.common.collect.HashBiMap;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Pooya on 2014-09-30.
 */
public abstract class Formula {

    //static variables;
    protected static Graph graph;
    protected static HashMap<Integer,Integer> reqVars;
    protected static int [][][] mutatedGraphVars;
    protected static HashBiMap<FormulaAtom, Integer> atomVarsMap;
    protected static HashBiMap<Integer, Integer> atomIDVars;
    protected static HashMap<FormulaAtom, QBF> atomQBFsMap;
    protected static HashMap<FormulaAtom, Integer[][]> atomIsoVarsMap;
    //protected static HashMap<FormulaAtom, ISolver> atomSolversMap;

    public abstract QBF eval(Entailment entailment, Counters counters);
    public abstract QBF eval(Counters counters, boolean isNewReduce);
    public abstract QBF formulaEval(Counters counters);
    public abstract QBF eval_CNF(Entailment entailment, Counters counters);
    public abstract void eval(Entailment entailment, Counters counters, ISolver solver, boolean isCNF, boolean isNewReduce) throws ContradictionException;
    @Override
    public abstract Formula clone();
}

