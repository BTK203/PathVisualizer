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
    private boolean 
        initalized,
        connected;
    private Point2D robotPosition;

    public ServerSocketHelper(int port) {
        this.port = port;
        initalized = false;
        robotPosition = new Point2D(0, 0, 0);
        try {
            this.serversocket = new ServerSocket(port);
        } catch(IOException ex) {
            printIOExceptionMessage(ex);
        }

        attemptToConnectSocket();
    }

    /**
     * This method only here to test if the client can handle multiple messages and still be able to handle all of them.
     */
    public void update() {
        String message = composeMessage("Pos", robotPosition.toString());

        sendMessage(message);
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
            sendMessage(message);
        } catch(IOException ex) {
            printIOExceptionMessage(ex);
        }
    }

    private void attemptToConnectSocket() {
        this.initalized = false;
        this.connected = false;
        new Thread(
            () -> {
                try {
                    this.clientSocket = serversocket.accept();
                    initalized = true;
                    connected = true;
                } catch(IOException ex) {
                    printIOExceptionMessage(ex);
                }
            }
        ).start();
    }

    private void sendMessage(String message) {
        if(initalized && connected) {
            try {
                clientSocket.getOutputStream().write(message.getBytes());
            } catch(SocketException ex) {
                //likely a connection issue. Terminate connection and look for a new one
                try {
                    clientSocket.close();
                } catch(IOException ex2) {
                    printIOExceptionMessage(ex2);
                }
                attemptToConnectSocket();
            } catch(IOException ex) {
                printIOExceptionMessage(ex);
            }
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