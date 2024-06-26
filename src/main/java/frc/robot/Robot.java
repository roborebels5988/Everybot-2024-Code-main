// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

// Imports that allow the usage of REV Spark Max motor controllers
import com.revrobotics.CANSparkBase;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.util.sendable.SendableRegistry;

//imports to allow use of victorspx motorcontrollers for claw and climber

import edu.wpi.first.wpilibj.Joystick;
//import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.drive.MecanumDrive;

//import camera drivers
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.cscore.VideoSink;

public class Robot extends TimedRobot {
  /*
   *  * Autonomous selection options.
   *  
   */
  private static final String kNothingAuto = "do nothing";
  private static final String kLaunchAndDrive = "launch drive";
  private static final String kLaunch = "launch";
  private static final String kDrive = "drive";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  // camera setup
  UsbCamera frontCamera;
  UsbCamera backCamera;
  VideoSink server;
  // PowerDistribution pd = new PowerDistribution(module:0, ModuleType.kCTRE);

  // these names are new for the mecanum drive class
  private static final int kFrontLeftDeviceID = 2;
  private static final int kRearLeftDeviceID = 1;
  private static final int kFrontRightDeviceID = 4;
  private static final int kRearRightDeviceID = 3;

  private CANSparkMax m_leftFrontMotor;
  private CANSparkMax m_leftRearMotor;
  private CANSparkMax m_rightFrontMotor;
  private CANSparkMax m_rightRearMotor;

  // drive joystick_logitech
  private static final int kJoystickChannel = 0;

  private MecanumDrive m_robotDrive;
  // private Joystick m_stick;

  /*
   *  * A class provided to control your drivetrain. Different drive styles can be
   * passed to differential drive:
   *  *
   * https://github.com/wpilibsuite/allwpilib/blob/main/wpilibj/src/main/java/edu/
   * wpi/first/wpilibj/drive/DifferentialDrive.java
   *  
   */
  // DifferentialDrive m_drivetrain;

  /*
   *  * Launcher motor controller instances.
   *  *
   *  * Like the drive motors, set the CAN id's to match your robot or use
   * different
   *  * motor controller classses (VictorSPX) to match your robot as necessary.
   *  *
   *  * Both of the motors used on the KitBot launcher are CIMs which are brushed
   * motors
   *  
   */
  CANSparkBase m_launchWheel = new CANSparkMax(6, MotorType.kBrushed);
  CANSparkBase m_feedWheel = new CANSparkMax(5, MotorType.kBrushed);

  /**
   *  * Roller Claw motor controller instance.
   */
  // CANSparkBase m_rollerClaw = new CANSparkMax(8, MotorType.kBrushed);
  MotorController m_rollerClaw = new WPI_VictorSPX(8);

  /**
   * Climber motor controller instance. In the stock Everybot configuration a
   * NEO is used, replace with kBrushed if using a brushed motor.
   */
  CANSparkBase m_climber = new CANSparkMax(7, MotorType.kBrushed);
  // MotorController m_climber = new WPI_VictorSPX(7);

  CANSparkBase m_groundIntake = new CANSparkMax(9, MotorType.kBrushed);

  /**
   *  * The starter code uses the most generic joystick class.
   *  *
   *  * To determine which button on your controller corresponds to which number,
   * open the FRC
   *  * driver station, go to the USB tab, plug in a controller and see which
   * button lights up
   *  * when pressed down
   *  *
   *  * Buttons index from 0
   *  
   */
  Joystick m_driverController = new Joystick(0);

  Joystick m_manipController = new Joystick(1);

  // --------------- Magic numbers. Use these to adjust settings. ---------------

  /**
   *  * How many amps can an individual drivetrain motor use.
   *  
   */
  // static final int DRIVE_CURRENT_LIMIT_A = 60;

  /**
   *  * How many amps the feeder motor can use.
   *  
   */
  static final int FEEDER_CURRENT_LIMIT_A = 100;

  /**
   *  * Percent output to run the feeder when expelling note
   *  
   */
  static final double FEEDER_OUT_SPEED = 1.0;

  /**
   *  * Percent output to run the feeder when intaking note
   *  
   */
  static final double FEEDER_IN_SPEED = -1;

  /**
   *  * Percent output for amp or drop note, configure based on polycarb bend
   *  
   */
  static final double FEEDER_AMP_SPEED = 1;

  /**
   *  * How many amps the launcher motor can use.
   *  *
   *  * In our testing we favored the CIM over NEO, if using a NEO lower this to
   * 60
   * static final int LAUNCHER_CURRENT_LIMIT_A = 100;
   * 
   * /**
   *  * Percent output to run the launcher when intaking AND expelling note
   *  
   */
  static final double LAUNCHER_SPEED = 1.0;

  /** percent output to run groundintake */
  static final double INTAKE_SPEED = 1.0;
  /**
   *  * Percent output for scoring in amp or dropping note, configure based on
   * polycarb bend
   * .14 works well with no bend from our testing
   *  
   */
  static final double LAUNCHER_AMP_SPEED = .5;
  /**
   * Percent output for the roller claw
   */
  static final double CLAW_OUTPUT_POWER = 1;
  /**
   * Percent output to help retain notes in the claw
   */
  // static final double CLAW_STALL_POWER = .1;
  /**
   * Percent output to power the climber
   */
  static final double CLIMBER_OUTPUT_POWER = 1;

  /**
   *  * This function is run when the robot is first started up and should be used
   * for any
   *  * initialization code.
   *  
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("do nothing", kNothingAuto);
    m_chooser.addOption("launch note and drive", kLaunchAndDrive);
    m_chooser.addOption("launch", kLaunch);
    m_chooser.addOption("drive", kDrive);
    SmartDashboard.putData("Auto choices", m_chooser);

    // below are new mecanum definitions and motorcontroller names
    m_leftFrontMotor = new CANSparkMax(kFrontLeftDeviceID, MotorType.kBrushed);
    m_leftRearMotor = new CANSparkMax(kRearLeftDeviceID, MotorType.kBrushed);
    m_rightFrontMotor = new CANSparkMax(kFrontRightDeviceID, MotorType.kBrushed);
    m_rightRearMotor = new CANSparkMax(kRearRightDeviceID, MotorType.kBrushed);

    SendableRegistry.addChild(m_robotDrive, m_leftFrontMotor);
    SendableRegistry.addChild(m_robotDrive, m_leftRearMotor);
    SendableRegistry.addChild(m_robotDrive, m_rightFrontMotor);
    SendableRegistry.addChild(m_robotDrive, m_rightRearMotor);

    // Invert the right side motors.
    // You may need to change or remove this to match your robot.
    m_rightFrontMotor.setInverted(true);
    m_rightRearMotor.setInverted(true);

    m_robotDrive = new MecanumDrive(m_leftFrontMotor::set, m_leftRearMotor::set, m_rightFrontMotor::set,
        m_rightRearMotor::set);

    m_driverController = new Joystick(kJoystickChannel);

    /*
     *  * Apply the current limit to the drivetrain motors
     *  
     */
    // m_leftFrontMotor. setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);
    // m_leftRearMotor.setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);
    // m_rightFrontMotor.setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);
    // m_rightRearMotor. setSmartCurrentLimit(DRIVE_CURRENT_LIMIT_A);

    /*
     *  * Launcher wheel(s) spinning the wrong direction? Change to true here.
     *  *
     *  * Add white tape to wheel to help determine spin direction.
     *  
     */
    m_feedWheel.setInverted(true);
    m_launchWheel.setInverted(true);

    /*
     *  * Apply the current limit to the launching mechanism
     *  
     */
    // m_feedWheel.setSmartCurrentLimit(FEEDER_CURRENT_LIMIT_A);
    // m_launchWheel.setSmartCurrentLimit(LAUNCHER_CURRENT_LIMIT_A);

    /*
     *  * Inverting and current limiting for roller claw and climber
     *  
     */
    m_rollerClaw.setInverted(false);
    m_climber.setInverted(false);

    // setsmart current method is for sparkmax not victors, same for idle brake mode
    // m_rollerClaw.setSmartCurrentLimit(60);
    m_climber.setSmartCurrentLimit(60);

    /*
     *  * Motors can be set to idle in brake or coast mode.
     *  *
     * Brake mode is best for these mechanisms
     *  
     */
    // m_rollerClaw.setIdleMode(IdleMode.kBrake);
    m_climber.setIdleMode(IdleMode.kBrake);

    m_groundIntake.setIdleMode(IdleMode.kBrake);

    frontCamera = CameraServer.startAutomaticCapture(0);
    backCamera = CameraServer.startAutomaticCapture(1);
    server = CameraServer.getServer();
    frontCamera.setFPS(10);
    backCamera.setFPS(10);
    frontCamera.setResolution(100, 100);
    backCamera.setResolution(100, 100);
  }

  /**
   *  * This function is called every 20 ms, no matter the mode. Use this for
   * items like diagnostics
   *  * that you want ran during disabled, autonomous, teleoperated and test
   * modes.
   *  
   */
  @Override
  public void robotPeriodic() {
    SmartDashboard.putNumber("Time (seconds)", Timer.getFPGATimestamp());
  }

  /*
   *  * Auto constants, change values below in autonomousInit()for different
   * autonomous behaviour
   *  *
   *  * A delayed action starts X seconds into the autonomous period
   *  *
   *  * A time action will perform an action for X amount of seconds
   *  *
   *  * Speeds can be changed as desired and will be set to 0 when
   *  * performing an auto that does not require the system
   *  
   */
  double AUTO_LAUNCH_DELAY_S;
  double AUTO_DRIVE_DELAY_S;

  double AUTO_DRIVE_TIME_S;

  double AUTO_DRIVE_SPEED;
  double AUTO_LAUNCHER_SPEED;

  double autonomousStartTime;

  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();

    m_leftFrontMotor.setIdleMode(IdleMode.kBrake);
    m_leftRearMotor.setIdleMode(IdleMode.kBrake);
    m_rightFrontMotor.setIdleMode(IdleMode.kBrake);
    m_rightRearMotor.setIdleMode(IdleMode.kBrake);

    AUTO_LAUNCH_DELAY_S = 2;
    AUTO_DRIVE_DELAY_S = 3;

    AUTO_DRIVE_TIME_S = 2.0;
    AUTO_DRIVE_SPEED = -0.5;
    AUTO_LAUNCHER_SPEED = 1;

    /*
     *  * Depeding on which auton is selected, speeds for the unwanted subsystems
     * are set to 0
     *  * if they are not used for the selected auton
     *  *
     *  * For kDrive you can also change the kAutoDriveBackDelay
     *  
     */
    if (m_autoSelected == kLaunch) {
      AUTO_DRIVE_SPEED = 0;
    } else if (m_autoSelected == kDrive) {
      AUTO_LAUNCHER_SPEED = 0;
    } else if (m_autoSelected == kNothingAuto) {
      AUTO_DRIVE_SPEED = 0;
      AUTO_LAUNCHER_SPEED = 0;
    }

    autonomousStartTime = Timer.getFPGATimestamp();
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {

    double timeElapsed = Timer.getFPGATimestamp() - autonomousStartTime;

    if (timeElapsed <= 4) {
      m_robotDrive.driveCartesian(0.6, 0, 0);
    } else {
      m_robotDrive.driveCartesian(0, 0, 0);
    }

    /*
     *  * Spins up launcher wheel until time spent in auto is greater than
     * AUTO_LAUNCH_DELAY_S
     *  *
     *  * Feeds note to launcher until time is greater than AUTO_DRIVE_DELAY_S
     *  *
     *  * Drives until time is greater than AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S
     *  *
     *  * Does not move when time is greater than AUTO_DRIVE_DELAY_S +
     * AUTO_DRIVE_TIME_S
     *  
     */
    /*
     * if(timeElapsed < AUTO_LAUNCH_DELAY_S)
     * {
     * m_launchWheel.set(AUTO_LAUNCHER_SPEED);
     * m_drivetrain.arcadeDrive(0, 0);
     * 
     * 
     * }
     * else if(timeElapsed < AUTO_DRIVE_DELAY_S)
     * {
     * m_feedWheel.set(AUTO_LAUNCHER_SPEED);
     * m_drivetrain.arcadeDrive(0, 0);
     * 
     * }
     * else if(timeElapsed < AUTO_DRIVE_DELAY_S + AUTO_DRIVE_TIME_S)
     * {
     * m_launchWheel.set(0);
     * m_feedWheel.set(0);
     * m_drivetrain.arcadeDrive(AUTO_DRIVE_SPEED, 0);
     * }
     * else
     * {
     * m_drivetrain.arcadeDrive(0, 0);
     * }
     */
    /*
     * For an explanation on differintial drive, squaredInputs, arcade drive and
     * tank drive see the bottom of this file
     */
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
    /*
     *  * Motors can be set to idle in brake or coast mode.
     *  *
     *  * Brake mode effectively shorts the leads of the motor when not running,
     * making it more
     *  * difficult to turn when not running.
     *  *
     *  * Coast doesn't apply any brake and allows the motor to spin down naturally
     * with the robot's momentum.
     *  *
     *  * (touch the leads of a motor together and then spin the shaft with your
     * fingers to feel the difference)
     *  *
     *  * This setting is driver preference. Try setting the idle modes below to
     * kBrake to see the difference.
     *  
     */
    // leftRear.setIdleMode(IdleMode.kCoast);
    // leftFront.setIdleMode(IdleMode.kCoast);
    // rightRear.setIdleMode(IdleMode.kCoast);
    // rightFront.setIdleMode(IdleMode.kCoast);
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    /*
     *  * Spins up the launcher wheel
     *  
     */
    if (m_manipController.getRawButton(1)) {
      m_launchWheel.set(LAUNCHER_SPEED);
    } else if (m_manipController.getRawButtonReleased(1)) {
      m_launchWheel.set(0);
    }

    /*
     *  * Spins feeder wheel, wait for launch wheel to spin up to full speed for
     * best results
     *  
     */
    if (m_manipController.getRawButton(6)) {
      m_feedWheel.set(FEEDER_OUT_SPEED);
    } else if (m_manipController.getRawButtonReleased(6)) {
      m_feedWheel.set(0);
    }

    /*
     *  * While the button is being held spin both motors to intake note
     *  
     */
    if (m_manipController.getRawButton(5)) {
      m_launchWheel.set(-LAUNCHER_SPEED);
      m_feedWheel.set(FEEDER_IN_SPEED);
    } else if (m_manipController.getRawButtonReleased(5)) {
      m_launchWheel.set(0);
      m_feedWheel.set(0);
    }

    /*
     *  * While the amp button is being held, spin both motors to "spit" the note
     *  * out at a lower speed into the amp
     *  *
     *  * (this may take some driver practice to get working reliably)
     *  
     */
    if (m_manipController.getRawButton(2)) {
      m_feedWheel.set(FEEDER_AMP_SPEED);
      m_launchWheel.set(LAUNCHER_AMP_SPEED);
    } else if (m_manipController.getRawButtonReleased(2)) {
      m_feedWheel.set(0);
      m_launchWheel.set(0);
    }

    /**
     * Hold one of the two buttons to either intake or exjest note from roller claw
     * 
     * One button is positive claw power and the other is negative
     * 
     * It may be best to have the roller claw passively on throughout the match to
     * better retain notes but we did not test this
     */

    if (m_manipController.getRawButton(3)) {
      m_rollerClaw.set(CLAW_OUTPUT_POWER);
    } else if (m_manipController.getRawButton(4)) {
      m_rollerClaw.set(-CLAW_OUTPUT_POWER);
    } else {
      m_rollerClaw.set(0);
    }

    /**
     * groundintake
     * Hold one of the two buttons to intake or exjest note from ground intake
     */
    if (m_manipController.getRawButton(8)) {
      m_groundIntake.set(INTAKE_SPEED);
    } else if (m_manipController.getRawButtonReleased(8)) {
      m_groundIntake.set(0);
    }

    /**
     * POV is the D-PAD (directional pad) on your controller, 0 == UP and 180 ==
     * DOWN
     * 
     * After a match re-enable your robot and unspool the climb
     */
    if (m_manipController.getPOV() == 0) {
      m_climber.set(1);
    } else if (m_manipController.getPOV() == 180) {
      m_climber.set(-1);
    } else {
      m_climber.set(0);
    }

    /*
     *  * Negative signs are here because the values from the analog sticks are
     * backwards
     *  * from what we want. Pushing the stick forward returns a negative when we
     * want a
     *  * positive value sent to the wheels.
     *  *
     *  * If you want to change the joystick axis used, open the driver station, go
     * to the
     *  * USB tab, and push the sticks determine their axis numbers
     *
     * This was setup with a logitech controller, note there is a switch on the back
     * of the
     * controller that changes how it functions
     *  
     */
    // m_drivetrain.arcadeDrive(-m_driverController.getRawAxis(1),
    // -m_driverController.getRawAxis(4), false);

    m_robotDrive.driveCartesian(-m_driverController.getY(), -m_driverController.getX(), -m_driverController.getZ());
  }
}

/*
 *  * The kit of parts drivetrain is known as differential drive, tank drive or
 * skid-steer drive.
 *  *
 *  * There are two common ways to control this drivetrain: Arcade and Tank
 *  *
 *  * Arcade allows one stick to be pressed forward/backwards to power both
 * sides of the drivetrain to move straight forwards/backwards.
 *  * A second stick (or the second axis of the same stick) can be pushed
 * left/right to turn the robot in place.
 *  * When one stick is pushed forward and the other is pushed to the side, the
 * robot will power the drivetrain
 *  * such that it both moves fowards and turns, turning in an arch.
 *  *
 *  * Tank drive allows a single stick to control of a single side of the robot.
 *  * Push the left stick forward to power the left side of the drive train,
 * causing the robot to spin around to the right.
 *  * Push the right stick to power the motors on the right side.
 *  * Push both at equal distances to drive forwards/backwards and use at
 * different speeds to turn in different arcs.
 *  * Push both sticks in opposite directions to spin in place.
 *  *
 *  * arcardeDrive can be replaced with tankDrive like so:
 *  *
 *  * m_drivetrain.tankDrive(-m_driverController.getRawAxis(1),
 * -m_driverController.getRawAxis(5))
 *  *
 *  * Inputs can be squared which decreases the sensitivity of small drive
 * inputs.
 *  *
 *  * It literally just takes (your inputs * your inputs), so a 50% (0.5) input
 * from the controller becomes (0.5 * 0.5) -> 0.25
 *  *
 *  * This is an option that can be passed into arcade or tank drive:
 *  * arcadeDrive(double xSpeed, double zRotation, boolean squareInputs)
 *  *
 *  *
 *  * For more information see:
 *  * https://docs.wpilib.org/en/stable/docs/software/hardware-apis/motors/wpi-
 * drive-classes.html
 *  *
 *  *
 * https://github.com/wpilibsuite/allwpilib/blob/main/wpilibj/src/main/java/edu/
 * wpi/first/wpilibj/drive/DifferentialDrive.java
 *  *
 *  
 */
