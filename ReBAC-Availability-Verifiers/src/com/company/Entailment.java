package com.company;

import com.google.common.collect.HashBiMap;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.*;
import org.sat4j.tools.ModelIterator;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public abstract class Entailment {
    //Attributes
    protected static int timeOut;// = 300;

    protected Formula formula;
    protected Formula positiveFormula;
    protected Formula negativeFormula;
    protected Graph graph;
    protected int distance;
    protected HashMap<Integer, Integer> reqVars;
    protected int [][][] deltaMatrix;
    protected HashSet<Integer> deltaVars;
    protected int [][][] mutatedGraphVars;
    protected int [][] mapVars;

    protected double formula2QBFTimes;
    protected double NNFTimes;
    protected double prenexTimes;
    protected double QCIRCompileTime;
    protected int QCIRNumOfVars;
    protected ArrayList<Integer> sat4JNumOfVars;
    protected ArrayList<Integer> sat4JNumOfVars_Pos;
    protected ArrayList<Integer> sat4JNumOfVars_Neg;
    protected ArrayList<Integer> sat4JNumOfClauses;
    protected ArrayList<Integer> sat4JNumOfClauses_Pos;
    protected ArrayList<Integer> sat4JNumOfClauses_Neg;
    protected int sat4JNumOfIter;
    protected double sat4JCompileTime;
    protected double sat4JTotalSolverTime;
    protected ArrayList<Double> modelTimes;
    protected int numModels;

    protected ArrayList<Double> string2StreamTime = new ArrayList<Double>();
    protected ArrayList<Double> stream2ClauseTime = new ArrayList<Double>();
    protected ArrayList<Double> solvingTime = new ArrayList<Double>();

    protected ArrayList<Double> string2StreamTime2 = new ArrayList<Double>();
    protected ArrayList<Double> stream2ClauseTime2 = new ArrayList<Double>();
    protected ArrayList<Double> solvingTime2 = new ArrayList<Double>();

    //Abstract methods
    //protected abstract String checkSat_QDIMACS();
    protected abstract String checkSat_QCIR();

    //Concrete methods

    //Generating requester variables
    protected QBF reqsCnstrts(Counters counters) {
        ArrayList<QBF> clauses = new ArrayList<QBF>();
        reqVars = new HashMap<Integer, Integer>();
        for (int i = 0; i < graph.vertices.size(); i++) {
            counters.incrVarCntr();
            reqVars.put(i, counters.getVarCounter());
        }

        for (int i = 0; i < graph.vertices.size(); i++) {
            for (int j = i + 1; j < graph.vertices.size(); j++) {
                counters.incrClauseCntr();
                ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                clauseArgs.add(new QBFNot(new QBFLiteral(reqVars.get(i))));
                clauseArgs.add(new QBFNot(new QBFLiteral(reqVars.get(j))));
                clauses.add(new QBFOr(clauseArgs));
            }
        }

        ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
        for (int i = 0; i < graph.vertices.size(); i++) {
            clauseArgs.add(new QBFLiteral(reqVars.get(i)));
        }
        counters.incrClauseCntr();
        clauses.add(new QBFOr(clauseArgs));
        return new QBFAnd(clauses);
    }

    protected void genReqVars(Counters counters) {
        reqVars = new HashMap<Integer, Integer>();
        for (int i = 0; i < graph.vertices.size(); i++) {
            counters.incrVarCntr();
            reqVars.put(i, counters.getVarCounter());
        }
    }

    protected QBF nnf(QBF formula) {
        if (formula instanceof QBFNot) {
            QBFNot nf = (QBFNot) formula;
            if (nf.qbf instanceof QBFAnd) {
                QBFAnd af = (QBFAnd) nf.qbf;
                for (int i = 0; i < af.qbfs.size(); i++) {
                    af.qbfs.set(i, nnf(new QBFNot(af.qbfs.get(i))));
                }
                //return new QBFOr(nnf(new QBFNot(af.qbf1)), nnf(new QBFNot(af.qbf2)));
                return new QBFOr(af.qbfs);
            } else if (nf.qbf instanceof QBFOr) {
                QBFOr of = (QBFOr) nf.qbf;
                for (int i = 0; i < of.qbfs.size(); i++) {
                    of.qbfs.set(i, nnf(new QBFNot(of.qbfs.get(i))));
                }
                //return new QBFAnd(nnf(new QBFNot(of.qbf1)), nnf(new QBFNot(of.qbf2)));
                return new QBFAnd(of.qbfs);
            } else if (nf.qbf instanceof QBFNot) {
                QBFNot n = (QBFNot) nf.qbf;
                return n.qbf;
            } else if (nf.qbf instanceof QBFExists) {
                QBFExists ef = (QBFExists) nf.qbf;
                return new QBFForAll(nnf(new QBFNot(ef.qbf)), ef.quantifier.quantVar);
            } else if (nf.qbf instanceof QBFForAll) {
                QBFForAll ff = (QBFForAll) nf.qbf;
                return new QBFExists(nnf(new QBFNot(ff.qbf)), ff.quantifier.quantVar);
            } else {
                QBFLiteral l = (QBFLiteral) nf.qbf;
                return new QBFLiteral(-l.atom);
                //return nf;
            }
        } else if (formula instanceof QBFAnd) {
            QBFAnd af = (QBFAnd) formula;
            for (int i = 0; i < af.qbfs.size(); i++) {
                af.qbfs.set(i, nnf(af.qbfs.get(i)));
            }
            //return new QBFAnd(nnf(af.qbf1), nnf(af.qbf2));
            return af;
        } else if (formula instanceof QBFOr) {
            QBFOr of = (QBFOr) formula;
            for (int i = 0; i < of.qbfs.size(); i++) {
                of.qbfs.set(i, nnf(of.qbfs.get(i)));
            }
            //return new QBFOr(nnf(of.qbf1), nnf(of.qbf2));
            return of;
        } else if (formula instanceof QBFExists) {
            QBFExists ef = (QBFExists) formula;
            return new QBFExists(nnf(ef.qbf), ef.quantifier.quantVar);
        } else if (formula instanceof QBFForAll) {
            QBFForAll ff = (QBFForAll) formula;
            return new QBFForAll(nnf(ff.qbf), ff.quantifier.quantVar);
        } else
            return formula;
    }

    protected void collectQuantifiers(QBF qbf, Stack<Quantifier> quantifiers) {
        if (qbf instanceof QBFExists) {
            QBFExists ef = (QBFExists) qbf;
            quantifiers.push(ef.quantifier);
            collectQuantifiers(ef.qbf, quantifiers);
        } else if (qbf instanceof QBFForAll) {
            QBFForAll ff = (QBFForAll) qbf;
            quantifiers.push(ff.quantifier);
            collectQuantifiers(ff.qbf, quantifiers);
        } else if (qbf instanceof QBFAnd) {
            QBFAnd af = (QBFAnd) qbf;
            if (!quantifiers.isEmpty()) {
                Quantifier lastQuantifier = quantifiers.peek();
                ArrayList<QBF> temp = new ArrayList<QBF>();
                for (QBF arg : af.qbfs) {
                    if (arg instanceof QBFExists && !lastQuantifier.isUniversal) {
                        collectQuantifiers(arg, quantifiers);
                    } else if (arg instanceof QBFForAll && lastQuantifier.isUniversal) {
                        collectQuantifiers(arg, quantifiers);
                    } else {
                        temp.add(arg);
                    }
                }
                for (QBF arg : temp) {
                    collectQuantifiers(arg, quantifiers);
                }
            }
            else {
                for (QBF arg : af.qbfs) {
                    collectQuantifiers(arg, quantifiers);
                }
            }
        } else if (qbf instanceof QBFOr) {
            QBFOr of = (QBFOr) qbf;
            if (!quantifiers.isEmpty()) {
                Quantifier lastQuantifier = quantifiers.peek();
                ArrayList<QBF> temp = new ArrayList<QBF>();
                for (QBF arg : of.qbfs) {
                    if (arg instanceof QBFExists && !lastQuantifier.isUniversal) {
                        collectQuantifiers(arg, quantifiers);
                    } else if (arg instanceof QBFForAll && lastQuantifier.isUniversal) {
                        collectQuantifiers(arg, quantifiers);
                    } else {
                        temp.add(arg);
                    }
                }
                for (QBF arg : temp) {
                    collectQuantifiers(arg, quantifiers);
                }
            }
            else {
                for (QBF arg : of.qbfs) {
                    collectQuantifiers(arg, quantifiers);
                }
            }
        }
    }

    protected void collectQuantifiers(QBF qbf, ArrayList<Quantifier> quantifiers) {
        if (qbf instanceof QBFExists) {
            QBFExists ef = (QBFExists) qbf;
            quantifiers.add(ef.quantifier);
            collectQuantifiers(ef.qbf, quantifiers);
        } else if (qbf instanceof QBFForAll) {
            QBFForAll ff = (QBFForAll) qbf;
            quantifiers.add(ff.quantifier);
            collectQuantifiers(ff.qbf, quantifiers);
        }
    }

    protected QBF removeQuantifiers(QBF qbf) {
        if (qbf instanceof QBFExists) {
            QBFExists ef = (QBFExists) qbf;
            return removeQuantifiers(ef.qbf);
        } else if (qbf instanceof QBFForAll) {
            QBFForAll ff = (QBFForAll) qbf;
            return removeQuantifiers(ff.qbf);
        } else if (qbf instanceof QBFAnd) {
            QBFAnd af = (QBFAnd) qbf;
            for (int i = 0; i < af.qbfs.size(); i++) {
                af.qbfs.set(i, removeQuantifiers(af.qbfs.get(i)));
            }
            return af;
        } else if (qbf instanceof QBFOr) {
            QBFOr of = (QBFOr) qbf;
            for (int i = 0; i < of.qbfs.size(); i++) {
                of.qbfs.set(i, removeQuantifiers(of.qbfs.get(i)));
            }
            return of;
        } else if (qbf instanceof QBFNot) {
            QBFNot nf = (QBFNot) qbf;
            return new QBFNot(removeQuantifiers(nf.qbf));
        } else {
            return qbf;
        }

    }

    protected QBF prenex(QBF qbf) {
        Stack<Quantifier> quantifiers = new Stack<Quantifier>();
        collectQuantifiers(qbf, quantifiers);
        qbf = removeQuantifiers(qbf);
        while (!quantifiers.isEmpty()) {
            Quantifier quantifier = quantifiers.pop();
            if (quantifier.isUniversal) {
                qbf = new QBFForAll(qbf, quantifier.quantVar);
            } else {
                qbf = new QBFExists(qbf, quantifier.quantVar);
            }
        }
        return qbf;
    }

    protected StringBuilder printQuantifiers(QBF qbf, String outType) {
        StringBuilder output = new StringBuilder();
        ArrayList<Quantifier> quantifiers = new ArrayList<Quantifier>();
        ArrayList<Quantifiers> quantifiersArray = new ArrayList<Quantifiers>();
        collectQuantifiers(qbf, quantifiers);
        Quantifier tempQuant = null;
        for (Quantifier quantifier : quantifiers) {
            if (tempQuant == null) {
                tempQuant = quantifier;
                quantifiersArray.add(new Quantifiers(quantifier.isUniversal, quantifier.quantVar));
            } else {
                if (tempQuant.isUniversal == quantifier.isUniversal) {
                    quantifiersArray.set(quantifiersArray.size() - 1, quantifiersArray.get(quantifiersArray.size() - 1).addVar(quantifier.quantVar));
                } else {
                    tempQuant = quantifier;
                    quantifiersArray.add(new Quantifiers(quantifier.isUniversal, quantifier.quantVar));
                }
            }
        }
        if (outType.equals("QDIMACS")) {
            for (Quantifiers q : quantifiersArray) {
                if (q.isUniversal) {
                    output.append("a ");
                    for (int i : q.vars) {
                        output.append(i).append(" ");
                    }
                    output.append("0").append(System.lineSeparator());
                } else {
                    output.append("e ");
                    for (int i : q.vars) {
                        output.append(i).append(" ");
                    }
                    output.append("0").append(System.lineSeparator());
                }
            }
            //System.out.println(output);
            return output;
        } else {
            for (Quantifiers q : quantifiersArray) {
                if (q.isUniversal) {
                    output.append("forall(");
                    for (int i : q.vars) {
                        output.append(i).append(", ");
                    }
                    output = new StringBuilder (output.substring(0, output.length() - 2));
                    output.append(")").append(System.lineSeparator());
                } else {
                    output.append("exists(");
                    for (int i : q.vars) {
                        output.append(i).append(", ");
                    }
                    output = new StringBuilder(output.substring(0, output.length() - 2));
                    output.append(")").append(System.lineSeparator());
                }
            }
            //System.out.println(output);
            return output;
        }

    }

    protected QBF cnf(QBFLiteral z, QBF qbf, Counters counters) {
        if (z == null) {
            if (qbf instanceof QBFLiteral) {
                return qbf;
            } else if (qbf instanceof QBFExists) {
                QBFExists ef = (QBFExists) qbf;
                return new QBFExists(cnf(null, ef.qbf, counters), ef.quantifier.quantVar);
            } else if (qbf instanceof QBFForAll) {
                QBFForAll ff = (QBFForAll) qbf;
                return new QBFForAll(cnf(null, ff.qbf, counters), ff.quantifier.quantVar);
            } else {
                counters.incrVarCntr();
                //QBF.varCounter++;
                counters.incrClauseCntr();
                //QBF.clauseCounter++;
                QBFLiteral z1 = new QBFLiteral(counters.getVarCounter());
                //QBFLiteral z1 = new QBFLiteral(QBF.varCounter);
                ArrayList<QBF> andArgs = new ArrayList<QBF>();
                andArgs.add(z1);
                andArgs.add(cnf(z1, qbf, counters));
                return new QBFExists(new QBFAnd(andArgs), z1.atom);
            }
        } else {
            if (qbf instanceof QBFAnd) {
                QBFAnd af = (QBFAnd) qbf;
                QBFLiteral x, y;

                if (af.qbfs.get(0) instanceof QBFLiteral) {
                    x = (QBFLiteral) af.qbfs.get(0);
                } else {
                    counters.incrVarCntr();
                    //QBF.varCounter++;
                    x = new QBFLiteral(counters.getVarCounter());
                    //x = new QBFLiteral(QBF.varCounter);
                }

                if (af.qbfs.get(1) instanceof QBFLiteral) {
                    y = (QBFLiteral) af.qbfs.get(1);
                } else {
                    counters.incrVarCntr();
                    //QBF.varCounter++;
                    y = new QBFLiteral(counters.getVarCounter());
                    //y = new QBFLiteral(QBF.varCounter);
                }
                ArrayList<QBF> orArgs1 = new ArrayList<QBF>();
                orArgs1.add(new QBFNot(z));
                orArgs1.add(x);

                ArrayList<QBF> orArgs2 = new ArrayList<QBF>();
                orArgs2.add(new QBFNot(z));
                orArgs2.add(y);

                ArrayList<QBF> orArgs3 = new ArrayList<QBF>();
                orArgs3.add(z);
                orArgs3.add(new QBFNot(x));
                orArgs3.add(new QBFNot(y));

                ArrayList<QBF> andArgs = new ArrayList<QBF>();
                andArgs.add(new QBFOr(orArgs1));
                andArgs.add(new QBFOr(orArgs2));
                andArgs.add(new QBFOr(orArgs3));

                QBF temp = new QBFAnd(andArgs);
                //QBF temp = new QBFAnd(new QBFAnd(new QBFOr(new QBFNot(z), x), new QBFOr(new QBFNot(z), y)),
                //        new QBFOr(new QBFOr(z, new QBFNot(x)), new QBFNot(y)));
                counters.setClauseCounter(counters.getClauseCounter() + 3);
                //QBF.clauseCounter += 3;

                if (!(af.qbfs.get(0) instanceof QBFLiteral)) {
                    ArrayList<QBF> tempAndArgs = new ArrayList<QBF>();
                    tempAndArgs.add(temp);
                    tempAndArgs.add(cnf(x, af.qbfs.get(0), counters));
                    temp = new QBFExists(new QBFAnd(tempAndArgs), x.atom);
                    //temp = new QBFExists(new QBFAnd(temp, cnf(x, af.qbfs.get(0))), x.atom);
                }
                if (!(af.qbfs.get(1) instanceof QBFLiteral)) {
                    ArrayList<QBF> tempAndArgs = new ArrayList<QBF>();
                    tempAndArgs.add(temp);
                    tempAndArgs.add(af.qbfs.get(1));
                    temp = new QBFExists(new QBFAnd(tempAndArgs), y.atom);
                    //temp = new QBFExists(new QBFAnd(temp, cnf(y, af.qbf2)), y.atom);
                }
                return temp;
            } else if (qbf instanceof QBFNot) {
                QBFNot nf = (QBFNot) qbf;
                QBFLiteral x;

                if (nf.qbf instanceof QBFLiteral) {
                    x = (QBFLiteral) nf.qbf;
                } else {
                    counters.incrVarCntr();
                    //QBF.varCounter++;
                    x = new QBFLiteral(counters.getVarCounter());
                    //x = new QBFLiteral(QBF.varCounter);
                }
                ArrayList<QBF> orArgs1 = new ArrayList<QBF>();
                orArgs1.add(new QBFNot(z));
                orArgs1.add(new QBFNot(x));

                ArrayList<QBF> orArgs2 = new ArrayList<QBF>();
                orArgs2.add(z);
                orArgs2.add(x);

                ArrayList<QBF> andArgs = new ArrayList<QBF>();
                andArgs.add(new QBFOr(orArgs1));
                andArgs.add(new QBFOr(orArgs2));
                QBF temp = new QBFAnd(andArgs);
                //QBF temp = new QBFAnd(new QBFOr(new QBFNot(z), new QBFNot(x)), new QBFOr(z, x));
                counters.setClauseCounter(counters.getClauseCounter() + 2);
                //QBF.clauseCounter += 2;

                if (!(nf.qbf instanceof QBFLiteral)) {
                    ArrayList<QBF> tempAndArgs = new ArrayList<QBF>();
                    tempAndArgs.add(temp);
                    tempAndArgs.add(cnf(x, nf.qbf, counters));
                    temp = new QBFExists(new QBFAnd(tempAndArgs), x.atom);
                }
                return temp;
            } else if (qbf instanceof QBFOr) {
                QBFOr of = (QBFOr) qbf;
                QBFLiteral x, y;

                if (of.qbfs.get(0) instanceof QBFLiteral) {
                    x = (QBFLiteral) of.qbfs.get(0);
                } else {
                    counters.incrVarCntr();
                    //QBF.varCounter++;
                    x = new QBFLiteral(counters.getVarCounter());
                    //x = new QBFLiteral(QBF.varCounter);
                }

                if (of.qbfs.get(1) instanceof QBFLiteral) {
                    y = (QBFLiteral) of.qbfs.get(1);
                } else {
                    counters.incrVarCntr();
                    //QBF.varCounter++;
                    y = new QBFLiteral(counters.getVarCounter());
                    //y = new QBFLiteral(QBF.varCounter);
                }

                ArrayList<QBF> orArgs1 = new ArrayList<QBF>();
                orArgs1.add(z);
                orArgs1.add(new QBFNot(x));

                ArrayList<QBF> orArgs2 = new ArrayList<QBF>();
                orArgs2.add(z);
                orArgs2.add(new QBFNot(y));

                ArrayList<QBF> orArgs3 = new ArrayList<QBF>();
                orArgs3.add(new QBFNot(z));
                orArgs3.add(x);
                orArgs3.add(y);

                ArrayList<QBF> andArgs = new ArrayList<QBF>();
                andArgs.add(new QBFOr(orArgs1));
                andArgs.add(new QBFOr(orArgs2));
                andArgs.add(new QBFOr(orArgs3));

                QBF temp = new QBFAnd(andArgs);
                //QBF temp = new QBFAnd(new QBFAnd(new QBFOr(z, new QBFNot(x)), new QBFOr(z, new QBFNot(y))),
                //        new QBFOr(new QBFOr(new QBFNot(z), x), y));
                counters.setClauseCounter(counters.getClauseCounter() + 3);
                //QBF.clauseCounter += 3;

                if (!(of.qbfs.get(0) instanceof QBFLiteral)) {
                    ArrayList<QBF> tempAndArgs = new ArrayList<QBF>();
                    tempAndArgs.add(temp);
                    tempAndArgs.add(cnf(x, of.qbfs.get(0), counters));
                    temp = new QBFExists(new QBFAnd(tempAndArgs), x.atom);
                    //temp = new QBFExists(new QBFAnd(temp, cnf(x, of.qbfs.get(0))), x.atom);
                }

                if (!(of.qbfs.get(1) instanceof QBFLiteral)) {
                    ArrayList<QBF> tempAndArgs = new ArrayList<QBF>();
                    tempAndArgs.add(temp);
                    tempAndArgs.add(cnf(y, of.qbfs.get(1), counters));
                    temp = new QBFExists(new QBFAnd(tempAndArgs), y.atom);
                    //temp = new QBFExists(new QBFAnd(temp, cnf(y, of.qbfs.get(1))), y.atom);
                }

                return temp;
            } else if (qbf instanceof QBFExists) {
                QBFExists ef = (QBFExists) qbf;
                return new QBFExists(cnf(null, ef.qbf, counters), ef.quantifier.quantVar);
            } else if (qbf instanceof QBFForAll) {
                QBFForAll ff = (QBFForAll) qbf;
                return new QBFForAll(cnf(null, ff.qbf, counters), ff.quantifier.quantVar);
            } else {
                return qbf;
            }
        }
    }

    protected QBF genDeltaMatrixQBF(Counters counters) {
        ArrayList<QBF> clauses = new ArrayList<QBF>();
        deltaMatrix = new int [graph.labels.size()][graph.vertices.size()][graph.vertices.size()];
        mapVars = new int [graph.labels.size() * graph.vertices.size() * graph.vertices.size()][distance];
        deltaVars = new HashSet<Integer>();

        //Generate Delta Matrix Variables
        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                for (int j = 0; j < graph.vertices.size(); j++) {
                    counters.incrVarCntr();
                    //QBF.varCounter ++;
                    deltaMatrix[l][i][j] = counters.getVarCounter();
                    deltaVars.add(deltaMatrix[l][i][j]);
                    //deltaMatrix[l][i][j] = QBF.varCounter;
                    //System.out.println("dm[" + l + "][" + i + "][" + j + "]" + " = " + QBF.varCounter);
                }
            }
        }

        //Generate Map Variables
        for (int i = 0; i < graph.labels.size() * graph.vertices.size() * graph.vertices.size(); i++) {
            for (int j = 0; j < distance; j++) {
                counters.incrVarCntr();
                //QBF.varCounter ++;
                mapVars[i][j] = counters.getVarCounter();
                //mapVars[i][j] = QBF.varCounter;
                //System.out.println("mv[" + i + "][" + j + "]" + " = " + QBF.varCounter);
            }
        }

        //Generate Map Function Constraints
        for (int i = 0; i <graph.labels.size() * graph.vertices.size() * graph.vertices.size(); i++) {
            for (int j = 0; j < distance; j++) {
                for (int k = j + 1; k < distance; k++) {
                    counters.incrClauseCntr();
                    //QBF.clauseCounter ++;
                    ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                    clauseArgs.add(new QBFNot(new QBFLiteral(mapVars[i][j])));
                    clauseArgs.add(new QBFNot(new QBFLiteral(mapVars[i][k])));
                    clauses.add(new QBFOr(clauseArgs));
                }
            }
        }

        //Generate One-to-One Constraints
        for (int i = 0; i < graph.labels.size() * graph.vertices.size() * graph.vertices.size(); i++) {
            for (int j = i + 1; j < graph.labels.size() * graph.vertices.size() * graph.vertices.size(); j++) {
                for (int k = 0; k < distance; k++) {
                    counters.incrClauseCntr();
                    //QBF.clauseCounter ++;
                    ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                    clauseArgs.add(new QBFNot(new QBFLiteral(mapVars[i][k])));
                    clauseArgs.add(new QBFNot(new QBFLiteral(mapVars[j][k])));
                    clauses.add(new QBFOr(clauseArgs));
                }
            }
        }

        //If mapVars[i][j] is true then corresponding entry in delta matrix must be true
        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                for (int j = 0; j < graph.vertices.size(); j++) {
                    for (int k = 0; k < distance ; k++) {
                        counters.incrClauseCntr();
                        //QBF.clauseCounter ++;
                        ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                        clauseArgs.add(new QBFLiteral(deltaMatrix[l][i][j]));
                        clauseArgs.add(new QBFNot(new QBFLiteral(mapVars[l * graph.vertices.size() * graph.vertices.size() + i * graph.vertices.size() + j][k])));
                        clauses.add(new QBFOr(clauseArgs));
                    }
                }
            }
        }

        //If an entry in delta matrix is true then the corresponding mapVars[i][j] is true
        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                for (int j = 0; j < graph.vertices.size(); j++) {
                    counters.incrClauseCntr();
                    //QBF.clauseCounter ++;
                    ArrayList<QBF> clauseArgs = new ArrayList<QBF>();
                    for (int k = 0; k < distance; k++) {
                        clauseArgs.add(new QBFLiteral(mapVars[l * graph.vertices.size() * graph.vertices.size() + i * graph.vertices.size() + j][k]));
                    }
                    clauseArgs.add(new QBFNot(new QBFLiteral(deltaMatrix[l][i][j])));
                    clauses.add(new QBFOr(clauseArgs));
                }
            }
        }

        return new QBFAnd(clauses);
    }

    protected void genDeltaMatrixVars(Counters counters) {
        deltaMatrix = new int [graph.labels.size()][graph.vertices.size()][graph.vertices.size()];
        deltaVars = new HashSet<Integer>();
        //Generate Delta Matrix Variables
        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                for (int j = 0; j < graph.vertices.size(); j++) {
                    counters.incrVarCntr();
                    deltaMatrix[l][i][j] = counters.getVarCounter();
                    deltaVars.add(deltaMatrix[l][i][j]);
                }
            }
        }
    }

    //Generate QBFs for XORing delta matrix and graph matrix
    protected QBF genXORQBF(Counters counters) {
        ArrayList<QBF> clauses = new ArrayList<QBF>();
        //Generate variables for mutated graph matrix
        mutatedGraphVars = new int[graph.labels.size()][graph.vertices.size()][graph.vertices.size()];
        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                for (int j = 0; j < graph.vertices.size(); j++) {
                    counters.incrVarCntr();
                    //QBF.varCounter ++;
                    mutatedGraphVars[l][i][j] = counters.getVarCounter();
                    //mutatedGraphVars[l][i][j] = QBF.varCounter;
                    //System.out.println("mg[" + l + "][" + i + "][" + j + "]" + " = " + QBF.varCounter);
                }
            }
        }

        //XORing
        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                for (int j = 0; j < graph.vertices.size(); j++) {
                    counters.setClauseCounter(counters.getClauseCounter() + 2);
                    //QBF.clauseCounter += 2;
                    ArrayList<QBF> clause1Args = new ArrayList<QBF>();
                    ArrayList<QBF> clause2Args = new ArrayList<QBF>();
                    if (graph.outEdges[l][i].contains(j)) {
                        clause1Args.add(new QBFNot(new QBFLiteral(mutatedGraphVars[l][i][j])));
                        clause1Args.add(new QBFNot(new QBFLiteral(deltaMatrix[l][i][j])));
                        clauses.add(new QBFOr(clause1Args));
                        clause2Args.add(new QBFLiteral(mutatedGraphVars[l][i][j]));
                        clause2Args.add(new QBFLiteral(deltaMatrix[l][i][j]));
                        clauses.add(new QBFOr(clause2Args));
                    }
                    else {
                        clause1Args.add(new QBFLiteral(mutatedGraphVars[l][i][j]));
                        clause1Args.add(new QBFNot(new QBFLiteral(deltaMatrix[l][i][j])));
                        clauses.add(new QBFOr(clause1Args));
                        clause2Args.add(new QBFNot(new QBFLiteral(mutatedGraphVars[l][i][j])));
                        clause2Args.add(new QBFLiteral(deltaMatrix[l][i][j]));
                        clauses.add(new QBFOr(clause2Args));
                    }
                }
            }
        }
        return new QBFAnd(clauses);
    }

    protected boolean isSat(String dimacs) {
        boolean result = false;
        //Covert string to input stream
        InputStream is = new ByteArrayInputStream(dimacs.getBytes());
        //SAT oracle
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(timeOut); // 1 hour timeout
        try {
            Reader reader = new DimacsReader(solver);
            IProblem problem = reader.parseInstance(is); //pass in input stream of formula string
            //System.out.println(reader.decode(problem.model()));
            result = problem.isSatisfiable();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            System.out.println("Error1");
        } catch (ParseFormatException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
            System.out.println(dimacs);
            //System.out.println("Error2");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Error3");
        } catch (ContradictionException e) {
            //System.out.println(e.getMessage());
            result = false;
        } catch (TimeoutException e) {
            System.out.println("Timeout (isSat)!");
        } finally {
            return result;
        }
    }

    protected boolean isSat(String dimacs, FormulaAtom a) {
        boolean result = false;
        InputStream is = new ByteArrayInputStream(dimacs.getBytes());
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(timeOut); // 1 hour timeout
        try {
            Reader reader = new DimacsReader(solver);

            long startTime = System.nanoTime();
            IProblem problem = reader.parseInstance(is); //pass in input stream of formula string
            long elapsedTime = System.nanoTime() - startTime;
            sat4JCompileTime += ((double) elapsedTime / 1000000000.0);

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

            startTime = System.nanoTime();
            result = problem.isSatisfiable();
            elapsedTime = System.nanoTime() - startTime;
            sat4JTotalSolverTime += ((double) elapsedTime / 1000000000.0);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            System.out.println("Error1");
        } catch (ParseFormatException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
            //System.out.println(dimacs);
            //System.out.println("Error2");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("Error3");
        } catch (TimeoutException e) {
            System.out.println("Timeout (isSat)!");
        } finally {
            return result;
        }
    }

    protected ISolver addConstraints( FormulaAtom a, QBF qbf, int requester, Counters counters) {
        ISolver solver = SolverFactory.newDefault();
        try {
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
            qbf.evalDIMACS(solver);
            long elapsedTime = System.nanoTime() - startTime;
            sat4JCompileTime += ((double) elapsedTime / 1000000000.0);

        } catch (ContradictionException e) {
            e.printStackTrace();
        }

        VecInt requesterClause = new VecInt();
        requesterClause.push(requester);

        try{
            solver.setExpectedNumberOfClauses(counters.getClauseCounter());
            solver.addClause(requesterClause);
        } catch (ContradictionException e) {
            System.out.println("Contradiction while adding requester clause to a negative atom");
        }
        return solver;
    }

    protected IProblem enumerateModels(String dimacs, ISolver solver) {
        IProblem problem = null;
        InputStream is = new ByteArrayInputStream(dimacs.getBytes());

        ModelIterator mi = new ModelIterator(solver);
        solver.setTimeout(timeOut); // 1 hour timeout
        Reader reader = new DimacsReader(mi);

        try {

            long startTime = System.nanoTime();
            problem = reader.parseInstance(is);
            long elapsedTime = System.nanoTime() - startTime;
            sat4JCompileTime += (((double) elapsedTime / 1000000000.0));

        } catch (FileNotFoundException e) {
            System.out.println("Error4");
            e.printStackTrace();
        } catch (ParseFormatException e) {
            System.out.println("Error5");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error6");
            e.printStackTrace();
        } catch (ContradictionException e) {
            //System.out.println("Unsatisfiable (trivial)!");
        } finally {
            return problem;
        }
    }

    protected IProblem enumerateModels(String dimacs, FormulaAtom atom, ISolver solver) {
        IProblem problem = null;
        InputStream is = new ByteArrayInputStream(dimacs.getBytes());
        ModelIterator mi = new ModelIterator(solver);
        solver.setTimeout(timeOut); // 1 hour timeout
        Reader reader = new DimacsReader(mi);

        try {
            problem = reader.parseInstance(is);

            VecInt v0 = new VecInt();
            for (int k : Formula.reqVars.values()) {
                v0.push(k);
            }
            mi.addExactly(v0, 1);

            for (int i = 0; i < Formula.atomIsoVarsMap.get(atom).length; i++) {
                VecInt v2 = new VecInt();
                for (int j = 0; j < Formula.atomIsoVarsMap.get(atom)[0].length; j++) {
                    if (Formula.atomIsoVarsMap.get(atom)[i][j] > 0)
                        v2.push(Formula.atomIsoVarsMap.get(atom)[i][j]);
                }
                solver.addExactly(v2, 1);
            }

            for (int j = 0; j < Formula.atomIsoVarsMap.get(atom)[0].length; j++) {
                VecInt v3 = new VecInt();
                for (int i = 0; i < Formula.atomIsoVarsMap.get(atom).length; i++) {
                    if (Formula.atomIsoVarsMap.get(atom)[i][j] > 0) {
                        v3.push(Formula.atomIsoVarsMap.get(atom)[i][j]);
                    }
                }
                solver.addAtMost(v3, 1);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error4");
            e.printStackTrace();
        } catch (ParseFormatException e) {
            System.out.println("Error5");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error6");
            e.printStackTrace();
        } catch (ContradictionException e) {
            //System.out.println("Unsatisfiable (trivial)!");
            problem = null;
        }
        finally {
            return problem;
        }
    }

    protected IProblem enumerateModels(String dimacs, HashSet<FormulaAtom> atoms, ISolver solver) {
        IProblem problem = null;
        //Covert string to input stream
        long st0 = System.nanoTime();
        InputStream is = new ByteArrayInputStream(dimacs.getBytes());
        long et0 = System.nanoTime() - st0;
        string2StreamTime2.add((double) et0 / 1000000000.0);

        ModelIterator mi = new ModelIterator(solver);
        solver.setTimeout(timeOut); // 1 hour timeout
        //Reader reader = new InstanceReader(mi);
        Reader reader = new DimacsReader(mi);

        // filename is given on the command line
        try {
            long st1 = System.nanoTime();
            problem = reader.parseInstance(is);
            long et1 = System.nanoTime() - st1;
            stream2ClauseTime2.add((double) et1 / 1000000000.0);

            VecInt v0 = new VecInt();
            for (int k : Formula.reqVars.values()) {
                v0.push(k);
            }
            mi.addExactly(v0, 1);

            for (FormulaAtom atom: atoms) {
                for (int i = 0; i < Formula.atomIsoVarsMap.get(atom).length; i++) {
                    //for (Integer[] i: Formula.atomIsoVarsMap.get(atom)) {
                    VecInt v2 = new VecInt();
                    for (int j = 0; j < Formula.atomIsoVarsMap.get(atom)[0].length; j++) {
                        //for (int j: i) {
                        if (Formula.atomIsoVarsMap.get(atom)[i][j] > 0)
                            v2.push(Formula.atomIsoVarsMap.get(atom)[i][j]);
                        //if (j > 0)
                        //    v2.push(j);
                    }
                    v2.push(-Formula.atomVarsMap.get(atom));
                    solver.addExactly(v2, 1);
                }

                for (int j = 0; j < Formula.atomIsoVarsMap.get(atom)[0].length; j++) {
                    VecInt v3 = new VecInt();
                    for (int i = 0; i < Formula.atomIsoVarsMap.get(atom).length; i++) {
                        if (Formula.atomIsoVarsMap.get(atom)[i][j] > 0) {
                            v3.push(Formula.atomIsoVarsMap.get(atom)[i][j]);
                        }
                    }
                    v3.push(-Formula.atomVarsMap.get(atom));
                    solver.addAtMost(v3, 1);
                }
            }

            VecInt v4 = new VecInt();
            for (FormulaAtom  atom: atoms) {
                v4.push(Formula.atomVarsMap.get(atom));
            }
            mi.addExactly(v4, 1);

        } catch (FileNotFoundException e) {
            System.out.println("Error4");
            e.printStackTrace();
        } catch (ParseFormatException e) {
            System.out.println("Error5");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error6");
            e.printStackTrace();
        } catch (ContradictionException e) {
            //System.out.println("Unsatisfiable (trivial)!");
            problem = null;
        }
        finally {
            return problem;
        }
    }

    protected IProblem enumerateModels(HashSet<FormulaAtom> atoms, ISolver solver) {
        //Covert string to input stream

        ModelIterator mi = new ModelIterator(solver);
        solver.setTimeout(timeOut); // 1 hour timeout
        //Reader reader = new InstanceReader(mi);

        // filename is given on the command line
        try {

            VecInt v0 = new VecInt();
            for (int k : Formula.reqVars.values()) {
                v0.push(k);
            }
            solver.addExactly(v0, 1);

            for (FormulaAtom atom: atoms) {
                for (int i = 0; i < Formula.atomIsoVarsMap.get(atom).length; i++) {
                    //for (Integer[] i: Formula.atomIsoVarsMap.get(atom)) {
                    VecInt v2 = new VecInt();
                    for (int j = 0; j < Formula.atomIsoVarsMap.get(atom)[0].length; j++) {
                        //for (int j: i) {
                        if (Formula.atomIsoVarsMap.get(atom)[i][j] > 0)
                            v2.push(Formula.atomIsoVarsMap.get(atom)[i][j]);
                        //if (j > 0)
                        //    v2.push(j);
                    }
                    v2.push(-Formula.atomVarsMap.get(atom));
                    solver.addExactly(v2, 1);
                }

                for (int j = 0; j < Formula.atomIsoVarsMap.get(atom)[0].length; j++) {
                    VecInt v3 = new VecInt();
                    for (int i = 0; i < Formula.atomIsoVarsMap.get(atom).length; i++) {
                        if (Formula.atomIsoVarsMap.get(atom)[i][j] > 0) {
                            v3.push(Formula.atomIsoVarsMap.get(atom)[i][j]);
                        }
                    }
                    v3.push(-Formula.atomVarsMap.get(atom));
                    solver.addAtMost(v3, 1);
                }
            }

            VecInt v4 = new VecInt();
            for (FormulaAtom  atom: atoms) {
                v4.push(Formula.atomVarsMap.get(atom));
            }
            solver.addExactly(v4, 1);

        } catch (ContradictionException e) {
            //System.out.println("Unsatisfiable (trivial)!");
            mi = null;
        }
        finally {
            return mi;
        }
    }


    protected IProblem enumerateModels2(HashBiMap<Integer, Integer> atomIDVars, ISolver solver) {
        //Covert string to input stream

        ModelIterator mi = new ModelIterator(solver);
        solver.setTimeout(timeOut); // 1 hour timeout
        //Reader reader = new InstanceReader(mi);

        // filename is given on the command line
        try {
            VecInt v0 = new VecInt();
            for (int k : Formula.reqVars.values()) {
                v0.push(k);
            }
            solver.addExactly(v0, 1);


            VecInt v4 = new VecInt();
            for (Integer atomVar: atomIDVars.values()) {
                v4.push(atomVar);
            }
            solver.addExactly(v4, 1);

        } catch (ContradictionException e) {
            //System.out.println("Unsatisfiable (trivial)!");
            mi = null;
        }
        finally {
            return mi;
        }
    }

    protected IProblem enumerateModels(String dimacs, HashSet<FormulaAtom> positiveAtoms, HashSet<Integer> deltaVars, int distance, ISolver solver) {
        IProblem problem = null;
        InputStream is = new ByteArrayInputStream(dimacs.getBytes());

        ModelIterator mi = new ModelIterator(solver);
        solver.setTimeout(timeOut); // 1 hour timeout
        Reader reader = new DimacsReader(mi);

        try {

            long startTime = System.nanoTime();
            problem = reader.parseInstance(is);
            long elapsedTime = System.nanoTime() - startTime;
            sat4JCompileTime += (((double) elapsedTime / 1000000000.0));

            VecInt v0 = new VecInt();
            for (int k : Formula.reqVars.values()) {
                v0.push(k);
            }
            solver.addExactly(v0, 1);

            VecInt v1 = new VecInt();
            for (Integer deltaVar : deltaVars) {
                v1.push(deltaVar);
            }
            solver.addAtMost(v1, distance);

            if (!positiveAtoms.isEmpty()) {
                for (FormulaAtom atom : positiveAtoms) {
                    for (int i = 0; i < Formula.atomIsoVarsMap.get(atom).length; i++) {
                        VecInt v2 = new VecInt();
                        for (int j = 0; j < Formula.atomIsoVarsMap.get(atom)[0].length; j++) {
                            if (Formula.atomIsoVarsMap.get(atom)[i][j] > 0)
                                v2.push(Formula.atomIsoVarsMap.get(atom)[i][j]);
                        }
                        solver.addExactly(v2, 1);
                    }

                    for (int j = 0; j < Formula.atomIsoVarsMap.get(atom)[0].length; j++) {
                        VecInt v3 = new VecInt();
                        for (int i = 0; i < Formula.atomIsoVarsMap.get(atom).length; i++) {
                            if (Formula.atomIsoVarsMap.get(atom)[i][j] > 0) {
                                v3.push(Formula.atomIsoVarsMap.get(atom)[i][j]);
                            }
                        }
                        solver.addAtMost(v3, 1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error4");
            e.printStackTrace();
        } catch (ParseFormatException e) {
            System.out.println("Error5");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error6");
            e.printStackTrace();
        } catch (ContradictionException e) {
            //System.out.println("Unsatisfiable (trivial)!");
            problem = null;
        } finally {
            return problem;
        }
    }

    protected IProblem enumerateModels(HashSet<FormulaAtom> positiveAtoms, HashSet<Integer> deltaVars, int distance, ISolver solver) {
        ModelIterator mi = new ModelIterator(solver);
        solver.setTimeout(timeOut); // 1 hour timeout
        try {
            VecInt v0 = new VecInt();
            for (int k : Formula.reqVars.values()) {
                v0.push(k);
            }
            solver.addExactly(v0, 1);

            VecInt v1 = new VecInt();
            for (Integer deltaVar : deltaVars) {
                v1.push(deltaVar);
            }
            solver.addAtMost(v1, distance);

            if (!positiveAtoms.isEmpty()) {
                for (FormulaAtom atom : positiveAtoms) {
                    for (int i = 0; i < Formula.atomIsoVarsMap.get(atom).length; i++) {
                        VecInt v2 = new VecInt();
                        for (int j = 0; j < Formula.atomIsoVarsMap.get(atom)[0].length; j++) {
                            if (Formula.atomIsoVarsMap.get(atom)[i][j] > 0)
                                v2.push(Formula.atomIsoVarsMap.get(atom)[i][j]);
                        }
                        v2.push(-Formula.atomVarsMap.get(atom));
                        solver.addExactly(v2, 1);
                    }

                    for (int j = 0; j < Formula.atomIsoVarsMap.get(atom)[0].length; j++) {
                        VecInt v3 = new VecInt();
                        for (int i = 0; i < Formula.atomIsoVarsMap.get(atom).length; i++) {
                            if (Formula.atomIsoVarsMap.get(atom)[i][j] > 0) {
                                v3.push(Formula.atomIsoVarsMap.get(atom)[i][j]);
                            }
                        }
                        v3.push(-Formula.atomVarsMap.get(atom));
                        solver.addAtMost(v3, 1);
                    }
                }
            }

            VecInt v4 = new VecInt();
            for (FormulaAtom  atom: positiveAtoms) {
                v4.push(Formula.atomVarsMap.get(atom));
            }
            solver.addExactly(v4, 1);
        } catch (ContradictionException e) {
            mi = null;
        } finally {
            return mi;
        }
    }

    protected void enumerateAtoms(Formula formula, HashSet<FormulaAtom> atoms) {
        if (formula instanceof FormulaAtom) {
            FormulaAtom a = (FormulaAtom) formula;
            if(!atoms.contains(a)) {
                atoms.add(a);
            }
        }
        else if (formula instanceof FormulaAnd) {
            FormulaAnd af = (FormulaAnd) formula;
            enumerateAtoms(af.formula1, atoms);
            enumerateAtoms(af.formula2, atoms);
        }
        else if (formula instanceof FormulaOr) {
            FormulaOr of = (FormulaOr) formula;
            enumerateAtoms(of.formula1, atoms);
            enumerateAtoms(of.formula2, atoms);
        }
        else if (formula instanceof FormulaNot) {
            FormulaNot nf = (FormulaNot) formula;
            enumerateAtoms(nf.formula, atoms);
        }

    }

    public void getAnchors(Formula formula, Set<Integer> anchors) {
        if (formula instanceof FormulaAnd) {
            FormulaAnd fa = (FormulaAnd) formula;
            getAnchors(fa.formula1, anchors);
            getAnchors(fa.formula2, anchors);
        }
        else if (formula instanceof FormulaOr) {
            FormulaOr fo = (FormulaOr) formula;
            getAnchors(fo.formula1, anchors);
            getAnchors(fo.formula2, anchors);
        }
        else if (formula instanceof FormulaNot) {
            FormulaNot fn = (FormulaNot) formula;
            getAnchors(fn.formula, anchors);
        }
        else {
            FormulaAtom ft = (FormulaAtom) formula;
            anchors.add(ft.anchor);
        }
    }

    private void getSubsets(List<Integer> superSet, int k, int idx, Set<Integer> current, Set<Set<Integer>> solution) {
        //successful stop clause
        if (current.size() == k) {
            Set<Integer> temp = new HashSet<Integer>(current){
                @Override
                public int hashCode() {
                    int hashCode = 0;
                    int counter = this.size()-1;
                    for (Integer i: this) {
                        hashCode = i*10^counter;
                        counter--;
                    }
                    return hashCode;
                }

                /*
                @Override
                public boolean equals(Object o) {
                    return this.hashCode() == o.hashCode();
                }
                */
            };
            if (!solution.contains(temp))
            solution.add(temp);
            return;
        }
        //unsuccessful stop clause
        if (idx == superSet.size()) return;

        Integer x = superSet.get(idx);
        current.add(x);
        Set<Integer> temp = new HashSet<Integer>(current){
            @Override
            public int hashCode() {
                int hashCode = 0;
                int counter = this.size()-1;
                for (Integer i: this) {
                    hashCode = i*10^counter;
                    counter--;
                }
                return hashCode;
            }

            /*
            @Override
            public boolean equals(Object o) {
                return this.hashCode() == o.hashCode();
            }
            */
        };
        if (!solution.contains(temp))
            solution.add(temp);
        //"guess" x is in the subset
        getSubsets(superSet, k, idx + 1, current, solution);
        current.remove(x);
        temp = new HashSet<Integer>(current){
            @Override
            public int hashCode() {
                int hashCode = 0;
                int counter = this.size()-1;
                for (Integer i: this) {
                    hashCode = i*10^counter;
                    counter--;
                }
                return hashCode;
            }

            /*
            @Override
            public boolean equals(Object o) {
                return this.hashCode() == o.hashCode();
            }
            */
        };
        if (!solution.contains(temp))
            solution.add(temp);
        //"guess" x is not in the subset
        getSubsets(superSet, k, idx+1, current, solution);
    }

    public Set<Set<Integer>> getSubsets(List<Integer> superSet, int k) {
        Set<Set<Integer>> res = new HashSet<Set<Integer>>();
        getSubsets(superSet, k, 0, new HashSet<Integer>(), res);
        return res;
    }
}

