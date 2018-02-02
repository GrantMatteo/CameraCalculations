package org.usfirst.frc.team1086.CameraCalculator;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The vision target class.
 * 
 * Each type of vision target should inherit from VisionTarget and be its own class. 
 * Rather than create multiple instances with the same name, *always* access each vision target by checking the static HashMap
 * 
 * @author Jack
 */
public abstract class VisionTarget {
    public double height;
    public double aspectRatio;
    public static HashMap<String, VisionTarget> targets = new HashMap();
    
    /**
     * Instantiates the vision target with a name
     * @param s the name of the vision target.
     */
    public VisionTarget(String s){
        VisionTarget.targets.put(s, this);
    }
    
    /**
     * This method should validate the sightings. 
     * This abstract method should take the list of possible sightings and determine whether or not they are a valid sighting of the vision target
     * For targets that have multiple parts, this should combine the separate parts in to one sighting
     * @param polys the sightings to evaluate.
     * @return the valid sightings
     */
    public abstract ArrayList<Sighting> validateSightings(ArrayList<Sighting> polys);
    
    /**
     * This method should determine whether or not the estimation provided by the CVCamera calculator is good.
     * @param s the sighting to validate
     * @return whether or not the estimation given is a good/usable estimation
     */
    public abstract boolean estimationIsGood(Sighting s);
    
    /**
     * Creates a CVCamera calculator to be used by a given CVCamera for this vision target
     * @param c the CVCamera
     * @return the CVCamera calculator
     */
    public CameraCalculator getCalculator(Camera c){
        return new CameraCalculator(c, this);
    }
}