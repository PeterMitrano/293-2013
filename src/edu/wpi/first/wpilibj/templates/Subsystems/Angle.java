package edu.wpi.first.wpilibj.templates.subsystems;

import edu.wpi.first.wpilibj.CounterBase;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.templates.RobotMap;

/**
 * This class is for controlling the lead screw that causes the angle to change.
 * Most of the code is self-explanatory. Also controls the winch, which is
 * utilized when climbing.
 */
public class Angle {

    private final static double //Angle adjustment constants: (Need to be tweaked)
            anglePWMUpwardSpeed = -0.56,
            anglePWMDownwardSpeed = 0.56,
            anglePWMUpwardInitSpeed = -0.4,
            anglePWMDownwardSpeedClimb = 0.65,
            winchPWMUpwardSpeed = 0.48,
            winchPWMDownwardSpeedClimb = -0.81,
            angleErrorValue = 0.25;
    private static double DriveHangSpeed = 0.4; //Good enough
    private static boolean climbAngleStatus = false;
    private static final DriverStationLCD LCD = DriverStationLCD.getInstance();
    private static final DigitalInput climberClawLimit1 = new DigitalInput(RobotMap.climberClawLimit1), //Closest to shooter
            climberClawLimit2 = new DigitalInput(RobotMap.climberClawLimit2);
    public static final Talon winchPWM = new Talon(RobotMap.winchPWM);
    private static final DigitalInput shooterAngleLimitLower = new DigitalInput(RobotMap.shooterAngleLimitLower);
    private static final DigitalInput shooterAngleLimitUpper = new DigitalInput(RobotMap.shooterAngleLimitUpper);
    public static final Talon anglePWM = new Talon(RobotMap.anglePWM);
    public static final Encoder angleEncoder = new Encoder(RobotMap.angleAChannelDin, RobotMap.angleBChannelDin, true, CounterBase.EncodingType.k4X);

    /**
     * Goes into robotInit. Only runs once in the beginning. While loops are
     * used because robotInit does not loop. Moves angle all the way up, and
     * starts and resets the encoder.
     */
    public static void angleInit() {
        if (shooterAngleLimitUpper.get() == false) {
            while (shooterAngleLimitUpper.get() == false) {
                anglePWM.set(anglePWMUpwardInitSpeed);
                winchPWM.set(winchPWMUpwardSpeed); //upward speed is release
            }
            angleStop(); //Stop the angle
        }
        angleStop(); //failsafe
        winchPWM.set(0); //Stop the winch
        angleEncoder.setDistancePerPulse(0.0128); //Random numbers FTW
        angleEncoder.start();
        angleEncoder.reset();
    }

    /**
     * Speeds should be calibrated so that winch and lead screw move at the same
     * pace. Otherwise, bad things will happen.
     */
    public static void autoClimb() {
        if (climbAngleStatus == false) {
            setAngle(0);
            if (anglePWM.getSpeed() == 0) {
                winchPWM.set(0.0); //Failsafe
                climbAngleStatus = true;
            }
        }
        if (climbAngleStatus == true) {
            if (climberClawLimit1.get() == true && climberClawLimit2.get() == true) {
                if (angleEncoder.getDistance() < 10.7) {
                    winchPWM.set(winchPWMDownwardSpeedClimb); //Pull in whinch
                    anglePWM.set(anglePWMDownwardSpeedClimb); //Lower the angle
                } else {
                    angleStop(); //Successful climb
                }
            } else {
                if (climberClawLimit1.get() == true) {
                    DriveTrain.tankDrive(DriveHangSpeed, 0); //rotate left
                } else if (climberClawLimit2.get() == true) {
                    DriveTrain.tankDrive(0, DriveHangSpeed); //rotate right
                } else if (angleEncoder.getDistance() > 0) { //If neither limit is pressed, the robot has fallen
                    winchPWM.set(winchPWMUpwardSpeed); //Reset winch
                    anglePWM.set(anglePWMUpwardInitSpeed); //Reset angle
                    DriveHangSpeed = 0.4; //Reset the speed if the robot has fallen
                } else {
                    angleStop(); //Stop and wait for everything to be correct
                }
                if (DriveHangSpeed < 0.5) { //Accelerates rotating speed to max in case the robot is stuck when climbing.  
                    DriveHangSpeed += 0.01;
                } else {
                    DriveHangSpeed = 1;
                }
            }
        }
    }

    /**
     * Sets the angle to a certain value with a certain degree of error.
     */
    public static void setAngle(double angleValue) {
        if (angleEncoder.getDistance() > (angleValue + angleErrorValue)) { //Move up if too low
            angleUp();
        } else if (angleEncoder.getDistance() < (angleValue - angleErrorValue)) { //Move down if too high
            angleDown();
        } else {
            angleStop();
        }
    }

    /**
     * Moves the angle up. Uses encoder reading to slow anglePWM when it's close
     * to it's max height to reduce the chance of damaging the top limit switch.
     */
    protected static void angleUp() { //Protected sounds so much more professional
        if (shooterAngleLimitUpper.get() == false) {
            if (angleEncoder.getDistance() < 1.25) {
                anglePWM.set(anglePWMUpwardSpeed / 2.5); //Slow down when close to limit switch  
            } else {
                anglePWM.set(anglePWMUpwardSpeed);
            }
            LCD.println(DriverStationLCD.Line.kUser2, 1, "Increasing Angle.");
        } else {
            angleStop();
        }
    }

    /**
     * Moves the angle down. Uses encoder reading to slow anglePWM when it's
     * close to it's minimum height to reduce the chance of damaging the bottom
     * limit switch.
     */
    protected static void angleDown() {
        LCD.println(DriverStationLCD.Line.kUser5, 1, ""+shooterAngleLimitLower.get());
        if (shooterAngleLimitLower.get() == false) {
            if (angleEncoder.getDistance() > 11) {
                anglePWM.set(anglePWMDownwardSpeed / 3); //Slow down when close to limit switch
            } else {
                anglePWM.set(anglePWMDownwardSpeed);
            }
        } else {
            angleStop();
        }
    }

    /**
     * Stops the winch and angle.
     */
    public static void angleStop() {
        anglePWM.set(0.0);
        winchPWM.set(0.0);
        LCD.println(DriverStationLCD.Line.kUser2, 1, "Angle Stopped.       ");
    }
}
