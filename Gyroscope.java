/*package frc.robot;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.controller.PIDController;

public class Gyroscope
{
    private static final double kP = 0.03; //turn to angle P constant that will need to be tuned
    private static final double kI = 0; //turn to angle I constant that will need to be tuned
    private static final double kD = 0; //turn to angle D constant that will need to be tuned
    private static final double kF = 0.02; //turn to angle period constant that will need to be tuned
    private static final double kToleranceDegrees = 2; //how close to on target the turn to angle will try to get
    private PIDController turnController; //initializes PID controller for turn heading stabilization
    private final AHRS gyro = new AHRS();  //Add PCID

    public Gyroscope()
    {
        turnController = new PIDController(kP, kI, kD, kF);
        turnController.enableContinuousInput(-180.0, 180.0);
        turnController.setTolerance(kToleranceDegrees);
        resetGyro();
    }

    public void resetGyro(){
        gyro.reset();
    }

    public double calculate(){
        return turnController.calculate(gyro.getAngle());
    }

    public void setPIDSetpoint(double _setPoint){
        turnController.setSetpiont(_setPoint);
    }
}*/