package com.company;

import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FormulaAtom extends Formula {

    //Attributes
    protected BirootedGraph graphPattern;
    protected Integer anchor;
    protected Integer [][] isoVars;
    protected Set<Integer> quantVars;
    protected ArrayList<QBF> clauses;
    protected QBF qbf;
    protected Counters counters;
    protected int atomID;

    //Constructors
    public FormulaAtom(BirootedGraph graphPattern, Integer anchor) {
        this.graphPattern = graphPattern;
        this.anchor = anchor;
        quantVars = new HashSet<Integer>();
        clauses = new ArrayList<QBF>();
    }

    public FormulaAtom(BirootedGraph graphPattern, Integer anchor, Integer atomID) {
        this.graphPattern = graphPattern;
        this.anchor = anchor;
        quantVars = new HashSet<Integer>();
        clauses = new ArrayList<QBF>();
        this.atomID = atomID;
    }

    public FormulaAtom(FormulaAtom formulaAtom) {
        this.graphPattern = new BirootedGraph(formulaAtom.graphPattern, formulaAtom.graphPattern.anchor, formulaAtom.graphPattern.requester);
        this.anchor = formulaAtom.anchor;
        quantVars = new HashSet<Integer>();
        clauses = new ArrayList<QBF>();
        this.atomID = formulaAtom.atomID;
    }
    //Generates isomorphism variables
    //Used in Satisfiability only
    protected void genIsoVars(Counters counters) {
        for (int i : graphPattern.vertices) {
            for (int j : graph.vertices) {
                counters.incrVarCntr();
                if (i == graphPattern.anchor && j == this.anchor) {
                    counters.incrClauseCntr();
                    clauses.add(new QBFLiteral(counters.getVarCounter()));
                }
                quantVars.add(counters.getVarCounter());
                isoVars[i][j] = counters.getVarCounter();
                }
            }
    }

    //Used in Satisfiability2 only
    protected void genIsoVars2(Counters counters) {
        for (int i : graphPattern.vertices) {
            HERE:
            for (int j : graph.vertices) {
                //Checking the degrees of vertices correspondence
                for (int l = 0; l < Graph.labelSetSize; l++) {
                    if (graphPattern.outEdges[l][i].size()
                            > graph.outEdges[l][j].size() ||
                            graphPattern.inEdges[l][i].size()
                                    > graph.inEdges[l][j].size()) {
                        isoVars[i][j] = -1;
                        continue HERE;
                    }
                }
                counters.incrVarCntr();
                if (i == graphPattern.anchor && j == this.anchor) {
                    counters.incrClauseCntr();
                    clauses.add(new QBFLiteral(counters.getVarCounter()));
                }
                quantVars.add(counters.getVarCounter());
                isoVars[i][j] = counters.getVarCounter();
            }
        }
        atomIsoVarsMap.put(this, isoVars);
    }

    protected void genIsoVars2(Counters counters, int atomVar) {
        for (int i : graphPattern.vertices) {
            HERE:
            for (int j : graph.vertices) {
                //Checking the degrees of vertices correspondence
                for (int l = 0; l < Graph.labelSetSize; l++) {
                    if (graphPattern.outEdges[l][i].size()
                            > graph.outEdges[l][j].size() ||
                            graphPattern.inEdges[l][i].size()
                                    > graph.inEdges[l][j].size()) {
                        isoVars[i][j] = -1;
                        continue HERE;
                    }
                }
                counters.incrVarCntr();
                if (i == graphPattern.anchor && j == this.anchor) {
                    counters.incrClauseCntr();
                    ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                    clauseArgs.add(new QBFLiteral(counters.getVarCounter()));
                    clauseArgs.add(new QBFNot(new QBFLiteral(atomVar)));
                    clauses.add(new QBFOr(clauseArgs));
                }
                quantVars.add(counters.getVarCounter());
                isoVars[i][j] = counters.getVarCounter();
            }
        }
        atomIsoVarsMap.put(this, isoVars);
    }

    //Used in Feasibility and Resiliency only
    protected void genIsoVarsGeneral(Counters counters) {
        for (int i = 0; i < graphPattern.vertices.size(); i++) {
            for (int j = 0; j < graph.vertices.size(); j++) {
                counters.incrVarCntr();
                if (i == graphPattern.anchor && j == this.anchor) {
                    counters.incrClauseCntr();
                    clauses.add(new QBFLiteral(counters.getVarCounter()));
                }
                quantVars.add(counters.getVarCounter());
                isoVars[i][j] = counters.getVarCounter();
            }
        }
    }


    protected void genIsoVarsGeneral(Counters counters, int atomVar) {
        for (int i = 0; i < graphPattern.vertices.size(); i++) {
            for (int j = 0; j < graph.vertices.size(); j++) {
                counters.incrVarCntr();
                if (i == graphPattern.anchor && j == this.anchor) {
                    counters.incrClauseCntr();
                    ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                    clauseArgs.add(new QBFLiteral(counters.getVarCounter()));
                    clauseArgs.add(new QBFNot(new QBFLiteral(atomVar)));
                    clauses.add(new QBFOr(clauseArgs));
                }
                quantVars.add(counters.getVarCounter());
                isoVars[i][j] = counters.getVarCounter();
            }
        }
    }

    protected void genIsoVarsGeneral(Counters counters, int atomVar, ISolver solver, boolean isCNF) {
        for (int i = 0; i < graphPattern.vertices.size(); i++) {
            for (int j = 0; j < graph.vertices.size(); j++) {
                counters.incrVarCntr();
                try {
                        if (i == graphPattern.anchor && j == this.anchor) {
                            counters.incrClauseCntr();
                            VecInt vec1 = new VecInt();
                            vec1.push(counters.getVarCounter());
                            if (isCNF)
                                vec1.push(-atomVar);
                            solver.addClause(vec1);
                        }
                } catch (ContradictionException e) {
                    System.out.println("Contradiction while translating atom (genIsoVarsGeneral)");
                }
                isoVars[i][j] = counters.getVarCounter();
            }
        }
    }
    //Used in Feasibility2 and Resiliency2 only
    protected void genIsoVarsGeneral2(Counters counters, int atomVar) {
        for (int i = 0; i < graphPattern.vertices.size(); i++) {
            for (int j = 0; j < graph.vertices.size(); j++) {
                counters.incrVarCntr();
                if (i == graphPattern.anchor && j == this.anchor) {
                    counters.incrClauseCntr();
                    ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                    clauseArgs.add(new QBFLiteral(counters.getVarCounter()));
                    clauseArgs.add(new QBFNot(new QBFLiteral(atomVar)));
                    clauses.add(new QBFOr(clauseArgs));
                }
                quantVars.add(counters.getVarCounter());
                isoVars[i][j] = counters.getVarCounter();
            }
        }
        atomIsoVarsMap.put(this, isoVars);
    }

    protected void genIsoVarsGeneral2(Counters counters) {
        for (int i = 0; i < graphPattern.vertices.size(); i++) {
            for (int j = 0; j < graph.vertices.size(); j++) {
                counters.incrVarCntr();
                if (i == graphPattern.anchor && j == this.anchor) {
                    counters.incrClauseCntr();
                    clauses.add(new QBFLiteral(counters.getVarCounter()));
                }
                quantVars.add(counters.getVarCounter());
                isoVars[i][j] = counters.getVarCounter();
            }
        }
        atomIsoVarsMap.put(this, isoVars);
    }

    //Generates the bijection constraints from graphPattern to graph
    protected void genBijectConstrs(Counters counters) {
        //Function constraints
        for (int i = 0; i < graphPattern.vertices.size(); i++) {
            for (int j = 0; j < graph.vertices.size(); j++) {
                for (int k = j + 1; k < graph.vertices.size(); k++) {
                    if (isoVars[i][j] > 0 && isoVars[i][k] > 0) {
                        counters.incrClauseCntr();
                        ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                        clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[i][j])));
                        clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[i][k])));
                        clauses.add(new QBFOr(clauseArgs));
                    }
                }
            }
        }

        //One-to-one constraints
        for (int i = 0; i < graph.vertices.size(); i++) {
            for (int j = 0; j < graphPattern.vertices.size(); j++) {
                for (int k = j + 1; k < graphPattern.vertices.size(); k++) {
                    if (isoVars[j][i] > 0 && isoVars[k][i] > 0) {
                        counters.incrClauseCntr();
                        ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                        clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[j][i])));
                        clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[k][i])));
                        clauses.add(new QBFOr(clauseArgs));
                    }
                }
            }
        }

        //All vertices in graph pattern must be mapped
        for (int i = 0; i < graphPattern.vertices.size(); i++) {
            ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
            for (int j = 0; j < graph.vertices.size(); j++) {
                if (isoVars[i][j] > 0)
                    clauseArgs.add(new QBFLiteral(isoVars[i][j]));
            }
            if (clauseArgs.size() > 0) {
                if (clauseArgs.size() == 1) {
                    counters.incrClauseCntr();
                    clauses.add(clauseArgs.get(0));
                }
                else {
                    counters.incrClauseCntr();
                    clauses.add(new QBFOr(clauseArgs));
                }
            }
            else {
                //Add a contradicting clause
                clauses.add(new QBFLiteral(1));
                clauses.add(new QBFNot(new QBFLiteral(1)));
            }
        }
    }

    //Requester vars with their corresponding isomorphism vars in the atom must match
    protected void genReqMatchConstrs(Counters counters) {
        for (int j = 0; j < graph.vertices.size(); j++) {
            if (isoVars[graphPattern.requester][j] > 0) {
                counters.incrClauseCntr();
                ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                clauseArgs.add(new QBFNot(new QBFLiteral(reqVars.get(j))));
                clauseArgs.add(new QBFLiteral(isoVars[graphPattern.requester][j]));
                clauses.add(new QBFOr(clauseArgs));

                counters.incrClauseCntr();
                ArrayList<QBF> clauseArgs2 = new ArrayList<QBF>();
                clauseArgs2.add(new QBFLiteral(reqVars.get(j)));
                clauseArgs2.add(new QBFNot(new QBFLiteral(isoVars[graphPattern.requester][j])));
                clauses.add(new QBFOr(clauseArgs2));
            }
            else {
                counters.incrClauseCntr();
                clauses.add(new QBFNot(new QBFLiteral(reqVars.get(j))));
            }
        }

    }

    protected void genReqMatchConstrs(Counters counters, int atomVar) {
        for (int j = 0; j < graph.vertices.size(); j++) {
            if (isoVars[graphPattern.requester][j] > 0) {
                counters.incrClauseCntr();
                ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                clauseArgs.add(new QBFNot(new QBFLiteral(reqVars.get(j))));
                clauseArgs.add(new QBFLiteral(isoVars[graphPattern.requester][j]));
                clauseArgs.add(new QBFNot(new QBFLiteral(atomVar)));
                clauses.add(new QBFOr(clauseArgs));

                counters.incrClauseCntr();
                ArrayList<QBF> clauseArgs2 = new ArrayList<QBF>();
                clauseArgs2.add(new QBFLiteral(reqVars.get(j)));
                clauseArgs2.add(new QBFNot(new QBFLiteral(isoVars[graphPattern.requester][j])));
                clauseArgs2.add(new QBFNot(new QBFLiteral(atomVar)));
                clauses.add(new QBFOr(clauseArgs2));
            }
            else {
                counters.incrClauseCntr();
                ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                clauseArgs.add(new QBFNot(new QBFLiteral(reqVars.get(j))));
                clauseArgs.add(new QBFNot(new QBFLiteral(atomVar)));
                clauses.add(new QBFOr(clauseArgs));
            }
        }

    }

    protected void genReqMatchConstrs(Counters counters, int atomVar, ISolver solver, boolean isCNF) {
        for (int j = 0; j < graph.vertices.size(); j++) {
            try {
                if (isoVars[graphPattern.requester][j] > 0) {
                    counters.incrClauseCntr();
                    VecInt vec1 = new VecInt();
                    vec1.push(-reqVars.get(j));
                    vec1.push(isoVars[graphPattern.requester][j]);
                    if (isCNF)
                        vec1.push(-atomVar);
                    solver.addClause(vec1);

                    counters.incrClauseCntr();
                    VecInt vec2 = new VecInt();
                    vec2.push(reqVars.get(j));
                    vec2.push(-isoVars[graphPattern.requester][j]);
                    if (isCNF)
                        vec2.push(-atomVar);
                    solver.addClause(vec2);
                } else {
//                        counters.incrClauseCntr();
//                        VecInt vec1 = new VecInt();
//                        vec1.push(-reqVars.get(j));
//                        vec1.push(-atomVar);
//                        solver.addClause(vec1);
                }
            } catch (ContradictionException e) {
                System.out.println("Contradiction while translating atom (genReqMatchConstrs)");
            }
        }

    }

    //Generates the edge mapping constraints in isomorphism
    //Used in Satisfiability only
    protected void genIsoEdgeConstrs(Counters counters){
        for (int l = 0; l < Graph.labelSetSize; l++) {
            for (int i = 0; i < graphPattern.vertices.size(); i++) {
                for (int j = 0; j < graphPattern.vertices.size(); j++) {
                    if (j != i) {
                        for (int p = 0; p < graph.vertices.size(); p++) {
                            for (int q = 0; q < graph.vertices.size(); q++) {
                                if (q != p) {
                                    if (graphPattern.outEdges[l][i].contains(j)
                                            && !graph.outEdges[l][p].contains(q)
                                                && isoVars[i][p] > 0 && isoVars[j][q] > 0) {
                                            counters.incrClauseCntr();
                                            ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                                            clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[i][p])));
                                            clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[j][q])));
                                            clauses.add(new QBFOr(clauseArgs));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void genIsoEdgeConstrs(Counters counters, int atomVar){
        for (int l = 0; l < Graph.labelSetSize; l++) {
            for (int i = 0; i < graphPattern.vertices.size(); i++) {
                for (int j = 0; j < graphPattern.vertices.size(); j++) {
                    if (j != i) {
                        for (int p = 0; p < graph.vertices.size(); p++) {
                            for (int q = 0; q < graph.vertices.size(); q++) {
                                if (q != p) {
                                    if (graphPattern.outEdges[l][i].contains(j)
                                            && !graph.outEdges[l][p].contains(q)
                                            && isoVars[i][p] > 0 && isoVars[j][q] > 0) {
                                        counters.incrClauseCntr();
                                        ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                                        clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[i][p])));
                                        clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[j][q])));
                                        clauseArgs.add(new QBFNot(new QBFLiteral(atomVar)));
                                        clauses.add(new QBFOr(clauseArgs));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void genIsoEdgeConstrs2(Counters counters){
        for (int l = 0; l < Graph.labelSetSize; l++) {
            for (int i = 0; i < graphPattern.vertices.size(); i++) {
                for (int j = 0; j < graphPattern.vertices.size(); j++) {
                    if (j != i) {
                        for (int p = 0; p < graph.vertices.size(); p++) {
                            if (graphPattern.outEdges[l][i].contains(j) && isoVars[i][p] > 0) {
                                counters.incrClauseCntr();
                                ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                                clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[i][p])));
                                for (int q: graph.outEdges[l][p]) {
                                    if (q != p) {
                                        if (isoVars[j][q] > 0)
                                            clauseArgs.add(new QBFLiteral(isoVars[j][q]));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void genIsoEdgeConstrs(Counters counters, int atomVar, ISolver solver, boolean isCNF){
        for (int l = 0; l < Graph.labelSetSize; l++) {
            for (int i = 0; i < graphPattern.vertices.size(); i++) {
                for (int j = 0; j < graphPattern.vertices.size(); j++) {
                    if (j != i) {
                        for (int p = 0; p < graph.vertices.size(); p++) {
                            for (int q = 0; q < graph.vertices.size(); q++) {
                                if (q != p) {
                                    try {
                                        if (graphPattern.outEdges[l][i].contains(j)
                                                && !graph.outEdges[l][p].contains(q)
                                                && isoVars[i][p] > 0 && isoVars[j][q] > 0) {
                                            counters.incrClauseCntr();
                                            VecInt vec1 = new VecInt();
                                            vec1.push(-isoVars[i][p]);
                                            vec1.push(-isoVars[j][q]);
                                            if (isCNF)
                                                vec1.push(-atomVar);
                                            solver.addClause(vec1);
                                        }
                                    } catch (ContradictionException e) {
                                        System.out.println("Contradiction while translating atom (genIsoEdgeConstrs)");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void genIsoEdgeConstrs2 (Counters counters, int atomVar, ISolver solver, boolean isCNF) throws ContradictionException{
        for (int l = 0; l < Graph.labelSetSize; l++) {
            for (int i = 0; i < graphPattern.vertices.size(); i++) {
                for (int j = 0; j < graphPattern.vertices.size(); j++) {
                    if (j != i) {
                        for (int p = 0; p < graph.vertices.size(); p++) {
                            //try {
                                if (graphPattern.outEdges[l][i].contains(j) && isoVars[i][p] > 0) {
                                counters.incrClauseCntr();
                                VecInt vec1 = new VecInt();
                                vec1.push(-isoVars[i][p]);
                                for (int q: graph.outEdges[l][p]) {
                                    if (q != p) {
                                        if (isoVars[j][q] > 0)
                                            vec1.push(isoVars[j][q]);
                                    }
                                }
                                if (isCNF)
                                    vec1.push(-atomVar);
                                solver.addClause(vec1);
                                }
//                            } catch (ContradictionException e) {
//                                System.out.println("Contradiction while translating atom (genIsoEdgeConstrs2)");
//                            }
                        }
                    }
                }
            }
        }
    }

    //Used in Feasibility and Resiliency only
    protected void genIsoEdgeConstrsGeneral(Counters counters){
        for (int l = 0; l < Graph.labelSetSize; l++) {
            for (int i = 0; i < graphPattern.vertices.size(); i++) {
                for (int j = 0; j < graphPattern.vertices.size(); j++) {
                    if (j != i) {
                        for (int p = 0; p < graph.vertices.size(); p++) {
                            for (int q = 0; q < graph.vertices.size(); q++) {
                                if (q != p) {
                                    if (graphPattern.outEdges[l][i].contains(j))
                                    {
                                        counters.incrClauseCntr();
                                        ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                                        clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[i][p])));
                                        clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[j][q])));
                                        clauseArgs.add(new QBFLiteral(mutatedGraphVars[l][p][q]));
                                        clauses.add(new QBFOr(clauseArgs));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void genIsoEdgeConstrsGeneral(Counters counters, int atomVar ){
        for (int l = 0; l < Graph.labelSetSize; l++) {
            for (int i = 0; i < graphPattern.vertices.size(); i++) {
                for (int j = 0; j < graphPattern.vertices.size(); j++) {
                    if (j != i) {
                        for (int p = 0; p < graph.vertices.size(); p++) {
                            for (int q = 0; q < graph.vertices.size(); q++) {
                                if (q != p) {
                                    if (graphPattern.outEdges[l][i].contains(j))
                                    {
                                        counters.incrClauseCntr();
                                        ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                                        clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[i][p])));
                                        clauseArgs.add(new QBFNot(new QBFLiteral(isoVars[j][q])));
                                        clauseArgs.add(new QBFLiteral(mutatedGraphVars[l][p][q]));
                                        clauseArgs.add(new QBFNot(new QBFLiteral(atomVar)));
                                        clauses.add(new QBFOr(clauseArgs));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public QBF eval (Entailment entailment, Counters counters) {
        if(!atomQBFsMap.containsKey(this)) {
            if (entailment instanceof Satisfiability) {
                isoVars = new Integer[graphPattern.vertices.size()][graph.vertices.size()];
                genIsoVars(counters);
                genBijectConstrs(counters);
                genReqMatchConstrs(counters);
                genIsoEdgeConstrs(counters);
                qbf = new QBFAnd(clauses);
                for (int i : quantVars) {
                    qbf = new QBFExists(qbf, i);
                }
                atomQBFsMap.put(this, qbf);
                return qbf;
            } else if (entailment instanceof Satisfiability2) {
                isoVars = new Integer[graphPattern.vertices.size()][graph.vertices.size()];
                //genIsoVars2(counters);
                genIsoVarsGeneral2(counters);
                genReqMatchConstrs(counters);
                genIsoEdgeConstrs(counters);
                qbf = new QBFAnd(clauses);
                for (int i : quantVars) {
                    qbf = new QBFExists(qbf, i);
                }
                atomQBFsMap.put(this, qbf);
                return qbf;
            } else if (entailment instanceof Feasibility) {
                isoVars = new Integer[graphPattern.vertices.size()][graph.vertices.size()];
                genIsoVarsGeneral(counters);
                genBijectConstrs(counters);
                genReqMatchConstrs(counters);
                genIsoEdgeConstrsGeneral(counters);
                qbf = new QBFAnd(clauses);
                for (int i : quantVars) {
                    qbf = new QBFExists(qbf, i);
                }
                atomQBFsMap.put(this, qbf);
                return qbf;
            } else { //if (entailment instanceof Feasibility2) {
                isoVars = new Integer[graphPattern.vertices.size()][graph.vertices.size()];
                genIsoVarsGeneral2(counters);
                genReqMatchConstrs(counters);
                genIsoEdgeConstrsGeneral(counters);
                //genIsoEdgeConstrs(counters); //for test
                qbf = new QBFAnd(clauses);
                for (int i : quantVars) {
                    qbf = new QBFExists(qbf, i);
                }
                atomQBFsMap.put(this, qbf);
                return qbf;
            }
        } else {
            return atomQBFsMap.get(this);
        }
    }

    public QBF eval (Counters counters, boolean isNewReduce) {
        if(!atomQBFsMap.containsKey(this)) {
            isoVars = new Integer[graphPattern.vertices.size()][graph.vertices.size()];
            genIsoVars(counters);
            genBijectConstrs(counters);
            genReqMatchConstrs(counters);

            if (isNewReduce)
                genIsoEdgeConstrs2(counters);

            else
                genIsoEdgeConstrs(counters);

            qbf = new QBFAnd(clauses);
            for (int i : quantVars) {
                qbf = new QBFExists(qbf, i);
            }
            atomQBFsMap.put(this, qbf);
            return qbf;
        } else {
            return atomQBFsMap.get(this);
        }
    }

    public QBF formulaEval (Counters counters) {
        if(!atomVarsMap.containsKey(this)) {
            counters.incrVarCntr();
            atomVarsMap.put(this, counters.getVarCounter());
            return new QBFLiteral(counters.getVarCounter());
        }
        else {
            return new QBFLiteral(atomVarsMap.get(this));
        }
    }

    public QBF eval_CNF(Entailment entailment, Counters counters) {
        if(!atomQBFsMap.containsKey(this)) {
            counters.incrVarCntr();
            int atomVar = counters.getVarCounter();
            atomVarsMap.put(this, atomVar);
            isoVars = new Integer[graphPattern.vertices.size()][graph.vertices.size()];
            if (entailment instanceof Satisfiability || entailment instanceof Satisfiability2) {
                //genIsoVars2(counters, atomVar);
                genIsoVarsGeneral2(counters, atomVar);
                genReqMatchConstrs(counters, atomVar);
                genIsoEdgeConstrs(counters, atomVar);
                qbf = new QBFAnd(clauses);
                for (int i : quantVars) {
                    qbf = new QBFExists(qbf, i);
                }
                atomQBFsMap.put(this, qbf);
                return qbf;
            } else {
                //genIsoVars2(counters, atomVar);
                genIsoVarsGeneral2(counters, atomVar);
                genReqMatchConstrs(counters, atomVar);
                genIsoEdgeConstrsGeneral(counters, atomVar);
                qbf = new QBFAnd(clauses);
                for (int i : quantVars) {
                    qbf = new QBFExists(qbf, i);
                }
                atomQBFsMap.put(this, qbf);
                return qbf;
            }
        }
        else {
            return atomQBFsMap.get(this);
        }
    }

    @Override
    public void eval(Entailment entailment, Counters counters, ISolver solver, boolean isCNF, boolean isNewReduce) throws ContradictionException{
        int atomVar = 0;
        if (isCNF) {
            counters.incrVarCntr();
            atomVar = counters.getVarCounter();
            //atomVarsMap.put(this, atomVar);
            atomIDVars.put(atomID, atomVar);
        }
        isoVars = new Integer[graphPattern.vertices.size()][graph.vertices.size()];
        if (entailment instanceof Satisfiability || entailment instanceof Satisfiability2) {
            genIsoVarsGeneral(counters, atomVar, solver, isCNF);
            genReqMatchConstrs(counters, atomVar, solver, isCNF);
            if (isNewReduce)
                genIsoEdgeConstrs2(counters, atomVar, solver, isCNF);
            else
                genIsoEdgeConstrs(counters, atomVar, solver, isCNF);
        }
        VecInt v0 = new VecInt();
        for (int k : Formula.reqVars.values()) {
            v0.push(k);
        }
        solver.addExactly(v0, 1);

        for (int i = 0; i < isoVars.length; i++) {
            VecInt v1 = new VecInt();
            for (int j = 0; j < isoVars[0].length; j++) {
                if (isoVars[i][j] > 0) {
                    v1.push(isoVars[i][j]);
                }
            }
            solver.addExactly(v1, 1);
        }

        for (int j = 0; j < isoVars[0].length; j++) {
            VecInt v2 = new VecInt();
            for (int i = 0; i < isoVars.length; i++) {
                if (isoVars[i][j] > 0) {
                    v2.push(isoVars[i][j]);
                }
            }
            solver.addAtMost(v2, 1);
        }

    }

    @Override
    public Formula clone() {
        return new FormulaAtom(this);
    }
}
