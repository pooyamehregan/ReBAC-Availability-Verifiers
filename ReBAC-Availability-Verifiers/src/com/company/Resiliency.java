package com.company;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Resiliency extends Entailment {

    //Constructors
    public Resiliency (Graph graph, Formula formula, int distance) {
        this.graph = graph;
        this.formula = formula;
        this.distance = distance;
    }

    public Resiliency () {

    }

    protected String checkSat_QCIR() {
        Counters counters = new Counters();
        QBF deltaQBF = genDeltaMatrixQBF(counters);
        QBF xorQBF = genXORQBF(counters);
        QBF requesterConstrts = reqsCnstrts(counters);

        Formula.graph = this.graph;
        Formula.reqVars = this.reqVars;
        Formula.mutatedGraphVars = this.mutatedGraphVars;

        long startTime = System.nanoTime();
        QBF fQbf = formula.eval(this, counters);
        ArrayList<QBF> andArgs = new ArrayList<QBF>();
        andArgs.add(requesterConstrts);
        andArgs.add(deltaQBF);
        andArgs.add(xorQBF);
        andArgs.add(fQbf);
        QBF temp = new QBFAnd(andArgs);

        //Quantifying temp with requester variables (v)
        for (int i : reqVars.values()) {
            temp = new QBFExists(temp, i);
        }

        //Quantifying temp with deltaMatrix variables
        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                for (int j = 0; j < graph.vertices.size(); j++) {
                    temp = new QBFForAll(temp, deltaMatrix[l][i][j]);
                }
            }
        }

        //Quantifying temp with map Variables
        for (int i = 0; i < graph.labels.size() * graph.vertices.size() * graph.vertices.size(); i++) {
            for (int j = 0; j < distance; j++) {
                temp = new QBFForAll(temp, mapVars[i][j]);
            }
        }

        //Quantifying temp with mutatedGraph variables
        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                for (int j = 0; j < graph.vertices.size(); j++) {
                    temp = new QBFForAll(temp, mutatedGraphVars[l][i][j]);
                }
            }
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
        boolean result = true;
        long startTime = System.nanoTime();
        for (int v: graph.vertices) {
            boolean flag1 = false;
            Counters counters = new Counters();
            QBF formulaQBF = new FormulaNot(formula).formulaEval(counters);
            StringBuilder stringBuilder = new StringBuilder();
            counters.incrClauseCntr();
            String formulaDIMACS = formulaQBF.evalQDIMACS(stringBuilder).toString();
            String str = String.format("p cnf %d %d%s%s 0", counters.getVarCounter(), counters.getClauseCounter(), System.lineSeparator(), formulaDIMACS);
            ISolver solver = SolverFactory.newDefault();
            IProblem problem = enumerateModels(str, solver);

            Counters counters1 = new Counters();
            QBF requesterConstrts = reqsCnstrts(counters1);
            Counters counters2 = new Counters();
            counters2.setVarCounter(counters1.getVarCounter());

            genDeltaMatrixVars(counters1);
            QBF xorQBF = genXORQBF(counters1);

            ArrayList<QBF> andArgs = new ArrayList<QBF>();
            andArgs.add(requesterConstrts);
            andArgs.add(xorQBF);

            Counters tempCounters = new Counters(counters1);
            if (problem != null) {
                try {
                    while (problem.isSatisfiable()) {
                        counters1.setClauseCounter(tempCounters.getClauseCounter());
                        counters1.setVarCounter(tempCounters.getVarCounter());
                        ArrayList<QBF> andArgs2 = new ArrayList<QBF>();
                        andArgs2.addAll(andArgs);
                        QBF tempQbf = new QBFAnd(andArgs2);

                        int[] model = problem.model();
                        HashSet<FormulaAtom> positiveAtoms = new HashSet<FormulaAtom>();
                        HashSet<FormulaAtom> negativeAtoms = new HashSet<FormulaAtom>();

                        for (int aModel : model) {
                            if (Formula.atomVarsMap.containsValue(Math.abs(aModel))) {
                                if (aModel > 0) {
                                    positiveAtoms.add(Formula.atomVarsMap.inverse().get(aModel));
                                } else {
                                    negativeAtoms.add(Formula.atomVarsMap.inverse().get(Math.abs(aModel)));
                                }
                            }
                        }

                        Formula positiveFormula = null;

                        HashSet<FormulaAtom> tempPositiveAtoms = new HashSet<FormulaAtom>();

                        for (FormulaAtom fa : positiveAtoms) {
                            FormulaAtom positiveAtom = new FormulaAtom(fa.graphPattern, fa.anchor);
                            if (positiveFormula == null) {
                                positiveFormula = positiveAtom;
                            } else {
                                positiveFormula = new FormulaAnd(positiveFormula, positiveAtom);
                            }
                            tempPositiveAtoms.add(positiveAtom);
                        }
                        positiveAtoms = tempPositiveAtoms;

                        if (positiveFormula != null) {
                            Formula.graph = this.graph;
                            Formula.mutatedGraphVars = this.mutatedGraphVars;
                            Formula.reqVars = this.reqVars;
                            QBF fQbf = positiveFormula.eval(new Resiliency(), counters1);
                            andArgs2.add(fQbf);
                            tempQbf = new QBFAnd(andArgs2);
                        }
                        counters1.incrClauseCntr();
                        String tempStr = String.format("p cnf %d %d%s%s%d 0", counters1.getVarCounter(), counters1.getClauseCounter(), System.lineSeparator(), tempQbf.evalQDIMACS(new StringBuilder()).toString(), reqVars.get(v));
                        ISolver tempSolver = SolverFactory.newDefault();
                        IProblem tempProblem = enumerateModels(tempStr, positiveAtoms, deltaVars, distance, tempSolver);
                        if (tempProblem != null) {
                            try {
                                while (tempProblem.isSatisfiable()) {
                                    boolean flag2 = true;
                                    int[] tempModel = tempProblem.model();
                                    Set<Integer> modelVars = new HashSet<Integer>();
                                    for (int aTempModel : tempModel) {
                                        modelVars.add(aTempModel);
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
                                    for (FormulaAtom fa : negativeAtoms) {
                                        FormulaAtom formulaAtom = new FormulaAtom(fa.graphPattern, fa.anchor);
                                        counters2.setClauseCounter(0);
                                        Formula.reqVars = this.reqVars;
                                        Formula.graph = new Graph(g);

                                        QBF qbf = formulaAtom.eval(new Satisfiability2(), counters2);
                                        counters2.incrClauseCntr();
                                        String qbfStr = String.format("p cnf %d %d%s%s%d 0", counters2.getVarCounter(), counters2.getClauseCounter(), System.lineSeparator(), qbf.evalQDIMACS(new StringBuilder()), reqVars.get(v));
                                        VecInt bc = new VecInt();
                                        //bc.push(requester);
                                        for (int i : tempModel) {
                                            if(deltaVars.contains(Math.abs(i)))
                                                bc.push(-i);
                                        }
                                        tempSolver.addBlockingClause(bc);

                                        if (isSat(qbfStr, formulaAtom)) {
                                            flag2 = false;
                                            break;
                                        }
                                    }
                                    if (flag2) {
                                        flag1 = true;
                                        break;
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
                            bc.push(i);
                        }
                        solver.addBlockingClause(bc);
                    }
                } catch (TimeoutException e) {
                    System.out.println("Timeout, sorry!");
                } catch (ContradictionException e) {
                    System.out.println("Contradiction!!!");
                }
            }
            if (!flag1) {
                result = false;
                break;
            }
        }
        long elapsedTime = System.nanoTime() - startTime;
        sat4JTotalSolverTime = (double) elapsedTime / 1000000000.0;
        return !result;
    }

    public boolean checkSat_Sat4J_ME(int d, int k, Formula positiveFormula, Formula negativeFormula, boolean isNewReduce) throws TimeoutException {
        boolean result;
        HashSet<Integer> anchors = new HashSet<Integer>();
        getAnchors(new FormulaAnd(positiveFormula, negativeFormula), anchors);
        HashMap<Integer, Edge> idxEdgeMap = new HashMap<Integer, Edge>();
        int counter = 1;
        for (int l: graph.labels) {
            for (int i: anchors) {
                for (int j: anchors) {
                    idxEdgeMap.put(counter, new Edge(l, i, j));
                    counter++;
                }
            }
        }
        Set<Set<Integer>> subsets = getSubsets(new ArrayList<Integer>(idxEdgeMap.keySet()), d);
        int c = 0;
        for (Set<Integer> subset: subsets) {
            Graph g = new Graph(graph);
            for (Integer key: subset) {
                Edge edge = idxEdgeMap.get(key);
                if (g.outEdges[edge.l][edge.i].contains(edge.j)) {
                    g.removeEdge(edge.l, edge.i, edge.j);
                } else {
                    g.addEdge(edge.l, edge.i, edge.j);
                }
            }
            Formula pos = positiveFormula.clone();
            Formula neg = negativeFormula.clone();
            Satisfiability2 sat = new Satisfiability2(g, new FormulaAnd(pos, neg));
            result = sat.checkSat_Sat4J_ME(k, pos, neg, isNewReduce);
            sat4JCompileTime += sat.sat4JCompileTime;
            sat4JTotalSolverTime += sat.sat4JTotalSolverTime;
            c++;
            if (!result) {
                System.out.println(" Not Resilient; # of Graphs Tried: " + c);
                return false;
            }
        }
        System.out.println(" Resilient; # of Graphs Tried: " + c);
        return true;
    }
}
