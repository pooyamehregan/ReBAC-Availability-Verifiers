package com.company;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import java.util.ArrayList;

public class FormulaAnd extends Formula {
    protected Formula formula1, formula2;
    public FormulaAnd(Formula formula1, Formula formula2) { this.formula1 = formula1; this.formula2 = formula2; }

    public QBF eval(Entailment entailment, Counters counters) {
        ArrayList<QBF> andArgs = new ArrayList<QBF>();
        andArgs.add(formula1.eval(entailment, counters));
        andArgs.add(formula2.eval(entailment, counters));
        return new QBFAnd(andArgs);
    }

    public QBF eval(Counters counters, boolean isNewReduce) {
        ArrayList<QBF> andArgs = new ArrayList<QBF>();
        andArgs.add(formula1.eval(counters, isNewReduce));
        andArgs.add(formula2.eval(counters, isNewReduce));
        return new QBFAnd(andArgs);
    }

    public QBF eval_CNF(Entailment entailment, Counters counters) {
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
        ArrayList<QBF> andArgs = new ArrayList<QBF>();
        andArgs.add(formula1.formulaEval(counters));
        andArgs.add(formula2.formulaEval(counters));
        counters.incrClauseCntr();
        //QBF.clauseCounter++;
        return new QBFAnd(andArgs);
    }

    @Override
    public Formula clone() {
        return new FormulaAnd(formula1.clone(), formula2.clone());
    }

}
