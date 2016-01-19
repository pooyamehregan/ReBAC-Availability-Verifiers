package com.company;

import com.apple.eawt.SystemSleepListener;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Pooya on 15-08-20.
 */
public class ASP {
    protected Graph graph;
    protected Formula formula;
    protected int atomCounter;

    public ASP (Entailment entailment) {
        this.graph = entailment.graph;
        this.formula = entailment.formula;
    }

    public StringBuilder translate (Entailment entailment, int k, int d, Formula positiveFormula, Formula negativeFromula) {
        if (entailment instanceof Satisfiability2) {
            return computeSatisfiability(k, positiveFormula, negativeFromula);
        } else if (entailment instanceof Feasibility2) {
            return computeFeasibilty(d, k, positiveFormula, negativeFromula);
        } else
            return computeResiliency(d, k, positiveFormula, negativeFromula);
    }

    private void translateAtoms(Formula formula, StringBuilder result, Set<StringBuilder> atoms, boolean isFormulaPositive) {

        if (formula instanceof FormulaAtom) {
            FormulaAtom formulaAtom = (FormulaAtom) formula;
            atomCounter++;
            if (isFormulaPositive) {
                atoms.add(new StringBuilder("alpha" + atomCounter));
                result.append("alpha" + atomCounter + "(R) :- ");
            } else {
                atoms.add(new StringBuilder("beta" + atomCounter));
                result.append("beta" + atomCounter + "(R) :- ");
            }
            boolean first = true;
            for (int i: formulaAtom.graphPattern.vertices) {
                for (int j: formulaAtom.graphPattern.vertices) {
                    if (i != j) {
                        String from = "";
                        String to = "";
                        if (i == formulaAtom.graphPattern.anchor) {
                            from = formulaAtom.anchor.toString();
                        } else if (i == formulaAtom.graphPattern.requester) {
                            from = "R";
                        } else {
                            from = "X" + i;
                        }
                        if (j == formulaAtom.graphPattern.anchor) {
                            to = formulaAtom.anchor.toString();
                        } else if (j == formulaAtom.graphPattern.requester) {
                            to = "R";
                        } else {
                            to = "X" + j;
                        }

                        for (int l : formulaAtom.graphPattern.labels) {
                            if (formulaAtom.graphPattern.outEdges[l][i].contains(j)) {
                                if (!first)
                                    result.append(", ");
                                result.append("e(" + l + "," + from + "," + to + ")");
                                first = false;
                            }
                        }

                        if (!first) {
                            result.append(", " + from + "!=" + to);
                        } else {
                            result.append(from + "!=" + to);
                            first = false;
                        }
                    }
                }
            }

            result.append(".").append(System.lineSeparator());

        } else if (formula instanceof FormulaNot) {
            FormulaNot formulaNot = (FormulaNot) formula;
            translateAtoms(formulaNot.formula, result, atoms, isFormulaPositive);
        } else if (formula instanceof FormulaAnd) {
            FormulaAnd formulaAnd = (FormulaAnd) formula;
            translateAtoms(formulaAnd.formula1, result, atoms, isFormulaPositive);
            translateAtoms(formulaAnd.formula2, result, atoms, isFormulaPositive);
        } else {
            FormulaOr formulaOr = (FormulaOr) formula;
            translateAtoms(formulaOr.formula1, result, atoms, isFormulaPositive);
            translateAtoms(formulaOr.formula2, result, atoms, isFormulaPositive);
        }
    }

    private void translatePhi(StringBuilder result, Set<StringBuilder> positiveAtoms, Set<StringBuilder> negativeAtoms) {
        for (StringBuilder pa: positiveAtoms) {
            result.append("phi(R) :- ").append(pa).append("(R)");
            for (StringBuilder na: negativeAtoms) {
                result.append(", ").append("not ").append(na).append("(R)");
            }
            result.append(".").append(System.lineSeparator());
        }
    }

    private void translateK(StringBuilder result, int k) {
        result.append(k + " {access(R):phi(R)}.").append(System.lineSeparator());
    }

    private void translateD(StringBuilder result, int d) {
        result.append("0 {mutate(L,I,J) : mutable(L,I,J)} " + d + ".").append(System.lineSeparator());
    }

    private void translateKAsIntegrity(StringBuilder result, int k) {
        result.append(":- " + k + " {access(R):phi(R)}.").append(System.lineSeparator());
    }

    private void translateEdges(StringBuilder result, Set<Integer> anchors) {
        for (int l: graph.labels) {
            for (int i : graph.vertices) {
                for (int j: graph.outEdges[l][i]) {
                    if (anchors.isEmpty()) {
                        result.append("e(" + l + "," + i + "," + j + ").").append(System.lineSeparator());
                    } else {
                        if (!anchors.contains(i) || !anchors.contains(j))
                            result.append("e(" + l + "," + i + "," + j + ").").append(System.lineSeparator());
                    }
                }
            }
        }
    }

    private void translateMutations(StringBuilder result, Set<Integer> anchors) {
        for (int l: graph.labels) {
            for (int i : anchors) {
                for (int j : anchors) {
                    if (graph.outEdges[l][i].contains(j)) {
                        result.append("e(" + l + "," + i + "," + j + ") :- not mutate(" + l + "," + i + "," + j +").").append(System.lineSeparator());
                    } else {
                        result.append("e(" + l + "," + i + "," + j + ") :- mutate(" + l + "," + i + "," + j +").").append(System.lineSeparator());
                    }
                    result.append("mutable(" + l + "," + i + "," + j + ").").append(System.lineSeparator());
                }
            }
        }
    }

    private void collectAtoms(Formula formula, Set<FormulaAtom> atoms) {
        if (formula instanceof FormulaAnd) {
            collectAtoms(((FormulaAnd) formula).formula1, atoms);
            collectAtoms(((FormulaAnd) formula).formula2, atoms);
        } else if (formula instanceof FormulaAtom) {
            atoms.add((FormulaAtom) formula);
        } else if (formula instanceof FormulaNot) {
            collectAtoms(((FormulaNot) formula).formula, atoms);
        } else {
            collectAtoms(((FormulaOr) formula).formula1, atoms);
            collectAtoms(((FormulaOr) formula).formula2, atoms);
        }

    }

    private StringBuilder computeSatisfiability(int k, Formula positiveFormula, Formula negativeFromula){
        StringBuilder result = new StringBuilder();
        Set<StringBuilder> positiveAtoms = new HashSet<StringBuilder>();
        Set<StringBuilder> negativeAtoms = new HashSet<StringBuilder>();

        result.append("% Facts").append(System.lineSeparator())
                .append("% Encoding Edges").append(System.lineSeparator());
        translateEdges(result, new HashSet<Integer>());

        result.append("% Generating").append(System.lineSeparator())
                .append("% Encoding k (= the number of requesters) in k-Sat").append(System.lineSeparator());
        translateK(result, k);

        result.append(System.lineSeparator()).append("% Defining").append(System.lineSeparator())
        .append("% Encoding the Atoms (Patterns and Owners)").append(System.lineSeparator());
        atomCounter = 0;
        translateAtoms(positiveFormula, result, positiveAtoms, true);
        translateAtoms(negativeFromula, result, negativeAtoms, false);

        result.append(System.lineSeparator()).append("% Encoding the policy phi")
                .append(System.lineSeparator());
        translatePhi(result, positiveAtoms, negativeAtoms);

        result.append(System.lineSeparator()).append("% Displaying").append(System.lineSeparator())
                .append("#show access/1.");
        return result;
    }

    private StringBuilder computeFeasibilty(int d, int k, Formula positiveFormula, Formula negativeFromula){
        StringBuilder result = new StringBuilder();
        Set<StringBuilder> positiveAtoms = new HashSet<StringBuilder>();
        Set<StringBuilder> negativeAtoms = new HashSet<StringBuilder>();
        Set<FormulaAtom> atoms = new HashSet<FormulaAtom>();

        collectAtoms(this.formula, atoms);
        Set<Integer> anchors = new HashSet<Integer>();
        for (FormulaAtom a: atoms) {
            anchors.add(a.anchor);
        }
        result.append("% Fatcs").append(System.lineSeparator()).append("% Encoding Immutable Edges").append(System.lineSeparator());
        translateEdges(result, anchors);

        result.append("% Generating").append(System.lineSeparator());

        result.append("% Encoding d (= the number of mutations) in (k,d)-Fea").append(System.lineSeparator());
        translateD(result, d);

        result.append("% Encoding k (= the number of requesters) in (k,d)-Fea").append(System.lineSeparator());
        translateK(result, k);

        result.append(System.lineSeparator()).append("% Defining").append(System.lineSeparator())
                .append("% Encoding the Atoms (Patterns and Owners)").append(System.lineSeparator());
        atomCounter = 0;
        translateAtoms(positiveFormula, result, positiveAtoms, true);
        translateAtoms(negativeFromula, result, negativeAtoms, false);

        result.append(System.lineSeparator()).append("% Encoding the policy phi").
                append(System.lineSeparator());
        translatePhi(result, positiveAtoms, negativeAtoms);

        result.append(System.lineSeparator()).append("% Encoding Mutable Edges").append(System.lineSeparator());
        translateMutations(result, anchors);

        result.append(System.lineSeparator()).append("% Displaying").append(System.lineSeparator())
                .append("#show mutate/3.").append(System.lineSeparator()).append("#show access/1.");
        return result;
    }

    private StringBuilder computeResiliency(int d, int k, Formula positiveFormula, Formula negativeFromula){
        StringBuilder result = new StringBuilder();
        Set<StringBuilder> positiveAtoms = new HashSet<StringBuilder>();
        Set<StringBuilder> negativeAtoms = new HashSet<StringBuilder>();
        Set<FormulaAtom> atoms = new HashSet<FormulaAtom>();

        collectAtoms(this.formula, atoms);
        Set<Integer> anchors = new HashSet<Integer>();
        for (FormulaAtom a: atoms) {
            anchors.add(a.anchor);
        }
        result.append("% Facts").append(System.lineSeparator()).append("% Encoding Immutable Edges").append(System.lineSeparator());
        translateEdges(result, anchors);

        result.append("% Generating").append(System.lineSeparator())
        .append("access(R) :- phi(R).").append(System.lineSeparator());

        result.append("% Encoding d (= the number of mutations) in (k,d)-Fea").append(System.lineSeparator());
        translateD(result, d);

        result.append(System.lineSeparator()).append("% Defining").append(System.lineSeparator())
                .append("% Encoding the Atoms (Patterns and Owners)").append(System.lineSeparator());
        atomCounter = 0;
        translateAtoms(positiveFormula, result, positiveAtoms, true);
        translateAtoms(negativeFromula, result, negativeAtoms, false);

        result.append(System.lineSeparator()).append("% Encoding the policy phi").
                append(System.lineSeparator());
        translatePhi(result, positiveAtoms, negativeAtoms);


        result.append(System.lineSeparator()).append("% Encoding Mutable Edges").append(System.lineSeparator());
        translateMutations(result, anchors);

        result.append("% Testing").append(System.lineSeparator())
                .append("% Encoding k (= the number of requesters) in (k,d)-Fea As an Integrity Constraint")
                .append(System.lineSeparator());
        translateKAsIntegrity(result, k);

        result.append(System.lineSeparator()).append("% Displaying").append(System.lineSeparator())
                .append("#show mutate/3.").append(System.lineSeparator()).append("#show access/1.");
        return result;
    }
}
