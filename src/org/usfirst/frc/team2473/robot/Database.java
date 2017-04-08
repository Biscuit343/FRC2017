package org.usfirst.frc.team2473.robot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.InternalButton;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Over database that stores a snapshot of the joysticks and sensor values in a thread safe way
 * This is constantly updated by multiple querying threads.
 * To use this class, you can access the values by using the getValue and getButton methods.
 * This class follows the <a href = "">singleton design pattern</a>
 * @author thatSteveFan, Will Fang
 *
 */
public class Database{

	public static final double LEFT_ENC_CONSTANT = 20.0/11240.0;
	public static final double RIGHT_ENC_CONSTANT = 20.0/11240.0;



	/**
	 * an enum that describes all the sensors that this database is tracking.<br>
	 * This includes the joysticks.<br>
	 * To add a new sensor, add it here as an enum element and update the appropriate calling method. ex Sensor Thread, OI
	 * 
	 * @author thatSteveFan
	 * 
	 */
	public enum Value {

		GYRO_POSITION, GYRO_VELOCITY, LEFT_ENCODER_POSITION, RIGHT_ENCODER_POSITION, LEFT_ENCODER_VELOCITY, BREAK_BEAM, SWITCH_ONE,  SWITCH_TWO,  SWITCH_THREE,  SWITCH_FOUR, WHEEL_TWIST, THROTTLE_VALUE, JOYSTICK_VALUE, CV_DISTANCE, CV_ANGLE_A, CV_BEARING, CV_L_OR_R, CV_TIME_STAMP, CV_PI_CONNECTED, CV_SUCCESS;// add
																																	// button
	}

	/**
	 * This is the enum that defines the different buttons that will be avaliable for use.
	 * To add a new button, add it as the enum element and update the appropriate thread, ex. OI
	 * 
	 * @author RehanDurrani
	 *
	 */
	public enum ButtonName {
		CLIMBER_SPEED_TOGGLE, START_CLIMBER, STOP_CLIMBER, CLIMBER_1_SEC;
	}

	/**
	 * the instance of this class.
	 * 
	 */
	static Database theInstance;
	static {
		theInstance = new Database();
	}

	public static Database getInstance() {
		return theInstance;
	}

	
	/**
	 * A map between the Value enum and their respective value holders
	 */
	private Map<Value, ThreadSafeHolder> map;
	/**
	 * A map between the ButtonName enum and their respective actual buttons
	 */
	private Map<ButtonName, ThreadSafeInternalButton> buttonMap;
	
	/**
	 * Constructor that makes the single instance
	 */
	private Database() {
		HashMap<Value, ThreadSafeHolder> tempMap = (new HashMap<>());//creates the map
		map = Collections.synchronizedMap(tempMap);//makes the map thread-safe
		//initializes the map with all the keys
		for (Value v : Value.values()) {
			map.put(v, new ThreadSafeHolder());
		}
		buttonMap = Collections.synchronizedMap(new HashMap<>());
		for(ButtonName b : ButtonName.values())
		{
			buttonMap.put(b, new ThreadSafeInternalButton());
		}
			
	}
	/**
	 * returns the value of the enum in a thread-safe manner
	 * @param v the value you are querying
	 * @return the value responding to the value enum
	 */
	public double getValue(Value v) {
		return map.get(v).getValue();
	}

	/**
	 * sets the double value of the value in a thread-safe manner
	 * @param v the value you are querying
	 * @param the value responding to the value enum
	 */
	public void setValue(Value v, double newValue) {


		map.get(v).setValue(newValue);
	}

	/**
	 * returns the internal button that is mapped to the ButtonName
	 * @param name the name of the button
	 * @return the button corresponding to the button enum
	 */
	public synchronized Button getButton(ButtonName name)
	{
		return buttonMap.get(name);
	}
	
	/**
	 * Sets whether the accessed button is pressed or not
	 * @param name the name of the button
	 * @param newValue the state of button, pressed or not
	 */
	public synchronized void setButtonValue(ButtonName name, boolean newValue)
	{
		buttonMap.get(name).setPressed(newValue);
	}
	

	/**
	 * Logs values directly to the dashboard
	 * Use this method to print sensor and joystick values
	 */
	public void log() {
		SmartDashboard.putNumber("Left Distance",
				Database.getInstance().getValue(Value.LEFT_ENCODER_POSITION));
		SmartDashboard.putNumber("Right Distance",
				Database.getInstance().getValue(Value.RIGHT_ENCODER_POSITION));
		SmartDashboard.putNumber("Left Velocity",
				Database.getInstance().getValue(Value.LEFT_ENCODER_VELOCITY));
		SmartDashboard.putNumber("Gyro Angle",
				Database.getInstance().getValue(Value.GYRO_POSITION));
		SmartDashboard.putNumber("Gyro Velocity",
				Database.getInstance().getValue(Value.GYRO_VELOCITY));
		SmartDashboard.putNumber("Wheel Twist",
				Database.getInstance().getValue(Value.WHEEL_TWIST));
		SmartDashboard.putNumber("Throttle Value",
				Database.getInstance().getValue(Value.THROTTLE_VALUE));
		SmartDashboard.putNumber("Break beam",
				Database.getInstance().getValue(Value.BREAK_BEAM));
		SmartDashboard.putNumber("Switch One",
				Database.getInstance().getValue(Value.SWITCH_ONE));
		SmartDashboard.putNumber("Switch Two",
				Database.getInstance().getValue(Value.SWITCH_TWO));
		SmartDashboard.putNumber("Switch Three",
				Database.getInstance().getValue(Value.SWITCH_THREE));
		SmartDashboard.putNumber("Switch Four",
				Database.getInstance().getValue(Value.SWITCH_FOUR));
		SmartDashboard.putNumber("PI Connection",
				Database.getInstance().getValue(Value.CV_PI_CONNECTED));

	}

}

/**
 * A class capable of storing a single double value in a thread-safe value. It uses the java <a href = "">ReentrantReadWriteLock</a>
 * to make a FIFO acess
 * @author thatSteveFan, Will Fang
 *
 */
class ThreadSafeHolder{

	private volatile double value;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

	public double getValue() {
		try {
			lock.readLock().lock();
			return value;
		} finally {
			lock.readLock().unlock();
		}

	}

	public void setValue(double newValue) {
		try {
			lock.writeLock().lock();
			value = newValue;
		} finally {
			lock.writeLock().unlock();
		}
	}
}

/**
 * essentially the InternalButton but synchronized.
 * @author thatSteveFan, Will Fang
 *
 */
class ThreadSafeInternalButton extends InternalButton
{

	@Override
	public synchronized void setInverted(boolean inverted) {
		// TODO Auto-generated method stub
		super.setInverted(inverted);
	}

	@Override
	public synchronized void setPressed(boolean pressed) {
		// TODO Auto-generated method stub
		super.setPressed(pressed);
	}

	@Override
	public synchronized boolean get() {
		// TODO Auto-generated method stub
		return super.get();
	}

	@Override
	public synchronized void whenPressed(Command command) {
		// TODO Auto-generated method stub
		super.whenPressed(command);
	}

	@Override
	public synchronized void whileHeld(Command command) {
		// TODO Auto-generated method stub
		super.whileHeld(command);
		
	}

	@Override
	public synchronized void whenReleased(Command command) {
		// TODO Auto-generated method stub
		super.whenReleased(command);
	}

	@Override
	public synchronized void toggleWhenPressed(Command command) {
		// TODO Auto-generated method stub
		super.toggleWhenPressed(command);
	}

	@Override
	public synchronized void cancelWhenPressed(Command command) {
		// TODO Auto-generated method stub
		super.cancelWhenPressed(command);
	}
	
}

