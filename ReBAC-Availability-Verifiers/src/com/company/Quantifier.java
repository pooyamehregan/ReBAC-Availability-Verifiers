package com.company;

/**
 * Created by Pooya on 2014-10-01.
 */
public final class Quantifier {
    protected final int quantVar;
    protected final boolean isUniversal;

    //Constructors
    public Quantifier (boolean isUniversal, int quantVar) {
        this.isUniversal = isUniversal;
        this.quantVar = quantVar;
    }

}
