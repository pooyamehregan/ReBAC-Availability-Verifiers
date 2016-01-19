package com.company;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.*;
import org.sat4j.tools.ModelIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Pooya on 15-02-19.
 */
public class Satisfiability2 extends Entailment {

    //Attributes
    protected HashMap<FormulaAtom, StringBuilder> atomDIMACSMap;
    protected QBF requesterConstrts;
    //protected Counters counters = new Counters();

    public Satisfiability2(Graph graph, Formula formula) {
        this.graph = graph;
        this.formula = formula;
        sat4JNumOfClauses = new ArrayList<Integer>();
        sat4JNumOfVars = new ArrayList<Integer>();
        sat4JNumOfClauses_Pos = new ArrayList<Integer>();
        sat4JNumOfClauses_Neg = new ArrayList<Integer>();
        sat4JNumOfVars_Pos = new ArrayList<Integer>();
        sat4JNumOfVars_Neg = new ArrayList<Integer>();
        modelTimes = new ArrayList<Double>();
    }

    public Satisfiability2() {
        sat4JNumOfClauses = new ArrayList<Integer>();
        sat4JNumOfVars = new ArrayList<Integer>();
        modelTimes = new ArrayList<Double>();
    }

    protected String checkSat_QCIR() {
        Counters counters = new Counters();
        QBF requesterConstrts = reqsCnstrts(counters);
        Formula.graph = this.graph;
        Formula.reqVars = this.reqVars;
        long startTime = System.nanoTime();
        QBF fQbf = formula.eval(new Satisfiability(), counters);
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

    protected String checkSat_QCIR(boolean isNewReduce) {
        Counters counters = new Counters();
        QBF requesterConstrts = reqsCnstrts(counters);
        Formula.graph = this.graph;
        Formula.reqVars = this.reqVars;
        long startTime = System.nanoTime();
        QBF fQbf = formula.eval(counters, isNewReduce);
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

    protected void evalAtoms(Formula formula, Counters counters) {
        if (formula instanceof FormulaAtom) {
            FormulaAtom a = (FormulaAtom) formula;
            if(!atomDIMACSMap.containsKey(a)) {
                Counters counters1 = new Counters(counters);
                a.qbf = null;
                QBF temp = a.eval(this, counters1);
                a.counters = new Counters(counters1);
                StringBuilder str = new StringBuilder();
                try {
                    long startTime = System.nanoTime();
                    atomDIMACSMap.put(a, temp.evalQDIMACS(str));
                    long elapsedTime = System.nanoTime() - startTime;
                    sat4JCompileTime += ((double) elapsedTime / 1000000000.0);
                } catch (Exception e) {
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


    private void evalAtoms2(Formula formula, Counters counters, HashMap<FormulaAtom, ISolver> atomSolverMap) {
        if (formula instanceof FormulaAtom) {
            FormulaAtom a = (FormulaAtom) formula;
            if(!atomSolverMap.containsKey(a)) {
                Counters counters1 = new Counters(counters);
                a.qbf = null;
                QBF temp = a.eval(new Satisfiability2(), counters1);
                a.counters = new Counters(counters1);
                try {
                    ISolver solver = SolverFactory.newDefault();

                    VecInt v0 = new VecInt();
                    for (int k : Formula.reqVars.values()) {
                        v0.push(k);
                    }
                    solver.addExactly(v0, 1);

                    for (int i = 0; i < Formula.atomIsoVarsMap.get(a).length; i++) {
                        VecInt v1 = new VecInt();
                        for (int j = 0; j < Formula.atomIsoVarsMap.get(a)[0].length; j++) {
                            if (Formula.atomIsoVarsMap.get(a)[i][j] > 0) {
                                v1.push(Formula.atomIsoVarsMap.get(a)[i][j]);
                            }
                        }
                        solver.addExactly(v1, 1);
                    }

                    for (int j = 0; j < Formula.atomIsoVarsMap.get(a)[0].length; j++) {
                        VecInt v2 = new VecInt();
                        for (int i = 0; i < Formula.atomIsoVarsMap.get(a).length; i++) {
                            if ( Formula.atomIsoVarsMap.get(a)[i][j] > 0) {
                                v2.push(Formula.atomIsoVarsMap.get(a)[i][j]);
                            }
                        }
                        solver.addAtMost(v2, 1);
                    }
                    long startTime = System.nanoTime();
                    temp.evalDIMACS(solver);
                    long elapsedTime = System.nanoTime() - startTime;
                    sat4JCompileTime += ((double) elapsedTime / 1000000000.0);
                    atomSolverMap.put(a, solver);
                } catch (ContradictionException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (formula instanceof FormulaAnd) {
            FormulaAnd af = (FormulaAnd) formula;
            evalAtoms2(af.formula1, counters, atomSolverMap);
            evalAtoms2(af.formula2, counters, atomSolverMap);
        }
        else if (formula instanceof FormulaOr) {
            FormulaOr of = (FormulaOr) formula;
            evalAtoms2(of.formula1, counters, atomSolverMap);
            evalAtoms2(of.formula2, counters, atomSolverMap);
        }
        else if (formula instanceof FormulaNot) {
            FormulaNot nf = (FormulaNot) formula;
            evalAtoms2(nf.formula, counters, atomSolverMap);
        }

    }


    private void evalAtoms3(Formula formula, Counters counters, HashMap<Integer, ISolver> atomIDSolverMap, HashMap<Integer, Counters> atomIDCountersMap, boolean isNewReduce){
        if (formula instanceof FormulaAtom) {
            FormulaAtom a = (FormulaAtom) formula;
            if(!atomIDSolverMap.containsKey(a.atomID)) {
                Counters counters1 = new Counters(counters);
                ISolver solver = SolverFactory.newDefault();
                long startTime = System.nanoTime();
                try {
                    a.eval(new Satisfiability2(), counters1, solver, false, isNewReduce);
                } catch (ContradictionException e) {
                    solver = null;
                }
                //a.counters = new Counters(counters1);
                sat4JNumOfVars_Neg.add(counters1.getVarCounter());
                sat4JNumOfClauses_Neg.add(counters1.getClauseCounter() + 1); //+ 1 is because of requester clause which will be added
                atomIDCountersMap.put(a.atomID, new Counters(counters1));
                long elapsedTime = System.nanoTime() - startTime;
                sat4JCompileTime += ((double) elapsedTime / 1000000000.0);
                atomIDSolverMap.put(a.atomID, solver);
            }
        }
        else if (formula instanceof FormulaAnd) {
            FormulaAnd af = (FormulaAnd) formula;
            evalAtoms3(af.formula1, counters, atomIDSolverMap, atomIDCountersMap, isNewReduce);
            evalAtoms3(af.formula2, counters, atomIDSolverMap, atomIDCountersMap, isNewReduce);
        }
        else if (formula instanceof FormulaOr) {
            FormulaOr of = (FormulaOr) formula;
            evalAtoms3(of.formula1, counters, atomIDSolverMap, atomIDCountersMap, isNewReduce);
            evalAtoms3(of.formula2, counters, atomIDSolverMap, atomIDCountersMap, isNewReduce);
        }
        else if (formula instanceof FormulaNot) {
            FormulaNot nf = (FormulaNot) formula;
            evalAtoms3(nf.formula, counters, atomIDSolverMap, atomIDCountersMap, isNewReduce);
        }

    }

    private boolean evalFormula(Formula formula, int i) {
        if (formula instanceof FormulaAtom) {
            boolean result;
            FormulaAtom a = (FormulaAtom) formula;
            StringBuilder temp = atomDIMACSMap.get(a);
            Counters counters1 = new Counters(a.counters);
            counters1.incrClauseCntr();
            sat4JNumOfVars.add(counters1.getVarCounter());
            sat4JNumOfClauses.add(counters1.getClauseCounter());
            long startTime = System.nanoTime();
            String str = String.format("p cnf %d %d%s%s%d 0", counters1.getVarCounter(), counters1.getClauseCounter(),
                    System.lineSeparator(), temp.toString(), Formula.reqVars.get(i));
            long elapsedTime = System.nanoTime() - startTime;
            sat4JCompileTime += ((double) elapsedTime / 1000000000.0);

            result = isSat(str, a);
            return result;
        } else if (formula instanceof FormulaAnd) {
            FormulaAnd af = (FormulaAnd) formula;
            return (evalFormula(af.formula1, i) & evalFormula(af.formula2, i));
        } else if (formula instanceof FormulaOr) {
            FormulaOr of = (FormulaOr) formula;
            return (evalFormula(of.formula1, i) | evalFormula(of.formula2, i));
        } else {
            FormulaNot nf = (FormulaNot) formula;
            return (!evalFormula(nf.formula, i));
        }
    }

    private boolean evalFormula2(Formula formula, int requester, HashMap<FormulaAtom, ISolver> atomSolverMap) {
        if (formula instanceof FormulaAtom) {
            boolean result = false;
            FormulaAtom a = (FormulaAtom) formula;
            ISolver solver = atomSolverMap.get(a);
            Counters counters1 = new Counters(a.counters);
            counters1.incrClauseCntr();
            VecInt requesterClause = new VecInt();
            requesterClause.push(requester);
            IConstr constraint = null;
            try {
                solver.setExpectedNumberOfClauses(counters1.getClauseCounter());
                constraint = solver.addClause(requesterClause);
                long startTime = System.nanoTime();
                result = solver.isSatisfiable();
                long elapsedTime = System.nanoTime() - startTime;
                sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);
            } catch (ContradictionException e) {
                //System.out.println("Contradiciton While trying a negative atom");
            } catch (TimeoutException e) {
                System.out.println("Time out while evaluating atoms!");
            } finally {
                if (constraint != null)
                    solver.removeConstr(constraint);
            }
            return result;
        } else if (formula instanceof FormulaAnd) {
            FormulaAnd af = (FormulaAnd) formula;
            return (evalFormula2(af.formula1, requester, atomSolverMap) & evalFormula2(af.formula2, requester, atomSolverMap));
        } else if (formula instanceof FormulaOr) {
            FormulaOr of = (FormulaOr) formula;
            return (evalFormula2(of.formula1, requester, atomSolverMap) | evalFormula2(of.formula2, requester, atomSolverMap));
        } else {
            FormulaNot nf = (FormulaNot) formula;
            return (!evalFormula2(nf.formula, requester, atomSolverMap));
        }
    }

    private boolean evalFormula3(Formula formula, int requester, HashMap<Integer, ISolver> atomIDSolverMap, HashMap<Integer, Counters> atomIDCountersMap) {
        if (formula instanceof FormulaAtom) {
            boolean result = false;
            FormulaAtom a = (FormulaAtom) formula;
            if(atomIDSolverMap.get(a.atomID) != null) {
                ISolver solver = atomIDSolverMap.get(a.atomID);
                Counters counters1 = new Counters(atomIDCountersMap.get(a.atomID));
                counters1.incrClauseCntr();
                VecInt requesterClause = new VecInt();
                requesterClause.push(requester);
                IConstr constraint = null;
                try {
                    solver.setExpectedNumberOfClauses(counters1.getClauseCounter());
                    constraint = solver.addClause(requesterClause);
                    long startTime = System.nanoTime();
                    result = solver.isSatisfiable();
                    long elapsedTime = System.nanoTime() - startTime;
                    sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);
                } catch (ContradictionException e) {
                    //System.out.println("Contradiciton While trying a negative atom");
                } catch (TimeoutException e) {
                    System.out.println("Time out while evaluating atoms!");
                } finally {
                    solver.clearLearntClauses();
                    if (constraint != null)
                        solver.removeConstr(constraint);
                }
                return result;
            } else
                return false;
        } else if (formula instanceof FormulaAnd) {
            FormulaAnd af = (FormulaAnd) formula;
            return (evalFormula3(af.formula1, requester, atomIDSolverMap, atomIDCountersMap) & evalFormula3(af.formula2, requester, atomIDSolverMap, atomIDCountersMap));
        } else if (formula instanceof FormulaOr) {
            FormulaOr of = (FormulaOr) formula;
            return (evalFormula3(of.formula1, requester, atomIDSolverMap, atomIDCountersMap) | evalFormula3(of.formula2, requester, atomIDSolverMap, atomIDCountersMap));
        } else {
            FormulaNot nf = (FormulaNot) formula;
            return (!evalFormula3(nf.formula, requester, atomIDSolverMap, atomIDCountersMap));
        }
    }

    public boolean checkSat_Sat4J() {
        Formula.graph = this.graph;
        Counters counters = new Counters();
        genReqVars(counters);
        Formula.reqVars = this.reqVars;
        atomDIMACSMap = new HashMap<FormulaAtom, StringBuilder>();
        Formula.atomIsoVarsMap = new HashMap<FormulaAtom, Integer[][]>();
        evalAtoms(formula, counters);
        for (int i : graph.vertices) {
            boolean output = evalFormula(formula, i);
            if (output) {
                sat4JNumOfIter = i;
                return true;
            }
        }
        sat4JNumOfIter = graph.vertices.size();
        return false;
    }

    public boolean checkSat_Sat4J(int k) {
        HashMap<FormulaAtom, ISolver> atomSolverMap = new HashMap<FormulaAtom, ISolver>();
        Formula.graph = this.graph;
        Counters counters = new Counters();
        genReqVars(counters);
        Formula.reqVars = this.reqVars;
        evalAtoms2(formula, counters, atomSolverMap);
        int count = 0;
        for (int i : graph.vertices) {
            boolean output = evalFormula2(formula, reqVars.get(i), atomSolverMap);
            sat4JNumOfIter ++;
            if (output) {
                count++;
            }
            if (count == k) {
                break;
            }
        }
        if (count == k)
            return true;
        else
            return false;
    }

    public boolean checkSat_Sat4J_VE(int k, boolean isNewReduce) {
        HashMap<Integer, ISolver> atomIDSolverMap = new HashMap<Integer, ISolver>();
        HashMap<Integer, Counters> atomIDCountersMap = new HashMap<Integer, Counters>();
        Formula.graph = this.graph;
        Counters counters = new Counters();
        genReqVars(counters);
        Formula.reqVars = this.reqVars;
        //evalAtoms2(formula, counters, atomSolverMap);
        evalAtoms3(formula, counters, atomIDSolverMap, atomIDCountersMap, isNewReduce);
        int count = 0;
        for (int i : graph.vertices) {
            boolean output = evalFormula3(formula, reqVars.get(i), atomIDSolverMap, atomIDCountersMap);
            sat4JNumOfIter ++;
            if (output) {
                count++;
            }
            if (count == k) {
                break;
            }
        }
        if (count == k)
            return true;
        else
            return false;
    }

    public boolean checkSat_Sat4J(int k, Formula positiveFormula, Formula negativeFormula) {
        int count;
        HashSet<FormulaAtom> positiveAtoms = new HashSet<FormulaAtom>();
        boolean result;
        HashMap<FormulaAtom, ISolver> negativesSolverMap = new HashMap<FormulaAtom, ISolver>();
        Formula.graph = this.graph;
        Counters counters = new Counters();
        genReqVars(counters);
        Formula.reqVars = this.reqVars;

        Counters counters1 = new Counters(counters);
        Counters counters2 = new Counters(counters);
        if (negativeFormula != null)
            evalAtoms2(negativeFormula, counters1, negativesSolverMap);
        enumerateAtoms(positiveFormula, positiveAtoms);
        ISolver positiveFormulaSolver = SolverFactory.newDefault();
        QBF qbf = positiveFormula.eval_CNF(this, counters2);
        long startTime;
        long elapsedTime;
        try {
            startTime = System.nanoTime();
            qbf.evalDIMACS(positiveFormulaSolver);
            elapsedTime = System.nanoTime() - startTime;
            sat4JCompileTime += ((double) elapsedTime / 1000000000.0);
        }
        catch (ContradictionException e) {
            System.out.println("Contradiction while generating positive formula");
        }

        int requester = 0;

        numModels = 0;
        count = 0;
        result = false;
        positiveFormulaSolver.setExpectedNumberOfClauses(counters2.getClauseCounter());
        IProblem problem = enumerateModels(positiveAtoms, positiveFormulaSolver);
        if (problem != null) {
            try {
                boolean isSat_pos;
                do {
                    startTime = System.nanoTime();
                    isSat_pos = problem.isSatisfiable();
                    elapsedTime = System.nanoTime() - startTime;
                    long tempTime = elapsedTime;
                    //solvingTime2.add((double) tempTime / 1000000000.0);
                    modelTimes.add((double) tempTime / 1000000000.0);
                    sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);

                    if (isSat_pos) {
                        numModels++;
                        int[] model = problem.model();
                        for (int var : model) {
                            if (reqVars.values().contains(var))
                                requester = var;
                        }
                        VecInt bc = new VecInt();
                        bc.push(-requester);
                        positiveFormulaSolver.addBlockingClause(bc);
                        boolean flag = true;
                        for (FormulaAtom na : negativesSolverMap.keySet()) {
                            ISolver negativeAtomSolver = negativesSolverMap.get(na);
                            Counters counters3 = new Counters(na.counters);
                            counters3.incrClauseCntr();
                            VecInt requesterClause = new VecInt();
                            requesterClause.push(requester);
                            boolean isSat_neg = false;
                            IConstr constraint = null;
                            try {
                                negativeAtomSolver.setExpectedNumberOfClauses(counters3.getClauseCounter());
                                constraint = negativeAtomSolver.addClause(requesterClause);

                                startTime = System.nanoTime();
                                isSat_neg = negativeAtomSolver.isSatisfiable();
                                elapsedTime = System.nanoTime() - startTime;
                                sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);

                            } catch (ContradictionException e) {
                                //System.out.println("Contradiciton While trying a negative atom");
                            } finally {
                                negativeAtomSolver.removeConstr(constraint);
                            }
                            if (isSat_neg) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag)
                            count++;
                        if (count == k) {
                            result = true;
                            break;
                        }
                    }
                } while (isSat_pos && count < k);
            } catch (TimeoutException e) {
                System.out.println("Timeout (enumerateModel)!");
            } catch (ContradictionException e) {
                System.out.println("Contradicition!!!");
            }
        }
        return result;
    }


    public boolean checkSat_Sat4J_2(int k, Formula positiveFormula, Formula negativeFormula) {
        int count;
        boolean result;
        HashMap<FormulaAtom, ISolver> negativesSolverMap = new HashMap<FormulaAtom, ISolver>();
        HashMap<FormulaAtom, ISolver> positivesSolverMap = new HashMap<FormulaAtom, ISolver>();
        Formula.graph = this.graph;
        Counters counters = new Counters();
        genReqVars(counters);
        Formula.reqVars = this.reqVars;

        Counters counters1 = new Counters(counters);
        Counters counters2 = new Counters(counters);
        evalAtoms2(positiveFormula, counters1, positivesSolverMap);
        evalAtoms2(negativeFormula, counters2, negativesSolverMap);
        long startTime;
        long elapsedTime;

        int requester = 0;

        numModels = 0;
        count = 0;
        result = false;
        for(FormulaAtom pa: positivesSolverMap.keySet()) {
            ISolver positiveAtomSolver = positivesSolverMap.get(pa);
            Counters counters3 = new Counters(pa.counters);
            positiveAtomSolver.setExpectedNumberOfClauses(counters3.getClauseCounter());
            ModelIterator mi = new ModelIterator(positiveAtomSolver);
            positiveAtomSolver.setTimeout(timeOut);
            IProblem problem = mi;
            if (problem != null) {
                try {
                    boolean isSat_pos;
                    do {
                        startTime = System.nanoTime();
                        isSat_pos = problem.isSatisfiable();
                        elapsedTime = System.nanoTime() - startTime;
                        long tempTime = elapsedTime;
                        modelTimes.add((double) tempTime / 1000000000.0);
                        sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);

                        if (isSat_pos) {
                            numModels++;
                            int[] model = problem.model();
                            for (int var : model) {
                                if (reqVars.values().contains(var))
                                    requester = var;
                            }
                            VecInt bc = new VecInt();
                            bc.push(-requester);

                            for (ISolver solver: positivesSolverMap.values())
                                solver.addBlockingClause(bc);

                            boolean flag = true;
                            for (FormulaAtom na : negativesSolverMap.keySet()) {
                                ISolver negativeAtomSolver = negativesSolverMap.get(na);
                                Counters counters4 = new Counters(na.counters);
                                counters3.incrClauseCntr();
                                VecInt requesterClause = new VecInt();
                                requesterClause.push(requester);
                                boolean isSat_neg = false;
                                IConstr constraint = null;
                                try {
                                    negativeAtomSolver.setExpectedNumberOfClauses(counters4.getClauseCounter());
                                    constraint = negativeAtomSolver.addClause(requesterClause);

                                    startTime = System.nanoTime();
                                    isSat_neg = negativeAtomSolver.isSatisfiable();
                                    elapsedTime = System.nanoTime() - startTime;
                                    sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);

                                } catch (ContradictionException e) {
                                    //System.out.println("Contradiciton While trying a negative atom");
                                } finally {
                                    if(constraint != null)
                                        negativeAtomSolver.removeConstr(constraint);
                                }
                                if (isSat_neg) {
                                    flag = false;
                                    break;
                                }
                            }
                            if (flag)
                                count++;
                            if (count == k) {
                                result = true;
                                break;
                            }
                        }
                    } while (isSat_pos && count < k);
                } catch (TimeoutException e) {
                    System.out.println("Timeout (enumerateModel)!");
                } catch (ContradictionException e) {
                    System.out.println("Contradicition!!!");
                }
            }
        }
        return result;
    }




    public boolean checkSat_Sat4J_ME(int k, Formula positiveFormula, Formula negativeFormula, boolean isNewReduce) throws TimeoutException{
        int count;
        //HashSet<FormulaAtom> positiveAtoms = new HashSet<FormulaAtom>();
        boolean result;
        HashMap<Integer, ISolver> negAtomIDSolversMap = new HashMap<Integer, ISolver>();
        HashMap<Integer, Counters> negAtomIDCountersMap = new HashMap<Integer, Counters>();
        Formula.graph = this.graph;
        Counters counters = new Counters();
        genReqVars(counters);
        Formula.reqVars = this.reqVars;

        Counters counters1 = new Counters(counters);
        Counters counters2 = new Counters(counters);
        if (negativeFormula != null)
            evalAtoms3(negativeFormula, counters1, negAtomIDSolversMap, negAtomIDCountersMap, isNewReduce);
        //enumerateAtoms(positiveFormula, positiveAtoms);
        ISolver positiveFormulaSolver = SolverFactory.newDefault();
        long startTime = System.nanoTime();
        try {
            positiveFormula.eval(this, counters2, positiveFormulaSolver, true, isNewReduce);
        } catch (ContradictionException e) {
            positiveFormulaSolver = null;
        }
        long elapsedTime = System.nanoTime() - startTime;
        sat4JCompileTime += ((double) elapsedTime / 1000000000.0);

        int requester = 0;
        numModels = 0;
        count = 0;
        result = false;
        boolean timeOutBefore = false;
        if (positiveFormulaSolver != null) {
            sat4JNumOfVars_Pos.add(counters2.getVarCounter());
            sat4JNumOfClauses_Pos.add(counters2.getClauseCounter());
            //positiveFormulaSolver.setKeepSolverHot(false);
            positiveFormulaSolver.setExpectedNumberOfClauses(counters2.getClauseCounter());
            IProblem problem = enumerateModels2(Formula.atomIDVars, positiveFormulaSolver);
            Set<Integer> triedRequesters = new HashSet<Integer>();
            if (problem != null) {
                try {
                    boolean isSat_pos = false;
                    do {
                        try {
                            startTime = System.nanoTime();
                            isSat_pos = problem.isSatisfiable();
                            elapsedTime = System.nanoTime() - startTime;
                            long tempTime = elapsedTime;
                            //solvingTime2.add((double) tempTime / 1000000000.0);
                            modelTimes.add((double) tempTime / 1000000000.0);
                            sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);

                            timeOutBefore = false;

                            if (isSat_pos) {
                                numModels++;
                                int[] model = problem.model();
                                for (int var : model) {
                                    if (reqVars.values().contains(var))
                                        requester = var;
                                }
                                boolean flag = true;
                                for (Integer naID : negAtomIDSolversMap.keySet()) {
                                    boolean isSat_neg = false;
                                    if (negAtomIDSolversMap.get(naID) != null) {
                                        ISolver negativeAtomSolver = negAtomIDSolversMap.get(naID);
                                        Counters counters3 = new Counters(negAtomIDCountersMap.get(naID));
                                        counters3.incrClauseCntr();
                                        VecInt requesterClause = new VecInt();
                                        requesterClause.push(requester);
                                        IConstr constraint = null;
                                        try {
                                            negativeAtomSolver.setExpectedNumberOfClauses(counters3.getClauseCounter());
                                            constraint = negativeAtomSolver.addClause(requesterClause);
                                            startTime = System.nanoTime();
                                            isSat_neg = negativeAtomSolver.isSatisfiable();
                                            elapsedTime = System.nanoTime() - startTime;
                                            sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);

                                        } catch (ContradictionException e) {
                                            //System.out.println("Contradiciton While trying a negative atom");
                                        } finally {
                                            negativeAtomSolver.removeConstr(constraint);
                                            negativeAtomSolver.clearLearntClauses();
                                        }
                                    }
                                    if (isSat_neg) {
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag)
                                    count++;
                                if (count == k) {
                                    result = true;
                                    break;
                                }
                                VecInt bc = new VecInt();
                                bc.push(-requester);
                                triedRequesters.add(requester);
                                positiveFormulaSolver.addBlockingClause(bc);
                            }
                        } catch (TimeoutException e) {
                            if (true)
                            //if (timeOutBefore)
                                throw new TimeoutException();
                            else {
                                positiveFormulaSolver.reset();
                                positiveFormulaSolver = null;
                                timeOutBefore = true;
                                positiveFormulaSolver = refreshSolver(triedRequesters, counters2, isNewReduce, positiveFormula);
                                problem = enumerateModels2(Formula.atomIDVars, positiveFormulaSolver);
                                System.out.println("# of models tried before time out: " + numModels);
                            }
                        }
                } while (timeOutBefore || (isSat_pos && count < k));
                } catch (ContradictionException e) {
                    System.out.println("Contradiction!!!");
                }
            }
        }
        //resetting the internal state of the solver
        positiveFormulaSolver.reset();
        return result;
    }

    private ISolver refreshSolver (Set<Integer> triedRequesters, Counters counters, boolean isNewReduce, Formula positiveFormula) {
        ISolver solver = SolverFactory.newDefault();
        try {
            Formula pf = positiveFormula.clone();
            pf.eval(this, counters, solver, true, isNewReduce);
        } catch (ContradictionException e) {
            System.out.println("Contradiction while adding clauses in refreshSolver!");
            solver = null;
        }
        solver.setExpectedNumberOfClauses(counters.getClauseCounter());
        for (int req: triedRequesters) {
            VecInt bc = new VecInt();
            bc.push(-req);
            try {
                solver.addBlockingClause(bc);
            } catch (ContradictionException e) {
                System.out.println("Contradiction while adding blocking clause in refreshSolver!");
            }
        }
        return solver;
    }

}
