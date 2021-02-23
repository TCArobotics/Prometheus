package frc.robot;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.PWMVictorSPX;


public class DriveControl
{
    private final XboxController driveController;
    private final DPadCalc Dpad;
    private final Debouncer startButton;
    private final Debouncer lbButton;
    private final Debouncer rbButton;

    //Controllers for motors
    private final SpeedController m_frontLeft;
    private final SpeedController m_rearLeft; 
    private final SpeedController m_frontRight;
    private final SpeedController m_rearRight;

    //Combines controllers into differential drive
    private final SpeedControllerGroup m_left;
    private final SpeedControllerGroup m_right;
    private final DifferentialDrive m_robotDrive;

    //Gyroscope
    //private final Gyroscope myGyroscope;

    //Variables
    private boolean driveType; //Stores the state of drive for joysticks
    private double speed; //Stores the speed the robot is going (-0.5 or -1)
    private boolean isStopped; //Stores if the robot is stopped
    private double leftMultiplier; //Value to multiply left speed by
    private double rightMultiplier; //Value to multiply right speed by
    private boolean rotateToAngle; //whether the robot is currently turning to a specific angle

    //Variables that do not have default values
    private double LeftDriveInput; //Stores the actual left input value for execute
    private double RightDriveInput; //Stores the actual right input value for execute
    private int selectedDrive; //Stores the actual drive type used by the execute function

    public DriveControl(/*Gyroscope _gyroscope*/)
    {
        m_frontLeft = new PWMVictorSPX(RobotMap.kFrontLeftPort);
        m_rearLeft = new PWMVictorSPX(RobotMap.kRearLeftPort);
        m_frontRight = new PWMVictorSPX(RobotMap.kFrontRightPort);
        m_rearRight = new PWMVictorSPX(RobotMap.kRearRightPort);

        m_left = new SpeedControllerGroup(m_frontLeft, m_rearLeft);
        m_right = new SpeedControllerGroup(m_frontRight, m_rearRight);
        m_robotDrive = new DifferentialDrive(m_left, m_right);
        
        driveController = new XboxController(RobotMap.kDriverControllerPort);
        Dpad = new DPadCalc(driveController);
        lbButton = new Debouncer(driveController, RobotMap.kLBPort);
        rbButton = new Debouncer(driveController, RobotMap.kRBPort);
        startButton = new Debouncer(driveController, RobotMap.kStartPort);

        //myGyroscope = _gyroscope;

        driveType = true;
        speed = -1;
        isStopped = false;
        leftMultiplier = 1;
        rightMultiplier = 0.95;
        rotateToAngle = false;
    }
    public void calculate(){
        if(rbButton.get())
        {
            driveType = !driveType;
        }

        if(lbButton.get())
        {
            speed -=.25;
            if (speed == -1.25)
            {
                speed = -0.5;
            }
        }
        
        if(startButton.get())
        {
            isStopped = !isStopped;
        }

        
        LeftDriveInput = driveController.getY(Hand.kLeft); //Default LeftDriveInput
        if (driveType) //If driveType is set to Arcade/Curvature
        {
            RightDriveInput = driveController.getX(Hand.kRight);
            if (Math.abs(driveController.getY(Hand.kLeft)) > 0.1)
            {
                selectedDrive = 0;
            }
            else
            {
                selectedDrive = 1;
            }
        }

        else //If driveType is set to Tank
        {
            RightDriveInput = driveController.getY(Hand.kRight);
            selectedDrive = 2;
        }

        rotateToAngle = false;
        /*switch(Dpad.get())
        {
        case 1:
            myGyroscope.setPIDSetpoint(-90.0);
            rotateToAngle = true;
            break;
        case 2:
            myGyroscope.setPIDSetpoint(0.0);
            rotateToAngle = true;
            break;
        case 3:
            myGyroscope.setPIDSetpoint(90.0);
            rotateToAngle = true;
            break;
        case 4:
            myGyroscope.setPIDSetpoint(179.9);
            rotateToAngle = true;
            break;
        default:
            break;
        }
        if (rotateToAngle) 
        {
            selectedDrive = 1;
            RightDriveInput = myGyroscope.calculate();
            LeftDriveInput = 0;
        }*/
    }

    //Calculate motor movement based
    public void calculateAutonomousDrive(double _currentTime, double _startTime, double _duration, double _rightInput, double _leftInput, double _speed)
    {
    	//Calculates whether the time span is correct for the function to run
    	if ((_currentTime >= _startTime) && (_currentTime <= _startTime + _duration))
        {
            this.selectedDrive = 2;
            this.RightDriveInput = _rightInput;
            this.LeftDriveInput = _leftInput;
            this.speed = _speed;
        }
    }  

    public void calculateAutonomousCircle(double _currentTime, double _startTime, double _duration, double _speed, double _radius, double _distanceBtwnWheels, boolean _isClockwise)
    {
        //Calculates whether the time span is correct for the function to run
        //REMEMBER TO MEASURE DISTANCE BETWEEN WHEELS
        if ((_currentTime >= _startTime) && (_currentTime <= _startTime + _duration))
            {
                if (_isClockwise == true)
                    {
                        this.selectedDrive = 2;
                        this.RightDriveInput = 1 / (1 + _radius / _distanceBtwnWheels);
                        this.LeftDriveInput = 1;
                        this.speed = _speed;
                    }
                    else
                    {
                        this.selectedDrive = 2;
                        this.RightDriveInput = 1;
                        this.LeftDriveInput = 1 / (1 + _radius / _distanceBtwnWheels);
                        this.speed = _speed;
                    }
            }
    }
    

    public void execute()
    {
        switch(selectedDrive)
        {
        case 0:
            m_robotDrive.curvatureDrive((isStopped? 0:1) * speed *  LeftDriveInput, 
            (isStopped? 0:1) * speed * RightDriveInput, false);
            break;
        case 1:
            m_robotDrive.arcadeDrive((isStopped? 0:1) * speed * LeftDriveInput, 
            (isStopped? 0:1) * speed * RightDriveInput);
            break;
        case 2:
            m_robotDrive.tankDrive((isStopped? 0:1) * speed * leftMultiplier * LeftDriveInput, 
            (isStopped? 0:1) * speed * rightMultiplier * RightDriveInput);
            break;
        }
    }
    /*public Gyroscope getGyroscope()
    {
        return this.myGyroscope;
    }*/
}