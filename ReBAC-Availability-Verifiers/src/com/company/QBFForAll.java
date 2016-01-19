package com.company;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public final class QBFForAll extends QBF {
    protected final QBF qbf;
    protected final Quantifier quantifier;

    public QBFForAll(QBF qbf, int quantvar) {
        this.qbf =qbf;
        quantifier = new Quantifier(true, quantvar);
    }

    public StringBuilder evalQDIMACS(StringBuilder stringBuilder) {
        qbf.evalQDIMACS(stringBuilder);
        return stringBuilder;
    }

    public VecInt evalDIMACS(ISolver solver) throws ContradictionException {
        return qbf.evalDIMACS(solver);
    }

    public StringBuilder evalQCIR(Integer var, StringBuilder stringBuilder, Counters counters) {
        int x;
        if (qbf instanceof QBFLiteral) {
            QBFLiteral l = (QBFLiteral) qbf;
            x = l.atom;
        } else {
            counters.incrVarCntr();
            //varCounter++;
            x = counters.getVarCounter();
            //x = varCounter;
            qbf.evalQCIR(x, stringBuilder, counters);
        }

        if (var == null) {
            counters.incrVarCntr();
            //varCounter++;
            stringBuilder.append(counters.getVarCounter()).append(" = forall(").append(quantifier.quantVar).append("; ").append(x).append(")").append(System.lineSeparator());
            //stringBuilder.append(varCounter).append(" = forall(").append(quantifier.quantVar).append("; ").append(x).append(")").append(System.lineSeparator());
        }
        else {
            stringBuilder.append(var).append(" = forall(").append(quantifier.quantVar).append("; ").append(x).append(")").append(System.lineSeparator());
        }
        return stringBuilder;
    }

}
