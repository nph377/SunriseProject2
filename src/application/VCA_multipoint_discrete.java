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
	
	private CartesianImpedanceControlMode ctrl_mode;
	private double x_span = 10; //mm
	private double y_span = 10; //mm
	private int response;
	private String prompt;

	@Override
	public void initialize() {
		logger.info("===================================");
		ctrl_mode = new  CartesianImpedanceControlMode();
		// TODO - update this
		ctrl_mode.parametrize(CartDOF.Z).setStiffness(70.0);
		tool.attachTo(robot.getFlange());
	}

	@Override
	public void run() {
		// ask user to confirm xspan and yspan
		logger.info("asking user to confirm xspan and yspan");
		prompt = "Are xspan and yspan correct?\n" +
			"xspan = " + String.valueOf(x_span) + "mm\n" + 
			"yspan = " + String.valueOf(y_span) + "mm"
		;
        response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "Yes", "Exit");
        if (response == 0) {
			logger.info("xspan and yspan confirmed");
        }
		else {
			logger.info("TERMINATING PROGRAM EARLY");
            return;
		}

		// startmark
		// TODO ask user about TCP orientation
		Frame f = robot.getCurrentCartesianPosition(robot.getFlange());
		double x = f.getX();
		double y = f.getY();
		double z = f.getZ();
		double a = f.getAlphaRad()*180/Math.PI;
		double b = f.getBetaRad()*180/Math.PI;
		double c = f.getGammaRad()*180/Math.PI;
        prompt = "Would you like to orient the TCP vertical?\n" + 
			"current position:\n" + 
			"x = " + String.valueOf(x) + "\n" +
			"y = " + String.valueOf(y) + "\n" +
			"z = " + String.valueOf(z) + "\n" +
			"a = " + String.valueOf(a) + "\n" +
			"b = " + String.valueOf(b) + "\n" +
			"c = " + String.valueOf(c)
		;
        logger.info(prompt);
    	response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "No", "Yes", "Exit");
		if (response == 0) {
			logger.info("TCP orientation confirmed");
		}
		else if (response == 1) {
			logger.info("orienting TCP");
			// mark
			double db = -b * Math.PI/180;
			double dc = 0;
			robot.move(linRel(0,0,0,0,db,dc).setJointVelocityRel(.2));
			logger.info("TCP orientation complete");
		}
		else {
			logger.info("TERMINATING PROGRAM EARLY");
			return;
		}
		// endmark

		// ask user to move TCP to top left of sample and above max height of sample
        prompt = "Move the TCP to the top left of sample (minimum world x and y)\nand above maximum height of sample";
        response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "Done", "Exit");
        if (response == 0) {
			logger.info("TCP starting location confirmed");
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
		// TODO record z ceiling
        
		// TODO loop through points
			// move to x,y,z_ceil
			// move down until touch surface
			// (optional) record z data for future use (need to figure out a way to find same starting point for future runs)
			// move back up to optimal distance for VCA
			// wait for VCA to be done at this point

		logger.info("END");
	}
	
}