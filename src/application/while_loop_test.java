package application;


import javax.inject.Inject;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
import com.kuka.roboticsAPI.deviceModel.LBR;

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
public class while_loop_test extends RoboticsAPIApplication {
	@Inject
	private boolean stopRequested = false;

	@Override
	public void initialize() {
		// initialize your application here
		getLogger().info("before loop");
	}

	@Override
	public void run() {
		// your application execution starts here
		while(!stopRequested) {
			
		}
		getLogger().info("out of loop");

	}
	
	@Override
    public void dispose() {
        stopRequested = true; // signal to stop the loop
        super.dispose();
    }
}