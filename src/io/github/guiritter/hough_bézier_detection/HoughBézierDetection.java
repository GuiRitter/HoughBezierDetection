package io.github.guiritter.hough_bézier_detection;

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
import javax.imageio.ImageIO;

/**
 *
 * @author Guilherme Alan Ritter
 */
public final class HoughBézierDetection {

    private final double boundingBoxHeight;

    private final double boundingBoxWidth;

    private final BézierCurve curve;

    private final int edgeMapHeight;

    private final BufferedImage edgeMapImage;

    private final WritableRaster edgeMapRaster;

    private final int edgeMapWidth;

    private int translationMaximumX;

    private int translationMaximumY;

    private final Point2D point = new Point2D.Double();

    private double t;

    private double translationX;

    private double translationY;

    @SuppressWarnings("empty-statement")
    public final void detect(
     double translationStepX,
     double translationStepY,
     double rotationStep
    ) {
        for (translationX = 0; ((int) translationX) < (int) (edgeMapWidth  - 1 - boundingBoxWidth ); translationX += translationStepX, translationMaximumX = ((int) translationX));
        for (translationY = 0; ((int) translationY) < (int) (edgeMapHeight - 1 - boundingBoxHeight); translationY += translationStepY, translationMaximumY = ((int) translationY));
        int minX = Integer.MAX_VALUE; // TODO test
        int minY = Integer.MAX_VALUE; // TODO test
        int maxX = Integer.MIN_VALUE; // TODO test
        int maxY = Integer.MIN_VALUE; // TODO test
        for (translationY = 0; ((int) translationY) < translationMaximumY; translationY++) {
            for (translationX = 0; ((int) translationX) < translationMaximumX; translationX++) {
                for (t = 0; t < 1; t += 0.01) {
                    curve.op(t);
//                    System.out.println(edgeMapImage.getRGB(
//                     (int) (point.getX() + translationX),
//                     (int) (point.getY() + translationY)
//                    ));
                    minX = Integer.min(minX, (int) (point.getX() + translationX));
                    minY = Integer.min(minY, (int) (point.getY() + translationY));
                    maxX = Integer.max(maxX, (int) (point.getX() + translationX));
                    maxY = Integer.max(maxY, (int) (point.getY() + translationY));
                }
            }
        }
        System.out.println(minX);
        System.out.println(minY);
        System.out.println(maxX);
        System.out.println(maxY);
    }

    public HoughBézierDetection(
     BufferedImage edgeMapImage,
     Point2D pointControlArray[]
    ) {
        this.edgeMapImage = edgeMapImage;
        edgeMapRaster = edgeMapImage.getRaster();
        edgeMapWidth  = edgeMapImage.getWidth() ;
        edgeMapHeight = edgeMapImage.getHeight();
        curve = new BézierCurve(pointControlArray, point);
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
            for (Point2D pointT : pointControlArray) {
                pointT.setLocation(
                 pointT.getX() - boundingBoxMinimumXTemporary,
                 pointT.getY() - boundingBoxMinimumYTemporary
                );
            }
        }
    }

    public static final void main(String args[]) throws IOException {
        HoughBézierDetection houghBézierDetection = new HoughBézierDetection(
         ImageIO.read(new File("/home/guir/NetBeansProjects/BézierFit/image/curve 3.png")),
//         ImageIO.read(new File("/home/guir/NetBeansProjects/BézierFit/image/IMG_2809_750p_Canny.png")),
         new Point2D[]{
            new Point2D.Double(46.68934740931896 ,  8.455795800042633),
            new Point2D.Double(51.70876997504402 , 23.92517841953924 ),
            new Point2D.Double(43.241905835916654, 15.254723167927162),
            new Point2D.Double(23.145958846066904, 28.240495798740664),
            new Point2D.Double(46.9533674635266  , 38.144264059312604),
            new Point2D.Double(48.5363376237529  , 23.319259842584184),
            new Point2D.Double(48.89321962067063 , 41.556246616451354)
        });
        houghBézierDetection.detect(1, 1, 0.01);
    }
}
