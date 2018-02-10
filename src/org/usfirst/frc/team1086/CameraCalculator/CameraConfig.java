

package org.usfirst.frc.team1086.CameraCalculator;

public class CameraConfig {
    public static double getXAngle(double rawA, double dis, double cameraOffset){
        return Math.acos(cameraOffset / dis) - rawA;
    }
    public static double getYAngle(double rawA, double dHeight, double dis){
        return Math.atan2(dHeight, dis) - rawA;
    }
}
