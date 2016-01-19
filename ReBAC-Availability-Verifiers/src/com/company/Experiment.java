package com.company;

import com.google.common.collect.HashBiMap;
import org.sat4j.specs.TimeoutException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Pooya on 15-02-22.
 */
public class Experiment {

    //Attributes
    private int repetition;
    private String entailmentType;
    protected int k;
    private int numAtoms;
    private int numPosAtoms;
    private int numNegAtoms;
    private int numVertices;
    private int numLabels;
    private double probOrAvgDeg;
    private int gpPosNumVer;
    private int gpNegNumVer;
    private int gpnumVertices;
    private int gpnumLabels;
    private double gpprobOrAvgDeg;
    private ArrayList<Double> totalConvertTimes;
    private ArrayList<String> results;
    private ArrayList<String> results2;
    private ArrayList<String> results3;
    private Statistics statistics1;
    private Statistics statistics2;
    private Statistics statistics3;
    private String args[];
    private int timeOut;

    public Graph graph;
    public Formula formula1;
    public Formula formula2;
    public Formula formula3;
    public Formula positiveFormula1;
    public Formula negativeFormula1;
    public Formula positiveFormula2;
    public Formula negativeFormula2;
    public int distance;



    //Constructors
    public Experiment(String args[]) {
        repetition = Integer.parseInt(args[0]);
        entailmentType = args[1];
        k = Integer.parseInt(args[2]);
        numPosAtoms = Integer.parseInt(args[3]);
        numNegAtoms = Integer.parseInt(args[4]);
        numVertices = Integer.parseInt(args[5]);
        numLabels = Integer.parseInt(args[6]);
        probOrAvgDeg = Double.parseDouble(args[7]);
        gpPosNumVer = Integer.parseInt(args[8]);
        gpNegNumVer = Integer.parseInt(args[9]);
        gpnumLabels = Integer.parseInt(args[10]);
        gpprobOrAvgDeg = Double.parseDouble(args[11]);
        distance = Integer.parseInt(args[12]);
        timeOut = Integer.parseInt(args[13]);
        /*
        k = Integer.parseInt(args[1]);
        repetition = Integer.parseInt(args[2]);
        numVertices = Integer.parseInt(args[3]);
        numLabels = Integer.parseInt(args[4]);
        probOrAvgDeg = Double.parseDouble(args[5]);
        gpnumVertices = Integer.parseInt(args[6]);
        gpnumLabels = Integer.parseInt(args[7]);
        gpprobOrAvgDeg = Double.parseDouble(args[8]);
        distance = Integer.parseInt(args[9]);
        */
        totalConvertTimes = new ArrayList<Double>();
        results = new ArrayList<String>();
        results2 = new ArrayList<String>();
        results3 = new ArrayList<String>();
        statistics1 = new Statistics();
        statistics2 = new Statistics();
        statistics3 = new Statistics();
        this.args = args;

        RandomUtil.random.setSeed(System.nanoTime());

        Formula.atomVarsMap = HashBiMap.create();
        Formula.atomIDVars = HashBiMap.create();
        Formula.atomQBFsMap = new HashMap<FormulaAtom, QBF>();
        Formula.atomIsoVarsMap = new HashMap<FormulaAtom, Integer[][]>();
        Entailment.timeOut = this.timeOut;
    }

    public Experiment(int numVertices, int numLabels, double probOrAvgDeg, int gpnumVertices, int gpnumLabels,
                      double gpprobOrAvgDeg, int distance, int numAtoms, int numClauses, int k) {
        this.numVertices = numVertices;
        this.numLabels = numLabels;
        this.probOrAvgDeg = probOrAvgDeg;
        this.gpnumVertices = gpnumVertices;
        this.gpnumLabels = gpnumLabels;
        this.gpprobOrAvgDeg = gpprobOrAvgDeg;
        this.distance = distance;
        this.numAtoms = numAtoms;
        this.k = k;

        totalConvertTimes = new ArrayList<Double>();
        results = new ArrayList<String>();
        results2 = new ArrayList<String>();
        results3 = new ArrayList<String>();
        statistics1 = new Statistics();
        statistics2 = new Statistics();

        RandomUtil.random.setSeed(System.nanoTime());

        Formula.atomVarsMap = HashBiMap.create();
        Formula.atomQBFsMap = new HashMap<FormulaAtom, QBF>();
        Formula.atomIsoVarsMap = new HashMap<FormulaAtom, Integer[][]>();
        Entailment.timeOut = this.timeOut;
    }

    public Experiment(int k, int numPosAtoms, int numNegAtoms, int numVertices, int numLabels,
                      double probOrAvgDeg, int gpPosNumVer, int gpNegNumVer, int gpnumLabels, double gpprobOrAvgDeg, int distance) {
        //this.repetition = repetition;
        //entailmentType = args[1];
        this.k = k;
        this.numPosAtoms = numPosAtoms;
        this.numNegAtoms = numNegAtoms;
        this.numVertices = numVertices;
        this.numLabels = numLabels;
        this.probOrAvgDeg = probOrAvgDeg;
        this.gpPosNumVer = gpPosNumVer;
        this.gpNegNumVer = gpNegNumVer;
        this.gpnumLabels = gpnumLabels;
        this.gpprobOrAvgDeg = gpprobOrAvgDeg;
        this.distance = distance;
        totalConvertTimes = new ArrayList<Double>();
        results = new ArrayList<String>();
        results2 = new ArrayList<String>();
        results3 = new ArrayList<String>();
        statistics1 = new Statistics();
        statistics2 = new Statistics();
        statistics3 = new Statistics();

        RandomUtil.random.setSeed(System.nanoTime());

        Formula.atomVarsMap = HashBiMap.create();
        Formula.atomIDVars = HashBiMap.create();
        Formula.atomQBFsMap = new HashMap<FormulaAtom, QBF>();
        Formula.atomIsoVarsMap = new HashMap<FormulaAtom, Integer[][]>();
        Entailment.timeOut = this.timeOut;
    }


    public void initialize(){
        Graph.labelSetSize = Integer.max(numLabels, gpnumLabels);
        FormulaGenerator fg = new FormulaGenerator();
        graph = fg.genRandomGraph(numVertices, numLabels, probOrAvgDeg);

        fg.randPosNegGen(numPosAtoms, numNegAtoms, gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg, numVertices);
        formula1 = fg.formula1;
        formula2 = fg.formula2;
        formula3 = fg.formula3;
        positiveFormula1 = fg.positiveFormula1;
        negativeFormula1 = fg.negativeFormula1;
        positiveFormula2 = fg.positiveFormula2;
        negativeFormula2 = fg.negativeFormula2;
    }

    public void writeOnFile(String str, String directory, int i) {
        try {
            String path =  System.getProperty("user.dir") + directory;//this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path + i);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(str);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeOnFile(String str, String fileName) {
        try {
            String path =  System.getProperty("user.dir") + "/" + fileName;//this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
//            else {
//                file.delete();
//                file.createNewFile();
//            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(str);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        Formula.atomIDVars.clear();
        Formula.atomVarsMap.clear();
        Formula.atomQBFsMap.clear();
        Formula.atomIsoVarsMap.clear();
        Formula.reqVars.clear();
        Formula.mutatedGraphVars = null;
    }

    public void sat_sat4J() {
        Satisfiability satisfiability = new Satisfiability(graph, formula1);
        boolean result = satisfiability.checkSat_Sat4J(k, positiveFormula1, negativeFormula1);
        results.add(result ? "SAT": "UNSAT");
        statistics1.sat4JCompileTime.add(satisfiability.sat4JCompileTime);
        statistics1.sat4JNumOfClauses.addAll(satisfiability.sat4JNumOfClauses);
        statistics1.sat4JNumOfIter.add(satisfiability.sat4JNumOfIter);
        statistics1.sat4JNumOfVars.addAll(satisfiability.sat4JNumOfVars);
        statistics1.sat4JTotalSolverTime.add(satisfiability.sat4JTotalSolverTime);
        writeOnFile(String.format("%s %d %d %d %.3f %d %d %d %f %d %d %d %d %f %f %f %b%s",
                entailmentType, k, numVertices, numLabels, probOrAvgDeg, gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg,
                (entailmentType.equals("s") ? 0 : distance), numPosAtoms, numNegAtoms, satisfiability.numModels,
                (satisfiability.modelTimes.size() > 0) ? Statistics.getMean(satisfiability.modelTimes) : 0,
                (satisfiability.modelTimes.size() > 0) ? Statistics.getStdDev(satisfiability.modelTimes) : 0,
                satisfiability.sat4JTotalSolverTime, result, System.lineSeparator()), "logs");
    }

    public void sat2_sat4J_vertexEnum() {
        Satisfiability2 satisfiability2 = new Satisfiability2(graph, formula1);
        boolean result = satisfiability2.checkSat_Sat4J_VE(k, true);
        results.add(result ? "SAT" : "UNSAT");
        statistics1.sat4JCompileTime.add(satisfiability2.sat4JCompileTime);
        statistics1.sat4JNumOfClauses.addAll(satisfiability2.sat4JNumOfClauses);
        statistics1.sat4JNumOfIter.add(satisfiability2.sat4JNumOfIter);
        statistics1.sat4JNumOfVars.addAll(satisfiability2.sat4JNumOfVars);
        statistics1.sat4JTotalSolverTime.add(satisfiability2.sat4JTotalSolverTime);
        writeOnFile(String.format("%s %d %d %d %.3f %d %d %d %.3f %d %d %d %d %f %f %f %b%s",
                entailmentType, k, numVertices, numLabels, probOrAvgDeg, gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg,
                (entailmentType.equals("s") ? 0 : distance), numPosAtoms, numNegAtoms, satisfiability2.sat4JNumOfIter,
                (satisfiability2.modelTimes.size() > 0) ? Statistics.getMean(satisfiability2.modelTimes) : 0,
                (satisfiability2.modelTimes.size() > 0) ? Statistics.getStdDev(satisfiability2.modelTimes) : 0,
                satisfiability2.sat4JTotalSolverTime, result, System.lineSeparator()), "logs");
    }

    public void sat2_sat4J_modelEnum_old() throws TimeoutException{
        Satisfiability2 satisfiability2 = new Satisfiability2(graph, formula2);
        boolean result = satisfiability2.checkSat_Sat4J_ME(k, positiveFormula1, negativeFormula1, false);
        results2.add(result ? "SAT" : "UNSAT");
        statistics2.sat4JCompileTime.add(satisfiability2.sat4JCompileTime);
        //statistics2.sat4JNumOfClauses.addAll(satisfiability2.sat4JNumOfClauses);
        statistics2.sat4JNumOfClauses_Pos.addAll(satisfiability2.sat4JNumOfClauses_Pos);
        statistics2.sat4JNumOfClauses_Neg.addAll(satisfiability2.sat4JNumOfClauses_Neg);
        statistics2.sat4JNumOfIter.add(satisfiability2.sat4JNumOfIter);
        //statistics2.sat4JNumOfVars.addAll(satisfiability2.sat4JNumOfVars);
        statistics2.sat4JNumOfVars_Pos.addAll(satisfiability2.sat4JNumOfVars_Pos);
        statistics2.sat4JNumOfVars_Neg.addAll(satisfiability2.sat4JNumOfVars_Neg);
        statistics2.sat4JTotalSolverTime.add(satisfiability2.sat4JTotalSolverTime);
        writeOnFile(String.format("%s %d %d %d %.3f %d %d %d %.3f %d %d %d %d %f %f %f %f %f %f %f %f %f %f %f %b%s",
                entailmentType, k, numVertices, numLabels, probOrAvgDeg, gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg,
                (entailmentType.equals("s") ? 0 : distance), numPosAtoms, numNegAtoms, satisfiability2.numModels,
                (satisfiability2.modelTimes.size() > 0) ? Statistics.getMean(satisfiability2.modelTimes) : 0,
                (satisfiability2.modelTimes.size() > 0) ? Statistics.getStdDev(satisfiability2.modelTimes) : 0,
                Statistics.getMean(satisfiability2.sat4JNumOfVars_Pos), Statistics.getStdDev(satisfiability2.sat4JNumOfVars_Pos),
                Statistics.getMean(satisfiability2.sat4JNumOfVars_Neg), Statistics.getStdDev(satisfiability2.sat4JNumOfVars_Neg),
                Statistics.getMean(satisfiability2.sat4JNumOfClauses_Pos), Statistics.getStdDev(satisfiability2.sat4JNumOfClauses_Pos),
                Statistics.getMean(satisfiability2.sat4JNumOfClauses_Neg), Statistics.getStdDev(satisfiability2.sat4JNumOfClauses_Neg),
                satisfiability2.sat4JTotalSolverTime, result, System.lineSeparator()), "logs2");
    }

    public void sat2_sat4J_modelEnum_new() throws TimeoutException {
        Satisfiability2 satisfiability2 = new Satisfiability2(graph, formula3);
        boolean result = satisfiability2.checkSat_Sat4J_ME(k, positiveFormula2, negativeFormula2, true);
        results3.add(result ? "SAT" : "UNSAT");
        statistics3.sat4JCompileTime.add(satisfiability2.sat4JCompileTime);
        //statistics3.sat4JNumOfClauses.addAll(satisfiability2.sat4JNumOfClauses);
        statistics3.sat4JNumOfClauses_Pos.addAll(satisfiability2.sat4JNumOfClauses_Pos);
        statistics3.sat4JNumOfClauses_Neg.addAll(satisfiability2.sat4JNumOfClauses_Neg);
        statistics3.sat4JNumOfIter.add(satisfiability2.sat4JNumOfIter);
        //statistics3.sat4JNumOfVars.addAll(satisfiability2.sat4JNumOfVars);
        statistics3.sat4JNumOfVars_Pos.addAll(satisfiability2.sat4JNumOfVars_Pos);
        statistics3.sat4JNumOfVars_Neg.addAll(satisfiability2.sat4JNumOfVars_Neg);
        statistics3.sat4JTotalSolverTime.add(satisfiability2.sat4JTotalSolverTime);
        writeOnFile(String.format("%s %d %d %d %.3f %d %d %d %.3f %d %d %d %d %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f %b%s",
                entailmentType, k, numVertices, numLabels, probOrAvgDeg, gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg,
                (entailmentType.equals("s") ? 0 : distance), numPosAtoms, numNegAtoms, satisfiability2.numModels,
                (satisfiability2.modelTimes.size() > 0) ? Statistics.getMean(satisfiability2.modelTimes) : 0,
                (satisfiability2.modelTimes.size() > 0) ? Statistics.getStdDev(satisfiability2.modelTimes) : 0,
                Statistics.getMean(satisfiability2.sat4JNumOfVars_Pos), Statistics.getStdDev(satisfiability2.sat4JNumOfVars_Pos),
                Statistics.getMean(satisfiability2.sat4JNumOfVars_Neg), Statistics.getStdDev(satisfiability2.sat4JNumOfVars_Neg),
                Statistics.getMean(satisfiability2.sat4JNumOfClauses_Pos), Statistics.getStdDev(satisfiability2.sat4JNumOfClauses_Pos),
                Statistics.getMean(satisfiability2.sat4JNumOfClauses_Neg), Statistics.getStdDev(satisfiability2.sat4JNumOfClauses_Neg),
                satisfiability2.sat4JTotalSolverTime, result, System.lineSeparator()), "logs3");
    }

    public void sat2_asp(int i) {
        Satisfiability2 satisfiability2 = new Satisfiability2(graph, formula1);
        ASP asp = new ASP(satisfiability2);
        //System.out.println(asp.translate(satisfiability2, k, 0, positiveFormula2, negativeFormula2));
        writeOnFile(asp.translate(satisfiability2, k, 0, positiveFormula1, negativeFormula1).toString(), "/asps/asp", i);
    }

    public void fea_sat4J() {
        Feasibility feasibility = new Feasibility(graph, formula1, distance);
        boolean result = feasibility.checkSat_Sat4J();
        results.add(result ? "SAT" : "UNSAT");
        statistics1.sat4JCompileTime.add(feasibility.sat4JCompileTime);
        statistics1.sat4JTotalSolverTime.add(feasibility.sat4JTotalSolverTime);
    }

    public void fea2_sat4J() {
        Feasibility2 feasibility2 = new Feasibility2(graph, formula1, distance);
        boolean result = feasibility2.checkSat_Sat4J();
        results.add(result ? "SAT" : "UNSAT");
        statistics1.sat4JCompileTime.add(feasibility2.sat4JCompileTime);
        statistics1.sat4JTotalSolverTime.add(feasibility2.sat4JTotalSolverTime);
    }

    public void fea2_sat4J_modelEnum() throws TimeoutException {
        Feasibility2 feasibility2 = new Feasibility2(graph, formula1, distance);
        boolean result = feasibility2.checkSat_Sat4J_ME(distance, k, positiveFormula1, negativeFormula1, true);
        results3.add(result ? "SAT" : "UNSAT");
        statistics3.sat4JCompileTime.add(feasibility2.sat4JCompileTime);
        statistics3.sat4JTotalSolverTime.add(feasibility2.sat4JTotalSolverTime);
        writeOnFile(String.format("%s %d %d %d %.3f %d %d %d %.3f %d %d %d %d %f %b%s",
                entailmentType, k, numVertices, numLabels, probOrAvgDeg, gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg,
                (entailmentType.equals("s") ? 0 : distance), numPosAtoms, numNegAtoms, feasibility2.sat4JNumOfIter,
                feasibility2.sat4JTotalSolverTime, result, System.lineSeparator()), "logs3");
    }

    public void fea2_sat4J_CNF() {
        Feasibility2 feasibility2 = new Feasibility2(graph, formula2, distance);
        boolean result = feasibility2.checkSat_Sat4J(positiveFormula2, negativeFormula2);
        results2.add(result ? "SAT" : "UNSAT");
        statistics2.sat4JCompileTime.add(feasibility2.sat4JCompileTime);
        statistics2.sat4JTotalSolverTime.add(feasibility2.sat4JTotalSolverTime);
        writeOnFile(String.format("%s %d %d %d %.3f %d %d %d %.3f %d %d %d %d %f %f %f %b%s",
                entailmentType, k, numVertices, numLabels, probOrAvgDeg, gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg,
                (entailmentType.equals("s") ? 0 : distance), numPosAtoms, numNegAtoms, feasibility2.numModels,
                (feasibility2.modelTimes.size() > 0) ? Statistics.getMean(feasibility2.modelTimes) : 0,
                (feasibility2.modelTimes.size() > 0) ? Statistics.getStdDev(feasibility2.modelTimes) : 0,
                feasibility2.sat4JTotalSolverTime, result, System.lineSeparator()), "logs2");
    }

    public void fea2_asp(int i) {
        Feasibility2 feasibility2 = new Feasibility2(graph, formula1, distance);
        ASP asp = new ASP(feasibility2);
        //System.out.println(asp.translate(satisfiability2, k, 0, positiveFormula2, negativeFormula2));
        writeOnFile(asp.translate(feasibility2, k, distance, positiveFormula1, negativeFormula1).toString(), "/asps/asp", i);
    }

    public void res_sat4J() {
        Resiliency resiliency = new Resiliency(graph, formula1, distance);
        boolean result = resiliency.checkSat_Sat4J();
        results.add(result ? "SAT" : "UNSAT");
        statistics1.sat4JTotalSolverTime.add(resiliency.sat4JTotalSolverTime);
    }

    public void res_sat4J_modelEnum() throws TimeoutException {
        Resiliency resiliency = new Resiliency(graph, formula1, distance);
        boolean result = resiliency.checkSat_Sat4J_ME(distance, k, positiveFormula1, negativeFormula1, true);
        results3.add(result ? "SAT" : "UNSAT");
        statistics3.sat4JCompileTime.add(resiliency.sat4JCompileTime);
        statistics3.sat4JTotalSolverTime.add(resiliency.sat4JTotalSolverTime);
        writeOnFile(String.format("%s %d %d %d %.3f %d %d %d %.3f %d %d %d %d %f %b%s",
                entailmentType, k, numVertices, numLabels, probOrAvgDeg, gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg,
                (entailmentType.equals("s") ? 0 : distance), numPosAtoms, numNegAtoms, resiliency.sat4JNumOfIter,
                resiliency.sat4JTotalSolverTime, result, System.lineSeparator()), "logs3");
    }

    public void res_asp(int i) {
        Resiliency resiliency = new Resiliency(graph, formula1, distance);
        ASP asp = new ASP(resiliency);
        //System.out.println(asp.translate(satisfiability2, k, 0, positiveFormula2, negativeFormula2));
        writeOnFile(asp.translate(resiliency, k, distance, positiveFormula1, negativeFormula1).toString(), "/asps/asp", i);
    }

    public void sat_qbfSolver(int i) {
        Satisfiability satisfiability = new Satisfiability(graph, formula3);
        long startTime = System.nanoTime();
        String str = satisfiability.checkSat_QCIR();
        long elapsedTime = System.nanoTime() - startTime;
        double seconds = (double) elapsedTime / 1000000000.0;
        totalConvertTimes.add(seconds);
        statistics3.formula2QBFTimes.add(satisfiability.formula2QBFTimes);
        statistics3.NNFTimes.add(satisfiability.NNFTimes);
        statistics3.prenexTimes.add(satisfiability.prenexTimes);
        statistics3.QCIRCompileTime.add(satisfiability.QCIRCompileTime);
        statistics3.QCIRNumOfVars.add(satisfiability.QCIRNumOfVars);
        writeOnFile(str, "/qcirs/qcir", i);
    }

    public void fea_qbfSolver(int i) {
        Feasibility feasibility = new Feasibility(graph, formula3, distance);
        long startTime = System.nanoTime();
        String str = feasibility.checkSat_QCIR();
        long elapsedTime = System.nanoTime() - startTime;
        double seconds = (double) elapsedTime / 1000000000.0;
        totalConvertTimes.add(seconds);
        statistics1.formula2QBFTimes.add(feasibility.formula2QBFTimes);
        statistics1.NNFTimes.add(feasibility.NNFTimes);
        statistics1.prenexTimes.add(feasibility.prenexTimes);
        statistics1.QCIRCompileTime.add(feasibility.QCIRCompileTime);
        statistics1.QCIRNumOfVars.add(feasibility.QCIRNumOfVars);
        writeOnFile(str, "/qcirs/qcir", i);
    }

    public void res_qbfSolver(int i) {
        Resiliency resiliency = new Resiliency(graph, formula3, distance);
        long startTime = System.nanoTime();
        String str = resiliency.checkSat_QCIR();
        long elapsedTime = System.nanoTime() - startTime;
        double seconds = (double) elapsedTime / 1000000000.0;
        totalConvertTimes.add(seconds);
        statistics1.formula2QBFTimes.add(resiliency.formula2QBFTimes);
        statistics1.NNFTimes.add(resiliency.NNFTimes);
        statistics1.prenexTimes.add(resiliency.prenexTimes);
        statistics1.QCIRCompileTime.add(resiliency.QCIRCompileTime);
        statistics1.QCIRNumOfVars.add(resiliency.QCIRNumOfVars);
        writeOnFile(str, "/qcirs/qcir", i);
    }

    public void sat2_qbfSolver(int i) {
        Satisfiability2 satisfiability = new Satisfiability2(graph, formula3);
        long startTime = System.nanoTime();
        String str = satisfiability.checkSat_QCIR();
        long elapsedTime = System.nanoTime() - startTime;
        double seconds = (double) elapsedTime / 1000000000.0;
        totalConvertTimes.add(seconds);
        statistics3.formula2QBFTimes.add(satisfiability.formula2QBFTimes);
        statistics3.NNFTimes.add(satisfiability.NNFTimes);
        statistics3.prenexTimes.add(satisfiability.prenexTimes);
        statistics3.QCIRCompileTime.add(satisfiability.QCIRCompileTime);
        statistics3.QCIRNumOfVars.add(satisfiability.QCIRNumOfVars);
        writeOnFile(str, "/qcirs/qcir", i);
    }

    private void computeSatisfiability() {
        int counter = 0;
        do {
            initialize();
            counter++;
//            sat2_sat4J_vertexEnum();
//            reset();
            sat2_asp(counter);
//            try {
//                System.out.println(counter);
//                sat2_sat4J_modelEnum_new();
//            } catch (TimeoutException e) {
//                System.out.println("Time Out!");
//                counter--;
//            }
//            reset();
//            sat2_qbfSolver(counter);
//            reset();
        } while (counter < repetition);
//        for (int i = 0; i < repetition; i++) {
//            initialize();
//            sat2_sat4J_vertexEnum();
//            reset();
//            sat2_qbfSolver(i);
//            sat2_sat4J_modelEnum_old();
//            reset();
//            sat2_sat4J_modelEnum_new();
//            reset();
//        }
    }

    private void computeFeasibility() {
        Formula.atomVarsMap = HashBiMap.create();
        int counter = 0;
        do {
            initialize();
            counter++;
            fea2_asp(counter);
//            try {
//                System.out.println(counter);
//                fea2_sat4J_modelEnum();
//            } catch (TimeoutException e) {
//                System.out.println("Time Out!");
//                counter--;
//            }
//            reset();
        } while (counter < repetition);
    }

    private void computeResiliency() {
//        for (int i = 0; i < repetition; i++) {
//            initialize();
//            res_sat4J();
//            reset();
//            res_qbfSolver(i);
//            reset();
//        }
        Formula.atomVarsMap = HashBiMap.create();
        int counter = 0;
        do {
            initialize();
            counter++;
            res_asp(counter);
//            try {
//                System.out.println(counter);
//                res_sat4J_modelEnum();
//            } catch (TimeoutException e) {
//                System.out.println("Time Out!");
//                counter--;
//            }
//            reset();
        } while (counter < repetition);
    }

    public void execute() {
        if (entailmentType.equals("s")) {
            computeSatisfiability();
        } else if (entailmentType.equals("f")) {
            computeFeasibility();
        } else {
            computeResiliency();
        }
        StringBuilder tempString = new StringBuilder();
        for (String str : results) {
            tempString.append((str.equals("SAT")) ? 1 : 0).append("; ");
        }

        StringBuilder tempString2 = new StringBuilder();
        for (String str : results2) {
            tempString2.append((str.equals("SAT")) ? 1 : 0).append("; ");
        }

        StringBuilder tempString3 = new StringBuilder();
        for (String str : results3) {
            tempString3.append((str.equals("SAT")) ? 1 : 0).append("; ");
        }

        StringBuilder sb1 = new StringBuilder();
        for (Integer posVar : statistics2.sat4JNumOfVars_Pos)
            sb1.append(posVar).append("; ");

        StringBuilder sb2 = new StringBuilder();
        for (Integer negVar : statistics2.sat4JNumOfVars_Neg)
            sb2.append(negVar).append("; ");

        StringBuilder sb3 = new StringBuilder();
        for (Integer posClause : statistics2.sat4JNumOfClauses_Pos)
            sb3.append(posClause).append("; ");

        StringBuilder sb4 = new StringBuilder();
        for (Integer negClause : statistics2.sat4JNumOfClauses_Neg)
            sb4.append(negClause).append("; ");

        StringBuilder sb5 = new StringBuilder();
        for (Integer posVar : statistics3.sat4JNumOfVars_Pos)
            sb5.append(posVar).append("; ");

        StringBuilder sb6 = new StringBuilder();
        for (Integer negVar : statistics3.sat4JNumOfVars_Neg)
            sb6.append(negVar).append("; ");

        StringBuilder sb7 = new StringBuilder();
        for (Integer posClause : statistics3.sat4JNumOfClauses_Pos)
            sb7.append(posClause).append("; ");

        StringBuilder sb8 = new StringBuilder();
        for (Integer negClause : statistics3.sat4JNumOfClauses_Neg)
            sb8.append(negClause).append("; ");

        System.out.println("\nTimeout set to: " + timeOut + " (ms)");
        System.out.format("Entailment Type:\t%s%sNumber of Experiment Repetition:\t%d%sk:\t%d%sNumber of Positive Atoms:\t%d%sNumber of Negative Atoms:\t%d%sNumber of Graph Vertices:\t%d%sNumber of Graph Labels:\t%d%sEdge Probability or Average Degree in Graph:\t%.3f%sNumber of positive graph pattern vertices:\t%d%sNumber of negative graph pattern vertices:\t%d%sNumber of Graph Pattern Labels:\t%d%sEdge Probability or Average Degree in Graph Pattern:\t%.3f%sDistance:\t%d%s",
                entailmentType, System.lineSeparator(), repetition, System.lineSeparator(), k, System.lineSeparator(), numPosAtoms, System.lineSeparator(), numNegAtoms, System.lineSeparator(), numVertices, System.lineSeparator(), numLabels, System.lineSeparator(), probOrAvgDeg, System.lineSeparator(), gpPosNumVer, System.lineSeparator(), gpNegNumVer, System.lineSeparator(), gpnumLabels, System.lineSeparator(), gpprobOrAvgDeg, System.lineSeparator(), distance, System.lineSeparator());


//        System.out.format("Sat4J_VertexEnum_New_Reduce Compile Time (AVG, STD):\t%f\t%f%s", Statistics.getMean(statistics1.sat4JCompileTime), Statistics.getStdDev(statistics1.sat4JCompileTime), System.lineSeparator());
//        System.out.format("Sat4J_VertexEnum_New_Reduce Total Solver Time (AVG, STD):\t%f\t%f%s", Statistics.getMean(statistics1.sat4JTotalSolverTime), Statistics.getStdDev(statistics1.sat4JTotalSolverTime), System.lineSeparator());
//        System.out.format("Sat4J_VertexEnum_New_Reduce Results:   \t%s%s", tempString, System.lineSeparator());


//        System.out.format(" Sat4J_ModelEnum_Old_Reduce Compile Time (AVG, STD):\t%f\t%f%s", Statistics.getMean(statistics2.sat4JCompileTime), Statistics.getStdDev(statistics2.sat4JCompileTime), System.lineSeparator());
//        System.out.format(" Sat4J_ModelEnum_Old_Reduce Total Solver Time (AVG, STD):\t%f\t%f%s", Statistics.getMean(statistics2.sat4JTotalSolverTime), Statistics.getStdDev(statistics2.sat4JTotalSolverTime), System.lineSeparator());
//        System.out.format(" Sat4J_ModelEnum_Old_Reduce Results:\t%s%s", tempString2, System.lineSeparator());

//        System.out.format(" Sat4J_ModelEnum_New_Reduce Compile Time (AVG, STD):\t%f\t%f%s", Statistics.getMean(statistics3.sat4JCompileTime), Statistics.getStdDev(statistics3.sat4JCompileTime), System.lineSeparator());
//        System.out.format(" Sat4J_ModelEnum_New_Reduce Total Solver Time (AVG, STD):\t%f\t%f%s", Statistics.getMean(statistics3.sat4JTotalSolverTime), Statistics.getStdDev(statistics3.sat4JTotalSolverTime), System.lineSeparator());
//        System.out.format(" Sat4J_ModelEnum_New_Reduce Results:\t%s%s", tempString3, System.lineSeparator());

//        System.out.println();
//        System.out.format(" Sat4J_ModelEnum_Old_Reduce Positive Vars:\t%s%s", sb1.toString(), System.lineSeparator());
//        System.out.format(" Sat4J_ModelEnum_New_Reduce Positive Vars:\t%s%s", sb5.toString(), System.lineSeparator());
//        System.out.println();
//
//        System.out.format(" Sat4J_ModelEnum_Old_Reduce Negative Vars:\t%s%s", sb2.toString(), System.lineSeparator());
//        System.out.format(" Sat4J_ModelEnum_New_Reduce Negative Vars:\t%s%s", sb6.toString(), System.lineSeparator());
//        System.out.println();
//
//        System.out.format(" Sat4J_ModelEnum_Old_Reduce Positive Clauses:\t%s%s", sb3.toString(), System.lineSeparator());
//        System.out.format(" Sat4J_ModelEnum_New_Reduce Positive Clauses:\t%s%s", sb7.toString(), System.lineSeparator());
//        System.out.println();
//
//        System.out.format(" Sat4J_ModelEnum_Old_Reduce Negative Clauses:\t%s%s", sb4.toString(), System.lineSeparator());
//        System.out.format(" Sat4J_ModelEnum_New_Reduce Negative Clauses:\t%s%s", sb8.toString(), System.lineSeparator());
//        System.out.println();

        float noSat = 0;
        for (String r : results) {
            if (r.equals("SAT"))
                noSat++;
        }

        float noSat2 = 0;
        for (String r : results2) {
            if (r.equals("SAT"))
                noSat2++;
        }

        float noSat3 = 0;
        for (String r : results3) {
            if (r.equals("SAT"))
                noSat3++;
        }

        if (entailmentType.equals("s")) {
//            writeOnFile(String.format("%d %d %d %d %d %.3f %d %d %d %.3f %d %.3f %.3f %.3f %.3f %.2f%s",
//                    k, numPosAtoms, numNegAtoms, numVertices, numLabels, probOrAvgDeg,
//                    gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg, distance,
//                    Statistics.getMean(statistics1.sat4JTotalSolverTime),
//                    Statistics.getStdDev(statistics1.sat4JTotalSolverTime) * 1.96 / Math.sqrt(repetition),
//                    Statistics.getMean(statistics1.sat4JCompileTime),
//                    Statistics.getStdDev(statistics1.sat4JCompileTime) * 1.96 / Math.sqrt(repetition), (noSat/repetition)*100,
//                    System.lineSeparator()), "sat4J_1.dat");

//        writeOnFile(String.format("%d %d %d %d %d %.3f %d %d %d %.3f %d %.3f %.3f %.3f %.3f %.2f %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f%s",
//                k, numPosAtoms, numNegAtoms, numVertices, numLabels, probOrAvgDeg,
//                gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg, distance,
//                Statistics.getMean(statistics2.sat4JTotalSolverTime),
//                Statistics.getStdDev(statistics2.sat4JTotalSolverTime) * 1.96 / Math.sqrt(repetition),
//                Statistics.getMean(statistics2.sat4JCompileTime),
//                Statistics.getStdDev(statistics2.sat4JCompileTime) * 1.96 / Math.sqrt(repetition),
//                (noSat2/repetition)*100,
//                Statistics.getMean(statistics2.sat4JNumOfVars_Pos),
//                Statistics.getStdDev(statistics2.sat4JNumOfVars_Pos) * 1.96 / Math.sqrt(repetition),
//                Statistics.getMean(statistics2.sat4JNumOfVars_Neg),
//                Statistics.getStdDev(statistics2.sat4JNumOfVars_Neg) * 1.96 / Math.sqrt(repetition * numNegAtoms),
//                Statistics.getMean(statistics2.sat4JNumOfClauses_Pos),
//                Statistics.getStdDev(statistics2.sat4JNumOfClauses_Pos) * 1.96 / Math.sqrt(repetition),
//                Statistics.getMean(statistics2.sat4JNumOfClauses_Neg),
//                Statistics.getStdDev(statistics2.sat4JNumOfClauses_Neg) * 1.96 / Math.sqrt(repetition * numNegAtoms),
//                System.lineSeparator()), "sat4J_2.dat");

//            writeOnFile(String.format("%d %d %d %d %d %.3f %d %d %d %.3f %d %.3f %.3f %.3f %.3f %.2f %.3f %.3f %.3f %.3f %.3f %.3f %.3f %.3f%s",
//                    k, numPosAtoms, numNegAtoms, numVertices, numLabels, probOrAvgDeg,
//                    gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg, distance,
//                    Statistics.getMean(statistics3.sat4JTotalSolverTime),
//                    Statistics.getStdDev(statistics3.sat4JTotalSolverTime) * 1.96 / Math.sqrt(repetition),
//                    Statistics.getMean(statistics3.sat4JCompileTime),
//                    Statistics.getStdDev(statistics3.sat4JCompileTime) * 1.96 / Math.sqrt(repetition),
//                    (noSat3 / repetition) * 100,
//                    Statistics.getMean(statistics3.sat4JNumOfVars_Pos),
//                    Statistics.getStdDev(statistics3.sat4JNumOfVars_Pos) * 1.96 / Math.sqrt(repetition),
//                    Statistics.getMean(statistics3.sat4JNumOfVars_Neg),
//                    Statistics.getStdDev(statistics3.sat4JNumOfVars_Neg) * 1.96 / Math.sqrt(repetition * numNegAtoms),
//                    Statistics.getMean(statistics3.sat4JNumOfClauses_Pos),
//                    Statistics.getStdDev(statistics3.sat4JNumOfClauses_Pos) * 1.96 / Math.sqrt(repetition),
//                    Statistics.getMean(statistics3.sat4JNumOfClauses_Neg),
//                    Statistics.getStdDev(statistics3.sat4JNumOfClauses_Neg) * 1.96 / Math.sqrt(repetition * numNegAtoms),
//                    System.lineSeparator()), "sat4J_3.dat");
        } else {
//            writeOnFile(String.format("%d %d %d %d %d %.3f %d %d %d %.3f %d %.3f %.3f %.3f %.3f %.2f %s",
//                    k, numPosAtoms, numNegAtoms, numVertices, numLabels, probOrAvgDeg,
//                    gpPosNumVer, gpNegNumVer, gpnumLabels, gpprobOrAvgDeg, distance,
//                    Statistics.getMean(statistics3.sat4JTotalSolverTime),
//                    Statistics.getStdDev(statistics3.sat4JTotalSolverTime) * 1.96 / Math.sqrt(repetition),
//                    Statistics.getMean(statistics3.sat4JCompileTime),
//                    Statistics.getStdDev(statistics3.sat4JCompileTime) * 1.96 / Math.sqrt(repetition),
//                    (noSat3 / repetition) * 100, System.lineSeparator()), "sat4J_3.dat");
        }
    }
}
