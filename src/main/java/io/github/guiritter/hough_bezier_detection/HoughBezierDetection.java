package io.github.guiritter.hough_bezier_detection;

import io.github.guiritter.fit_linear.FitLinear;
import io.github.guiritter.hough_bezier_detection.math.BezierCurve;
import io.github.guiritter.hough_bezier_detection.math.MaximaLocal;
import io.github.guiritter.hough_bezier_detection.math.Point3D;
import java.awt.Color;
import static java.awt.Color.getHSBColor;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
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
 * Detects the location and angle of a given Bézier curve in a given image
 * using the Hough transform. The Bézier curve is represented
 * by its control points. The parameter space is translation of control points
 * in {@code x} and {@code y} and rotation in radians.
 * @author Guilherme Alan Ritter
 */
public final class HoughBezierDetection {

    /**
     * Holds the votes for each position and angle.
     */
    private Point3D accumulator[][][];

    private Color colorColor;

    private int colorInt[] = new int[3];

    private final BezierCurve curve;

    /**
     * Inversely proportional to the density of points rendered in the curve.
     */
    private final double curveStep;

    private final int edgeMapHeight;

    private final BufferedImage edgeMapImage;

//    private final boolean edgeMapMatrix[][];

    private final WritableRaster edgeMapRaster;

    private final int edgeMapWidth;

    private FitLinear fitLinear;

    private int i;

    private BufferedImage image;

    private final MaximaLocal maximaLocal = new MaximaLocal();

    private HashSet<Point3D> maximaLocalSet;

    private final Point2D pointBezier = new Point2D.Double();

    private final int pointControlAmount;

    private final Point2D pointControlArray[];

    private final Point2D pointControlOriginalArray[];

    private final Point2D pointControlRotationArray[];

    private WritableRaster raster;

    private double rotation;

    private int rotationI;

    private int rotationMaximum;

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
     * Applies the Hough transform, adapted for Bézier curves,
     * finds the local maxima in the result and returns them
     * and the original image with the detected curves overlayed.
     * The control points are translated to the image origin with respect to
     * the resulting curve's centroid. The parameters are integral steps
     * multiplied by the provided step size, sweeping the curve
     * from the image origin to its opposite border at different angles.
     * The angle space is specified by a starting angle and a displacement,
     * where the final angle will be the starting one plus the displacement,
     * as well as an angle step size.
     *
     * After computing the transform, it will be searched for local maxima,
     * which indicates potential curve detections. Local maxima can still occur
     * at low levels of detection, so a threshold must be provided
     * to filter these results. The original image is returned
     * with the detected curves overlayed, for ease of inspection. They are
     * color coded by their rotation. Blue represents the initial angle,
     * going through green towards red, which is the final angle.
     * @param translationStepX size of {@code x} translation increments
     * @param translationStepY size of {@code y} translation increments
     * @param rotationStart initial angle in radians
     * @param rotationRange positive angle displacement in radians
     * @param rotationStep positive size of rotation increment
     * @param maximaLocalThreshold minimum value for local maxima detection
     * @return set of local maxima and original image with detected curves
     */
    public final MaximaLocalListAndImage detect(
     double translationStepX,
     double translationStepY,
     double rotationStart,
     double rotationRange,
     double rotationStep,
     long maximaLocalThreshold
    ) {
        translationMaximumX = 1;
        translationMaximumY = 1;
           rotationMaximum  = 1;
        for (i = 0; (i * translationStepX) < edgeMapWidth ; i++, translationMaximumX = i);
        for (i = 0; (i * translationStepY) < edgeMapHeight; i++, translationMaximumY = i);
        for (i = 0; (i *    rotationStep ) < rotationRange; i++,    rotationMaximum  = i);
        accumulator = new Point3D[rotationMaximum][translationMaximumY][translationMaximumX]; // TODO test
        long matrixMaximum = MIN_VALUE;
        for (y = 0; y < translationMaximumY; y++) {
            for (x = 0; x < translationMaximumX; x++) {
                for (i = 0; i < rotationMaximum; i++) {
                    accumulator[i][y][x] = new Point3D(x, y, i, 0);
                }
            }
        }
        colorInt = new int[1];
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
                        x = (int) pointBezier.getX();
                        y = (int) pointBezier.getY();
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
                            edgeMapRaster.getPixel(xV, yV, colorInt);
//                            if (edgeMapMatrix[yV][xV]) {
                            if (colorInt[0] > 0) {
                                accumulator[rotationI][translationY][translationX].w++;
                                matrixMaximum = Long.max(matrixMaximum, accumulator[rotationI][translationY][translationX].w);
                            }
                        }
                    }
                }
            }
        }
        maximaLocalSet = maximaLocal.op(accumulator, maximaLocalThreshold);
        /*
        fitLinear = new FitLinear(0, 0, matrixMaximum, 255);
        colorInt = new int[3];
        for (rotationI = 0; rotationI < rotationMaximum; rotationI++) {
            BufferedImage image = new BufferedImage(translationMaximumX, translationMaximumY, TYPE_INT_RGB);
            WritableRaster raster = image.getRaster();
            for (y = 0; y < translationMaximumY; y++) {
               for (x = 0; x < translationMaximumX; x++) {
                   colorInt[0] = (int) Math.round(fitLinear.f(accumulator[rotationI][y][x].w));
                   colorInt[2] = colorInt[1] = colorInt[0];
                   raster.setPixel(x, y, colorInt);
                }
            }
            //
            FitLinear colorFit = new FitLinear(0, 2d / 3d, rotationMaximum - 1, 0);
            for (Point3D point : maximaLocalSet) {
                if (point.z != rotationI) {
                    continue;
                }
                rotation = (point.z * rotationStep) + rotationStart;
                for (i = 0; i < pointControlAmount; i++) {
                    xD = pointControlOriginalArray[i].getX();
                    yD = pointControlOriginalArray[i].getY();
                    pointControlArray[i].setLocation(
                     (cos(rotation) * xD) - (sin(rotation) * yD) + (((double) point.x) * translationStepX),
                     (sin(rotation) * xD) + (cos(rotation) * yD) + (((double) point.y) * translationStepY)
                    );
                }
                colorColor = getHSBColor((float) colorFit.f(point.z), 1, 1);
                colorInt[0] = colorColor.getRed();
                colorInt[1] = colorColor.getGreen();
                colorInt[2] = colorColor.getBlue();
                for (t = 0; t < 1; t += curveStep) {
                    curve.op(t);
                    x = (int) pointBezier.getX();
                    y = (int) pointBezier.getY();
                    if ((x < 0) || (x >= edgeMapWidth )
                     || (y < 0) || (y >= edgeMapHeight)) {
                        continue;
                    }
                    raster.setPixel(x, y, colorInt);
                }
            }
            //
            try {
                ImageIO.write(image, "png", new File(String.format("/home/guir/NetBeansProjects/HoughBézierDetection/image/detect_%02d.png", rotationI)));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        /**/
        image = new BufferedImage(edgeMapWidth, edgeMapHeight, TYPE_INT_RGB);
        raster = image.getRaster();
        for (y = 0; y < edgeMapHeight; y++) {
            for (x = 0; x < edgeMapWidth; x++) {
                image.setRGB(x, y, edgeMapImage.getRGB(x, y));
            }
        }
        fitLinear = new FitLinear(0, 2d / 3d, rotationMaximum - 1, 0);
        colorInt = new int[3];
        for (Point3D point : maximaLocalSet) {
            rotation = (point.z * rotationStep) + rotationStart;
            for (i = 0; i < pointControlAmount; i++) {
                xD = pointControlOriginalArray[i].getX();
                yD = pointControlOriginalArray[i].getY();
                pointControlArray[i].setLocation(
                 (cos(rotation) * xD) - (sin(rotation) * yD) + (((double) point.x) * translationStepX),
                 (sin(rotation) * xD) + (cos(rotation) * yD) + (((double) point.y) * translationStepY)
                );
            }
            colorColor = getHSBColor((float) fitLinear.f(point.z), 1, 1);
            colorInt[0] = colorColor.getRed();
            colorInt[1] = colorColor.getGreen();
            colorInt[2] = colorColor.getBlue();
            for (t = 0; t < 1; t += curveStep) {
                curve.op(t);
                x = (int) pointBezier.getX();
                y = (int) pointBezier.getY();
                if ((x < 0) || (x >= edgeMapWidth )
                 || (y < 0) || (y >= edgeMapHeight)) {
                    continue;
                }
                raster.setPixel(x, y, colorInt);
            }
        }
        return new MaximaLocalListAndImage(maximaLocalSet, image);
    }

    public HoughBezierDetection(
     BufferedImage edgeMapImage,
     Point2D pointControlArray[],
     double curveStep
    ) {
        this.edgeMapImage = edgeMapImage;
        edgeMapRaster = edgeMapImage.getRaster();
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
        curve = new BezierCurve(this.pointControlArray, pointBezier);
        this.curveStep = curveStep;
//        edgeMapMatrix = new boolean[edgeMapHeight][edgeMapWidth];
//        int color[] = edgeMapRaster.getPixel(0, 0, (int[]) null);
//        for (y = 0; y < edgeMapHeight; y++) {
//            for (x = 0; x < edgeMapWidth; x++) {
//                edgeMapRaster.getPixel(x, y, color);
//                edgeMapMatrix[y][x] = color[0] > 0;
//            }
//        }
        double centroidX = 0;
        double centroidY = 0;
        int centroidZ = 0;
        for (t = 0; t < 1; t += 0.01) {
            curve.op(t);
            centroidX += pointBezier.getX();
            centroidY += pointBezier.getY();
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
//        System.out.println(new Date());
//        BufferedImage image = ImageIO.read(new File("/home/guir/NetBeansProjects/BézierFit/image/curve 3.png"            ));
//        BufferedImage image = ImageIO.read(new File("/home/guir/NetBeansProjects/BézierFit/image/IMG_2809_750p_Canny.png"));
        BufferedImage image = ImageIO.read(new File("/home/guir/NetBeansProjects/BézierFit/image/IMG_2809_50p_Canny.png"));
        Point2D pointArray[] = new Point2D[]{
            new Point2D.Double(46.68934740931896  , 8.455795800042633),
            new Point2D.Double(51.70876997504402 , 23.92517841953924 ),
            new Point2D.Double(43.241905835916654, 15.254723167927162),
            new Point2D.Double(23.145958846066904, 28.240495798740664),
            new Point2D.Double(46.9533674635266,   38.144264059312604),
            new Point2D.Double(48.5363376237529,   23.319259842584184),
            new Point2D.Double(48.89321962067063,  41.556246616451354)
        };
       long timeA = System.nanoTime();
        HoughBezierDetection houghBezierDetection = new HoughBezierDetection(
         image, pointArray, 0.01
        );
        MaximaLocalListAndImage detect = houghBezierDetection.detect(1, 1,
         -11d * PI / 180d,
         +21d * PI / 180d,
         PI / 180d,
         29);
       long timeB = System.nanoTime();
//        ImageIO.write(detect.image, "png", new File("/home/guir/NetBeansProjects/HoughBézierDetection/image/maxima local 29 750p.png"));
//        for (Point3D point : detect.maximaLocalSet) {
//            System.out.println(point);
//        }
//        System.out.println(new Date());
       System.out.println(timeB - timeA);
//        JOptionPane.showMessageDialog(null, "done");
    }
}
