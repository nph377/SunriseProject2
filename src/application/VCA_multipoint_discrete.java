/*
Moving from point to point, you can get the surface topography by first retracting 
the VCA to almost its bottom (leave maybe 10% range) and move the robot arm vertically 
towards the sample. Once you see the encoder value change due to contact, recording the 
end effector XYZ position will give you the desired location to trace the surface 
topography as discrete points.
*/ 

package application;

import javax.inject.Inject;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.roboticsAPI.uiModel.ApplicationDialogType;
import com.kuka.task.ITaskLogger;

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

public class VCA_multipoint_discrete extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	@Inject
	private ITaskLogger logger;
	@Inject
	private Tool tool;
	
	// SET THESE
	private double xspan = 0; //mm
	private double yspan = 0; //mm
	private double x_increment = 1; //mm
	private double y_increment = 1; //mm

	private int response;
	private String prompt;

	private double x;
	private double y;
	private double z;
	private double a;
	private double b;
	private double c;

	private double x0;
	private double y0;
	private double z0;

	@Override
	public void initialize() {
		logger.info("===================================");
		// ctrl_mode = new  CartesianImpedanceControlMode();
		// ctrl_mode.parametrize(CartDOF.Z).setStiffness(70.0);
		tool.attachTo(robot.getFlange());
	}

	@Override
	public void run() {
		// ask user to confirm xspan and yspan
		logger.info("asking user to confirm xspan and yspan");
		prompt = "Are xspan and yspan correct?\n" +
			"xspan = " + String.valueOf(xspan) + "mm\n" + 
			"yspan = " + String.valueOf(yspan) + "mm"
		;
        response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "Yes", "Exit");
        if (response == 0) {
			logger.info("xspan and yspan confirmed");
        }
		else {
			logger.info("TERMINATING PROGRAM EARLY");
            return;
		}

		// ask user about TCP orientation
		Frame f = robot.getCurrentCartesianPosition(robot.getFlange());
		x = f.getX();
		y = f.getY();
		z = f.getZ();
		a = f.getAlphaRad();
		b = f.getBetaRad();
		c = f.getGammaRad();
        prompt = "Would you like to orient the TCP vertical?\n" + 
			"current position:\n" + 
			"x = " + String.valueOf(x) + " mm\n" +
			"y = " + String.valueOf(y) + " mm\n" +
			"z = " + String.valueOf(z) + " mm\n" +
			"a = " + String.valueOf(a * 180/Math.PI) + " deg\n" +
			"b = " + String.valueOf(b * 180/Math.PI) + " deg\n" +
			"c = " + String.valueOf(c * 180/Math.PI) + " deg\n"
		;
        logger.info(prompt);
    	response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "No", "Yes", "Exit");
		if (response == 0) {
			logger.info("TCP orientation confirmed");
		}
		else if (response == 1) {
			logger.info("orienting TCP");
			double db = b;
			double dc;
			if (c > 0)
				dc = -c + Math.PI;
			else
				dc = -c - Math.PI;
			robot.move(linRel(0,0,0,0,db,dc).setJointVelocityRel(.2));
			logger.info("TCP orientation complete");
		}
		else {
			logger.info("TERMINATING PROGRAM EARLY");
			return;
		}

		// ask user to move TCP to top left of sample and above max height of sample
        prompt = "Move the TCP to the corner of sample - minimum world x,y (check the sticky note)\n" +
			"and above maximum height of sample (THIS IS VERY IMPORTANT TO AVOID COLLISIONS)";
        response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "Done", "Exit");
        if (response == 0) {
			logger.info("TCP starting location confirmed");
			Frame f0 = robot.getCurrentCartesianPosition(robot.getFlange());
			x0 = f.getX();
			y0 = f.getY();
			z0 = f.getZ();
        }
		else {
			logger.info("TERMINATING PROGRAM EARLY");
            return;
		}

		// ask user to confirm ready to begin
        logger.info("asking user to confirm ready to begin");
        prompt = "Ready to begin?";
        response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "Yes", "Exit");
        if (response == 0) {
			logger.info("beginning sweep");
        }
		else {
			logger.info("TERMINATING PROGRAM EARLY");
            return;
		}
        
		// TODO loop through points
		// mark
		for (x = x0; x<=x0+xspan; x += x_increment) {
			logger.info(String.valueOf(x));
		}
			// move to z_ceil
			// move to next point x,y
			// move down until touch surface
			// (optional) record z data for future use (need to figure out a way to find same starting point for future runs)
			// move back up to optimal distance for VCA
			// wait for VCA to be done at this point

		logger.info("END");
	}
	
}