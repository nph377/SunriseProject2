package application;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.linRel;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import java.io.BufferedReader;
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


public class TCP_vca_sweep extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	
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


	public boolean isNumeric(String input) {
	    if (input == null || input.trim().isEmpty()) {
	        return false;
	    }
	    try {
	        Integer.parseInt(input.trim());
	        return true; 
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
	
	public void hello(){
		getLogger().info("heloooooo");
	}

	@Override
	public void run() {
		//////////////    ORIENT TCP VERTICAL
		prompt = "Would you like to orient the TCP vertical?";
    	response = getApplicationUI().displayModalDialog(ApplicationDialogType.QUESTION, prompt, "No", "Yes", "Exit");
		if (response == 0) {
			getLogger().info("TCP orientation confirmed");
		}
		else if (response == 1) {
			f = robot.getCurrentCartesianPosition(robot.getFlange());
			b = f.getBetaRad();
			c = f.getGammaRad();
			getLogger().info("orienting TCP");
			double db = b;
			double dc;
			if (c > 0)
				dc = -c + Math.PI;
			else
				dc = -c - Math.PI;
			robot.move(linRel(0,0,0,0,db,dc).setJointVelocityRel(.2));
			getLogger().info("TCP orientation complete");
		}
		else {
			getLogger().info("TERMINATING PROGRAM EARLY");
			return;
		}
		
		//////////////    CONFIRM STARTING LOCATION
		prompt = "Is TCP at the corner of sample - minimum world x,y (check the sticky note)\n" +
			"and above maximum height of sample? \n*** THIS IS VERY IMPORTANT TO AVOID COLLISIONS ***\n" + 
			"this must be done before starting the program because of a bug I cannot figure out";
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
            return;
		}
        
		//////////////    BEGIN TCP COMMS AND SWEEP
	    ServerSocket serverSocket = null;
	    Socket clientSocket = null;
	    BufferedReader reader = null;
	    int port = 30004;
	    try {
	        getLogger().info("Starting TCP server...");
	        serverSocket = new ServerSocket(port);
	        clientSocket = serverSocket.accept();
	        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//	        writer = new PrintWriter(clientSocket.getOutputStream(), true);

	        getLogger().info("TCP server online");

	        String line;
	        while ((line = reader.readLine()) != null) {
	            getLogger().info("Received: " + line);
	            if (line.trim().equalsIgnoreCase("exit")) {
	                getLogger().info("'exit' received. Stopping server.");
	                break;
	            }
	            else if (isNumeric(line)) {
	                int value = Integer.parseInt(line);
	                getLogger().info("User input is a number: " + value);
	                //add logic here
	                /*if (value < 99 && value > 0) {
	                    PTP ptpToTransportPosition = ptp(value, Math.toRadians(25), 0, Math.toRadians(90), 0, 0, 0);
	                    ptpToTransportPosition.setJointVelocityRel(0.25);
	                    lbr.move(ptpToTransportPosition);
	                }*/
				} 
				else if (line.trim().split(" ")[0].equals("move")) {
					String[] values = line.split(" ");
					if (values.length==4) {
						double x = Double.parseDouble(values[1]);
						double y = Double.parseDouble(values[2]);
						double z = Double.parseDouble(values[3]);
						move(x, y, z);
					}
					else {
						getLogger().info("INVALID COMMAND: move takes 3 parameters");
					}
				}
				else if (line.trim().equalsIgnoreCase("hi")){
					hello();
				}
				else {
					getLogger().info("User input is a string: " +line);
	            }
	        }
	    } catch (Exception e) {
	        getLogger().error("TCP error: " + e.getMessage());
	    } finally {
	        try {
	            if (reader != null) reader.close();
	            if (clientSocket != null) clientSocket.close();
	            if (serverSocket != null) serverSocket.close();
	        } catch (Exception e) {
	            getLogger().error("Close error: " + e.getMessage());
	        }
	    }
	}

	// relative to program starting position
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

}