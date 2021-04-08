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
    
    private long lastUpdateTime;
    private HashMap<String, String> unclaimedMessages;
    private String currentData;

    /**
     * Creates a new SocketHelper trying to connect to address and port.
     * @param address The ipv4 address of the robot.
     * @param port The port to connect on.
     */
    public SocketHelper(String address, int port) {
        startConnectingTo(address, port);
        lastUpdateTime = 0;
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
                //message formatted as such: "[start sequence] [subject] [subject sequence if there is one] [subject contents if applicable] [split sequence] [contents of message] [end sequence]"
                while(currentData.indexOf(Constants.START_SEQUENCE) > -1 && currentData.indexOf(Constants.END_SEQUENCE) > -1) {
                    int
                        startSequenceIndex = currentData.indexOf(Constants.START_SEQUENCE),
                        endSequenceIndex = currentData.indexOf(Constants.END_SEQUENCE);
                    
                    String relavantData = "";
                    try {
                        relavantData = currentData.substring(startSequenceIndex, endSequenceIndex);
                    } catch(StringIndexOutOfBoundsException ex) {
                        System.out.println("SIOOB Error! Data was VERY invalid!");
                    }
                    currentData = currentData.substring(endSequenceIndex + 1);

                    String[] completedMessages = relavantData.split(Constants.END_SEQUENCE);
                    for(int i=0; i<completedMessages.length; i++) {
                        String completedMessage = completedMessages[i];

                        int splitSequenceIndex = completedMessage.indexOf(Constants.SPLIT_SEQUENCE);
                        if(splitSequenceIndex > -1) { //now it is defintely a full message
                            String subject = completedMessage.substring(0, splitSequenceIndex);
                            String message = completedMessage.substring(splitSequenceIndex + Constants.SPLIT_SEQUENCE.length());
                            String info = "";

                            if(subject.startsWith(Constants.START_SEQUENCE)) {
                                subject = subject.substring(Constants.START_SEQUENCE.length()); //substring off the start sequence
                            }

                            if(subject.contains(Constants.SUBJECT_SEQUENCE)) {
                                info = subject.substring(subject.indexOf(Constants.SUBJECT_SEQUENCE) + Constants.SUBJECT_SEQUENCE.length());
                            }

                            handleMessage(subject, message, info);
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

    public String sendMessageAndGetResponse(MessageType subject, String message, String extraInfo) {
        if(!getInitalizedAndConnected()) {
            return "";
        }
        
        String formattedMessage = "";
        if(extraInfo.isEmpty()) {
            formattedMessage = composeMessage(subject, message);
        } else {
            formattedMessage = composeMessage(subject, extraInfo, message);
        }
        sendMessage(formattedMessage);

        String formattedSubject = "";
        try {
            formattedSubject = formattedMessage.split(Constants.SPLIT_SEQUENCE)[0].substring(1);
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

            try {
                Thread.sleep(Constants.UPDATE_RATE);
            } catch(InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("sendMessageAndGetResponse() timed out.");
        return "";
    }

    /**
     * Sends a message to the robot and returns the robot's response to the message if there is one.
     * This method will block until either the response is received, or the operation times out.
     * @param subject The subject of the message to send.
     * @param message The body of the message to send.
     * @return The robot's response to the message, or an empty String if it does not respond.
     */
    public String sendMessageAndGetResponse(MessageType subject, String message) {
        return sendMessageAndGetResponse(subject, message, "");
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

    public boolean getUpdated() {
        return System.currentTimeMillis() - lastUpdateTime < Constants.STABLE_UPDATE_THRESHOLD;
    }

    /**
     * Tries to connect the socket to the robot in a separate thread, and keeps trying even when it times out.
     */
    private void attemptToConnectSocket() {
        if(!connecting) {
            String currentAddr = address;
            int currentPort = port;

            if(!initalized && socket != null) {
                try {
                    socket.close();
                } catch(IOException ex) {
                    printIOExceptionMessage(ex);
                }
            }

            initalized = false;
            this.connecting = true;
            new Thread(() -> {
                while(currentAddr.equals(address) && currentPort == port) {
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
    }

    /**
     * Creates a properly formatted String message for the robot.
     * @param subject The subject of the message.
     * @param message The contents of the message.
     * @return A properly formatted message.
     */
    private String composeMessage(MessageType subject, String message) {
        return Constants.START_SEQUENCE + subject.getCode() + Constants.SPLIT_SEQUENCE + message + Constants.END_SEQUENCE;
    }

    /**
     * Creates a properly formatted String message for the robot.
     * @param subject The subject of the message
     * @param subjectInfo Additional info needed for the robot to carry out the task depicted by the message
     * @param message The contents of the message.
     * @return A properly formatted String message.
     */
    private String composeMessage(MessageType subject, String subjectInfo, String message) {
        return Constants.START_SEQUENCE + subject.getCode() + Constants.SUBJECT_SEQUENCE + subjectInfo + Constants.SPLIT_SEQUENCE + message + Constants.END_SEQUENCE;
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
    private void handleMessage(String subject, String message, String info) {
        MessageType type = MessageType.fromString(subject); //the type of message
        Object contents = null; //the information to pass along to the Manager to forward to another system.

        lastUpdateTime = System.currentTimeMillis();

        switch(type) {
            case POSITION:
                contents = Point2D.fromString(message);
                break;
            case PATH: {
                    try {
                        String pathName = subject.split(Constants.SUBJECT_SEQUENCE)[1];
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