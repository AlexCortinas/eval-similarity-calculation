package org.librairy.eval.metrics;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;

import cc.mallet.util.Maths;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */

public class JensenShannon {
    public static AtomicInteger totalComparisons = new AtomicInteger(0);

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(JensenShannon.class);


    public static double similarity(List<Double> v1, List<Double> v2){
        totalComparisons.incrementAndGet();
        return similarity(Doubles.toArray(v1),Doubles.toArray(v2));
    }

    public static double similarity(double[] v1, double[] v2){
        return 1 - Maths.jensenShannonDivergence(v1,v2);
    }

    public static double divergence(List<Double> v1, List<Double> v2){
        return divergence(Doubles.toArray(v1),Doubles.toArray(v2));
    }

    public static double divergence(double[] v1, double[] v2){
        return Maths.jensenShannonDivergence(v1,v2);
    }

}
