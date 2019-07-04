package io.github.guiritter.hough_bezier_detection.math;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.HashSet;
import javax.imageio.ImageIO;

/**
 * Finds the local maxima in a 3D matrix. A value is considered a local maxima
 * if it's greater than all values in it's 26-connected neighborhood.
 * If it's equal to one value, it's still considered a local maxima
 * if this value also satisfies these conditions. A threshold can be used
 * to discard points with too low value.
 * @author Guilherme Alan Ritter
 */
public final class MaximaLocal {

    /**
     * Set containing all local maxima found.
     */
    private HashSet<Point3D> maximaLocalSet;

    /**
     * Point that need to have it's neighborhood tested.
     */
    private Point3D pointCenter;

    /**
     * Center points that have had it's neighborhood tested.
     */
    private final HashSet<Point3D> pointCenterTreated = new HashSet<>();

    /**
     * Center points with neighborhood pending to be tested.
     */
    private final HashSet<Point3D> pointCenterTreating = new HashSet<>();

    /**
     * Neighbor points to center points pending to be tested.
     */
    private final HashSet<Point3D> pointNeighborTreated = new HashSet<>();

    private int x;

    private int xN;

    private int xHigh;

    private int xLow;

    private int xMaximum;

    private int y;

    private int yN;

    private int yHigh;

    private int yLow;

    private int yMaximum;

    private int z;

    private int zN;

    private int zHigh;

    private int zLow;

    private int zMaximum;

    /**
     * Finds the local maxima.
     * @param matrix
     * @param threshold discards points with value below this value
     * @return local maxima
     */
    public final HashSet<Point3D> op(Point3D matrix[][][], long threshold) {
        maximaLocalSet = new HashSet<>();
        zMaximum = matrix.length;
        yMaximum = matrix[0].length;
        xMaximum = matrix[0][0].length;
        for (z = 0; z < zMaximum; z++) {
            for (y = 0; y < yMaximum; y++) {
                point_main:
                for (x = 0; x < xMaximum; x++) {
                    if (matrix[z][y][x].w < threshold) {
                        continue /*point_main*/;
                    }
                    pointCenterTreated.clear();
                    pointCenterTreating.clear();
                    pointNeighborTreated.clear();
                    pointCenterTreating.add(matrix[z][y][x]);
                    while (!pointCenterTreating.isEmpty()) {
                        for (Point3D point : pointCenterTreating) {
                            pointCenter = point;
                            break;
                        }
                        pointCenterTreating.remove(pointCenter);
                        if (pointCenterTreated.contains(pointCenter)) {
                            continue;
                        }
                        pointCenterTreated.add(pointCenter);
                        xLow  = max(0, pointCenter.x - 1);
                        yLow  = max(0, pointCenter.y - 1);
                        zLow  = max(0, pointCenter.z - 1);
                        xHigh = min(pointCenter.x + 1, xMaximum - 1);
                        yHigh = min(pointCenter.y + 1, yMaximum - 1);
                        zHigh = min(pointCenter.z + 1, zMaximum - 1);
                        for (zN = zLow; zN <= zHigh; zN++) {
                            for (yN = yLow; yN <= yHigh; yN++) {
                                // point_neighbor:
                                for (xN = xLow; xN <= xHigh; xN++) {
                                    if (pointNeighborTreated.contains(matrix[zN][yN][xN])) {
                                        continue /*point_neighbor*/;
                                    } else {
                                        pointNeighborTreated.add(matrix[zN][yN][xN]);
                                    }
                                    if (matrix[zN][yN][xN].w > pointCenter.w) {
                                        continue point_main;
                                    } else if ((matrix[zN][yN][xN].w == pointCenter.w) && (!pointCenterTreated.contains(matrix[zN][yN][xN]))) {
                                        pointCenterTreating.add(matrix[zN][yN][xN]);
                                    }
                                }
                            }
                        }
                    }
                    maximaLocalSet.add(matrix[z][y][x]);
                }
            }
        }
        return maximaLocalSet;
    }

    public static void main(String args[]) throws IOException {
        MaximaLocal maximaLocal = new MaximaLocal();
        /*
        Point3D matrix[][][] = new Point3D[][][]{
            {
                {new Point3D(0, 0, 0, 8), new Point3D(1, 0, 0, 8), new Point3D(2, 0, 0, 9)},
                {new Point3D(0, 1, 0, 0), new Point3D(1, 1, 0, 0), new Point3D(2, 1, 0, 0)},
                {new Point3D(0, 2, 0, 0), new Point3D(1, 2, 0, 0), new Point3D(2, 2, 0, 0)}
            }
        };
        /**/
        //*
        Point3D matrix[][][] = new Point3D[21][50][1000];
        BufferedImage image;
        WritableRaster raster;
        int color[] = new int[1];
        int i, j;
        for (int k = 0; k < 21; k++) {
            image = ImageIO.read(new File(String.format("/home/guir/NetBeansProjects/HoughBézierDetection/image/%02d.png", k)));
            raster = image.getRaster();
            for (j = 0; j < 50; j++) {
                for (i = 0; i < 1000; i++) {
                    raster.getPixel(i, j, color);
                    matrix[k][j][i] = new Point3D(i, j, k, color[0]);
                }
            }
        }
        /**/
        HashSet<Point3D> maximaLocalSet = maximaLocal.op(matrix, 176);
        StringBuilder xBuilder = new StringBuilder("[");
        StringBuilder yBuilder = new StringBuilder("[");
        StringBuilder zBuilder = new StringBuilder("[");
        for (Point3D point : maximaLocalSet) {
            xBuilder.append(point.x).append(", ");
            yBuilder.append(point.y).append(", ");
            zBuilder.append(point.z).append(", ");
        }
        System.out.println(("points(" + xBuilder + "], " + yBuilder + "], " + zBuilder + "])").replace(", ]", "]"));
//        System.out.println(maximaLocal.size()); // 171 - 176
    }
}
