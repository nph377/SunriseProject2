package application;
import java.io.PrintWriter;
import java.net.Socket;

import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;

public class KukaToPcSender extends RoboticsAPIApplication {

    public void run() {
        String pcIP = "172.31.1.100";  // Replace with your PC's IP
        int port = 30000;              // Port your PC will listen on

        Socket socket = null;
        PrintWriter out = null;

        try {
            getLogger().info("Connecting to PC...");
            socket = new Socket(pcIP, port);  // Connect to PC
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Hello from KUKA\n");
            out.println("STATUS:OK\n");
            out.println("STOP\n");

        } catch (Exception e) {
            getLogger().error("Error: " + e.getMessage());
        } finally {
            try {
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                getLogger().error("Close error: " + e.getMessage());
            }
        }
    }
}