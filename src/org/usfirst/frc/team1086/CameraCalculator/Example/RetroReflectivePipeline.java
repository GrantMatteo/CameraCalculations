package org.usfirst.frc.team1086.CameraCalculator.Example;
import org.usfirst.frc.team1086.CameraCalculator.Pipeline;
import org.usfirst.frc.team1086.CameraCalculator.Sighting;
import org.usfirst.frc.team1086.CameraCalculator.VisionTarget;
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
public class RetroReflectivePipeline implements Pipeline {
    Mat hsvThresholdOutput = new Mat();
    ArrayList<MatOfPoint> findContoursOutput = new ArrayList();
    ArrayList<MatOfPoint> filterContoursOutput = new ArrayList();
    @Override public ArrayList<Sighting> process(Mat source){
        Mat output = new Mat();
        Mat hsvThresholdInput = source;
        double[] hsvThresholdHue = {49, 95};
        double[] hsvThresholdSaturation = {231, 255};
        double[] hsvThresholdValue = {30, 248};
        hsvThreshold(hsvThresholdInput, hsvThresholdHue, hsvThresholdSaturation, hsvThresholdValue, output);
        hsvThresholdOutput = output.clone();
        Mat findContoursInput = hsvThresholdOutput;
        boolean findContoursExternalOnly = false;
        findContours(findContoursInput, findContoursExternalOnly, findContoursOutput);
        ArrayList<MatOfPoint> filterContoursContours = findContoursOutput;
        double filterContoursMinArea = 50;
        double filterContoursMinPerimeter = 0.0;
        double filterContoursMinWidth = 0;
        double filterContoursMaxWidth = 700.0;
        double filterContoursMinHeight = 0;
        double filterContoursMaxHeight = 1000;
        double[] filterContoursSolidity = {30, 100.0};
        double filterContoursMaxVertices = 1000000;
        double filterContoursMinVertices = 0;
        double filterContoursMinRatio = 0;
        double filterContoursMaxRatio = 1000;
        filterContours(filterContoursContours, filterContoursMinArea, filterContoursMinPerimeter, filterContoursMinWidth, filterContoursMaxWidth, filterContoursMinHeight, filterContoursMaxHeight, filterContoursSolidity, filterContoursMaxVertices, filterContoursMinVertices, filterContoursMinRatio, filterContoursMaxRatio, filterContoursOutput);
        ArrayList<Sighting> sightings = new ArrayList();
        filterContoursOutput.forEach(mop -> sightings.add(new Sighting(mop)));
        return sightings;
    }
    @Override public ArrayList<VisionTarget> getSupportedTargets(){
        ArrayList<VisionTarget> supported = new ArrayList();
        supported.add(VisionTarget.targets.get("Gear Lift"));
        return supported;
    }
}