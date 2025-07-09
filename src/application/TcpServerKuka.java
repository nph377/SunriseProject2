package application;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import com.kuka.roboticsAPI.deviceModel.LBR;

public class TcpServerKuka extends RoboticsAPIApplication {

    private LBR robot;

    @Override
    public void initialize() {
        robot = getContext().getDeviceFromType(LBR.class);
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        BufferedReader in = null;

        int port = 4000;
        getLogger().info("Starting TCP Server on port " + port);

        try {
            serverSocket = new ServerSocket(port);
            getLogger().info("Waiting for client connection...");

            clientSocket = serverSocket.accept();
            getLogger().info("Client connected: " + clientSocket.getInetAddress());

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                getLogger().info("Received: " + inputLine);

                if ("exit".equalsIgnoreCase(inputLine.trim())) {
                    getLogger().info("Exit command received. Closing connection.");
                    break;
                }

                // You can add parsing/command execution here
            }

        } catch (IOException e) {
            getLogger().error("TCP Server error: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                getLogger().error("Error closing sockets: " + e.getMessage());
            }
        }
    }
}