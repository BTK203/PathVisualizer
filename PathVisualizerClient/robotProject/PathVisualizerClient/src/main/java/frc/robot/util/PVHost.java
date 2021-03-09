// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.Constants;

/** 
 * Robot code host for the PathVisualizer application.
 */
public class PVHost {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private boolean connected;
    private String currentData;

    /**
     * Creates a new PVHost. It will listen for connections on the specified port.
     * @param port The port that the host will operate on. The port you enter in PathVisualizer should match the one passed here.
     */
    public PVHost(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch(IOException ex) {
            DriverStation.reportError("PVHost could not create a ServerSocket!\n" + ex.getMessage(), true);
        }

        attemptToConnectSocket();
    }

    /**
     * Sends the specified robot position to the PathVisualizer client.
     * @param robotPosition The current robot position.
     */
    public void update(Point2D robotPosition) {
        String message = composeMessage(MessageType.POSITION, robotPosition.toString());
        sendMessage(message);
        handleIncomingMessages();
    }

    /**
     * Sends a path to the PathVisualizer client for viewing.
     * @param path The Path to send.
     * @param name The name of the path. Will appear on the manifest with that name.
     */
    public void sendPath(Path path, String name) {
        if(path.isValid()) {
            String message = composeMessage(MessageType.PATH, name, path.toString());
            sendMessage(message);
        } else {
            DriverStation.reportError("PVHost could not send a path because it was invalid!", false);
        }
    }

    /**
     * Attempts to connect the host to the client.
     * This method starts a new Thread, so it will return immediately, but the socket may not be connected.
     * Use this.connected to check if the client is connected.
     */
    private void attemptToConnectSocket() {
        connected = false;
        new Thread(
            () -> {
                try {
                    clientSocket = serverSocket.accept();
                    connected = true;
                } catch(IOException ex) {
                    DriverStation.reportError("PVHost could not connect to client!\n" + ex.getMessage(), true);
                }
            }
        ).start();
    }

    private void redoConnection() {
        //likely due to the client disconnecting. Terminate connection and try to get it going again
        try {
            clientSocket.close();
        } catch(IOException ex2) {
            DriverStation.reportError("PVHost could not close client socket!\n" + ex2.getMessage(), true);
        }

        attemptToConnectSocket();
    }

    /**
     * Sends a message to the client. The message should be formatted using composeMessage().
     * @param message The message to send (should be formatted).
     */
    private void sendMessage(String message) {
        if(connected) {
            try {
                clientSocket.getOutputStream().write(message.getBytes());
            } catch(SocketException ex) {
                redoConnection();
            } catch(IOException ex) {
                DriverStation.reportError("PVHost could not send message!\n" + ex.getMessage(), true);
            }
        }
    }

    /**
     * Receives messages from the client and handles them.
     */
    private void handleIncomingMessages() {
        if(!connected) {
            return;
        }

        try {
            //add previously received data to currentData.
            byte[] buffer = new byte[Constants.SOCKET_BUFFER_SIZE];
            clientSocket.getInputStream().read(buffer);
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
            DriverStation.reportError("PVHost could not read the client data!", true);
        }
    }

    /**
     * Handles a singular message.
     * @param subject The subject of the message in string form.
     * @param message The body of the message.
     */
    private void handleMessage(String subject, String message) {
        MessageType messageType = MessageType.fromString(subject);
        switch(messageType) {
        case DIRECTORY_REQUEST: { //return a message with all contents of the directory separated by newlines
                String[] paths = PVUtils.getFilesInDirectory(message, true);
                String returnMessage = "";
                for(String path : paths) {
                    returnMessage += path + "\n";
                }
                sendMessage(composeMessage(MessageType.DIRECTORY_REQUEST, returnMessage));
            }
            break;
        default:
            DriverStation.reportError("PVHost could not handle message of type \"" + messageType.getCode() + "\"!", false);
            return;
        }
    }

    /**
     * Formats a message into a format that the client can read.
     * @param subject The subject of the message. Should be "Pos" if sending a position, or "Path-[pathname] if sending a path."
     * @param message The body of the message.
     */
    private String composeMessage(MessageType subject, String message) {
        return "(" + subject.getCode() + ":" + message + ")";
    }

    /**
     * Formats a message into a format that the client can read.
     * @param subject The subject of the message. Should be "Pos" if sending a position, or "Path-[pathname] if sending a path."
     * @param subjectInfo additional information needed for the client to carry out the task depicted by the message.
     * @param message The body of the message.
     */
    private String composeMessage(MessageType subject, String subjectInfo, String message) {
        return "(" + subject.getCode() + "-" + subjectInfo + ":" + message + ")";
    }
}
