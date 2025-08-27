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
	private LBR robot;
	
	private boolean program_running = true;
	ServerSocket serverSocket = null;
	Socket clientSocket = null;
	BufferedReader reader = null;
	PrintWriter writer = null;
	int port = 30007;
	
	private String prompt;
	private int response;

	private Frame f;
	private double x;
	private double y;
	private double z;
	private double a;
	private double b;
	private double c;
	private Frame f0;
	private double x0;
	private double y0;
	private double z0;
	private double a0;
	private double b0;
	private double c0;

	////////////////////////////////////////////////////////////////////////////
	/////////////////////////    CUSTOM METHODS    /////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	
	public void hello(){
		getLogger().info("hello from kuka");
		writer.println("hello from kuka");
	}

	public void confirm_starting_coordinates() {
		prompt = "confirm the starting location.\n" +
			"is the tool above the maximum height of sample? \n*** THIS IS VERY IMPORTANT TO AVOID COLLISIONS ***\n";
	    response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "Yes", "No");
	    if (response == 0) {
			getLogger().info("TCP starting location confirmed");
			f0 = robot.getCurrentCartesianPosition(robot.getFlange());
			x0 = f0.getX();
			y0 = f0.getY();
			z0 = f0.getZ();
			a0 = f0.getAlphaRad();
			b0 = f0.getBetaRad();
			c0 = f0.getGammaRad();
			getLogger().info(
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
			getLogger().info("TERMINATING PROGRAM EARLY");
	        program_running = false;
		}
	}
	
	// relative to program starting position
	// TODO: use lin instead of linrel
	public void move(double x, double y, double z) {
		String log = "move: " +
		String.valueOf(x) + ", " + 
		String.valueOf(y) + ", " +
		String.valueOf(z);
		getLogger().info(log);
		
		// on the first iteration, the robot moves to original position ...
		// at program start if this line isn't here - I have no clue why
		// robot.moveAsync(linRel(0,0,0,0,0,0).setReferenceFrame(robot.getRootFrame()).setJointVelocityRel(.2));

		Frame f1 = robot.getCurrentCartesianPosition(robot.getFlange());
		double x1 = f1.getX() - x0;
		double y1 = f1.getY() - y0;
		double z1 = f1.getZ() - z0;
		double dx = x - x1;
		double dy = y - y1;
		double dz = z - z1;
		robot.moveAsync(linRel(dx,dy,dz,0,0,0).setReferenceFrame(robot.getRootFrame()).setJointVelocityRel(.2));
	}
	
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