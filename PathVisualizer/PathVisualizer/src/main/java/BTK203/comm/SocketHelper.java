package BTK203.comm;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import BTK203.App;
import BTK203.Constants;
import BTK203.util.Path;
import BTK203.util.Point2D;

/**
 * A utility that deals with all communication with the robot.
 */
public class SocketHelper {
    private Socket socket;
    private String address;
    private int port;
    private boolean
        connecting,
        initalized;

    private String currentData;

    /**
     * Creates a new SocketHelper trying to connect to address and port.
     * @param address The ipv4 address of the robot.
     * @param port The port to connect on.
     */
    public SocketHelper(String address, int port) {
        startConnectingTo(address, port);
        currentData = "";
    }

    /**
     * Updates the SocketHelper.
     */
    public void update() {
        if(getInitalizedAndConnected()) {
            try {
                //add previously received data to currentData.
                byte[] buffer = new byte[Constants.SOCKET_BUFFER_SIZE];
                socket.getInputStream().read(buffer);
                currentData += new String(buffer);

                //parse data and look for messages.
                //messages formatted as such: "([subject]:[message])"
                while(currentData.indexOf(")") > -1 && currentData.indexOf("(") > -1) {
                    int
                        openParenIndex = currentData.indexOf("("),
                        closeParenIndex = currentData.indexOf(")");
                        
                    String relavantData = currentData.substring(openParenIndex, closeParenIndex);
                    currentData = currentData.substring(closeParenIndex + 1);

                    String[] completedMessages = relavantData.split("\\)");
                    for(int i=0; i<completedMessages.length; i++) {
                        String completedMessage = completedMessages[i];
                        if(completedMessage.contains(":")) { //now it is defintely a full message
                            String[] segments = completedMessage.split(":");
                            String subject = segments[0];
                            String message = segments[1];

                            if(subject.startsWith("(")) {
                                subject = subject.substring(1); //substring off "("
                            }

                            if(subject.equals("Pos")) { //robot is declaring a change in position. The "message" part of the message will be the point.
                                Point2D newRobotPosition = Point2D.fromString(message);
                                App.getManager().updateRobotPosition(newRobotPosition);
                            }

                            if(subject.startsWith("Path")) { //just what it sounds like
                                if(subject.contains("-")) { //"-" separates prefix from the path name.
                                    //subject formatted as such: "Path-"[name]
                                    String pathName = subject.split("-")[1];

                                    String[] pointStrings = message.split("\n");
                                    Point2D[] points = new Point2D[pointStrings.length];
                                    for(int p=0; p<points.length; p++) {
                                        points[p] = Point2D.fromString(pointStrings[p]);
                                    }

                                    Path newPath = new Path(points, Path.getNextColor());
                                    App.getManager().renderPathWithName(newPath, pathName);
                                }
                            }
                        }
                    }
                }
            } catch(SocketTimeoutException ex) { //to stop if from printing a stack trace every time the read times out
            } catch(IOException ex) {
                printIOExceptionMessage(ex);
            }
        }
    }

    /**
     * Starts trying to connect the socket to the given address and port.
     * @param address The new ipv4 address to connect to.
     * @param port The new port to connect on.
     */
    public void startConnectingTo(String address, int port) {
        this.address = address;
        this.port = port;
        attemptToConnectSocket();
    }

    /**
     * Returns whether or not the Socket is actively trying to connect.
     * @return true if the socket is connecting, false otherwise.
     */
    public boolean getConnecting() {
        return connecting;
    }

    /**
     * Returns whether or not the socket is fully initalized, including connected.
     * @return true if the socket is initalized, false otherwise.
     */
    public boolean getInitalizedAndConnected() {
        if(initalized && !connecting) {
            return socket.isConnected();
        }

        return false;
    }

    /**
     * Tries to connect the socket to the robot in a separate thread, and keeps trying even when it times out.
     */
    private void attemptToConnectSocket() {
        initalized = false;
        this.connecting = true;
        new Thread(() -> {
            while(true) {
                try {
                    socket = new Socket(InetAddress.getByName(address), port);
                    socket.setSoTimeout(Constants.SOCKET_TIMEOUT);
                    initalized = true;
                    break;
                } catch(UnknownHostException ex) {
                    printUnknownHostExceptionMessage(ex, address);
                } catch(ConnectException ex) {       //connection timed out.
                } catch(NoRouteToHostException ex) { //not on a network. We don't care about this one because we want it to connect as soon as it is able.
                } catch(IOException ex) {
                    printIOExceptionMessage(ex);
                    break;
                }
            }

            connecting = false;
        }).start();
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