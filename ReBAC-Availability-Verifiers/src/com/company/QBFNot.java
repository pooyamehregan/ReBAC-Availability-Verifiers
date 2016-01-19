package com.company;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public final class QBFNot extends QBF {
    protected final QBF qbf;

    public QBFNot(QBF qbf) {
        this.qbf = qbf;
    }

    public StringBuilder evalQDIMACS(StringBuilder stringBuilder) {
        stringBuilder.append("-");
        qbf.evalQDIMACS(stringBuilder);
        return stringBuilder;
    }

    public VecInt evalDIMACS (ISolver solver) throws ContradictionException{
        VecInt vec = new VecInt();
        VecInt vec2 = qbf.evalDIMACS(solver);
        for (int i = 0; i < vec2.size(); i++) {
            vec.push(-vec2.get(i));
        }
        return vec;
    }

    public StringBuilder evalQCIR(Integer var, StringBuilder stringBuilder, Counters counters) {
        stringBuilder.append("-");
        qbf.evalQCIR(var, stringBuilder, counters);
        return stringBuilder;
    }

}
