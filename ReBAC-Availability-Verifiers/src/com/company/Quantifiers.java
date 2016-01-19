package com.company;

import java.util.TreeSet;

/**
 * Created by Pooya on 2014-10-06.
 */

public class Quantifiers {
    protected boolean isUniversal;
    protected TreeSet<Integer> vars;
    public Quantifiers (boolean isUniversal, int var) {this.isUniversal = isUniversal; vars = new TreeSet<Integer>(); vars.add(var);}
    protected Quantifiers addVar(int var) {
        this.vars.add(var);
        return this;
    }
}
