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
	private double xspan = 5; //mm
	private double yspan = 5; //mm
	private double x_increment = 5; //mm
	private double y_increment = 5; //mm

	// runtime data
	private int response;
	private String prompt;

	private Frame f;
	private double x;
	private double y;
	private double z;
	private double a;
	private double b;
	private double c;

	private double dx;
	private double dy;
	private double dz;
	private double da;
	private double db;
	private double dc;

	private Frame f0;
	private double x0;
	private double y0;
	private double z0;
	private double a0;
	private double b0;
	private double c0;

	@Override
	public void initialize() {
		logger.info("===================================");
		logger.info("NEW RUN");
		// ctrl_mode = new  CartesianImpedanceControlMode();
		// ctrl_mode.parametrize(CartDOF.Z).setStiffness(70.0);
		tool.attachTo(robot.getFlange());
	}

	@Override
	public void run() {
		// ask user to confirm xspan and yspan
		prompt = "Are xspan and yspan correct?\n" +
			"xspan = " + String.valueOf(xspan) + "mm\n" + 
			"yspan = " + String.valueOf(yspan) + "mm"
		;
		logger.info(prompt);
        response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "Yes", "Exit");
        if (response == 0) {
			logger.info("xspan and yspan confirmed");
        }
		else {
			logger.info("TERMINATING PROGRAM EARLY");
            return;
		}

		// ask user about TCP orientation
		f = robot.getCurrentCartesianPosition(robot.getFlange());
		x = f.getX();
		y = f.getY();
		z = f.getZ();
		a = f.getAlphaRad();
		b = f.getBetaRad();
		c = f.getGammaRad();
        prompt = "Would you like to orient the TCP vertical?";
        logger.info(prompt);
    	response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "No", "Yes", "Exit");
		if (response == 0) {
			logger.info("TCP orientation confirmed");
		}
		else if (response == 1) {
			logger.info("orienting TCP");
			db = b;
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
			"and above maximum height of sample \n*** THIS IS VERY IMPORTANT TO AVOID COLLISIONS ***";
        response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "Done", "Exit");
        if (response == 0) {
			logger.info("TCP starting location confirmed");
			f0 = robot.getCurrentCartesianPosition(robot.getFlange());
			x0 = f.getX();
			y0 = f.getY();
			z0 = f.getZ();
			a0 = f.getAlphaRad();
			b0 = f.getBetaRad();
			c0 = f.getGammaRad();
			logger.info(
				"start position:\n" + 
				"x0 = " + String.valueOf(x0) + " mm\n" +
				"y0 = " + String.valueOf(y0) + " mm\n" +
				"z0 = " + String.valueOf(z0) + " mm\n" +
				"a0 = " + String.valueOf(a0 * 180/Math.PI) + " deg\n" +
				"b0 = " + String.valueOf(b0 * 180/Math.PI) + " deg\n" +
				"c0 = " + String.valueOf(c0 * 180/Math.PI) + " deg\n"
			);
        }
		else {
			logger.info("TERMINATING PROGRAM EARLY");
            return;
		}

		// ask user to confirm ready to begin
        prompt = "Ready to begin?";
        logger.info(prompt);
        response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "Yes", "Exit");
        if (response == 0) {
			logger.info("beginning sweep");
        }
		else {
			logger.info("TERMINATING PROGRAM EARLY");
            return;
		}
        
		// loop through points
		boolean y_up = true;
		double xn, yn;
		for (xn = x0; xn<=x0+xspan; xn += x_increment) {
			if (y_up) {
				for (yn = y0; yn<=y0+yspan; yn += y_increment) {
					test_point(xn, yn);
				}
			}
			else {
				for (yn = y0+yspan; yn>=y0; yn -= y_increment) {
					test_point(xn, yn);
				}
			}
			y_up = !y_up;
		}

		logger.info("moving back to start position");
		f = robot.getCurrentCartesianPosition(robot.getFlange());
		x = f.getX();
		y = f.getY();
		z = f.getZ();
		a = f.getAlphaRad();
		b = f.getBetaRad();
		c = f.getGammaRad();
		dx = x0 - x;
		dy = y0 - y;
		dz = z0 - z;
		da = a0 - a;
		db = b0 - b;
		dc = c0 - c;
		//mark
		logger.info(
			"dx = " + String.valueOf(dx) + "\n" + 
			"dy = " + String.valueOf(dy) + "\n" + 
			"dz = " + String.valueOf(dz) + "\n"
		);
		robot.move(linRel(dx,dy,dz,da,db,dc).setReferenceFrame(robot.getRootFrame()).setJointVelocityRel(.2));

		logger.info("END");
	}

	public void test_point(double xn, double yn){
		logger.info(
			"--------------\n" + 
			"new point \n"
		);

		// move to next point x,y
		logger.info("moving to point");
		f = robot.getCurrentCartesianPosition(robot.getFlange());
		x = f.getX();
		y = f.getY();
		dx = xn - x;
		dy = yn - y;
		// logger.info(
		// 	"before moving: \n" +
		// 	"     actual x = " + String.valueOf(x) + "\n" +
		// 	"    desired x = " + String.valueOf(xn) + "\n" +
		// 	"calculated dx = " + String.valueOf(dx) + "\n" +
		// 	"     actual y = " + String.valueOf(y) + "\n" +
		// 	"    desired y = " + String.valueOf(yn) + "\n" + 
		// 	"calculated dy = " + String.valueOf(dy)
		// );
		robot.move(linRel(dx,dy,0,0,0,0).setReferenceFrame(robot.getRootFrame()).setJointVelocityRel(.2));

		// // check movement
		// f = robot.getCurrentCartesianPosition(robot.getFlange());
		// x = f.getX();
		// y = f.getY();
		// double ex = xn - x;
		// double ey = yn - y;
		// logger.info(
		// 	"after moving: \n" + 
		// 	" actual x = " + String.valueOf(x) + "\n" + 
		// 	"desired x = " + String.valueOf(xn) + "\n" + 
		// 	"  x error = " + String.valueOf(ex) + "\n" + 
		// 	" actual y = " + String.valueOf(y) + "\n" + 
		// 	"desired y = " + String.valueOf(yn) + "\n" + 
		// 	"  y error = " + String.valueOf(ey)
		// );

		// mark
		// TODO move down until touch surface
		logger.info("moving down to touch surface");
		dz = -5;
		robot.move(linRel(0,0,dz,0,0,0).setReferenceFrame(robot.getRootFrame()).setJointVelocityRel(.2));

		// TODO (optional) record z data for future use (need to figure out a way to find same starting point for future runs)

		// TODO move back up to optimal distance for VCA

		// TODO wait for VCA to be done at this point

		// move to z0
		logger.info("moving back to z0");
		f = robot.getCurrentCartesianPosition(robot.getFlange());
		z = f.getZ();
		dz = z0 - z;
		robot.move(linRel(0,0,dz,0,0,0).setReferenceFrame(robot.getRootFrame()).setJointVelocityRel(.2));

		logger.info("done with point");
	}
	
}