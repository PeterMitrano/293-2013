package edu.wpi.first.wpilibj.templates.subsystems;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.templates.RobotMap;

/**
 * Self explanatory code below. Controls the driveTrain using built in WPILib
 * class.
 */
public class DriveTrain {

    public static final DigitalInput frontLimit = new DigitalInput(RobotMap.frontLimit);
    public static final Talon leftDrivePWM1 = new Talon(RobotMap.leftDrivePWM1),
            leftDrivePWM2 = new Talon(RobotMap.leftDrivePWM2),
            rightDrivePWM1 = new Talon(RobotMap.rightDrivePWM1),
            rightDrivePWM2 = new Talon(RobotMap.rightDrivePWM2);
    static final RobotDrive drive = new RobotDrive(leftDrivePWM1, leftDrivePWM2, rightDrivePWM1, rightDrivePWM2);

    /**
     * TankDrive is driving the robot like a... tank.
     */
    public static void tankDrive(double leftSpeed, double rightSpeed) {
        drive.tankDrive(leftSpeed, rightSpeed);
    }

    /**
     * ArcadeDrive is driving the robot similar to a RC car.
     */
    public static void arcadeDrive(double moveSpeed, double rotationalSpeed) {
        drive.arcadeDrive(moveSpeed, rotationalSpeed);
    }

    /**
     * Rotates the robot at a defined speed.
     */
    public static void rotateDrive(double rotationalSpeed) {
        drive.arcadeDrive(0, rotationalSpeed);
    }
}
