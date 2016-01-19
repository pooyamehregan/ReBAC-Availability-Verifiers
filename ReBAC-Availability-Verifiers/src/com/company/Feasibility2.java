package com.company;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.*;

import java.util.*;

/**
 * Created by Pooya on 15-02-24.
 */
public class Feasibility2 extends Entailment {

    public Feasibility2 (Graph graph, Formula formula, int distance) {
        this.graph = graph;
        this.formula = formula;
        this.distance = distance;
    }

    public Feasibility2 () {
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

        long startTime = System.nanoTime();
        String formulaDIMACS = formulaQBF.evalQDIMACS(stringBuilder).toString();
        long elapsedTime = System.nanoTime() - startTime;
        sat4JCompileTime += ((double) elapsedTime / 1000000000.0);

        String str = String.format("p cnf %d %d%s%s 0", counters.getVarCounter(), counters.getClauseCounter(), System.lineSeparator(), formulaDIMACS);
        ISolver solver = SolverFactory.newDefault();
        IProblem problem = enumerateModels(str, solver);

        Counters counters1 = new Counters();
        genReqVars(counters1);
        Counters counters2 = new Counters(counters1);

        genDeltaMatrixVars(counters1);
        QBF xorQBF = genXORQBF(counters1);

        Counters tempCounters = new Counters(counters1);

        if (problem != null) {
            try {
                boolean isSat_for;
                do {

                    startTime = System.nanoTime();
                    isSat_for = problem.isSatisfiable();
                    elapsedTime = System.nanoTime() - startTime;
                    sat4JTotalSolverTime += (((double) elapsedTime / 1000000000.0));

                    if (isSat_for) {
                        counters1.setClauseCounter(tempCounters.getClauseCounter());
                        counters1.setVarCounter(tempCounters.getVarCounter());
                        ArrayList<QBF> andArgs = new ArrayList<QBF>();
                        andArgs.add(xorQBF);

                        QBF tempQbf = new QBFAnd(andArgs);

                        int[] model = problem.model();
                        HashSet<FormulaAtom> positiveAtoms = new HashSet<FormulaAtom>();
                        HashSet<FormulaAtom> negativeAtoms = new HashSet<FormulaAtom>();

                        for (int var : model) {
                            if (Formula.atomVarsMap.containsValue(Math.abs(var))) { //If the input formula is not in CNF we need to check whether a variable is an atom?
                                if (var > 0) {
                                    positiveAtoms.add(Formula.atomVarsMap.inverse().get(var));
                                } else {
                                    negativeAtoms.add(Formula.atomVarsMap.inverse().get(Math.abs(var)));
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
                            Formula.atomQBFsMap.clear();
                            QBF fQbf = positiveFormula.eval(new Feasibility2(), counters1);
                            andArgs.add(fQbf);
                            tempQbf = new QBFAnd(andArgs);
                        }

                        startTime = System.nanoTime();
                        String tempStr = String.format("p cnf %d %d%s%s 0", counters1.getVarCounter(), counters1.getClauseCounter(), System.lineSeparator(), tempQbf.evalQDIMACS(new StringBuilder()).toString());
                        elapsedTime = System.nanoTime() - startTime;
                        sat4JCompileTime += ((double) elapsedTime / 1000000000.0);

                        ISolver tempSolver = SolverFactory.newDefault();
                        IProblem tempProblem = enumerateModels(tempStr, positiveAtoms, deltaVars, distance, tempSolver);
                        if (tempProblem != null) {
                            try {
                                boolean isSat_pos;
                                do {

                                    startTime = System.nanoTime();
                                    isSat_pos = tempProblem.isSatisfiable();
                                    elapsedTime = System.nanoTime() - startTime;
                                    sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);

                                    if (isSat_pos) {
                                        boolean flag = true;
                                        int[] tempModel = tempProblem.model();
                                        Set<Integer> modelVars = new HashSet<Integer>();
                                        int requester = 0;
                                        for (int var : tempModel) {
                                            modelVars.add(var);
                                        }
                                        for (int r : reqVars.values()) {
                                            if (modelVars.contains(r)) {
                                                requester = r;
                                                break;
                                            }
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
                                        for (int var : tempModel) {
                                            if (deltaVars.contains(Math.abs(var)))
                                                bc.push(-var);
                                        }
                                        tempSolver.addBlockingClause(bc);

                                        for (FormulaAtom fa : negativeAtoms) {
                                            counters2.setClauseCounter(0);
                                            Formula.reqVars = this.reqVars;
                                            Formula.graph = new Graph(g);
                                            FormulaAtom formulaAtom = new FormulaAtom(fa.graphPattern, fa.anchor);
                                            QBF qbf = formulaAtom.eval(new Satisfiability2(), counters2);
                                            counters2.incrClauseCntr();

                                            startTime = System.nanoTime();
                                            String qbfStr = String.format("p cnf %d %d%s%s%d 0", counters2.getVarCounter(), counters2.getClauseCounter(), System.lineSeparator(), qbf.evalQDIMACS(new StringBuilder()), requester);
                                            elapsedTime = System.nanoTime() - startTime;
                                            sat4JCompileTime += ((double) elapsedTime / 1000000000.0);

                                            boolean isSat_neg = isSat(qbfStr, formulaAtom);

                                            if (isSat_neg) {
                                                flag = false;
                                                break;
                                            }
                                        }
                                        if (flag) {
                                            return true;
                                        }
                                    }
                                } while (isSat_pos);
                            } catch (TimeoutException e) {
                                System.out.println("Timeout 1");
                            } catch (ContradictionException e) {
                                System.out.println("Contradiction 1");
                            }
                        }
                    }
                } while (isSat_for);
            } catch (TimeoutException e) {
                System.out.println("Timeout 2");
            }
        }
        return false;
    }

    public boolean checkSat_Sat4J(Formula positiveFormula, Formula negativeFormula) {
        HashSet<FormulaAtom> positiveAtoms = new HashSet<FormulaAtom>();
        HashSet<FormulaAtom> negativeAtoms = new HashSet<FormulaAtom>();
        Formula.graph = this.graph;
        Counters counters1 = new Counters();

        genReqVars(counters1);
        Counters counters2 = new Counters(counters1);
        Formula.reqVars = this.reqVars;

        genDeltaMatrixVars(counters1);
        QBF xorQBF = genXORQBF(counters1);
        Formula.mutatedGraphVars = this.mutatedGraphVars;
        Formula.atomQBFsMap.clear();

        ArrayList<QBF> andArgs = new ArrayList<QBF>();
        andArgs.add(xorQBF);

        if (negativeFormula != null)
            enumerateAtoms(negativeFormula, negativeAtoms);
        enumerateAtoms(positiveFormula, positiveAtoms);
        QBF fQbf = positiveFormula.eval_CNF(this, counters1);
        andArgs.add(fQbf);
        QBF tempQbf = new QBFAnd(andArgs);

        ISolver positiveFormulaSolver = SolverFactory.newDefault();
        long startTime;
        long elapsedTime;
        try {
            startTime = System.nanoTime();
            tempQbf.evalDIMACS(positiveFormulaSolver);
            elapsedTime = System.nanoTime() - startTime;
            sat4JCompileTime += ((double) elapsedTime / 1000000000.0);
        }
        catch (ContradictionException e) {
            System.out.println("Contradiction while generating positive formula");
        }

        numModels = 0;
        positiveFormulaSolver.setExpectedNumberOfClauses(counters1.getClauseCounter());
        IProblem problem = enumerateModels(positiveAtoms, deltaVars, distance, positiveFormulaSolver);

        if (problem != null) {
            try {
                boolean isSat_pos;
                do {
                    startTime = System.nanoTime();
                    numModels++;
                    isSat_pos = problem.isSatisfiable();
                    //System.out.println("# of positive atoms models tried: " + numModels);
                    elapsedTime = System.nanoTime() - startTime;
                    sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);

                    if(isSat_pos) {
                        boolean flag = true;
                        int[] tempModel = problem.model();
                        Set<Integer> modelVars = new HashSet<Integer>();
                        int requester = 0;
                        for (int var : tempModel) {
                            modelVars.add(var);
                        }
                        for (int r : reqVars.values()) {
                            if (modelVars.contains(r)) {
                                requester = r;
                                break;
                            }
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
                        for (int var : tempModel) {
                            if (deltaVars.contains(Math.abs(var)))
                                bc.push(-var);
                        }
                        positiveFormulaSolver.addBlockingClause(bc);

                        for (FormulaAtom fa : negativeAtoms) {
                            counters2.setClauseCounter(0);
                            Formula.reqVars = this.reqVars;
                            Formula.graph = new Graph(g);
                            FormulaAtom formulaAtom = new FormulaAtom(fa.graphPattern, fa.anchor);
                            QBF qbf = formulaAtom.eval(new Satisfiability2(), counters2);
                            ISolver negativeAtomSolver = addConstraints(formulaAtom, qbf, requester, counters2);

                            startTime = System.nanoTime();
                            boolean isSat_neg = negativeAtomSolver.isSatisfiable();
                            elapsedTime = System.nanoTime() - startTime;
                            sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);

                            if (isSat_neg) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            return true;
                        }
                    }
                } while (isSat_pos);
            } catch (TimeoutException e) {
                System.out.println("Timeout 3!");
            } catch (ContradictionException e) {
                System.out.println("Contradiction 2");
            }
        }
        return false;
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
            if (result) {
                System.out.println(" Feasible; # of Graphs Tried: " + c);
                return true;
            }
        }
        System.out.println(" Not Feasible; # of Graphs Tried: " + c);
        return false;
    }

}
