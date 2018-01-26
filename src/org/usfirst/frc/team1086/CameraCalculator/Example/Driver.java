package org.usfirst.frc.team1086.CameraCalculator.Example;

import org.usfirst.frc.team1086.CameraCalculator.Camera;
import edu.wpi.first.wpilibj.CameraServer;

public class Driver {
    public void initialize(){
        GearTarget gear = new GearTarget();
        RetroReflectivePipeline primary = new RetroReflectivePipeline();
        Camera c = new Camera(67, 67, 320, 240, 10, 8, 2, 0, 0);
        c.initializeCamera(CameraServer.getInstance().addAxisCamera("10.10.86.22"), "Gear_Camera");
        c.addVisionTarget(gear);
        c.addPipeline(primary);
        c.
    }
}