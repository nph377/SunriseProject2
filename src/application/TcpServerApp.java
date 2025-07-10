package application;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;

public class TcpServerApp extends RoboticsAPIApplication {
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