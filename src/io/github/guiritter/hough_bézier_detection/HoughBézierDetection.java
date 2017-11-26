package io.github.guiritter.hough_bézier_detection;

import io.github.guiritter.hough_bézier_detection.math.BézierCurve;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import javax.imageio.ImageIO;

/**
 *
 * @author Guilherme Alan Ritter
 */
public final class HoughBézierDetection {

    private final BézierCurve curve;

    private final BufferedImage edgeMapImage;

    private final int offsetMaximumX;

    private final int offsetMaximumY;

    private final int offsetMinimumX;

    private final int offsetMinimumY;

    private final WritableRaster edgeMapRaster;

    private final Point2D point = new Point2D.Double();

    private double t;

    public final void detect() {
        for (int offSetY = offsetMinimumY; offSetY <= offsetMaximumY; offSetY++) {
            for (int offSetX = offsetMinimumX; offSetX <= offsetMaximumX; offSetX++) {
                for (t = 0; t < 1; t += 0.01) {
                    curve.op(t);
                    System.out.println(edgeMapImage.getRGB(
                     ((int) point.getX()) + offSetX,
                     ((int) point.getY()) + offSetY
                    ));
                }
            }
        }
    }

    public HoughBézierDetection(
     BufferedImage edgeMapImage,
     Point2D pointControlArray[]
    ) {
        this.edgeMapImage = edgeMapImage;
        edgeMapRaster = edgeMapImage.getRaster();
        curve = new BézierCurve(pointControlArray, point);
        {
            int boundingBoxMinimumX = MAX_VALUE;
            int boundingBoxMinimumY = MAX_VALUE;
            int boundingBoxMaximumX = MIN_VALUE;
            int boundingBoxMaximumY = MIN_VALUE;
            for (Point2D pointT : pointControlArray) {
                boundingBoxMinimumX = min(
                 boundingBoxMinimumX,
                 (int) pointT.getX()
                );
                boundingBoxMinimumY = min(
                 boundingBoxMinimumY,
                 (int) pointT.getY()
                );
                boundingBoxMaximumX = max(
                 boundingBoxMaximumX,
                 (int) pointT.getX()
                );
                boundingBoxMaximumY = max(
                 boundingBoxMaximumY,
                 (int) pointT.getY()
                );
            }
            offsetMinimumX = -boundingBoxMinimumX;
            offsetMinimumY = -boundingBoxMinimumY;
            offsetMaximumX = edgeMapImage.getWidth()  - boundingBoxMaximumX - 1;
            offsetMaximumY = edgeMapImage.getHeight() - boundingBoxMaximumY - 1;
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
        houghBézierDetection.detect();
    }
}
