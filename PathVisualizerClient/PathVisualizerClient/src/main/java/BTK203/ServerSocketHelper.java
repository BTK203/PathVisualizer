package BTK203;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A utility that assists with handling PathVisualizer data from the computer
 * client.
 */
public class ServerSocketHelper {
    private ServerSocket serversocket;
    private Socket clientSocket;
    private int port;
    private boolean initalized;
    private Point2D robotPosition;

    public ServerSocketHelper(int port) {
        this.port = port;
        initalized = false;
        robotPosition = new Point2D(0, 0, 0);

        try {
            this.serversocket = new ServerSocket(port);
            this.clientSocket = serversocket.accept();
            initalized = true;
        } catch(IOException ex) {
            printIOExceptionMessage(ex);
        }
    }

    /**
     * This method only here to test if the client can handle multiple messages and still be able to handle all of them.
     */
    public void update() {
        String message = composeMessage("Pos", robotPosition.toString());

        try {
            clientSocket.getOutputStream().write(message.getBytes());
        } catch(IOException ex) {
            printIOExceptionMessage(ex);
        }
    }

    public void setRobotPosition(Point2D position) {
        robotPosition = position;
    }

    public Point2D getRobotPosition() {
        return robotPosition;
    }

    public void transferPathFile(String filePath, String name) {
        try {
            String contents = Files.readString(Path.of(filePath));
            String message = composeMessage("Path-" + name, contents);
            System.out.println("transferring " + message);
            clientSocket.getOutputStream().write(message.getBytes());
        } catch(IOException ex) {
            printIOExceptionMessage(ex);
        }
    }

    private String composeMessage(String subject, String message) {
        return "(" + subject + ":" + message + ")";
    }

    /**
     * Prints the generic error message from a SocketException.
     */
    private void printSocketExceptionMessage(SocketException ex) {
        System.out.println("An error occurred in the DatagramSocket.");
        ex.printStackTrace();
    }

    /**
     * Prints the generic error message from an UnknownHostException.
     */
    private void printUnknownHostExceptionMessage(UnknownHostException ex, String address) {
        System.out.println("No host " + address + " is known.");
        ex.printStackTrace();
    }

    /**
     * Prints the generic error message from an IOException
     */
    private void printIOExceptionMessage(IOException ex) {
        System.out.println("IOException occurred in SocketHelper.");
        ex.printStackTrace();
    }
}