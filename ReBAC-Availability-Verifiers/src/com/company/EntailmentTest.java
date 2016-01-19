package com.company;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Pooya on 15-07-07.
 */
public class EntailmentTest {

    @Test
    public void testGetSubsets() throws Exception {
        Feasibility feasibility = new Feasibility();
        List<Integer> superSet = new ArrayList<Integer>();
        superSet.add(1);
        superSet.add(2);
        superSet.add(3);
        superSet.add(4);
        System.out.println(feasibility.getSubsets(superSet, 2));
    }
}