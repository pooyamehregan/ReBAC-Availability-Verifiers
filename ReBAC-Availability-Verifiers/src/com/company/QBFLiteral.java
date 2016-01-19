package com.company;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public final class QBFLiteral extends QBF {
    protected final int atom;

    public QBFLiteral(int atom) {
        this.atom = atom;
    }

    public StringBuilder evalQDIMACS(StringBuilder stringBuilder) { return stringBuilder.append(atom); }

    public VecInt evalDIMACS(ISolver solver) throws ContradictionException {
        VecInt vec = new VecInt();
        vec.push(atom);
        return vec;
    }

    public StringBuilder evalQCIR(Integer var, StringBuilder stringBuilder, Counters counters) {
        return stringBuilder.append(atom);
    }

}
