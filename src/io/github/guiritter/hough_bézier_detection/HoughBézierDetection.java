package io.github.guiritter.hough_bézier_detection;

import io.github.guiritter.bézier_drawer.FitLinear;
import io.github.guiritter.hough_bézier_detection.math.BézierCurve;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.max;
import static java.lang.Double.min;
import static java.lang.Long.MIN_VALUE;
import java.util.HashMap;
import java.util.HashSet;
import javax.imageio.ImageIO;

/**
 *
 * @author Guilherme Alan Ritter
 */
public final class HoughBézierDetection {

    private final double boundingBoxHeight;

    private final double boundingBoxWidth;

    private final BézierCurve curve;

    private final double curveStep;

    private final int edgeMapHeight;

    private final boolean edgeMapMatrix[][];

    private final int edgeMapWidth;

    private FitLinear fitLinear;

    private int i;

    private final Point2D point = new Point2D.Double();

    private final int pointControlAmount;

    private final Point2D pointControlArray[];

    private final Point2D pointControlOriginalArray[];

    private double t;

    private int translationX;

    private int translationY;

    private int translationMaximumX;

    private int translationMaximumY;

    private final HashMap<Integer, HashSet<Integer>> visitedPointCurveMap = new HashMap<>();

    private HashSet<Integer> visitedPointCurveSet;

    private int x;

    private int y;

    @SuppressWarnings("empty-statement")
    public final void detect(
     double translationStepX,
     double translationStepY,
     double rotationStep
    ) {
        for (i = 0; (i * translationStepX) < ((int) (edgeMapWidth  - 1 - boundingBoxWidth )); i++, translationMaximumX = i);
        for (i = 0; (i * translationStepY) < ((int) (edgeMapHeight - 1 - boundingBoxHeight)); i++, translationMaximumY = i);
        long matrix[][] = new long[translationMaximumY][translationMaximumX]; // TODO test
        long matrixMaximum = MIN_VALUE;
        for (y = 0; y < translationMaximumY; y++) {
            for (x = 0; x < translationMaximumX; x++) {
                matrix[y][x] = 0;
            }
        }
        for (translationY = 0; translationY < translationMaximumY; translationY++) {
            for (translationX = 0; translationX < translationMaximumX; translationX++) {
                for (i = 0; i < pointControlAmount; i++) {
                    pointControlArray[i].setLocation(
                     pointControlOriginalArray[i].getX() + (((double) translationX) * translationStepX),
                     pointControlOriginalArray[i].getY() + (((double) translationY) * translationStepY)
                    );
                }
                visitedPointCurveMap.clear();
                for (t = 0; t < 1; t += curveStep) {
                    curve.op(t);
                    x = (int) point.getX();
                    y = (int) point.getY();
                    visitedPointCurveSet = visitedPointCurveMap.get(y);
                    if (visitedPointCurveSet == null) {
                        visitedPointCurveMap.put(y, visitedPointCurveSet = new HashSet<>());
                    }
                    if (visitedPointCurveSet.contains(x)) {
                        continue;
                    }
                    visitedPointCurveSet.add(x);
                }
                for (int y : visitedPointCurveMap.keySet()) {
                    for (int x : visitedPointCurveMap.get(y)) {
                        if (edgeMapMatrix[y][x]) {
                            matrix[translationY][translationX]++;
                            matrixMaximum = Long.max(matrixMaximum, matrix[translationY][translationX]);
                        }
                    }
                }
            }
        }
        fitLinear = new FitLinear(0, 0, matrixMaximum, 255);
        BufferedImage image = new BufferedImage(translationMaximumX, translationMaximumY, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();
        for (y = 0; y < translationMaximumY; y++) {
           for (x = 0; x < translationMaximumX; x++) {
               raster.setPixel(x, y, new int[]{(int) Math.round(fitLinear.f(matrix[y][x]))});
            }
        }
        try {
            ImageIO.write(image, "png", new File("/home/guir/test.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public HoughBézierDetection(
     BufferedImage edgeMapImage,
     Point2D pointControlArray[],
     double curveStep
    ) {
        edgeMapWidth  = edgeMapImage.getWidth() ;
        edgeMapHeight = edgeMapImage.getHeight();
        pointControlAmount = pointControlArray.length;
        pointControlOriginalArray = new Point2D[pointControlAmount];
        this.pointControlArray = new Point2D[pointControlAmount];
        for (i = 0; i < pointControlAmount; i++) {
            pointControlOriginalArray[i] = new Point2D.Double(
             pointControlArray[i].getX(),
             pointControlArray[i].getY()
            );
            this.pointControlArray[i] = new Point2D.Double(
             pointControlArray[i].getX(),
             pointControlArray[i].getY()
            );
        }
        curve = new BézierCurve(this.pointControlArray, point);
        this.curveStep = curveStep;
        {
            double boundingBoxMinimumXTemporary = POSITIVE_INFINITY;
            double boundingBoxMinimumYTemporary = POSITIVE_INFINITY;
            double boundingBoxMaximumXTemporary = NEGATIVE_INFINITY;
            double boundingBoxMaximumYTemporary = NEGATIVE_INFINITY;
            for (t = 0; t < 1; t += 0.01) {
                curve.op(t);
                boundingBoxMinimumXTemporary = min(boundingBoxMinimumXTemporary, point.getX());
                boundingBoxMinimumYTemporary = min(boundingBoxMinimumYTemporary, point.getY());
                boundingBoxMaximumXTemporary = max(boundingBoxMaximumXTemporary, point.getX());
                boundingBoxMaximumYTemporary = max(boundingBoxMaximumYTemporary, point.getY());
            }
            boundingBoxWidth  = boundingBoxMaximumXTemporary - boundingBoxMinimumXTemporary;
            boundingBoxHeight = boundingBoxMaximumYTemporary - boundingBoxMinimumYTemporary;
            for (Point2D pointT : pointControlOriginalArray) {
                pointT.setLocation(
                 pointT.getX() - boundingBoxMinimumXTemporary,
                 pointT.getY() - boundingBoxMinimumYTemporary
                );
            }
        }
        WritableRaster edgeMapRaster = edgeMapImage.getRaster();
        edgeMapMatrix = new boolean[edgeMapHeight][edgeMapWidth];
        int color[] = edgeMapRaster.getPixel(0, 0, (int[]) null);
        for (y = 0; y < edgeMapHeight; y++) {
            for (x = 0; x < edgeMapWidth; x++) {
                edgeMapRaster.getPixel(x, y, color);
                edgeMapMatrix[y][x] = color[0] > 0;
            }
        }
    }

    public static final void main(String args[]) throws IOException {
        HoughBézierDetection houghBézierDetection = new HoughBézierDetection(
//         ImageIO.read(new File("/home/guir/NetBeansProjects/BézierFit/image/curve 3.png")),
         ImageIO.read(new File("/home/guir/NetBeansProjects/BézierFit/image/IMG_2809_750p_Canny.png")),
         new Point2D[]{
            new Point2D.Double(46.68934740931896 ,  8.455795800042633),
            new Point2D.Double(51.70876997504402 , 23.92517841953924 ),
            new Point2D.Double(43.241905835916654, 15.254723167927162),
            new Point2D.Double(23.145958846066904, 28.240495798740664),
            new Point2D.Double(46.9533674635266  , 38.144264059312604),
            new Point2D.Double(48.5363376237529  , 23.319259842584184),
            new Point2D.Double(48.89321962067063 , 41.556246616451354)
        },
         0.01
        );
        houghBézierDetection.detect(1, 1, 0.01);
    }
}
