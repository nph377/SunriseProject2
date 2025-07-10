package application;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;

public class TcpClientApp extends RoboticsAPIApplication {

    private LBR robot;

    public void initialize() {
        robot = getContext().getDeviceFromType(LBR.class);
    }

    public void run() {
        String serverIP = "172.31.1.110"; // Replace with your PC/server IP
        int port = 30000;

        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            getLogger().info("Connecting to server " + serverIP + ":" + port);
            socket = new Socket(serverIP, port);
            getLogger().info("Connected to server.");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Optional: send hello message
            out.println("Hello from KUKA");

            String line;
            while ((line = in.readLine()) != null) {
                getLogger().info("Received: " + line);

                if ("start".equalsIgnoreCase(line)) {
                    getLogger().info("Received start command.");
                    // Add robot motion logic here
                } else if ("stop".equalsIgnoreCase(line)) {
                    getLogger().info("Received stop command.");
                    break;
                } else {
                    getLogger().info("Unknown command.");
                }
            }

        } catch (Exception e) {
            getLogger().error("TCP client error: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                getLogger().error("Error closing resources: " + e.getMessage());
            }
        }

        getLogger().info("TCP client shut down.");
    }
}