package application;

import javax.inject.Inject;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.controllerModel.Controller;
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

public class X3_pin_identification_and_testing extends RoboticsAPIApplication {
//	@Inject
//	private LBR lBR_iiwa_14_R820;
	@Inject
	private Controller controller;
	@Inject
	private MediaFlangeIOGroup flange = new MediaFlangeIOGroup(controller);
	
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
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		// your application execution starts here
		flange.setOutputX3Pin1(false);
		flange.setOutputX3Pin2(false);
		flange.setOutputX3Pin11(false);
		flange.setOutputX3Pin12(false);
		flange.setLEDBlue(false);
		
		while(true) {
			if(flange.getInputX3Pin16()) {
				flange.setLEDBlue(true);
			}
			else {
				flange.setLEDBlue(false);
			}
		}
	}
}