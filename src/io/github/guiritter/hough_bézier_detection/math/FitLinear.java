package io.github.guiritter.hough_bézier_detection.math;

/**
 * A linear equation constructed from two points.
 * Doesn't work for points in a vertical line.
 * @author Guilherme Alan Ritter
 */
public final class FitLinear {

    /**
     * y = a * x + b
     */
    public final double a;

    /**
     * y = a * x + b
     */
    public final double b;

    /**
     * Computes y(x).
     * @param x
     * @return y
     */
    public double f(double x) {
        return (a * x) + b;
    }

    /**
     * Fits a line give two points.
     * @param x1  first point's x
     * @param y1  first point's y
     * @param x2 second point's x
     * @param y2 second point's y
     */
    public FitLinear(double x1, double y1, double x2, double y2) {
        a = (y2 - y1) / (x2 - x1);
        b = y1 - (a * x1);
    }
}