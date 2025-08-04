package application;


import javax.inject.Inject;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.Frame;

/**
 * Implementation of a robot application.
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
public class orient_TCP extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	private Frame f;
	private double b;
	private double c;
	private double db;
	private double dc;

	@Override
	public void initialize() {
	}

	@Override
	public void run() {
		f = robot.getCurrentCartesianPosition(robot.getFlange());
		b = f.getBetaRad();
		c = f.getGammaRad();
		getLogger().info("orienting TCP");
		double db = b;
		double dc;
		if (c > 0)
			dc = -c + Math.PI;
		else
			dc = -c - Math.PI;
		robot.move(linRel(0,0,0,0,db,dc).setJointVelocityRel(.2));
		getLogger().info("TCP orientation complete");
	}
}
