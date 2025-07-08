package application;

import javax.inject.Inject;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.task.ITaskLogger;
//import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
//import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import java.lang.Thread;

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

public class encoder_test extends RoboticsAPIApplication {
//	@Inject
//	private LBR lBR_iiwa_14_R820;
	@Inject
	private Controller controller;
	@Inject
	private MediaFlangeIOGroup flange = new MediaFlangeIOGroup(controller);
	@Inject
	private ITaskLogger logger;

	private boolean p4 = false;
	private boolean p10 = false;
	private boolean n4;
	private boolean n10;
	
	@Override
	public void initialize() {
		// initialize your application here
		for (int i=0; i<3; i++) {
			flange.setLEDBlue(true);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			flange.setLEDBlue(false);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		// your application execution starts here
		flange.setLEDBlue(false);
		
		while(true) {
			n4 = flange.getInputX3Pin4();
			n10 = flange.getInputX3Pin10();
			if(n4 != p4 || n10 != p10) {
				flange.setLEDBlue(true);
				p4 = n4;
				p10 = n10;
				logger.info(String.valueOf(p4) + " " + String.valueOf(p10));
			}
			else {
				flange.setLEDBlue(false);
			}
		}
	}
}