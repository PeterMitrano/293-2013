package edu.wpi.first.wpilibj.templates.subsystems;

import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Most vision processing is done with RoboRealm on the Laptop and sent over to
 * the robot via networktables. This class uses networktables to get the values.
 */
public class Vision {

    private static final NetworkTable cameraTable = NetworkTable.getTable("SmartDashboard");
    private static final DriverStationLCD LCD = DriverStationLCD.getInstance();
    public static double encoderAngle, emergencyCorrectionValue = 0.0;

    public static double centerOfGravity() { //Only COG_X is required to center the robot
        double centerOfGravity = cameraTable.getNumber("COG_X", 160); //160 as default, so nothing will happen if no value is acquired from smartdashboard
        return centerOfGravity;
    }

    public static double getDistance() { //In feet
        double distance = cameraTable.getNumber("Distance", -1);
        return distance;
    }

    public static double calculateAngle() {//This is an best fit line generated from a table of values with excel
        double pixelTargetDistanceTop = cameraTable.getNumber("pixelTargetDistanceTop", 80);
        double pixelTargetDistanceBottom = cameraTable.getNumber("pixelTargetDistanceBottom", 80);
        LCD.println(DriverStationLCD.Line.kUser5, 1, pixelTargetDistanceBottom + " degrees");
        double averagePixelDistance = (pixelTargetDistanceTop + pixelTargetDistanceBottom) / 2.0;
        double angle = 0.0;
        double FOVWidth = 62.0 / (averagePixelDistance / 320.0);
        double distanceToTarget = (FOVWidth / 2.0) / Math.tan(Math.toRadians(33.5));
        double d = Math.sqrt(distanceToTarget * distanceToTarget - 109.5 * 109.5) - 20;
        SmartDashboard.putNumber("distnaceOnFloor", d);
        emergencyCorrectionValue = SmartDashboard.getNumber("EMERGENCY CORRECTION VALUE",0.0);
        LCD.println(DriverStationLCD.Line.kUser4, 1, emergencyCorrectionValue+ "");
        encoderAngle = 2E-06 * d * d * d - 0.0011 * d * d + 0.2918 * d - 16.928 + emergencyCorrectionValue;
        return encoderAngle;
    }
}
