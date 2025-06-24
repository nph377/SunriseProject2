package application;


import javax.inject.Inject;

import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.task.ITaskLogger;

/**
 * Implementation of a robot application.+
 * <p>
 * The application provides a {@link RoboticsAPITask#initialize()} and a 
 * {@link RoboticsAPITask#run()} method, which will be called successively in 
 * the application lifecycle. The application will terminate automatically after 
 * the {@link RoboticsAPITask#run()} method has finished or after stopping the 
 * task. The {@link RoboticsAPITask#dispose()} method will be called, even if an 
 * exception is thrown during initialization or run. 
 * <p>
 * <b>It is imperative to call <code>super.dispose()</code> when overriding the 
 * {@link RoboticsAPITask#dispose()} method.</b> 
 * 
 * @see UseRoboticsAPIContext
 * @see #initialize()
 * @see #run()
 * @see #dispose()
 */
public class Compliant_ctrl_test extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	@Inject
	private ITaskLogger logger;
	@Inject
	private Tool tool;

	private CartesianImpedanceControlMode ctrl_mode;

	@Override
	public void initialize() {
		logger.info("init");
		ctrl_mode = new  CartesianImpedanceControlMode();
		ctrl_mode.parametrize(CartDOF.Z).setStiffness(300.0);
		tool.attachTo(robot.getFlange());
	}

	@Override
	public void run() {
		logger.info("run");
		ObjectFrame tf = tool.getDefaultMotionFrame();
		logger.info(tf.toString());
		
		Frame f0 = robot.getCurrentCartesianPosition(tf);
		logger.info("initial position: " + f0.toString());
		
//		logger.info("moving in z direction");
//		double dz = 50.0;
//		robot.move(linRel(0,0,dz,0,0,0).setCartVelocity(80).setMode(ctrl_mode));
//		logger.info("moving back");
//		robot.move(linRel(0,0,-dz,0,0,0).setCartVelocity(80).setMode(ctrl_mode));
		
		logger.info("moving in y direction");
		double dy = 100.0;
		robot.move(linRel(0,dy,0,0,0,0).setCartVelocity(80).setMode(ctrl_mode));
		logger.info("moving back");
		robot.move(linRel(0,-dy,0,0,0,0).setCartVelocity(80).setMode(ctrl_mode));
	}
} 