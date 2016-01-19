package com.company;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import java.util.ArrayList;

public class FormulaOr extends Formula {
    protected Formula formula1, formula2;
    public FormulaOr(Formula formula1, Formula formula2) { this.formula1 = formula1; this.formula2 = formula2; }

    public QBF eval(Entailment entailment, Counters counters) {
        ArrayList<QBF> orArgs = new ArrayList<QBF>();
        orArgs.add(formula1.eval(entailment, counters));
        orArgs.add(formula2.eval(entailment, counters));
        return new QBFOr(orArgs);
    }

    public QBF eval(Counters counters, boolean isNewReduce) {
        ArrayList<QBF> orArgs = new ArrayList<QBF>();
        orArgs.add(formula1.eval(counters, isNewReduce));
        orArgs.add(formula2.eval(counters, isNewReduce));
        return new QBFOr(orArgs);
    }

    public QBF eval_CNF(Entailment entailment, Counters counters) {
        //Because of the way we turn disjunction of atoms into cnf we do and instead of or
        ArrayList<QBF> andArgs = new ArrayList<QBF>();
        andArgs.add(formula1.eval_CNF(entailment, counters));
        andArgs.add(formula2.eval_CNF(entailment, counters));
        return new QBFAnd(andArgs);
    }

    public void eval(Entailment entailment, Counters counters, ISolver solver, boolean isCNF, boolean isNewReduce) throws ContradictionException{
        formula1.eval(entailment, counters, solver, isCNF, isNewReduce);
        formula2.eval(entailment, counters, solver, isCNF, isNewReduce);
    }

    public QBF formulaEval(Counters counters) {
        ArrayList<QBF> orArgs = new ArrayList<QBF>();
        orArgs.add(formula1.formulaEval(counters));
        orArgs.add(formula2.formulaEval(counters));
        return new QBFOr(orArgs);
    }

    @Override
    public Formula clone() {
        return new FormulaOr(formula1.clone(), formula2.clone());
    }
}
