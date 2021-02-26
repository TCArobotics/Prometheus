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
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot 
{
  /* Variable naming conventions: 
  camelCase, 
  C[name] for constants
  */

  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private double m_autoTimerCurrent;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  private final SpeedController m_frontLeft = new PWMVictorSPX(RobotMap.kFrontLeftPort);
  private final SpeedController m_rearLeft = new PWMVictorSPX(RobotMap.kRearLeftPort);
  private final SpeedController m_frontRight = new PWMVictorSPX(RobotMap.kFrontRightPort);
  private final SpeedController m_rearRight = new PWMVictorSPX(RobotMap.kRearRightPort);

  private final SpeedControllerGroup m_left = new SpeedControllerGroup(m_frontLeft, m_rearLeft);
  private final SpeedControllerGroup m_right = new SpeedControllerGroup(m_frontRight, m_rearRight);
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_left, m_right);
  private final double CleftMultiplier = 1; //edit these constants to correct for left/right drift
  private final double CrightMultiplier = 1;
  
  final XboxController m_driverController = new XboxController(RobotMap.kDriverControllerPort);
  final DriveControl driveController = new DriveControl();

  private double LyValue; //value of the joysticks
  private double RxValue;
  private double RyValue;

  private final Debouncer startButton = new Debouncer(m_driverController, RobotMap.kStartPort);
  private final Debouncer lbButton = new Debouncer(m_driverController, RobotMap.kLBPort);
  private final Debouncer rbButton = new Debouncer(m_driverController, RobotMap.kRBPort);
  private final DPadCalc Dpad = new DPadCalc(m_driverController);

  private int selectedDrive = 2; //Stores the actual drive type used by the execute function (1 = arcade, 2 = curvature, 3 = tank)
  private boolean driveType = true; //Stores the state of drive for joysticks (1 = arcade/curvature, 2 = tank)
  private double speed = -.75; //speed of the robot (-1 to 1)
  private boolean isStopped = false;  //boolean in case of emergency stop
  private final String barrelRacing = "BarrelRacing"; //the different choices for autonomousChoice
  private final String slalomPath = "SlalomPath";
  private final String bouncePath = "BouncePath";
  private String autonomousChoice = barrelRacing; //which path to default to


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
    System.out.println("In robotPeriodic");
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
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    m_autoTimerCurrent = Timer.getFPGATimestamp();
    //edit time for each section to refine accuracy

    switch(autonomousChoice) 
    {
    case barrelRacing:
      barrelRacing();
      break;
    case slalomPath:
      slalomPath();
      break;
    case bouncePath:
      bouncePath();
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
    }

    if(lbButton.get())
    {
      speed -= .25;
      if (speed < -1) 
      {
        speed = -.5;
      }
      System.out.println("Speed: " + speed);
    }
    
    if(startButton.get())
    {
      isStopped = !isStopped;
      System.out.println("isStopped: " + isStopped);
    }

    if (driveType)
    {
      selectedDrive = (Math.abs(m_driverController.getY(Hand.kLeft)) > 0.1) ? 0 : 1;
    }
    else
    { 
     selectedDrive = 2;
    }

    RyValue = m_driverController.getY(Hand.kRight);
    RxValue = m_driverController.getX(Hand.kRight);
    LyValue = m_driverController.getY(Hand.kLeft);

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
        m_robotDrive.curvatureDrive((isStopped? 0:1) * speed * LyValue, 
        (isStopped? 0:1) * speed * RxValue, false);
        break;
      case 1:
        m_robotDrive.arcadeDrive((isStopped? 0:1) * speed * LyValue, 
        (isStopped? 0:1) * speed * RxValue);
        break;
      case 2:
        m_robotDrive.tankDrive((isStopped? 0:1) * speed * CleftMultiplier * LyValue, 
        (isStopped? 0:1) * speed * CrightMultiplier * RyValue);
        break;
    }
  }

  public void barrelRacing()
  {
    //forward for 7.5 ft
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 0, 5 , 1.0, 1.0, -0.5);

    //drive in a circle with (radius (3ft), speed (-0.5))
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 5, 5, -0.5, 3, true);
  
    //forward for 9 ft
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 10, 5 , 1.0, 1.0, -0.5);
  
    //drive in a circle with (radius (3ft), speed (-0.5))
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 15, 5, -0.5, 3, false);
  
    //forward for 7 ft
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 20, 5 , 1.0, 1.0, -0.5);
  
    //drive in a circle with (radius (3ft), speed (-0.5))
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 15, 5, -0.5, 3, false);
  
    //forward for 20 ft
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 30, 5 , 1.0, 1.0, -0.5);
  
    //execute function
    driveController.execute();
  }

  public void slalomPath()
  {
    //drive in a circle with (radius (0ft), speed (-0.5))
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 0, 5, -0.5, 0, false);

    //forward for 7 ft
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 5, 5, 1.0, 1.0, -0.5);
  
    //drive in a circle with (radius (5ft), speed (-0.5))
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 10, 5, -0.5, 5, true);
  
    //forward for 7 ft
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 15, 5, 1.0, 1.0, -0.5);
  
    //drive in a circle with (radius (2.5ft), speed (-0.5))
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 20, 5, -0.5, 2.5, false);
  
    //forward for 7 ft
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 25, 5, 1.0, 1.0, -0.5);
  
    //drive in a circle with (radius (5ft), speed (-0.5))
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 30, 5, -0.5, 5, true);
  
    //forward for 7 ft
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 35, 5, 1.0, 1.0, -0.5);
  
    //drive in a circle with (radius (0ft), speed (-0.5))
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 40, 5, -0.5, 5, false);
  }

  public void bouncePath()
  {
    //Turn Left to face cone
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 0, 1, -0.5, 3, false);

    //Drive into cone A3
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 1, 5, 1.0, 1.0, -0.5);
  
    //Turn Left to face back away from cone while heading backwards
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 6, 2, -0.5, 5, false);
  
    //Drive Forward
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 8, 3, -1.0, -1.0, -0.5);
  
    //Circle Around D5 CCW
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 11, 5, 0.5, 5, false);
  
    //Head into cone A6
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 16, 5, -1.0, -1.0, -0.5);
  
    //Turn Left to face back away from cone while heading backwards
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 21, 2, -0.5, 5, false);
  
    //Drive Forward
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 23, 3, 1.0, 1.0, -0.5);
  
    //Turn around D7 CCW
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 26, 2, 0.5, 5, false);
  
    //Head forwards past D8
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 28, 4, 1.0, 1.0, -0.5);
  
    //Turn around D8 CCW
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 30, 2, 0.5, 5, false);
  
    //Head into cone A9
    driveController.calculateAutonomousDrive(m_autoTimerCurrent, 32, 5, 1.0, 1.0, -0.5);
  
    //Circle into finish zone
    driveController.calculateAutonomousCircle(m_autoTimerCurrent, 37, 4, -0.5, 15, false);    
  }
}