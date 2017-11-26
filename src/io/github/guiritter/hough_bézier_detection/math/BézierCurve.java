package io.github.guiritter.hough_bézier_detection.math;

import java.awt.geom.Point2D;
import static java.lang.Math.pow;

/**
 * Computes a Bézier curve from a list of control points.
 * @author Guilherme Alan Ritter
 */
public final class BézierCurve {

    private double b;

    private final Point2D BézierControlPointList[];

    private final BinomialCoefficient binomialCoefficient = new BinomialCoefficient();

    private long i;

    private final Point2D output;

    private long n;

    private double x;

    private double y;

    /**
     * Computes a point in the curve for a given {@code 0 ≤ t ≤ 1}.
     * @param t
     */
    public void op(double t) {
        x = 0;
        y = 0;
        n = BézierControlPointList.length - 1;
        for (i = 0; i <= n; i++) {
            b = ((double) binomialCoefficient.op(n, i)) * pow(t, (double) i) * pow(1.0 - t, (double) (n - i));
            x += BézierControlPointList[(int) i].getX() * b;
            y += BézierControlPointList[(int) i].getY() * b;
        }
        output.setLocation(x, y);
    }

    public BézierCurve(Point2D BézierControlPointList[], Point2D output) {
        this.BézierControlPointList = BézierControlPointList;
        this.output = output;
    }
}
