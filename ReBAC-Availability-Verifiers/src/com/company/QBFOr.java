package com.company;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import java.util.ArrayList;

public final class QBFOr extends QBF {
    protected final ArrayList<QBF> qbfs;

    public QBFOr(ArrayList<QBF> qbfs) {
        this.qbfs = qbfs;
    }

    public StringBuilder evalQDIMACS(StringBuilder stringBuilder) {
        for (int i = 0; i < qbfs.size(); i++) {
            qbfs.get(i).evalQDIMACS(stringBuilder);
            if (i < qbfs.size() - 1)
                stringBuilder.append(" ");
        }
        return stringBuilder;
    }

    public VecInt evalDIMACS(ISolver solver) throws ContradictionException{
        VecInt vec = new VecInt();
        for (QBF qbf: qbfs) {
            VecInt vec2 = qbf.evalDIMACS(solver);
            for (int i = 0; i < vec2.size(); i++) {
                vec.push(vec2.get(i));
            }
        }
        return vec;
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
            stringBuilder.append(counters.getVarCounter()).append(" = or(");
            //stringBuilder.append(varCounter).append(" = or(");
        }
        else {
            stringBuilder.append(var).append(" = or(");
        }

        boolean entering = true;

        for (int arg : args) {
            if (entering)
                entering = false;
            else {
                stringBuilder.append(" ,");
            }
            stringBuilder.append(arg);
        }
        stringBuilder.append(")").append(System.lineSeparator());
        return stringBuilder;
    }
}
