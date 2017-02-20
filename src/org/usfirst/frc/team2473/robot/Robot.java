package org.usfirst.frc.team2473.robot;

import java.util.Timer;
import java.util.TimerTask;

import org.usfirst.frc.team2473.robot.Database.Value;
import org.usfirst.frc.team2473.robot.subsystems.ClimberSystem;
import org.usfirst.frc.team2473.robot.subsystems.DriveTrain;
import org.usfirst.frc.team2473.robot.commands.AutoAlign;
import org.usfirst.frc.team2473.robot.commands.DriveStraightForward;
import org.usfirst.frc.team2473.robot.commands.Network;
import org.usfirst.frc.team2473.robot.commands.Turn;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot{

	Command autonomousCommand;
	boolean timerRunning;

	public static DriveTrain driveTrain;
	public static ClimberSystem climbSystem;
	public static Command auto;
	public static OI oi;
	public static AnalogGyro gyro;
	public static Relay led;
	public static SensorThread sensorThread;
	Timer robotControlLoop;
	public Database d;
	public static Networking networking;
	
	double lastTime;

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	public void robotInit() {
		driveTrain = new DriveTrain();
		climbSystem = new ClimberSystem();
		gyro = new AnalogGyro(RobotMap.gyro);
		led = new Relay(0);
		oi = new OI();
		
		sensorThread = new SensorThread(5);
		sensorThread.start();
		//networking = Networking.getInstance();
		//networking.start();
		d = Database.getInstance();
		robotControlLoop = new Timer(false);
		timerRunning = false;
		
		CameraServer server = CameraServer.getInstance();
		server.startAutomaticCapture("router side", 0);
		server.startAutomaticCapture("climber side", 1);
		
		SmartDashboard.putData(driveTrain);
		SmartDashboard.putData(climbSystem);
		}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString code to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional commands to the
	 * chooser code above (like the commented example) or additional comparisons
	 * to the switch structure below with additional strings & commands.
	 */
	public void autonomousInit() {
		
		//autonomousCommand = new Turn(Double.parseDouble(SmartDashboard.getString("Auto Selector",
		//		 "10")));
		led.set(Relay.Value.kForward);
		autonomousCommand = new AutoAlign();
		
		/*
		 * String autoSelected = SmartDashboard.getString("Auto Selector",
		 * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
		 * = new MyAutoCommand(); break; case "Default Auto": default:
		 * autonomousCommand = new ExampleCommand(); break; }
		 */

		// schedule the autonomous command (example)
		if (autonomousCommand != null){
			autonomousCommand.start();
		}
	}

	/**
	 * This function is called periodically during autonomous
	 */
	public void autonomousPeriodic() {
		if (!timerRunning) {
			robotControlLoop.scheduleAtFixedRate(new TimerTask(){

				@Override
				public void run() {
					Scheduler.getInstance().run();
				}
			}, 0, 20);
			timerRunning = true;
		}
		log();
	}

	public void teleopInit() {
		// This makes sure that the autonomous stops running when
		// teleop starts running. If you want the autonomous to
		// continue until interrupted by another command, remove
		// this line or comment it out.
	}

	/**
	 * This function is called periodically during operator control
	 */
	public void teleopPeriodic() {

		//System.out.println(System.currentTimeMillis() - lastTime);

		if (!timerRunning) {
			robotControlLoop.scheduleAtFixedRate(new TimerTask(){

				@Override
				public void run() {
					Scheduler.getInstance().run();
				}
			}, 0, 20);
			timerRunning = true;
		}

		oi.updateButtons();
		oi.updateJoysticks();
		
		log();
		lastTime = System.currentTimeMillis();



	}

	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {
	}
	
	@Override
	public void disabledInit(){
		if (autonomousCommand != null)
			autonomousCommand.cancel();
	}
	
	@Override
	public void disabledPeriodic() {

		if (timerRunning) {
			// ends the timer and stops it from executing any tasks
			robotControlLoop.cancel();
			robotControlLoop = new Timer();
			timerRunning = false;
		}
	}

	public void log() {
		Database.getInstance().log();
	}

	@Override
	public void finalize() {
		try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// set motors to 0
	}
}
