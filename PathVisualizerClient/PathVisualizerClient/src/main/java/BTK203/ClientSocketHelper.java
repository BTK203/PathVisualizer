package BTK203;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * A socket client to the PathVisaulizer program.
 */
public class ClientSocketHelper {
    private DatagramSocket datagramSocket;
    private Socket streamSocket;
    private int port;
    private boolean initalized;
    private String datagramString;

    /**
     * Creates a new ClientSocketHelper. This helper will listen for a host address on the specified port.
     * @param port The port to listen on.
     */
    public ClientSocketHelper(int port) {
        this.port = port;
        streamSocket = new Socket();
        initalized = false;
        datagramString = "";
        
        try {
            datagramSocket = new DatagramSocket(3695);
            datagramSocket.setReuseAddress(true);
            datagramSocket.setSoTimeout(1); //makes it so that receiving a message doesn't block the whole program.
            initalized = true;
        } catch(SocketException ex) {
            printSocketExceptionMessage(ex);
        }
    }

    /**
     * Updates the Helper.
     */
    public void update() {
        if(isStreamSocketConnected()) {
            System.out.println("This is pretty epic bro");
        } else {
            //listen for a message. The message will contain an ip address and be coded as follows:
            //"Robot?[address];"
            try {
                DatagramPacket packet = new DatagramPacket(new byte[Constants.DATAGRAM_RECEIVE_BUFFER_SIZE], Constants.DATAGRAM_RECEIVE_BUFFER_SIZE);
                datagramSocket.receive(packet);
                datagramString += new String(packet.getData());

                System.out.println("Received: " + new String(packet.getData()));

                //attempt to parse the data we have
                if(datagramString.contains(";")) {
                    //split uncomplete messages from complete messages.
                    int semicolonIndex = datagramString.lastIndexOf(";");
                    String completedMessagesConcatinated = datagramString.substring(0, semicolonIndex);
                    String[] completedMessages = completedMessagesConcatinated.split(";");
                    datagramString = datagramString.substring(semicolonIndex + 1);
                    
                    for(int i=0; i<completedMessages.length; i++) {
                        //now parsing all completed messages.
                        //Message format is as follows: "Robot?[address];"
                        String completedMessage = completedMessages[i];
                        if(completedMessage.contains("?")) {
                            String[] segments = completedMessage.split("?");
                            if(segments.length > 1 && segments[0].equals("Robot")) { //at least 2
                                String hostAddress = segments[1];
                                if(Util.ipv4AddressIsValid(hostAddress)) {
                                    //we now have new host address. Connect that babey!!
                                    connectStreamSocket(hostAddress);
                                }
                            }
                        }
                    }
                }
            } catch(SocketTimeoutException ex) { //this particular catch just stops barrages of annoying prints
            } catch(IOException ex) {
                printIOExceptionMessage(ex);
            }
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
        if(isInitalized()) {
            return streamSocket.isConnected();
        }

        return false;
    }

    private void connectStreamSocket(String address) {
        try {
            streamSocket.close();
            streamSocket = new Socket(InetAddress.getByName(address), port + 1);
        } catch(UnknownHostException ex) {
            printUnknownHostExceptionMessage(ex, address);
        } catch(IOException ex) {
            printIOExceptionMessage(ex);
        }
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
