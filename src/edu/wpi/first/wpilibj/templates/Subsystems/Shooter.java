package edu.wpi.first.wpilibj.templates.subsystems;

import edu.wpi.first.wpilibj.CounterBase;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.templates.RobotMap;

/**
 * This class controls the Frisbee pushing arm and also keeps the shooter motors
 * running at a set RPM.
 */
public class Shooter {

    private static final DriverStationLCD LCD = DriverStationLCD.getInstance();
    private static final double encoderCountPerRev = 256,
            ratio = 0.01, //This is to account for the additional friction on the second PWM
            spinUpTolerance = 350, //Spin up speed from 0 to targetRPM-spinUpTolerance so we can fire quicker after loading. 
            tolerance = 45, //+/- this amount of RPM
            shooterPWMAccelerationSpeed = 0.9,
            rate = 0.0015, //Tune it.
            targetRPM = 2400;
    private static double //RPM
            PWMCurrentSpeed = 0.85,
            rawValue, //the current encoder value used for calculations.  
            lastRawValue,
            deltaCounts, //change in counts
            lastTime,
            currentTime,
            deltaTime; //change in time
    public static double currentRPM;
    public static final Talon shooterPWM1 = new Talon(RobotMap.shooterPWM1),
            shooterPWM2 = new Talon(RobotMap.shooterPWM2);
    private static final Relay triggerRelay = new Relay(RobotMap.triggerRelay);
    private static final DigitalInput triggerLimit = new DigitalInput(RobotMap.triggerLimit);
    public static final Encoder RPMEncoder = new Encoder(RobotMap.shooterAChannelDin, RobotMap.shooterBChannelDin, false, CounterBase.EncodingType.k1X);

    /**
     * Should only be run in robotInit sequences. Revs the shooter up to speed ,
     * makes sure the trigger is reset, and starts and resets the shooter wheel
     * encoder.
     */
    public static void shooterInit() {
        if (triggerLimit.get() == false) {
            while (triggerLimit.get() == false) { //Move the trigger motor until it hits the limit.  
                triggerRelay.set(Relay.Value.kForward);
            }
        }
        RPMEncoder.start(); //Start the encoder
        RPMEncoder.reset();
        shooterPWM1.set(PWMCurrentSpeed + ratio); //Rev the movors up.  
        shooterPWM2.set(-PWMCurrentSpeed);
    }

    /**
     * I considered using a PID for this, but it would take too much time and
     * effort to configure. A TBH algorithm was used, but it was too effective
     * and the second motor could not move fast enough due to the wheels sharing
     * one encoder. Because of this, a simple stepping function is used, which
     * seems to do its job. It might not be the best way to control the RPM, but
     * it is reliable and quick enough.
     */
    public static void runShooter() { //Calculate RPM Must be running somewhere in the code.  
        //Following should be self-explanatory.  Join the build team if you do not understand. 
        LCD.println(DriverStationLCD.Line.kUser3, 1, "Shooter Running");
        if (currentRPM < targetRPM - spinUpTolerance) {
            shooterPWM1.set(shooterPWMAccelerationSpeed + ratio); //"ratio" is the offset to account for friction in shooter motor
            shooterPWM2.set(-shooterPWMAccelerationSpeed);
        } else if (currentRPM > targetRPM + tolerance) { //Decrease RPM if too high
            PWMCurrentSpeed -= rate;
        } else if (currentRPM < targetRPM - tolerance) { //Increase RPM if too low
            PWMCurrentSpeed += rate;
        }
        shooterPWM1.set(PWMCurrentSpeed + ratio); //Motors run in opposite directions
        shooterPWM2.set(-PWMCurrentSpeed);
    }

    /**
     * Stops the shooter
     */
    public static void stopShooter() {
        shooterPWM1.set(0.0);
        shooterPWM2.set(0.0);
        LCD.println(DriverStationLCD.Line.kUser3, 1, "Shooter Stopped");
    }

    /**
     * Fires the shooter.
     */
    public static void fireShooter(boolean button) {
        if (button == true) { //Fire if button boolean is true
            LCD.println(DriverStationLCD.Line.kUser6, 1, "FIRING!!! PEW! PEW!");
        }
        //Makes sure the robot is able to fire accurately and safely.  
        if (((button == true && shooterStatus() == true) || triggerLimit.get() == false) && Angle.angleEncoder.getDistance() < 11.7) {
            triggerRelay.set(Relay.Value.kForward);
            LCD.println(DriverStationLCD.Line.kUser6, 1, "Patience...                ");
        } else if (triggerLimit.get() == true) { //Reset the trigger after each Frisbee
            triggerRelay.set(Relay.Value.kOff);
            LCD.println(DriverStationLCD.Line.kUser6, 1, "........................");
        }
    }

    /**
     * This is where the magic happens.
     */
    public static double calculateRPM() {
        rawValue = Math.abs(RPMEncoder.getRaw());
        currentTime = Timer.getFPGATimestamp(); //gets the current time
        deltaCounts = rawValue - lastRawValue; //Change in encoder reading
        deltaTime = currentTime - lastTime; //Change in time
        currentRPM = deltaCounts * (60 / encoderCountPerRev) / deltaTime; //Calculates RPM
        lastRawValue = rawValue;
        lastTime = currentTime;
        return currentRPM;
    }

    /**
     * Returns true if the RPM is in a reasonable range.  
     */
    public static boolean shooterStatus() {
        if (Math.abs(currentRPM - targetRPM) < tolerance) {
            return true;
        } else {
            return false;
        }
    }
}
