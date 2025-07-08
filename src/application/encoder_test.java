package application;

import javax.inject.Inject;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.task.ITaskLogger;
//import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;
//import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.Thread;
import java.net.Socket;

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

	String serverIP = "todo";
	int serverPort = 0; // todo
	Socket socket;
	PrintStream out;
	BufferedReader in;
	
	@Override
	public void initialize() {
		logger.info("init");
		try {
			socket = new Socket(serverIP, serverPort);
			out = new PrintStream(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			logger.info("error");
			logger.info(e.toString());
		} finally {
			logger.info("done with socket thing");
		}
	}

	@Override
	public void run() {
		
	}
}