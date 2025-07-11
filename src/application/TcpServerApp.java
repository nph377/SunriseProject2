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
	@Inject
	private LBR lbr;
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
            while ((line = reader.readLine()) != null && (line !="exit") ) {
                getLogger().info("Received: " + line);
                // Add robot logic here
                int value=Integer.parseInt(line);
                PTP ptpToTransportPosition = ptp(value, Math.toRadians(25), 0, Math.toRadians(90), 0, 0, 0);
                lbr.move(ptpToTransportPosition);
            }
            while ((line = reader.readLine()) != null ) {
                getLogger().info("Received: " + line);
                // Add robot logic here
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