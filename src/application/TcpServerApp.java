package application;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;

public class TcpServerApp extends RoboticsAPIApplication {

    private LBR robot;

    public void initialize() {
        robot = getContext().getDeviceFromType(LBR.class);
    }

    public void run() {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        BufferedReader reader = null;
        int port = 4000;

        try {
            getLogger().info("Starting TCP server on port " + port);
            serverSocket = new ServerSocket(port);

            getLogger().info("Waiting for client connection...");
            clientSocket = serverSocket.accept();

            getLogger().info("Client connected from: " + clientSocket.getInetAddress());
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                getLogger().info("Received: " + line);

                // Example command handling
                if ("start".equalsIgnoreCase(line)) {
                    getLogger().info("Start command received.");
                    // Add motion code or task start logic here
                } else if ("stop".equalsIgnoreCase(line)) {
                    getLogger().info("Stop command received.");
                    break;
                } else {
                    getLogger().info("Unknown command.");
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
                getLogger().error("Error closing sockets: " + e.getMessage());
            }
        }

        getLogger().info("TCP server shut down.");
    }
}