package com.company;

/**
 * Created by Pooya on 15-02-11.
 */
public class Counters {

    private int clauseCounter;
    private int varCounter;

    public Counters () {
        this.clauseCounter = 0;
        this.varCounter = 0;
    }

    public Counters(Counters counters) {
        this.varCounter = counters.getVarCounter();
        this.clauseCounter = counters.getClauseCounter();
    }

    public Counters (int varCounter, int clauseCounter) {
        this.varCounter = varCounter;
        this.clauseCounter = clauseCounter;
    }

    public void incrVarCntr() {
        varCounter++;
    }

    public void incrClauseCntr() {
        clauseCounter++;
    }

    public int getVarCounter() {
        return varCounter;
    }

    public int getClauseCounter() {
        return clauseCounter;
    }


    public void setClauseCounter(int clauseCounter) {
        this.clauseCounter = clauseCounter;
    }

    public void setVarCounter(int varCounter) {
        this.varCounter = varCounter;
    }

}
