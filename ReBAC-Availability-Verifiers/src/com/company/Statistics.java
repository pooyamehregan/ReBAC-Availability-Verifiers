package com.company;

import java.util.ArrayList;

/**
 * Created by Pooya on 15-02-20.
 */
public class Statistics {
    //Attributes
    protected ArrayList<Double> formula2QBFTimes;
    protected ArrayList<Double> NNFTimes;
    protected ArrayList<Double> prenexTimes;
    protected ArrayList<Double> QCIRCompileTime;
    protected ArrayList<Integer> QCIRNumOfVars;
    protected ArrayList<Integer> sat4JNumOfVars;
    protected ArrayList<Integer> sat4JNumOfVars_Pos;
    protected ArrayList<Integer> sat4JNumOfVars_Neg;
    protected ArrayList<Integer> sat4JNumOfClauses;
    protected ArrayList<Integer> sat4JNumOfClauses_Pos;
    protected ArrayList<Integer> sat4JNumOfClauses_Neg;
    protected ArrayList<Integer> sat4JNumOfIter;
    protected ArrayList<Double> sat4JCompileTime;
    protected ArrayList<Double> sat4JTotalSolverTime;
    protected ArrayList<ArrayList<Double>> modelTimes;
    protected ArrayList<Integer> numModels;

    public Statistics () {
        formula2QBFTimes = new ArrayList<Double>();
        NNFTimes = new ArrayList<Double>();
        prenexTimes = new ArrayList<Double>();
        QCIRCompileTime = new ArrayList<Double>();
        QCIRNumOfVars = new ArrayList<Integer>();
        sat4JNumOfVars = new ArrayList<Integer>();
        sat4JNumOfVars_Pos = new ArrayList<Integer>();
        sat4JNumOfVars_Neg = new ArrayList<Integer>();
        sat4JNumOfClauses = new ArrayList<Integer>();
        sat4JNumOfClauses_Pos = new ArrayList<Integer>();
        sat4JNumOfClauses_Neg = new ArrayList<Integer>();
        sat4JNumOfIter = new ArrayList<Integer>();
        sat4JCompileTime = new ArrayList<Double>();
        sat4JTotalSolverTime = new ArrayList<Double>();
        modelTimes = new ArrayList<ArrayList<Double>>();
        numModels = new ArrayList<Integer>();
    }

    public static double getMean(ArrayList objects)
    {
        double sum = 0.0;
        if(objects.get(0) instanceof Integer)
        {
            for(Object o: objects)
                sum += (Integer) o;
        }
        else
        {
            for(Object o: objects)
                sum += (Double) o;
        }
        return sum/objects.size();
    }

    public static double getVariance(ArrayList objects)
    {
        double mean = getMean(objects);
        double temp = 0;
        if (objects.get(0) instanceof Integer) {
            for(Object o: objects)
                temp += (mean - (Integer) o) * (mean - (Integer) o);
        }
        else
        {
            for(Object o: objects)
                temp += (mean - (Double) o) * (mean - (Double) o);
        }
        return temp/objects.size();
    }

    public static double getStdDev(ArrayList objects)
    {
        return Math.sqrt(getVariance(objects));
    }


}
