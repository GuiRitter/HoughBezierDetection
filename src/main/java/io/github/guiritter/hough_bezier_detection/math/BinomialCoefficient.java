package io.github.guiritter.hough_bezier_detection.math;

import static java.lang.Math.min;

/**
 * Computes the binomial coefficient.
 * @author Guilherme Alan Ritter
 */
public final class BinomialCoefficient {

    private long c;

    private long i;

    /**
     * source:
     * https://en.wikipedia.org/wiki/Binomial_coefficient
     * @param n the number that goes above
     * @param k the number that goes below
     * @return
     */
    public long op(long n, long k) {
        if ((k < 0) || (k > n))
            return 0;
        if ((k == 0) || (k == n))
            return 1;
        k = min(k, n - k);// take advantage of symmetry
        c = 1;
        for (i = 0; i < k; i++)
            c = c * (n - i) / (i + 1);
        return c;
    }
}
