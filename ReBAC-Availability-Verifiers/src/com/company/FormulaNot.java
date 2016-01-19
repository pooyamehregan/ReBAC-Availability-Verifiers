package com.company;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

public class FormulaNot extends Formula {
    protected Formula formula;
    public FormulaNot(Formula formula) { this.formula = formula; }
    public QBF eval(Counters counters, boolean isNewReduce) {
            return new QBFNot(formula.eval(counters, isNewReduce));
    }
    public QBF eval(Entailment entailment, Counters counters) {
        return new QBFNot(formula.eval(entailment, counters));
    }
    public QBF eval_CNF(Entailment entailment, Counters counters) {
        return new QBFNot(formula.eval_CNF(entailment, counters));
    }

    public void eval(Entailment entailment, Counters counters, ISolver solver, boolean isCNF, boolean isNewReduce) throws ContradictionException{
        formula.eval(entailment, counters, solver, isCNF, isNewReduce);
    }
    public QBF formulaEval(Counters counters) {
        return new QBFNot(formula.formulaEval(counters));
    }

    @Override
    public Formula clone() {
        return new FormulaNot(formula.clone());
    }
}
