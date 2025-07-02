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
	private LBR lBR_iiwa_14_R820_1;
	@Inject
	private ITaskLogger logger;
	
	private double x_span = 10; //mm
	private double y_span = 10; //mm

	@Override
	public void initialize() {
		logger.info("xspan = " + String.valueOf(x_span) + ", " + "yspan = " + String.valueOf(y_span));
		// ask user to confirm xspan and yspan
		getLogger().info("Are xspan and yspan correct?");
        int isCancel = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, "text", "Yes", "No");
        if (isCancel == 1) {
			logger.info("terminating program early");
            return;
        }
		else {
			logger.info("xspan and yspan confirmed");
		}

		// ask user if ready to make TCP vertical
		// make TCP vertical

		// ask user to move TCP to top left of sample and above max height of sample
		// ask user to confirm ready to begin
		// record z ceiling

	}

	@Override
	public void run() {
		// loop through points
			// move to x,y,z_ceil
			// move down until touch surface
			// (optional) record z data for future use (need to figure out a way to find same starting point for future runs)
			// move back up to optimal distance for VCA
			// wait for VCA to be done at this point
		logger.info("done");
	}
	
}