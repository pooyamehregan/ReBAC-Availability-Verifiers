package com.company;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * Created by Pooya on 2014-10-08.
 */
public abstract class QBF {
    public abstract StringBuilder evalQDIMACS(StringBuilder stringBuilder);
    public abstract VecInt evalDIMACS(ISolver solver) throws ContradictionException;
    public abstract StringBuilder evalQCIR(Integer var, StringBuilder stringBuilder, Counters counters);
}

