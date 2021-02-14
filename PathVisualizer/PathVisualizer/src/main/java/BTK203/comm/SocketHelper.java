package BTK203.comm;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import BTK203.Constants;

/**
 * The utility that handles all communication with the robot.
 */
public class SocketHelper {
    private DatagramSocket datagramSocket; //used to search for the robot and alert it of our address.
    private Socket streamSocket; //used to transfer path files and other data
    private boolean
        initalized,
        streamInitalized;
    private long lastPingTime;

    /**
     * Creates a new SocketHelper. Uses the preferences to set the IP Address and port.
     */
    public SocketHelper(String defaultAddress, int defaultPort) {
        initalized = false;
        streamInitalized = false;
        lastPingTime = System.currentTimeMillis();

        try {
            datagramSocket = new DatagramSocket(defaultPort);
            datagramSocket.connect(InetAddress.getByName(defaultAddress), defaultPort);
            initalized = true;
        } catch(SocketException ex) {
            printSocketExceptionMessage(ex);
        } catch(UnknownHostException ex) {
            printUnknownHostExceptionMessage(ex, defaultAddress);
        }

        new Thread(() -> {
            try {
                streamSocket = new Socket(InetAddress.getByName(defaultAddress), defaultPort + 1);
                streamInitalized = true;
            } catch(UnknownHostException ex) {
                printUnknownHostExceptionMessage(ex, defaultAddress);
            } catch(IOException ex) {
                printIOExceptionMessage(ex);
            }
            
        }).start();
    }

    /**
     * Updates the SocketHelper.
     * This method will check for messages from the robot and handle them as necessary.
     */
    public void update() {
        if(isStreamSocketConnected()) {
            System.out.println("this is epic bro");
        } else {
            if(isInitalized()) {
                //ping robot to alert it of our address. Ping once every Constants.PING_RATE ms.
                long currentTime = System.currentTimeMillis();
                if(currentTime - lastPingTime > Constants.PING_RATE) {
                    try {
                        //send message to robot containing our IP Address, so robot can connect to our stream socket.
                        String discoveryMessage = "Robot?" + InetAddress.getLocalHost().getHostAddress() + ";";
                        System.out.println(discoveryMessage);
                        byte[] messageBytes = discoveryMessage.getBytes();
                        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
                        datagramSocket.send(packet);
                        lastPingTime = currentTime;
                    } catch(UnknownHostException ex) {
                        printUnknownHostExceptionMessage(ex, "localhost");
                    } catch(IOException ex) {
                        printIOExceptionMessage(ex);
                    }
                }
            }
        }
    }

    /**
     * Starts searching for a robot on a new network address and port.
     * So long as update() is called periodically, stream socket will automatically connect when the robot is found.
     * @param address The new ipv4 address.
     * @param port The new port.
     */
    public void startSearchOn(String address, int port) {
        if(isInitalized()) {
            datagramSocket.close(); //close to prevent resource leaks.
        }

        try {
            datagramSocket = new DatagramSocket(port);
            datagramSocket.connect(InetAddress.getByName(address), port);
        } catch(SocketException ex) {
            printSocketExceptionMessage(ex);
        } catch(UnknownHostException ex) {
            printUnknownHostExceptionMessage(ex, address);
        }
    }

    /**
     * Returns true if the datagram socket is initalized, and false otherwise.
     * This method does not bother to look at the stream socket because it relies on the datagram to connect.
     */
    public boolean isInitalized() {
        return initalized;
    }

    /**
     * Returns true if the socket is bound to the computer, and false otherwise.
     */
    public boolean isDatagramSocketConnected() {
        if(isInitalized()) {
            return datagramSocket.isConnected();
        }

        return false;
    }

    /**
     * Returns true if the socket is connected to the robot, and false otherwise.
     */
    public boolean isStreamSocketConnected() {
        if(isInitalized() && streamInitalized) {
            return streamSocket.isConnected();
        }

        return false;
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
