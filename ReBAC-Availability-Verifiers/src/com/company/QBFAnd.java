package com.company;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

import java.util.ArrayList;

public final class QBFAnd extends QBF {
    protected final ArrayList<QBF> qbfs;

    public QBFAnd(ArrayList<QBF> qbfs) {
        this.qbfs = qbfs;
    }

    public StringBuilder evalQDIMACS(StringBuilder stringBuilder)
    {
        for (QBF qbf : qbfs) {
            qbf.evalQDIMACS(stringBuilder);
            if (qbf instanceof QBFLiteral || qbf instanceof QBFNot || qbf instanceof QBFOr)
                stringBuilder.append(" 0").append(System.lineSeparator());
        }
        return stringBuilder;
    }

    public VecInt evalDIMACS (ISolver solver) throws ContradictionException {
        for (QBF qbf : qbfs) {
            VecInt vec = qbf.evalDIMACS(solver);
            if (qbf instanceof QBFLiteral || qbf instanceof QBFNot || qbf instanceof QBFOr)
                solver.addClause(vec);
        }
        return new VecInt();
    }

    public StringBuilder evalQCIR(Integer var, StringBuilder stringBuilder, Counters counters) {
        ArrayList<Integer> args = new ArrayList<Integer>();
        for (QBF qbf : qbfs) {
            if (qbf instanceof QBFLiteral) {
                QBFLiteral l = (QBFLiteral) qbf;
                args.add(l.atom);
            } else {
                counters.incrVarCntr();
                //varCounter++;
                args.add(counters.getVarCounter());
                //args.add(varCounter);
                qbf.evalQCIR(counters.getVarCounter(), stringBuilder, counters);
                //qbf.evalQCIR(varCounter, stringBuilder);
            }
        }
        if (var == null) {
            counters.incrVarCntr();
            //varCounter++;
            stringBuilder.append(counters.getVarCounter()).append(" = and(");
            //stringBuilder.append(varCounter).append(" = and(");
        } else {
            stringBuilder.append(var).append(" = and(");
        }

        boolean entering = true;

        for (int arg : args) {
            if (entering) {
                entering = false;
            }
            else {
                stringBuilder.append(", ");
            }
            stringBuilder.append(arg);
        }
        stringBuilder.append(")").append(System.lineSeparator());
        return stringBuilder;
    }


}
