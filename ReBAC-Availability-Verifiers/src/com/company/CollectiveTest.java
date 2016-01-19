package com.company;

import org.junit.Test;
import org.sat4j.specs.TimeoutException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Pooya on 15-03-10.
 */
public class CollectiveTest {
    //@Test
    public void implicationTestFS() {
        int numTest = 50;
        Experiment experiment = new Experiment(10, 1, 0.1, 5, 1, 0.1, 1, 6, 2, 1);
        for (int i = 0; i < numTest; i++) {
            experiment.initialize();
            Graph g1 = new Graph(experiment.graph);
            Graph g2 = new Graph(experiment.graph);

            //Satisfiability satisfiability = new Satisfiability(g1, experiment.formula1);
            Satisfiability2 satisfiability = new Satisfiability2(g1, experiment.formula1);
            //Feasibility feasibility = new Feasibility(g2, experiment.formula2, experiment.distance);
            Feasibility2 feasibility = new Feasibility2(g2, experiment.formula2, experiment.distance);

            boolean satResult = satisfiability.checkSat_Sat4J();
            experiment.reset();
            boolean feaResult = feasibility.checkSat_Sat4J();
            experiment.reset();

            if (satResult){
                //System.out.println("1");
                assertEquals(true, feaResult);
            }

            if (!feaResult) {
                //System.out.println("3");
                assertEquals(false, satResult);
            }

        }

    }

    @Test
    public void implicationTestFS_ME() throws TimeoutException {
        int numTest = 50;
        Experiment experiment = new Experiment(1, 3, 3, 100, 1, 0.1, 5, 5, 1, 0.05, 1);
        for (int i = 0; i < numTest; i++) {
            experiment.initialize();
            Graph g1 = new Graph(experiment.graph);
            Graph g2 = new Graph(experiment.graph);

            //Satisfiability satisfiability = new Satisfiability(g1, experiment.formula1);
            Satisfiability2 satisfiability = new Satisfiability2(g1, experiment.formula1);
            //Feasibility feasibility = new Feasibility(g2, experiment.formula2, experiment.distance);
            Feasibility2 feasibility = new Feasibility2(g2, experiment.formula2, experiment.distance);

            boolean satResult = satisfiability.checkSat_Sat4J_ME(experiment.k, experiment.positiveFormula1, experiment.negativeFormula1, true);
            experiment.reset();
            boolean feaResult = feasibility.checkSat_Sat4J_ME(experiment.distance, experiment.k, experiment.positiveFormula2, experiment.negativeFormula2, true);
            experiment.reset();

            if (satResult){
                //System.out.println("1");
                assertEquals(true, feaResult);
            }

            if (!feaResult) {
                //System.out.println("3");
                assertEquals(false, satResult);
            }

        }

    }

    //@Test
    public void implicationTestFR() {
        int numTest = 10;
        Experiment experiment = new Experiment(3, 2, 0.1, 3, 2, 0.1, 1, 6, 2, 0);
        for (int i = 0; i < numTest; i++) {
            experiment.initialize();
            Graph g1 = new Graph(experiment.graph);
            Graph g2 = new Graph(experiment.graph);

            //Feasibility feasibility = new Feasibility(g1, experiment.formula2, experiment.distance);
            Feasibility2 feasibility = new Feasibility2(g1, experiment.formula2, experiment.distance);
            Resiliency resiliency = new Resiliency(g2, experiment.formula3, experiment.distance);

            boolean feaResult = feasibility.checkSat_Sat4J();
            experiment.reset();
            boolean resResult = resiliency.checkSat_Sat4J();
            experiment.reset();

            if (!feaResult) {
                //System.out.println("3");
                assertEquals(false, resResult);
            }

            if (resResult) {
                //System.out.println("4");
                assertEquals(true, feaResult);
            }

        }

    }

    //@Test
    public void implicationTestRS() {
        int numTest = 10;
        Experiment experiment = new Experiment(5, 2, 0.1, 5, 2, 0.1, 1, 6, 2, 0);
        for (int i = 0; i < numTest; i++) {
            experiment.initialize();
            Graph g1 = new Graph(experiment.graph);
            Graph g2 = new Graph(experiment.graph);

            //Satisfiability satisfiability = new Satisfiability(g1, experiment.formula1);
            Satisfiability2 satisfiability = new Satisfiability2(g1, experiment.formula1);
            Resiliency resiliency = new Resiliency(g2, experiment.formula3, experiment.distance);

            boolean satResult = satisfiability.checkSat_Sat4J();
            experiment.reset();
            boolean resResult = resiliency.checkSat_Sat4J();
            experiment.reset();

            if (!satResult){
                //System.out.println("2");
                assertEquals(false, resResult);
            }

            if (resResult) {
                //System.out.println("4");
                assertEquals(true, satResult);
            }

        }

    }

    //@Test
    public void equalityTestFS() {
        int numTest = 50;
        for (int i = 0; i < numTest; i++) {
            Experiment experiment = new Experiment(10, 1, 0.1, 5, 1, 0.1, 0, 6, 2, 1);
            experiment.initialize();
            Graph g1 = new Graph(experiment.graph);
            Graph g2 = new Graph(experiment.graph);

            //Satisfiability satisfiability = new Satisfiability(g1, experiment.formula1);
            Satisfiability2 satisfiability = new Satisfiability2(g1, experiment.formula1);
            boolean satResult = satisfiability.checkSat_Sat4J();
            experiment.reset();

            //Feasibility feasibility = new Feasibility(g2, experiment.formula2, experiment.distance);
            Feasibility2 feasibility = new Feasibility2(g2, experiment.formula2, experiment.distance);
            boolean feaResult = feasibility.checkSat_Sat4J();
            experiment.reset();

            //System.out.println("round: " + i + "\t" + "Sat = " + satResult);
            assertEquals(satResult, feaResult);
        }

    }

    @Test
    public void equalityTestFS_ME() throws TimeoutException{
        int numTest = 50;
            Experiment experiment = new Experiment(1, 3, 3, 100, 1, 0.1, 5, 5, 1, 0.05, 0);
            for (int i = 0; i < numTest; i++) {
            experiment.initialize();
            Graph g1 = new Graph(experiment.graph);
            Graph g2 = new Graph(experiment.graph);

            //Satisfiability satisfiability = new Satisfiability(g1, experiment.formula1);
            Satisfiability2 satisfiability = new Satisfiability2(g1, experiment.formula1);
            //Feasibility feasibility = new Feasibility(g2, experiment.formula2, experiment.distance);
            Feasibility2 feasibility = new Feasibility2(g2, experiment.formula2, experiment.distance);

            boolean satResult = satisfiability.checkSat_Sat4J_ME(experiment.k, experiment.positiveFormula1, experiment.negativeFormula1, true);
            experiment.reset();
            boolean feaResult = feasibility.checkSat_Sat4J_ME(experiment.distance, experiment.k, experiment.positiveFormula2, experiment.negativeFormula2, true);
            experiment.reset();

            //System.out.println("round: " + i + "\t" + "Sat = " + satResult);
            assertEquals(satResult, feaResult);
        }
    }

    //@Test
    public void equalityTestFR() {
        int numTest = 10;
        for (int i = 0; i < numTest; i++) {
            Experiment experiment = new Experiment(5, 2, 0.1, 5, 2, 0.1, 0, 6, 2, 0);
            experiment.initialize();
            Graph g1 = new Graph(experiment.graph);
            Graph g2 = new Graph(experiment.graph);

            //Feasibility feasibility = new Feasibility(g1, experiment.formula2, experiment.distance);
            Feasibility2 feasibility = new Feasibility2(g1, experiment.formula2, experiment.distance);
            boolean feaResult = feasibility.checkSat_Sat4J();
            experiment.reset();

            Resiliency resiliency = new Resiliency(g2, experiment.formula3, experiment.distance);
            boolean resResult = resiliency.checkSat_Sat4J();
            experiment.reset();

            //System.out.println("round: " + i + "\t" + "Sat = " + satResult);
            assertEquals(feaResult, resResult);
        }

    }

    //@Test
    public void equalityTestRS() {
        int numTest = 10;
        for (int i = 0; i < numTest; i++) {
            Experiment experiment = new Experiment(5, 2, 0.1, 5, 2, 0.1, 0, 6, 2, 0);
            experiment.initialize();
            Graph g1 = new Graph(experiment.graph);
            Graph g2 = new Graph(experiment.graph);

            //Satisfiability satisfiability = new Satisfiability(g1, experiment.formula1);
            Satisfiability2 satisfiability = new Satisfiability2(g1, experiment.formula1);
            boolean satResult = satisfiability.checkSat_Sat4J();
            experiment.reset();

            Resiliency resiliency = new Resiliency(g2, experiment.formula3, experiment.distance);
            boolean resResult = resiliency.checkSat_Sat4J();
            experiment.reset();

            //System.out.println("round: " + i + "\t" + "Sat = " + satResult);
            assertEquals(satResult, resResult);
        }

    }
}
