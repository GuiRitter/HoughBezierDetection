package io.github.guiritter.hough_bézier_detection;

import java.awt.image.BufferedImage;
import java.util.HashSet;

/**
 *
 * @author Guilherme Alan Ritter
 */
public final class MaximaLocalListAndImage {

    public final BufferedImage image;

    public final HashSet<Point3D> maximaLocalSet;

    public MaximaLocalListAndImage(HashSet<Point3D> maximaLocalSet, BufferedImage image) {
        this.image = image;
        this.maximaLocalSet = maximaLocalSet;
    }
}
