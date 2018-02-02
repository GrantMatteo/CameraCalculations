package org.usfirst.frc.team1086.CameraCalculator;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Camera {
    public double vFOV;
    public double hFOV;
    public double xPixels;
    public double yPixels;
    public double horizontalOffset, verticalOffset, depthOffset;
    public double hAngle, vAngle;
    public final int REFRESH_RATE;
    HashMap<VisionTarget, CameraCalculator> calculators = new HashMap();

    /**
     * Instantiates the Camera object
     * @param refreshRate the number of frames to process per second
     * @param vFOV the vertical FOV on the Camera
     * @param hFOV the horizontal FOV on the Camera
     * @param xPixels the number of pixels in the x direction
     * @param yPixels the number of pixels in the y direction
     * @param horizontalOffset the number of inches from the center of the robot the front bottom center of the lens of the Camera is (in the horizontal direction)
     * @param verticalOffset the number of inches from the ground the center of the Camera lens is.
     * @param depthOffset the number of inches how deep in to the robot the Camera is. Currently unused.
     * @param hAngle the horizontal angle of the Camera
     * @param vAngle the vertical angle of the Camera
     */
    public Camera(int refreshRate, double vFOV, double hFOV, double xPixels, double yPixels, double horizontalOffset, double verticalOffset, double depthOffset, double hAngle, double vAngle){
        this.REFRESH_RATE = refreshRate;
        this.vFOV = vFOV;
        this.hFOV = hFOV;
        this.xPixels = xPixels;
        this.yPixels = yPixels;
        this.horizontalOffset = horizontalOffset;
        this.verticalOffset = verticalOffset;
        this.depthOffset = depthOffset;
        this.hAngle = hAngle;
        this.vAngle = vAngle;
    }


    /**
     * Tells the number of sightings of a target the CVCamera has identified
     * @param vt the vision target to check
     * @return the number of sightings identified
     */
    public int numberOfSightings(VisionTarget vt){
        if(!calculators.containsKey(vt))
            return 0;
        return calculators.get(vt).sightingCount();
    }

    /**
     * Returns a list of all validated target sightings
     * @param vt the specified vision target
     * @return the list of sightings
     */
    public ArrayList<Sighting> getSightings(VisionTarget vt){
        return calculators.containsKey(vt) ? calculators.get(vt).getSightings() : null;
    }

    /**
     * Adds a vision target for the CVCamera to send the data to
     * @param target the vision target to add
     */
    public void addVisionTarget(VisionTarget target){
        calculators.put(target, target.getCalculator(this));
    }

    /**
     * Configures the horizontal and vertical angles of the CVCamera
     * @param target the target used to configure the robot. The center of the robot must be perfectly aligned with the given target
     * @param distance the distance between the robot and the target
     * @throws java.lang.Exception To configure, the CVCamera must only be able to see one valid sighting.
     */
    public void configure(VisionTarget target, double distance) throws Exception {
        if(calculators.get(target).sightingCount() != 1)
            throw new Exception("CVCamera must only see one target to perform configuration!");
        if(!calculators.get(target).getSightings().get(0).rawH.isPresent() || !calculators.get(target).getSightings().get(0).rawV.isPresent())
            throw new Exception("Raw vertical angle or raw horizontal angle is not available!");
        hAngle = CameraConfig.getXAngle(calculators.get(target).getSightings().get(0).rawH.getAsDouble(), distance, horizontalOffset);
        vAngle = CameraConfig.getYAngle(calculators.get(target).getSightings().get(0).rawV.getAsDouble(), target.height - verticalOffset, distance);
    }
}
