package io.github.guiritter.hough_bézier_detection;

import io.github.guiritter.hough_bézier_detection.math.BézierCurve;
import io.github.guiritter.hough_bézier_detection.math.FitLinear;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import static java.lang.Long.MIN_VALUE;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.util.HashMap;
import java.util.HashSet;
import javax.imageio.ImageIO;

/**
 * circle parabola
 * @author Guilherme Alan Ritter
 */
public final class HoughBézierDetection {

    private final BézierCurve curve;

    private final double curveStep;

    private final int edgeMapHeight;

    private final boolean edgeMapMatrix[][];

    private final int edgeMapWidth;

    private FitLinear fitLinear;

    private int i;

    private final Point2D pointBézier = new Point2D.Double();

    private final int pointControlAmount;

    private final Point2D pointControlArray[];

    private final Point2D pointControlOriginalArray[];

    private final Point2D pointControlRotationArray[];

    private double rotation;

    private int rotationI;

    private int rotationMaximum;

//    private double rotationStop;

    private double t;

    private int translationX;

    private int translationY;

    private int translationMaximumX;

    private int translationMaximumY;

    private final HashMap<Integer, HashSet<Integer>> visitedPointCurveMap = new HashMap<>();

    private HashSet<Integer> visitedPointCurveSet;

    private int x;

    private double xD;

    private int y;

    private double yD;

    /**
     *
     * @param translationStepX
     * @param translationStepY
     * @param rotationStart
     * @param rotationRange must be positive
     * @param rotationStep must be positive
     */
    @SuppressWarnings({"empty-statement", "CallToPrintStackTrace"})
    public final void detect(
     double translationStepX,
     double translationStepY,
     double rotationStart,
     double rotationRange,
     double rotationStep
    ) {
//        rotationStop = rotationStart + rotationRange;;
        for (i = 0; (i * translationStepX) < edgeMapWidth ; i++, translationMaximumX = i);
        for (i = 0; (i * translationStepY) < edgeMapHeight; i++, translationMaximumY = i);
        for (i = 0; (i *    rotationStep ) < rotationRange; i++,    rotationMaximum  = i);
        long matrix[][][] = new long[rotationMaximum][translationMaximumY][translationMaximumX]; // TODO test
        long matrixMaximum = MIN_VALUE;
        for (y = 0; y < translationMaximumY; y++) {
            for (x = 0; x < translationMaximumX; x++) {
                for (i = 0; i < rotationMaximum; i++) {
                    matrix[i][y][x] = 0;
                }
            }
        }
        for (rotationI = 0; rotationI < rotationMaximum; rotationI++) {
            rotation = (rotationI * rotationStep) + rotationStart;
            for (i = 0; i < pointControlAmount; i++) {
                xD = pointControlOriginalArray[i].getX();
                yD = pointControlOriginalArray[i].getY();
                pointControlRotationArray[i].setLocation(
                 (cos(rotation) * xD) - (sin(rotation) * yD),
                 (sin(rotation) * xD) + (cos(rotation) * yD)
                );
            }
            for (translationY = 0; translationY < translationMaximumY; translationY++) {
                System.out.println(rotationI + " < " + rotationMaximum + "\t\t" + translationY + " < " + translationMaximumY);
                for (translationX = 0; translationX < translationMaximumX; translationX++) {
                    for (i = 0; i < pointControlAmount; i++) {
                        pointControlArray[i].setLocation(
                         pointControlRotationArray[i].getX() + (((double) translationX) * translationStepX),
                         pointControlRotationArray[i].getY() + (((double) translationY) * translationStepY)
                        );
                    }
                    visitedPointCurveMap.clear();
                    for (t = 0; t < 1; t += curveStep) {
                        curve.op(t);
                        x = (int) pointBézier.getX();
                        y = (int) pointBézier.getY();
                        if ((x < 0) || (x >= edgeMapWidth )
                         || (y < 0) || (y >= edgeMapHeight)) {
                            continue;
                        }
                        visitedPointCurveSet = visitedPointCurveMap.get(y);
                        if (visitedPointCurveSet == null) {
                            visitedPointCurveMap.put(y, visitedPointCurveSet = new HashSet<>());
                        }
                        if (visitedPointCurveSet.contains(x)) {
                            continue;
                        }
                        visitedPointCurveSet.add(x);
                    }
                    for (int yV : visitedPointCurveMap.keySet()) {
                        for (int xV : visitedPointCurveMap.get(yV)) {
                            if (edgeMapMatrix[yV][xV]) {
                                matrix[rotationI][translationY][translationX]++;
                                matrixMaximum = Long.max(matrixMaximum, matrix[rotationI][translationY][translationX]);
                            }
                        }
                    }
                }
            }
        }
        fitLinear = new FitLinear(0, 0, matrixMaximum, 255);
        for (rotationI = 0; rotationI < rotationMaximum; rotationI++) {
            BufferedImage image = new BufferedImage(translationMaximumX, translationMaximumY, BufferedImage.TYPE_BYTE_GRAY);
            WritableRaster raster = image.getRaster();
            for (y = 0; y < translationMaximumY; y++) {
               for (x = 0; x < translationMaximumX; x++) {
                   raster.setPixel(x, y, new int[]{(int) Math.round(fitLinear.f(matrix[rotationI][y][x]))});
                }
            }
            try {
                ImageIO.write(image, "png", new File("/home/guir/NetBeansProjects/HoughBézierDetection/image/" + rotationI + ".png"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
        pointControlRotationArray = new Point2D[pointControlAmount];
        this.pointControlArray = new Point2D[pointControlAmount];
        for (i = 0; i < pointControlAmount; i++) {
            pointControlOriginalArray[i] = new Point2D.Double(
             pointControlArray[i].getX(),
             pointControlArray[i].getY()
            );
            this.pointControlRotationArray[i] = new Point2D.Double(
             pointControlArray[i].getX(),
             pointControlArray[i].getY()
            );
            this.pointControlArray[i] = new Point2D.Double(
             pointControlArray[i].getX(),
             pointControlArray[i].getY()
            );
        }
        curve = new BézierCurve(this.pointControlArray, pointBézier);
        this.curveStep = curveStep;
        WritableRaster edgeMapRaster = edgeMapImage.getRaster();
        edgeMapMatrix = new boolean[edgeMapHeight][edgeMapWidth];
        int color[] = edgeMapRaster.getPixel(0, 0, (int[]) null);
        for (y = 0; y < edgeMapHeight; y++) {
            for (x = 0; x < edgeMapWidth; x++) {
                edgeMapRaster.getPixel(x, y, color);
                edgeMapMatrix[y][x] = color[0] > 0;
            }
        }
        double centroidX = 0;
        double centroidY = 0;
        int centroidZ = 0;
        for (t = 0; t < 1; t += 0.01) {
            curve.op(t);
            centroidX += pointBézier.getX();
            centroidY += pointBézier.getY();
            centroidZ++;
        }
        for (Point2D point : pointControlOriginalArray) {
            point.setLocation(
             point.getX() - (centroidX / ((double) centroidZ)),
             point.getY() - (centroidY / ((double) centroidZ))
            );
        }
    }

    public static final void main(String args[]) throws IOException {
        HoughBézierDetection houghBézierDetection = new HoughBézierDetection(
//         ImageIO.read(new File("/home/guir/NetBeansProjects/BézierFit/image/curve 3.png")),
//         ImageIO.read(new File("/home/guir/NetBeansProjects/BézierFit/image/IMG_2809_750p_Canny.png")),
         ImageIO.read(new File("/home/guir/NetBeansProjects/BézierFit/image/IMG_2809_50p_Canny.png")),
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
        houghBézierDetection.detect(1, 1,
         -11d * PI / 180d,
         +21d * PI / 180d,
         PI / 180d);
    }
}
