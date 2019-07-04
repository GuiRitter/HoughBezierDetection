package io.github.guiritter.hough_bezier_detection.math;

/**
 * Point with 3D coordinates and value.
 * @author Guilherme Alan Ritter
 */
public final class Point3D {

    /**
     * Value.
     */
    public long w;

    public final int x;

    public final int y;

    public final int z;

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (!(obj instanceof Point3D))) {
            return false;
        }
        return (x == ((Point3D) obj).x) && (y == ((Point3D) obj).y) && (z == ((Point3D) obj).z);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + x;
        hash = 97 * hash + y;
        hash = 97 * hash + z;
        return hash;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d,%d) = %d", x, y, z, w);
    }

    public Point3D(int x, int y, int z, long w) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
