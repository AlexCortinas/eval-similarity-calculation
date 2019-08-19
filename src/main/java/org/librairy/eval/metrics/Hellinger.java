package org.librairy.eval.metrics;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class Hellinger {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(Hellinger.class);

    public static Double distance(List<Double> v1, List<Double> v2) {
        return distance(Doubles.toArray(v1), Doubles.toArray(v2));
    }

    public static Double distance(double[] v1, double[] v2) {

        Double sum = 0.0;

        if (v1.length != v2.length)
            return -1.0;

        for (int i = 0; i < v1.length; i++) {
            sum += Math.pow(Math.sqrt(v1[i]) - Math.sqrt(v2[i]), 2);
        }

        return (1 / Math.sqrt(2)) * Math.sqrt(sum);
    }

}
