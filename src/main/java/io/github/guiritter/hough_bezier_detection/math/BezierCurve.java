package io.github.guiritter.hough_bezier_detection.math;

import java.awt.geom.Point2D;
import static java.lang.Math.pow;

/**
 * Computes a Bézier curve from a list of control points.
 * @author Guilherme Alan Ritter
 */
public final class BezierCurve {

    private double b;

    private final Point2D BezierControlPointList[];

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
        n = BezierControlPointList.length - 1;
        for (i = 0; i <= n; i++) {
            b = ((double) binomialCoefficient.op(n, i)) * pow(t, (double) i) * pow(1.0 - t, (double) (n - i));
            x += BezierControlPointList[(int) i].getX() * b;
            y += BezierControlPointList[(int) i].getY() * b;
        }
        output.setLocation(x, y);
    }

    public BezierCurve(Point2D BezierControlPointList[], Point2D output) {
        this.BezierControlPointList = BezierControlPointList;
        this.output = output;
    }
}
