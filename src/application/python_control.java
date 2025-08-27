package application;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.linRel;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.inject.Inject;

import com.kuka.roboticsAPI.geometricModel.Frame;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.motionModel.CartesianPTP;
import com.kuka.roboticsAPI.motionModel.LIN;
import com.kuka.roboticsAPI.motionModel.PTP;
import com.kuka.roboticsAPI.uiModel.ApplicationDialogType;
import com.kuka.roboticsAPI.motionModel.BasicMotions;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;


public class python_control extends RoboticsAPIApplication {
	@Inject
	LBR robot;
	
	boolean program_running = true;
	ServerSocket serverSocket = null;
	Socket clientSocket = null;
	BufferedReader reader = null;
	PrintWriter writer = null;
	
	int port = 30007;
	
	////////////////////////////////////////////////////////////////////////////
	/////////////////////////    CUSTOM METHODS    /////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	
	void hello(){
		getLogger().info("hello from kuka");
		writer.println("hello from kuka");
	}

	public void send_coordinates(boolean print_log) {
		getLogger().info("TCP starting location confirmed");
		Frame f = robot.getCurrentCartesianPosition(robot.getFlange());
		double x = f.getX();
		double y = f.getY();
		double z = f.getZ();
		double a = f.getAlphaRad();
		double b = f.getBetaRad();
		double c = f.getGammaRad();
		writer.println(
			String.valueOf(x) + " " +
			String.valueOf(y) + " " +
			String.valueOf(z) + " " +
			String.valueOf(a * 180/Math.PI) + " " +
			String.valueOf(b * 180/Math.PI) + " " +
			String.valueOf(c * 180/Math.PI) + "\n"
		);
		if(print_log) {
			getLogger().info(
					"start position:\n" + 
					"x = " + String.valueOf(x) + " mm\n" +
					"y = " + String.valueOf(y) + " mm\n" +
					"z = " + String.valueOf(z) + " mm\n" +
					"a = " + String.valueOf(a * 180/Math.PI) + " deg\n" +
					"b = " + String.valueOf(b * 180/Math.PI) + " deg\n" +
					"c = " + String.valueOf(c * 180/Math.PI) + " deg\n"
			);
		}
	}
	
	// relative to program starting position
	// TODO: use lin instead of linrel
//	public void move(double x, double y, double z) {
//		String log = "move: " +
//		String.valueOf(x) + ", " + 
//		String.valueOf(y) + ", " +
//		String.valueOf(z);
//		getLogger().info(log);
//		
//		// on the first iteration, the robot moves to original position ...
//		// at program start if this line isn't here - I have no clue why
//		// robot.moveAsync(linRel(0,0,0,0,0,0).setReferenceFrame(robot.getRootFrame()).setJointVelocityRel(.2));
//
//		Frame f1 = robot.getCurrentCartesianPosition(robot.getFlange());
//		double x1 = f1.getX() - x0;
//		double y1 = f1.getY() - y0;
//		double z1 = f1.getZ() - z0;
//		double dx = x - x1;
//		double dy = y - y1;
//		double dz = z - z1;
////		robot.moveAsync(linRel(dx,dy,dz,0,0,0).setReferenceFrame(robot.getRootFrame()).setJointVelocityRel(.2));
//	}
	
	public void init_tcp() {
		try {
		    getLogger().info("Starting TCP server...");
			serverSocket = new ServerSocket(port);
			clientSocket = serverSocket.accept();
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			writer = new PrintWriter(clientSocket.getOutputStream(), true);
			getLogger().info("TCP server online");
		}
		catch (Exception e) {
	        getLogger().error("TCP error: " + e.getMessage());
	        program_running = false;
	        try {
	            if (reader != null) reader.close();
	            if (clientSocket != null) clientSocket.close();
	            if (serverSocket != null) serverSocket.close();
	        }
	        catch (Exception e2) {
	            getLogger().error("Close error: " + e2.getMessage());
	        }
	    }
	}
	
	////////////////////////////////////////////////////////////////////////////
	/////////////////////////    INIT/MAIN/EXIT METHODS    /////////////////////
	////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void initialize() {
		init_tcp();
	}
	
	@Override
	public void run() {
        String line;
		while (program_running) {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				program_running = false;
				break;
			}
		    String[] command = line.trim().split(" ");
		    
		    if (command[0].equalsIgnoreCase("exit"))
		    {
		        getLogger().info("'exit' received. Stopping server.");
		        break;
		    }
		    else if (command[0].equalsIgnoreCase("send_coordinates"))
			{
		    	if (command.length > 1 && command[1].equalsIgnoreCase("print_log")) {
		    		send_coordinates(true);
		    	}
		    	else {
		    		send_coordinates(false);
		    	}
			}
			else if (command[0].equals("move"))
			{
				String[] values = line.split(" ");
				if (values.length==4) {
					double x = Double.parseDouble(values[1]);
					double y = Double.parseDouble(values[2]);
					double z = Double.parseDouble(values[3]);
//						move(x, y, z);
				}
				else {
					getLogger().info("INVALID COMMAND: move takes 3 parameters");
				}
			}
			else if (command[0].equalsIgnoreCase("hello"))
			{
				hello();
			}
			else
			{
				getLogger().info("Unknown command from python: " + line);
		    }
		}
		
		dispose();
	}
	
	@Override
    public void dispose() {
        program_running = false; // signal to stop the loop
        try {
            if (reader != null) reader.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        }
        catch (Exception e2) {
            getLogger().error("Close error: " + e2.getMessage());
        }
        super.dispose();
    }

}