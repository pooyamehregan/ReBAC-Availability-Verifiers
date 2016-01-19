package com.company;

import java.util.Random;

/**
 * Created by Pooya on 15-01-16.
 */
public class RandomUtil {

    public static final Random random = new Random();

    public RandomUtil (long seed) {
        random.setSeed(seed);
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

}
