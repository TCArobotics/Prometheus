/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;
package com.kauailabs.navx.frc; //have to fix this. figure out how to import the AHRS class.

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.controller.PIDController;
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
  private double m_autoTimer;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  private final SpeedController m_frontLeft = new PWMVictorSPX(RobotMap.kFrontLeftPort);
  private final SpeedController m_rearLeft = new PWMVictorSPX(RobotMap.kRearLeftPort);
  private final SpeedController m_frontRight = new PWMVictorSPX(RobotMap.kFrontRightPort);
  private final SpeedController m_rearRight = new PWMVictorSPX(RobotMap.kRearRightPort);

  private final SpeedControllerGroup m_left = new SpeedControllerGroup(m_frontLeft, m_rearLeft);
  private final SpeedControllerGroup m_right = new SpeedControllerGroup(m_frontRight, m_rearRight);
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_left, m_right);
  
  final XboxController m_driverController = new XboxController(RobotMap.kDriverControllerPort);

  private final Debouncer aButton = new Debouncer(m_driverController, RobotMap.kAPort);
  private final Debouncer bButton = new Debouncer(m_driverController, RobotMap.kBPort);
  private final Debouncer xButton = new Debouncer(m_driverController, RobotMap.kXPort);
  private final Debouncer yButton = new Debouncer(m_driverController, RobotMap.kYPort);
  private final Debouncer startButton = new Debouncer(m_driverController, RobotMap.kStartPort);
  private final Debouncer lbButton = new Debouncer(m_driverController, RobotMap.kLBPort);
  private final Debouncer rbButton = new Debouncer(m_driverController, RobotMap.kRBPort);

  private int driveType = 0;
  private double speed = -1;
  private boolean isStopped = false;
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
    m_autoTimer = 0;
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
        m_autoTimer = Timer.getFPGATimestamp();
        if (m_autoTimer <= 5)
        {
          m_robotDrive.arcadeDrive(speed, 0);
        }
          break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    // Drive with split arcade drive.
    // That means that the Y axis of the left stick moves forward
    // and backward, and the X of the right stick turns left and right.
    
    if(rbButton.get())
    {
      if(driveType == 2)
      {
        driveType = 0;
      }
      else
      {
        driveType ++;
      }
      System.out.println("DriveType: " + driveType);
    }

    if(lbButton.get())
    {
      speed = (speed == -1? -0.5: -1);
      System.out.println("Speed: " + speed);
    }
    
    if(startButton.get())
    {
      isStopped = !isStopped;
      System.out.println("isStopped: " + isStopped);
    }

    switch (driveType)
    {
      case 0:
        m_robotDrive.arcadeDrive((isStopped? 0:1) * speed * m_driverController.getY(Hand.kLeft), 
        speed * m_driverController.getX(Hand.kRight));
        break;
      case 1:
        m_robotDrive.tankDrive((isStopped? 0:1) * speed * m_driverController.getY(Hand.kLeft), 
        speed * m_driverController.getY(Hand.kRight));
        break;
      case 2:
        m_robotDrive.curvatureDrive((isStopped? 0:1) * speed * m_driverController.getY(Hand.kLeft), 
        speed * m_driverController.getX(Hand.kRight), false);
        break;
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
    System.out.println("Test!");
  }


  public double getGyroValue() {
    double totalYaw = AHRS.getAngle();

    return totalYaw;
  }
}