
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Compressor;
import com.kauailabs.navx.frc.AHRS;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot 
{
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private double m_autoTimerCurrent;
  private double m_autoTimerFirst;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  //Controllers for solenoids
  private Solenoid ballDoorSolenoid = new Solenoid(0); //SOLENOID CODE (Add Channel and PCID)

  //Controllers for compressors
  private Compressor m_pneumaticCompressor = new Compressor(); //SOLENOID CODE (Add PCID)

  //Controllers for navx gyro
  private final AHRS gyro = new AHRS();  //Add PCID

  //Controllers for motors
  private SpeedController m_frontLeft;
  private SpeedController m_rearLeft; 
  private SpeedController m_frontRight;
  private SpeedController m_rearRight;

  //Combines controllers into differential drive
  private SpeedControllerGroup m_left;
  private SpeedControllerGroup m_right;
  private DifferentialDrive m_robotDrive;
  
  //Xbox Controller
  private XboxController m_driverController;

  //Buttons and controls
  private DPadCalc Dpad;
  private Debouncer startButton;
  private Debouncer lbButton;
  private Debouncer rbButton;
  private Debouncer backButton;
  private Debouncer aButton;

  //Variables that have default values set in robotInit()
  private boolean driveType; //Stores the state of drive for joysticks
  private double speed; //Stores the speed the robot is going (-0.5 or -1)
  private boolean isStopped; //Stores if the robot is stopped
  private double heading; //Stores robot heading relative to initial heading
  private double leftMultiplier; //Value to multiply left speed by
  private double rightMultiplier; //Value to multiply right speed by

  //Variables that do not have default values
  private double LeftDriveInput; //Stores the actual left input value for execute
  private double RightDriveInput; //Stores the actual right input value for execute
  private int selectedDrive; //Stores the actual drive type used by the execute function

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() 
  {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    m_frontLeft = new PWMVictorSPX(RobotMap.kFrontLeftPort);
    m_rearLeft = new PWMVictorSPX(RobotMap.kRearLeftPort);
    m_frontRight = new PWMVictorSPX(RobotMap.kFrontRightPort);
    m_rearRight = new PWMVictorSPX(RobotMap.kRearRightPort);

    m_left = new SpeedControllerGroup(m_frontLeft, m_rearLeft);
    m_right = new SpeedControllerGroup(m_frontRight, m_rearRight);
    m_robotDrive = new DifferentialDrive(m_left, m_right);
    
    m_driverController = new XboxController(RobotMap.kDriverControllerPort);
    Dpad = new DPadCalc(m_driverController);

    lbButton = new Debouncer(m_driverController, RobotMap.kLBPort);
    rbButton = new Debouncer(m_driverController, RobotMap.kRBPort);
    startButton = new Debouncer(m_driverController, RobotMap.kStartPort);
    backButton = new Debouncer(m_driverController, RobotMap.kBackPort); //NEW SOLENOID
    aButton = new Debouncer(m_driverController, RobotMap.kAPort); //NEW SOLENOID

    driveType = true;
    speed = -1;
    isStopped = false;
    leftMultiplier = 1;
    rightMultiplier = 0.9;
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() 
  {
    //System.out.println("In robotPeriodic");
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    m_autoTimerFirst = Timer.getFPGATimestamp();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
      while (m_autoTimerCurrent - m_autoTimerFirst <= 5)
      {
        m_autoTimerCurrent = Timer.getFPGATimestamp();
        m_robotDrive.tankDrive(.4, .4);
      }
          break;
      case kDefaultAuto:
      default:
      while (m_autoTimerCurrent - m_autoTimerFirst <= 5)
      {
        m_autoTimerCurrent = Timer.getFPGATimestamp();
        m_robotDrive.tankDrive(.4, .4);
      }
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() 
  {
    // Drive with split arcade drive.
    // That means that the Y axis of the left stick moves forward
    // and backward, and the X of the right stick turns left and right.
    
    if(rbButton.get())
    {
      driveType = !driveType;
      System.out.println("DriveType: " + driveType);
      SmartDashboard.putBoolean("Drive Type", driveType);
    }

    if(lbButton.get())
    {
      speed -=.25;
      if (speed == -1.25)
      {
        speed = -0.5;
      }
      System.out.println("Speed: " + speed);
      SmartDashboard.putNumber("Drive Speed", speed);
    }
    
    if(startButton.get())
    {
      isStopped = !isStopped;
      System.out.println("isStopped: " + isStopped);
    }

    if(backButton.get()) //NEW SOLENOID
    {
      if(m_pneumaticCompressor.enabled())
      {
        m_pneumaticCompressor.start();
      }
      else
      {
        m_pneumaticCompressor.stop();
      }
    }

    if(aButton.get()) //NEW SOLENOID 
    {
      if(ballDoorSolenoid.get())
      {
        ballDoorSolenoid.set(false);
      }
      else
      {
        ballDoorSolenoid.set(true);
      }
    }



    LeftDriveInput = m_driverController.getY(Hand.kLeft); //Default LeftDriveInput
    if (driveType) //If driveType is set to Arcade/Curvature
    {
      RightDriveInput = m_driverController.getX(Hand.kRight);
      if (Math.abs(m_driverController.getY(Hand.kLeft)) > 0.1)
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
      RightDriveInput = m_driverController.getY(Hand.kRight);
      selectedDrive = 2;
    }

    switch(Dpad.get())
    {
      case 1:
        LeftDriveInput = 0;
        break;
      case 2:
        LeftDriveInput = 0;
        break;
      case 3:
        LeftDriveInput = 0;
        break;
      case 4:
        LeftDriveInput = 0;
        break;
      default:
        break;
    }

    execute();
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
    System.out.println("Test!");
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
}