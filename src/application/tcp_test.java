package application;

import javax.inject.Inject;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.task.ITaskLogger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
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

public class tcp_test extends RoboticsAPIApplication {
	@Inject
	private ITaskLogger logger;

	String serverIP = "172.31.1.147";
	int serverPort = 3363; // todo
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
			logger.info("successful socket connection");
		} catch (Exception e) {
			logger.info("socket error");
			logger.info(e.toString());
		} finally {
			logger.info("init done");
		}
	}

	@Override
	public void run() {
		logger.info("run");
		logger.info("run done");
	}
}