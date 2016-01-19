package com.company;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.minisat.core.Solver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class Feasibility extends Entailment {

    public Feasibility (Graph graph, Formula formula, int distance) {
        this.graph = graph;
        this.formula = formula;
        this.distance = distance;
    }

    public Feasibility () {
    }

    protected String checkSat_QDIMACS() {
        Counters counters = new Counters();
        QBF requesterConstrts = reqsCnstrts(counters);

        Formula.graph = this.graph;
        Formula.reqVars = this.reqVars;
        QBF fQbf = formula.eval(this, counters);

        fQbf = nnf(fQbf);
        fQbf = prenex(fQbf);

        counters.setClauseCounter(0);
        //QBF.clauseCounter = 0;
        //fQbf = cnf(null, fQbf);
        fQbf = nnf(fQbf);

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

    protected String checkSat_QCIR() {

        Counters counters = new Counters();
        QBF deltaQBF = genDeltaMatrixQBF(counters);
        QBF xorQBF = genXORQBF(counters);
        QBF requesterConstrts = reqsCnstrts(counters);

        Formula.reqVars = this.reqVars;
        Formula.graph = this.graph;
        Formula.mutatedGraphVars = this.mutatedGraphVars;

        long startTime = System.nanoTime();
        QBF fQbf = formula.eval(this, counters);
        ArrayList<QBF> andArgs = new ArrayList<QBF>();
        andArgs.add(deltaQBF);
        andArgs.add(xorQBF);
        andArgs.add(requesterConstrts);
        andArgs.add(fQbf);
        QBF temp = new QBFAnd(andArgs);

        //Quantifying temp with deltaMatrix variables
        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                for (int j = 0; j < graph.vertices.size(); j++) {
                    temp = new QBFExists(temp, deltaMatrix[l][i][j]);
                }
            }
        }

        //Quantifying temp with map Variables
        for (int i = 0; i < graph.labels.size() * graph.vertices.size() * graph.vertices.size(); i++) {
            for (int j = 0; j < distance; j++) {
                temp = new QBFExists(temp, mapVars[i][j]);
            }
        }

        //Quantifying temp with mutatedGraph variables
        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                for (int j = 0; j < graph.vertices.size(); j++) {
                    temp = new QBFExists(temp, mutatedGraphVars[l][i][j]);
                }
            }
        }

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


    protected boolean checkSat_Sat4J() {
        Counters counters = new Counters();
        QBF formulaQBF = formula.formulaEval(counters);
        StringBuilder stringBuilder = new StringBuilder();
        counters.incrClauseCntr();
        String formulaDIMACS = formulaQBF.evalQDIMACS(stringBuilder).toString();
        String str = String.format("p cnf %d %d%s%s 0", counters.getVarCounter(), counters.getClauseCounter(), System.lineSeparator(), formulaDIMACS);
        ISolver solver = SolverFactory.newDefault();
        IProblem problem = enumerateModels(str, solver);

        //compute small phi and psi
        Counters counters1 = new Counters();
        //QBF requesterConstrts = reqsCnstrts(counters1);
        genReqVars(counters1);
        Counters counters2 = new Counters();
        counters2.setVarCounter(counters1.getVarCounter());
        QBF deltaQBF = genDeltaMatrixQBF(counters1);
        QBF xorQBF = genXORQBF(counters1);

        ArrayList<QBF> andArgs = new ArrayList<QBF>();
        //andArgs.add(requesterConstrts);
        andArgs.add(deltaQBF);
        andArgs.add(xorQBF);
        Counters tempCounters = new Counters(counters1);

        long startTime = System.nanoTime();

        if (problem != null) {
            try {
                while (problem.isSatisfiable()) {
                    counters1.setClauseCounter(tempCounters.getClauseCounter());
                    counters1.setVarCounter(tempCounters.getVarCounter());
                    //QBF.varCounter = tempVarCounter;
                    //QBF.clauseCounter = tempClauseCounter;
                    ArrayList<QBF> andArgs2 = new ArrayList<QBF>();
                    andArgs2.addAll(andArgs);
                    QBF tempQbf = new QBFAnd(andArgs2);
                    int[] model = problem.model();
                    Set<FormulaAtom> positive = new HashSet<FormulaAtom>();
                    Set<FormulaAtom> negative = new HashSet<FormulaAtom>();

                    for (int aModel : model) {
                        if (Formula.atomVarsMap.containsValue(Math.abs(aModel))) {
                            if (aModel > 0) {
                                positive.add(Formula.atomVarsMap.inverse().get(aModel));
                            } else {
                                negative.add(Formula.atomVarsMap.inverse().get(Math.abs(aModel)));
                            }
                        }
                    }

                    Formula positiveFormula = null;

                    for (FormulaAtom fa : positive) {
                        if (positiveFormula == null) {
                            positiveFormula = new FormulaAtom(fa.graphPattern, fa.anchor);
                        } else {
                            positiveFormula = new FormulaAnd(positiveFormula, new FormulaAtom(fa.graphPattern, fa.anchor));
                        }
                    }

                    if (positiveFormula != null) {
                        Formula.graph = this.graph;
                        Formula.mutatedGraphVars = this.mutatedGraphVars;
                        Formula.reqVars = this.reqVars;
                        QBF fQbf = positiveFormula.eval(new Feasibility(), counters1);
                        andArgs2.add(fQbf);
                        tempQbf = new QBFAnd(andArgs2);
                    }

                    String tempStr = String.format("p cnf %d %d%s%s 0", counters1.getVarCounter(), counters1.getClauseCounter(), System.lineSeparator(), tempQbf.evalQDIMACS(new StringBuilder()).toString());
                    //String tempStr = String.format("p cnf %d %d%s%s 0", QBF.varCounter, QBF.clauseCounter, System.lineSeparator(), tempQbf.evalQDIMACS(new StringBuilder()).toString());
                    ISolver tempSolver = SolverFactory.newDefault();
                    IProblem tempProblem = enumerateModels(tempStr, tempSolver);
                    if (tempProblem != null) {
                        try {
                            while (tempProblem.isSatisfiable()) {
                                boolean flag = true;
                                int[] tempModel = tempProblem.model();
                                Set<Integer> modelVars = new HashSet<Integer>();
                                int requester = 0;
                                for (int aTempModel : tempModel) {
                                    modelVars.add(aTempModel);
                                }
                                for (int r: reqVars.values()) {
                                    if (modelVars.contains(r))
                                        requester = r;
                                }
                                Graph g = new Graph(this.graph.vertices, this.graph.labels);
                                for (int l = 0; l < graph.labels.size(); l++) {
                                    for (int i = 0; i < graph.vertices.size(); i++) {
                                        for (int j = 0; j < graph.vertices.size(); j++) {
                                            if (modelVars.contains(mutatedGraphVars[l][i][j]))
                                                g.addEdge(l, i, j);
                                        }
                                    }
                                }

                                VecInt bc = new VecInt();
                                bc.push(-requester);
                                for (int i : tempModel) {
                                    if(deltaVars.contains(Math.abs(i)))
                                        bc.push(-i);
                                }
                                tempSolver.addBlockingClause(bc);

                                for (FormulaAtom fa : negative) {
                                    FormulaAtom formulaAtom = new FormulaAtom(fa.graphPattern, fa.anchor);
                                    //QBF.varCounter = this.varCounter;
                                    //QBF.clauseCounter = 0;
                                    counters2.setClauseCounter(0);
                                    Formula.reqVars = this.reqVars;
                                    Formula.graph = new Graph(g);
                                    QBF qbf = formulaAtom.eval(new Satisfiability(), counters2);
                                    counters2.incrClauseCntr();
                                    String qbfStr = String.format("p cnf %d %d%s%s%d 0", counters2.getVarCounter(), counters2.getClauseCounter(), System.lineSeparator(), qbf.evalQDIMACS(new StringBuilder()), requester);
                                    //String qbfStr = String.format("p cnf %d %d%s%s 0", QBF.varCounter, QBF.clauseCounter, System.lineSeparator(), qbf.evalQDIMACS(new StringBuilder()));

                                    if (isSat(qbfStr)) {
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    long elapsedTime = System.nanoTime() - startTime;
                                    sat4JTotalSolverTime = (double) elapsedTime / 1000000000.0;
                                    return true;
                                }
                            }
                        } catch (TimeoutException e) {
                            System.out.println("Timeout, sorry!");
                        } catch (ContradictionException e) {
                            System.out.println("Contradiction!!!");
                        }
                    }
                    VecInt bc = new VecInt();
                    for (int i : model) {
                        bc.push(-i);
                    }
                    solver.addBlockingClause(bc);
                }
            } catch (TimeoutException e) {
                System.out.println("Timeout, sorry!");
            } catch (ContradictionException e) {
                System.out.println("Contradiction!!!");
            }

        }
        long elapsedTime = System.nanoTime() - startTime;
        sat4JTotalSolverTime = (double) elapsedTime / 1000000000.0;
        return false;
    }

}
