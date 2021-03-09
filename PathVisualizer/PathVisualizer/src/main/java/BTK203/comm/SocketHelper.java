package BTK203.comm;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import BTK203.App;
import BTK203.Constants;
import BTK203.enumeration.MessageType;
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
    
    private HashMap<String, String> unclaimedMessages;
    private String currentData;

    /**
     * Creates a new SocketHelper trying to connect to address and port.
     * @param address The ipv4 address of the robot.
     * @param port The port to connect on.
     */
    public SocketHelper(String address, int port) {
        startConnectingTo(address, port);
        unclaimedMessages = new HashMap<String, String>();
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

                //now that the buffer is cleared, if the user doesn't want to see live data, they will not.
                if(!App.getManager().dataIsLive()) {
                    currentData = "";
                }

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

                        int colon = completedMessage.indexOf(":");
                        if(colon > -1) { //now it is defintely a full message
                            String subject = completedMessage.substring(0, colon);
                            String message = completedMessage.substring(colon + 1);

                            if(subject.startsWith("(")) {
                                subject = subject.substring(1); //substring off "("
                            }


                            handleMessage(subject, message);
                        }
                    }
                }
            } catch(SocketException ex) {
                redoConnection(); //SocketExceptions are usually caused by the host disconnecting or some other comms problem.
            } catch(SocketTimeoutException ex) { //to stop if from printing a stack trace every time the read times out
            } catch(IOException ex) {
                printIOExceptionMessage(ex);
            }
        } 
        
        //if there are too many unclaimed messages, clear the buffer to maximize efficency
        if(unclaimedMessages.size() > Constants.MAX_UNCLAIMED_MESSAGES) {
            unclaimedMessages.clear();
        }
    }

    /**
     * Sends a message to the robot and returns the robot's response to the message if there is one.
     * @param subject The subject of the message to send.
     * @param message The body of the message to send.
     * @return The robot's response to the message, or an empty String if it does not respond.
     */
    public String sendMessageAndGetResponse(MessageType subject, String message) {
        if(!getInitalizedAndConnected()) {
            return "";
        }

        String formattedMessage = composeMessage(subject, message);
        sendMessage(formattedMessage);

        String formattedSubject = "";
        try {
            formattedSubject = formattedMessage.split(":")[0].substring(1);
        } catch(IndexOutOfBoundsException ex) {
            System.out.println("sendMessageAndGetResponse() encountered an internal error.");
            return "";
        }

        //start timer. Method will give up after a certain timeout.
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < Constants.MESSAGE_TIMEOUT) {
            //look for a message with the same subject line
            if(unclaimedMessages.get(formattedSubject) != null) {
                String returnMessage = unclaimedMessages.get(formattedSubject); //claim the message
                unclaimedMessages.remove(formattedSubject);
                return returnMessage;
            }
        }

        System.out.println("sendMessageAndGetResponse() timed out.");
        return "";
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
     * Creates a properly formatted String message for the robot.
     * Format: ([subject]:[message])
     * @param subject The subject of the message.
     * @param message The contents of the message.
     * @return A properly formatted message.
     */
    private String composeMessage(MessageType subject, String message) {
        return "(" + subject.getCode() + ":" + message + ")";
    }

    /**
     * Creates a properly formatted String message for the robot.
     * Format: ([subject]-[subjectinfo]:message)
     * @param subject The subject of the message
     * @param subjectInfo Additional info needed for the robot to carry out the task depicted by the message
     * @param message The contents of the message.
     * @return A properly formatted String message.
     */
    private String composeMessage(MessageType subject, String subjectInfo, String message) {
        return "(" + subject.getCode() + "-" + subjectInfo + ":" + message + ")";
    }

    /**
     * Sends a message through the Socket.
     * @param message The message to send.
     */
    private void sendMessage(String message) {
        if(getInitalizedAndConnected()) {
            try {
                socket.getOutputStream().write(message.getBytes());
            } catch(SocketException ex) {
                redoConnection();
            } catch(IOException ex) {
                printIOExceptionMessage(ex);
            }
        }
    }

    /**
     * Closes the current socket connection and attempts to re-open it.
     * Use this when having communication issues.
     */
    private void redoConnection() {
        //this exception is usually thrown because of a connection issue. To handle it, we terminate the current connection and look for a new one
        try {
            this.socket.close();
        } catch(IOException ex2) {
            printIOExceptionMessage(ex2);
        }
        attemptToConnectSocket();
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

    /**
     * Processes a raw message into usable objects and then passes it along to the Manager for further processing.
     * @param subject The subject of the message (part after open paren but before colon)
     * @param message The body of the message (part after colon but before close paren)
     */
    private void handleMessage(String subject, String message) {
        MessageType type = MessageType.fromString(subject); //the type of message
        Object contents = null; //the information to pass along to the Manager to forward to another system.

        switch(type) {
            case POSITION:
                contents = Point2D.fromString(message);
                break;
            case PATH: {
                    try {
                        String pathName = subject.split("-")[1];
                        contents = Path.fromString(message, pathName);
                    } catch(IndexOutOfBoundsException ex) {
                        System.out.println("Error parsing Path Message! Message was likely missing a \"-\"");
                        ex.printStackTrace();
                    }
                }
                break;
            default: {
                    if(type != MessageType.UNKNOWN) {
                        unclaimedMessages.put(subject, message);
                    }
                }
                return;
        }

        if(contents != null) {
            App.getManager().forwardData(type, contents);
        } else {
            System.out.println("Tried to handle message, but there was no body!");
        }
    }
}