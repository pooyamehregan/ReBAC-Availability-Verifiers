package com.company;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.ModelIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Satisfiability extends Entailment {

    //Attributes
    protected HashMap<FormulaAtom, StringBuilder> atomDIMACSMap;
    protected QBF requesterConstrts;
    //protected Counters counters = new Counters();

    public Satisfiability(Graph graph, Formula formula) {
        this.graph = graph;
        this.formula = formula;
        sat4JNumOfClauses = new ArrayList<Integer>();
        sat4JNumOfVars = new ArrayList<Integer>();
        modelTimes = new ArrayList<Double>();
    }

    public Satisfiability() {
        sat4JNumOfClauses = new ArrayList<Integer>();
        sat4JNumOfVars = new ArrayList<Integer>();
        modelTimes = new ArrayList<Double>();
    }

    public String checkSat_QDIMACS() {
        //QBF graphRep = genGraphEdgeVars();
        Counters counters = new Counters();
        QBF requesterConstrts = reqsCnstrts(counters);

        formula.graph = this.graph;
        //formula.gEdgeVars = this.gEdgeVars;
        formula.reqVars = this.reqVars;
        QBF fQbf = formula.eval(this, counters);


        fQbf = nnf(fQbf);
        fQbf = prenex(fQbf);

        counters.setClauseCounter(0);
        //QBF.clauseCounter = 0;
        //fQbf = cnf(null, fQbf);

        fQbf = nnf(fQbf);
        /*
        QBF temp = new AndQBF(graphRep, fQbf);

        //Quantifying temp with the graph edge representation variables
        for (int i : gEdgeVars.values()) {
            temp = new ExistsQBF(temp, i);
        }

        temp = new AndQBF(requesterConstrts, temp);
        */
        ArrayList<QBF> andArgs = new ArrayList<QBF>();
        andArgs.add(requesterConstrts);
        andArgs.add(fQbf);
        QBF temp = new QBFAnd(andArgs);


        //Quantifying temp with requester variables (v)
        for (int i : reqVars.values()) {
            temp = new QBFExists(temp, i);
        }

        temp = prenex(temp);

        StringBuilder quantStr = printQuantifiers(temp, "QDIMACS");
        StringBuilder str = new StringBuilder();
        str.append(temp.evalQDIMACS(str));
        str.append(" 0").append(System.lineSeparator());
        str.append("p cnf ").append(counters.getVarCounter()).append(" ").append(counters.getClauseCounter()).append(System.lineSeparator()).append(quantStr).append(str);
        //str.append("p cnf ").append(QBF.varCounter).append(" ").append(QBF.clauseCounter).append(System.lineSeparator()).append(quantStr).append(str);
        return str.toString();

    }

    public String checkSat_QCIR() {
        Counters counters = new Counters();
        QBF requesterConstrts = reqsCnstrts(counters);
        formula.graph = this.graph;
        formula.reqVars = this.reqVars;
        long startTime = System.nanoTime();
        QBF fQbf = formula.eval(this, counters);
        ArrayList<QBF> andArgs = new ArrayList<QBF>();
        andArgs.add(requesterConstrts);
        andArgs.add(fQbf);
        QBF temp = new QBFAnd(andArgs);

        //Quantifying temp with requester variables (v)
        for (int i : reqVars.values()) {
            temp = new QBFExists(temp, i);
        }
        long elapsedTime = System.nanoTime() - startTime;
        formula2QBFTimes = (double) elapsedTime / 1000000000.0;
        //System.out.format("Formula to QBF Conversion Time:\t%f%s", seconds, System.lineSeparator());

        startTime = System.nanoTime();
        temp = nnf(temp);
        elapsedTime = System.nanoTime() - startTime;
        NNFTimes = (double) elapsedTime / 1000000000.0;
        //System.out.format("NNF Conversion Time:\t%f%s", seconds, System.lineSeparator());

        startTime = System.nanoTime();
        temp = prenex(temp);
        elapsedTime = System.nanoTime() - startTime;
        prenexTimes = (double) elapsedTime / 1000000000.0;
        //System.out.format("Prenex Conversion Time:\t%f%s", seconds, System.lineSeparator());


        StringBuilder quantStr = printQuantifiers(temp, "QCIR");
        StringBuilder str = new StringBuilder();

        startTime = System.nanoTime();
        str = removeQuantifiers(temp).evalQCIR(null, str, counters);
        String output = String.format("#QCIR-G14 %d%s%soutput(%d)%s%s", counters.getVarCounter(), System.lineSeparator(), quantStr.toString(), counters.getVarCounter(), System.lineSeparator(), str.toString());
        //String output = String.format("#QCIR-G14 %d%s%soutput(%d)%s%s", QBF.varCounter, System.lineSeparator(), quantStr.toString(), QBF.varCounter, System.lineSeparator(), str.toString());
        elapsedTime = System.nanoTime() - startTime;
        QCIRCompileTime = (double) elapsedTime / 1000000000.0;
        QCIRNumOfVars = counters.getVarCounter();
        //QCIRNumOfVars.add(QBF.varCounter);
        //System.out.format("Compilation Time\t%f%s", seconds, System.lineSeparator());
        //System.out.format("# of Vars:\t%d%s", QBF.varCounter, System.lineSeparator());
        return output;
    }

    private void evalAtoms(Formula formula, Counters counters) {
        if (formula instanceof FormulaAtom) {
            FormulaAtom a = (FormulaAtom) formula;
            if(!atomDIMACSMap.containsKey(a)) {
                Counters counters1 = new Counters(counters);
                //QBF.varCounter = this.varCounter;
                //QBF.clauseCounter = this.clauseCounter;
                a.qbf = null;
                QBF temp = a.eval(this, counters1);
                ArrayList<QBF> andArgs = new ArrayList<QBF>();
                andArgs.add(requesterConstrts);
                andArgs.add(temp);
                temp = new QBFAnd(andArgs);
                a.counters = new Counters(counters1);
                //a.varCounter = QBF.varCounter;
                //a.clauseCounter = QBF.clauseCounter;
                StringBuilder str = new StringBuilder();
                try {
                    atomDIMACSMap.put(a, temp.evalQDIMACS(str));
                } catch (Exception e) {
                    //System.out.format("Error with %d bytes (%s)", str.length(), new Date());
                    e.printStackTrace();
                }
            }
        }
        else if (formula instanceof FormulaAnd) {
            FormulaAnd af = (FormulaAnd) formula;
            evalAtoms(af.formula1, counters);
            evalAtoms(af.formula2, counters);
        }
        else if (formula instanceof FormulaOr) {
            FormulaOr of = (FormulaOr) formula;
            evalAtoms(of.formula1, counters);
            evalAtoms(of.formula2, counters);
        }
        else if (formula instanceof FormulaNot) {
            FormulaNot nf = (FormulaNot) formula;
            evalAtoms(nf.formula, counters);
        }

    }

    private void evalAtoms2(Formula formula, Counters counters, HashMap<FormulaAtom, StringBuilder> atomDIMACSMap) {
        if (formula instanceof FormulaAtom) {
            FormulaAtom a = (FormulaAtom) formula;
            if(!atomDIMACSMap.containsKey(a)) {
                Counters counters1 = new Counters(counters);
                a.qbf = null;
                QBF temp = a.eval(new Satisfiability2(), counters1);
                a.counters = new Counters(counters1);
                StringBuilder str = new StringBuilder();
                try {
                    atomDIMACSMap.put(a, temp.evalQDIMACS(str));
                } catch (Exception e) {
                    //System.out.format("Error with %d bytes (%s)", str.length(), new Date());
                    e.printStackTrace();
                }
            }
        }
        else if (formula instanceof FormulaAnd) {
            FormulaAnd af = (FormulaAnd) formula;
            evalAtoms2(af.formula1, counters, atomDIMACSMap);
            evalAtoms2(af.formula2, counters, atomDIMACSMap);
        }
        else if (formula instanceof FormulaOr) {
            FormulaOr of = (FormulaOr) formula;
            evalAtoms2(of.formula1, counters, atomDIMACSMap);
            evalAtoms2(of.formula2, counters, atomDIMACSMap);
        }
        else if (formula instanceof FormulaNot) {
            FormulaNot nf = (FormulaNot) formula;
            evalAtoms2(nf.formula, counters, atomDIMACSMap);
        }

    }

    private boolean evalFormula(Formula formula, int i) {
        if (formula instanceof FormulaAtom) {
            boolean result;
            FormulaAtom a = (FormulaAtom) formula;
            StringBuilder temp = atomDIMACSMap.get(a);
            Counters counters1 = new Counters(a.counters);
            counters1.incrClauseCntr();
            //QBF.varCounter = a.varCounter;
            //QBF.clauseCounter = a.clauseCounter;
            //QBF.clauseCounter++;
            sat4JNumOfVars.add(counters1.getVarCounter());
            sat4JNumOfClauses.add(counters1.getClauseCounter());
            String str = String.format("p cnf %d %d%s%s%d 0", counters1.getVarCounter(), counters1.getClauseCounter(), System.lineSeparator(), temp.toString(), a.reqVars.get(i));
            //String str = String.format("p cnf %d %d%s%s%d 0", QBF.varCounter, QBF.clauseCounter, System.lineSeparator(), temp.toString(), a.reqVars.get(i));
            result = isSat(str);
            return result;
        } else if (formula instanceof FormulaAnd) {
            FormulaAnd af = (FormulaAnd) formula;
            return (evalFormula(af.formula1, i) && evalFormula(af.formula2, i));
        } else if (formula instanceof FormulaOr) {
            FormulaOr of = (FormulaOr) formula;
            return (evalFormula(of.formula1, i) || evalFormula(of.formula2, i));
        } else {
            FormulaNot nf = (FormulaNot) formula;
            return (!evalFormula(nf.formula, i));
        }
    }

    public boolean checkSat_Sat4J() {
        formula.graph = this.graph;
        Counters counters = new Counters();
        requesterConstrts = reqsCnstrts(counters);
        formula.reqVars = this.reqVars;
        atomDIMACSMap = new HashMap<FormulaAtom, StringBuilder>();


        long startTime = System.nanoTime();
        evalAtoms(formula, counters);
        long elapsedTime = System.nanoTime() - startTime;
        sat4JCompileTime = (double) elapsedTime / 1000000000.0;

        startTime = System.nanoTime();
        for (int i : graph.vertices) {
            boolean output = evalFormula(formula, i);
            if (output) {
                elapsedTime = System.nanoTime() - startTime;
                sat4JTotalSolverTime = (double) elapsedTime / 1000000000.0;
                sat4JNumOfIter = i;
                //System.out.format("Total Time:\t%f%s", seconds, System.lineSeparator());
                return true;
            }
        }
        elapsedTime = System.nanoTime() - startTime;
        sat4JTotalSolverTime = (double) elapsedTime / 1000000000.0;
        sat4JNumOfIter = graph.vertices.size();
        //System.out.format("Total Time:\t%f%s", seconds, System.lineSeparator());
        return false;
    }

    public boolean checkSat_Sat4J(int k, Formula positiveFormula, Formula negativeFormula) {
        int count;
        boolean result = false;
        HashMap<FormulaAtom, StringBuilder> positivesDIMACSMap = new HashMap<FormulaAtom, StringBuilder>();
        HashMap<FormulaAtom, StringBuilder> negativesDIMACSMap = new HashMap<FormulaAtom, StringBuilder>();
        positiveFormula.graph = this.graph;
        negativeFormula.graph = this.graph;
        Counters counters = new Counters();
        genReqVars(counters);
        positiveFormula.reqVars = this.reqVars;
        negativeFormula.reqVars = this.reqVars;


        Counters counters1 = new Counters(counters);
        Counters counters2 = new Counters(counters);
        long startTime = System.nanoTime();
        if (negativeFormula != null)
            evalAtoms2(negativeFormula, counters1, negativesDIMACSMap);
        evalAtoms2(positiveFormula, counters2, positivesDIMACSMap);
        long elapsedTime = System.nanoTime() - startTime;
        sat4JCompileTime = (double) elapsedTime / 1000000000.0;

        int requester = 0;

        startTime = 0;
        elapsedTime = 0;
        numModels = 0;
        for (FormulaAtom pa: positivesDIMACSMap.keySet()) {
            count = 0;
            result = false;
            String str = String.format("p cnf %d %d%s%s", pa.counters.getVarCounter(), pa.counters.getClauseCounter(), System.lineSeparator(), positivesDIMACSMap.get(pa));
            startTime = System.nanoTime();
            ISolver solver = SolverFactory.newDefault();
            IProblem problem = enumerateModels(str, pa, solver);
            elapsedTime = elapsedTime + System.nanoTime() - startTime;
            if (problem != null) {
                try {
                    boolean isSat_pos;
                    do {
                        startTime = System.nanoTime();
                        isSat_pos = problem.isSatisfiable();
                        long tempTime = System.nanoTime() - startTime;
                        elapsedTime = elapsedTime + tempTime;
                        modelTimes.add((double) tempTime / 1000000000.0);
                        if (isSat_pos) {
                            numModels++;
                            int[] model = problem.model();
                            for (int var : model) {
                                if (reqVars.values().contains(var))
                                    requester = var;
                            }
                            VecInt bc = new VecInt();
                            bc.push(-requester);
                            solver.addBlockingClause(bc);
                            boolean flag = true;
                            for (FormulaAtom na : negativesDIMACSMap.keySet()) {
                                StringBuilder temp = negativesDIMACSMap.get(na);
                                Counters counters3 = new Counters(na.counters);
                                counters3.incrClauseCntr();
                                String str1 = String.format("p cnf %d %d%s%s%d 0", counters3.getVarCounter(), counters3.getClauseCounter(), System.lineSeparator(), temp.toString(), requester);
                                startTime = System.nanoTime();
                                boolean isSat_neg = isSat(str1, na);
                                elapsedTime = elapsedTime + System.nanoTime() - startTime;
                                if (isSat_neg) {
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag)
                                count++;
                        }
                    } while (isSat_pos && count < k);
                } catch (TimeoutException e) {
                    System.out.println("Timeout (enumerateModel)!");
                    //sat4JTotalSolverTime = 0;
                } catch (ContradictionException e) {
                    System.out.println("Contradicition!!!");
                }
            }
            if (count == k) {
                result = true;
                break;
            }
        }

        //elapsedTime = System.nanoTime() - startTime;
        sat4JTotalSolverTime = (double) elapsedTime / 1000000000.0;
        return result;
    }


}
