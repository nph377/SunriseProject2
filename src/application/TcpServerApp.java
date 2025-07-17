package application;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.ptp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.inject.Inject;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.motionModel.PTP;

public class TcpServerApp extends RoboticsAPIApplication {
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
	
	@Inject
	private LBR lbr;
	@Override
	public void run() {
	    ServerSocket serverSocket = null;
	    Socket clientSocket = null;
	    BufferedReader reader = null;
	    int port = 30000;

	    try {
	        getLogger().info("Starting TCP server...");
	        serverSocket = new ServerSocket(port);
	        clientSocket = serverSocket.accept();
	        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

	        String line;
	        while ((line = reader.readLine()) != null) {
	            getLogger().info("Received: " + line);
	            if (line.trim().equalsIgnoreCase("exit")) {
	                getLogger().info("'exit' command received. Stopping server.");
	                break;
	            }
	            if (isNumeric(line)) {
	                int value = Integer.parseInt(line);
	                getLogger().info("User input is a number: " + value);
	                //add logic here
	                /*if (value < 99 && value > 0) {
	                    PTP ptpToTransportPosition = ptp(value, Math.toRadians(25), 0, Math.toRadians(90), 0, 0, 0);
	                    ptpToTransportPosition.setJointVelocityRel(0.25);
	                    lbr.move(ptpToTransportPosition);
	                }*/
	            } else {
	            	if (line.trim().equalsIgnoreCase("hi")){
	            		hello();
	            	}
	            	else {
						getLogger().info("User input is a string: " + line);
	            	}
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
}