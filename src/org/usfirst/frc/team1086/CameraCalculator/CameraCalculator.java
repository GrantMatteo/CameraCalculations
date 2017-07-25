package org.usfirst.frc.team1086.CameraCalculator;

import java.util.ArrayList;
import java.util.OptionalDouble;

public class CameraCalculator {
    ArrayList<Sighting> visionObjects = new ArrayList();
    ArrayList<Sighting> validSightings = new ArrayList();
    Camera camera;
    VisionTarget visionTarget;
    
    /**
     * Creates the camera calculator
     * @param c the camera the calculator uses to get its image data
     * @param v the vision target it finds
     */
    public CameraCalculator(Camera c, VisionTarget v){
        this.camera = c;
        this.visionTarget = v;
    }
    
    /**
     * Updates the data from the camera
     * @param polys the data from the camera
     */
    void updateObjects(ArrayList<Sighting> polys){
        visionObjects.clear();
        visionObjects.addAll(polys);
        visionObjects = visionTarget.validateSightings(visionObjects);
        calculateDistance();
        calculateAngle();
        calculateRotation();
        eliminateBadTargets();
        validSightings = new ArrayList(visionObjects);
    }
    
    /**
     * Calculates the distance to the specified vision target
     */
    public void calculateDistance(){
        for(Sighting p : visionObjects){
            double midY = p.centerY;
            double angleFromCameraToTarget = getYAngle(midY);
            p.rawV = OptionalDouble.of(angleFromCameraToTarget);
            double verticalAngle = angleFromCameraToTarget + camera.vAngle;
            double changeInY = visionTarget.height - camera.verticalOffset;
            double distanceToTarget = changeInY / Math.sin(verticalAngle);
            double horizontalDistance = distanceToTarget * Math.cos(verticalAngle);
            p.distance = OptionalDouble.of(horizontalDistance);
        }
    }
    
    /**
     * Calculates the angle to the specified vision target
     */
    public void calculateAngle(){
        for(Sighting p : visionObjects){
            double midX = p.centerX;
            p.rawH = OptionalDouble.of(getXAngle(midX));
            double horizontalAngle = Math.PI / 2 - getXAngle(midX);
            double distance = p.distance.getAsDouble();
            double f = Math.sqrt(distance * distance + Math.pow(camera.horizontalOffset, 2) - 2 * distance * camera.horizontalOffset * Math.cos(horizontalAngle));
            double c = Math.asin(camera.horizontalOffset * Math.sin(horizontalAngle) / f);
            double b = Math.PI - horizontalAngle - c;
            p.angle = OptionalDouble.of((Math.PI / 2 - b) - camera.hAngle);
        }
    }
    
    /**
     * Calculates the horizontal rotation of the vision target relative to the camera
     */
    public void calculateRotation(){
        for(Sighting s : visionObjects){
            s.relativeAspectRatio = s.aspectRatio / visionTarget.aspectRatio;
            if(Math.abs(s.relativeAspectRatio) < 1)
            s.rotation = OptionalDouble.of(Math.acos(s.relativeAspectRatio));
        }
    }
    
    /**
     * Finds the horizontal angle to a specific pixel on the camera
     * @param x the pixel
     * @return the angle
     */
    public double getXAngle(double x){
        double HF = (camera.xPixels / 2) / Math.tan(camera.hFOV / 2);
        double cx = (camera.xPixels / 2) - 0.5;
        return Math.atan((cx - x) / HF);
    }
    
    /**
     * Eliminates sightings that have invalid distances, angles, or are considered incorrect for some reason.
     */
    public void eliminateBadTargets(){
        visionObjects.removeIf(s -> !visionTarget.estimationIsGood(s));
    }
    
    /**
     * Finds the vertical angle to a specific pixel on the camera
     * @param y the pixel
     * @return the angle
     */
    public double getYAngle(double y){
        double VF = (camera.yPixels / 2) / Math.tan(camera.vFOV / 2);
        double cy = (camera.yPixels / 2) - 0.5;
        return Math.atan((cy - y) / VF);
    }
    
    /**
     * Returns the most recent target sightings
     * @return the most recent distance calculated to the specified target or -1 if the target is not found.
     */
    public ArrayList<Sighting> getSightings(){
        return new ArrayList<>(validSightings);
    }
    
    /**
     * Tells whether or not the specified vision target can be seen
     * @return whether or not the specified vision target can be seen
     */
    public int sightingCount(){
        return validSightings.size();
    }
}