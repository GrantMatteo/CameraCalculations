package org.usfirst.frc.team1086.CameraCalculator;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public interface Pipeline {
    /**
     * Convert a frame captured by the camera to an ArrayList of sighting objects
     * @param source the frame to process
     * @return the result
     */
    public ArrayList<Sighting> process(Mat source);
    
    /**
     * Returns a list of vision targets that can be identified by processing an image on this pipeline
     * @return a list of supported vision targets
     */
    public ArrayList<VisionTarget> getSupportedTargets();
    
    /**
     * Performs a HSV threshold and returns a binary image
     * @param input the input frame
     * @param hue the hue ranges
     * @param sat the saturation ranges
     * @param val the value ranges
     * @param out the binary image
     */
    default void hsvThreshold(Mat input, double[] hue, double[] sat, double[] val, Mat out){
        Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2HSV);
        Core.inRange(out, new Scalar(hue[0], sat[0], val[0]), new Scalar(hue[1], sat[1], val[1]), out);
    }
    
    /**
     * Finds contours in a binary image
     * @param input the binary image
     * @param externalOnly determines whether or not it should be external contours only
     * @param contours the list to be written to.
     */
    default void findContours(Mat input, boolean externalOnly, List<MatOfPoint> contours){
        Mat hierarchy = new Mat();
        contours.clear();
        int mode;
        if(externalOnly){
            mode = Imgproc.RETR_EXTERNAL;
        } else {
            mode = Imgproc.RETR_LIST;
        }
        int method = Imgproc.CHAIN_APPROX_SIMPLE;
        Imgproc.findContours(input, contours, hierarchy, mode, method);
    }
    
    /**
     * Filters contours on a variety of conditions
     * @param inputContours the starting contours
     * @param minArea the minimum area of a contour
     * @param minPerimeter the minimum perimeter of a contour
     * @param minWidth the minimum width of a contour
     * @param maxWidth the maximum width of a contour
     * @param minHeight the minimum height of a contour
     * @param maxHeight the maximum height of a contour
     * @param solidity the solidity range of the contour
     * @param maxVertexCount the maximum number of vertices in a contour
     * @param minVertexCount the minimum number of vertices in a contour
     * @param minRatio the minimum aspect ratio
     * @param maxRatio the maximum aspect ratio
     * @param output the list to be written to
     */
    default void filterContours(List<MatOfPoint> inputContours, double minArea, double minPerimeter, double minWidth, double maxWidth, double minHeight, double maxHeight, double[] solidity, double maxVertexCount, double minVertexCount, double minRatio, double maxRatio, List<MatOfPoint> output){
        final MatOfInt hull = new MatOfInt();
        output.clear();
        for (int i = 0; i < inputContours.size(); i++) {
            final MatOfPoint contour = inputContours.get(i);
            final Rect bb = Imgproc.boundingRect(contour);
            if (bb.width < minWidth || bb.width > maxWidth) {
                continue;
            }
            if (bb.height < minHeight || bb.height > maxHeight) {
                continue;
            }
            final double area = Imgproc.contourArea(contour);
            if (area < minArea) {
                continue;
            }
            if (Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true) < minPerimeter) {
                continue;
            }
            Imgproc.convexHull(contour, hull);
            MatOfPoint mopHull = new MatOfPoint();
            mopHull.create((int) hull.size().height, 1, CvType.CV_32SC2);
            for (int j = 0; j < hull.size().height; j++) {
                int index = (int) hull.get(j, 0)[0];
                double[] point = new double[]{contour.get(index, 0)[0], contour.get(index, 0)[1]};
                mopHull.put(j, 0, point);
            }
            final double solid = 100 * area / Imgproc.contourArea(mopHull);
            if (solid < solidity[0] || solid > solidity[1]) {
                continue;
            }
            if (contour.rows() < minVertexCount || contour.rows() > maxVertexCount) {
                continue;
            }
            final double ratio = bb.width / (double) bb.height;
            if (ratio < minRatio || ratio > maxRatio) {
                continue;
            }
            output.add(contour);
        }
    }
}
