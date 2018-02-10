package org.usfirst.frc.team1086.CameraCalculator;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.wpilibj.CameraServer;
import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.HashMap;
import org.opencv.core.Mat;

public class CVCamera extends Camera {
    ArrayList<Pipeline> pipes = new ArrayList();
    
    /**
     * Instantiates the CVCamera object
     * @param refreshRate the number of frames to process per second
     * @param vFOV the vertical FOV on the CVCamera
     * @param hFOV the horizontal FOV on the CVCamera
     * @param xPixels the number of pixels in the x direction
     * @param yPixels the number of pixels in the y direction
     * @param horizontalOffset the number of inches from the center of the robot the front bottom center of the lens of the CVCamera is (in the horizontal direction)
     * @param verticalOffset the number of inches from the ground the center of the CVCamera lens is.
     * @param depthOffset the number of inches how deep in to the robot the CVCamera is. Currently unused.
     * @param hAngle the horizontal angle of the CVCamera
     * @param vAngle the vertical angle of the CVCamera
     */
    public CVCamera(int refreshRate, double vFOV, double hFOV, double xPixels, double yPixels, double horizontalOffset, double verticalOffset, double depthOffset, double hAngle, double vAngle){
        super(refreshRate, vFOV, hFOV, xPixels, yPixels, horizontalOffset, verticalOffset, depthOffset, hAngle, vAngle);
    }
    
    /**
     * Adds a pipeline that processes the images the CVCamera takes
     * @param p the pipeline to add.
     */
    public void addPipeline(Pipeline p){
        pipes.add(p);
    }
    
    /**
     * Initializes the CVCamera with a VisionSource
     * @param source the source of the CVCamera
     * @param name the name of the CVCamera
     */
    public void initializeCamera(VideoSource source, String name){
        CvSink sink = new CvSink(name);
        sink.setSource(source);
        sink.setEnabled(true);
        new Thread(() -> {
            try {
                CameraServer.getInstance().startAutomaticCapture(source);
                Mat sourceMat = new Mat();
                while (!interrupted()) {
                    sink.grabFrame(sourceMat);
                    process(sourceMat);
                    sleep((int)(1000.0 / REFRESH_RATE));
                }
            } catch (Exception e) {}
        }).start();
    }
    
    /**
     * Processes the frames that the CVCamera processes
     * @param source the captured frame
     */
    private void process(Mat source){
        for(Pipeline pipe : pipes){
            ArrayList<Sighting> sightings = pipe.process(source);
            pipe.getSupportedTargets().stream().filter(target -> calculators.containsKey(target)).forEach(target -> {
                calculators.get(target).updateObjects((ArrayList<Sighting>) sightings.clone());
            });
        }
    }
}