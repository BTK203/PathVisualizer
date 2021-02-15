package BTK203.ui;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import BTK203.App;
import BTK203.util.Util;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Ribbon extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private JToggleButton liveButton;
    private JTextField 
        ipAddress,
        ipPort;

    private SocketWidget socketStatus;

    /**
     * Creates a new Ribbon.
     */
    public Ribbon() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(Util.generateVerticalMargin());

        //buttons (load from file, save to file)
        JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

            JButton loadFromFile = new JButton("Load");
                loadFromFile.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        App.getManager().loadPath();
                    }
                });
                
                buttonPanel.add(loadFromFile);

            JButton saveToFile = new JButton("Save");
                saveToFile.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("save file");
                    }
                });

                buttonPanel.add(saveToFile);
            
            add(buttonPanel);

        //socket stuff
        JPanel socketPanel = new JPanel();
            socketPanel.setAlignmentX(RIGHT_ALIGNMENT);
            socketPanel.setLayout(new BoxLayout(socketPanel, BoxLayout.X_AXIS));

            this.liveButton = new JToggleButton("Live");
                socketPanel.add(liveButton);
            
            //ip address
            socketPanel.add(new JLabel("IPv4 Address: "));

            this.ipAddress = new JTextField();
            ipAddress.setText("Not Yet Updated");
            ipAddress.setBorder(Util.generateHorizontalMargin());
            socketPanel.add(ipAddress);

            //ip port
            socketPanel.add(new JLabel("Port: "));

            ipPort = new JTextField();
            ipPort.setText("Not Yet Updated");
            ipPort.setBorder(Util.generateHorizontalMargin());
            socketPanel.add(ipPort);

            socketStatus = new SocketWidget();
            socketPanel.add(socketStatus);
                
            //buttons and things
            JButton connectButton = new JButton("Connect");
            connectButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    App.getManager().connectSocket();
                }
            });
            socketPanel.add(connectButton);

            add(socketPanel);
    }

    /**
     * Sets the look of the socket status widget
     * @param socketConnecting True if the socket is bound, false otherwise.
     * @param socketInitalized True if the socket is connected, false otherwise.
     */
    public void setSocketStatus(boolean socketConnecting, boolean socketInitalized) {
        socketStatus.update(socketConnecting, socketInitalized);
    }

    /**
     * Gets the contents of the "IPv4 address" field.
     * @return The user's desired IP address.
     */
    public String getDesiredSocketAddress() {
        String address = ipAddress.getText();
        if(Util.ipv4AddressIsValid(address)) {
            return address;
        }

        String defaultAddress = App.getManager().getPreference("defaultIPAddress", "10.36.95.2");
        ipAddress.setText(defaultAddress);
        return defaultAddress;
    }

    /**
     * Gets the contents of the "Port" field.
     * NOTE: If the contents of the field are non-numeric, this method will reset the contents of the field to the default port and return that value.
     * @return The user's desired socket port.
     */
    public int getDesiredPort() {
        String portString = ipPort.getText();
        if(Util.portIsValid(portString)) { //test for validity
            //convert port to int and return
            try {
                return Integer.valueOf(portString).intValue();
            } catch(NumberFormatException ex) {
            }
        }
        
        //if any of the above failed, then this code will be run.
        String defaultPort = App.getManager().getPreference("defaultPort", "3695");
        ipPort.setText(defaultPort);
        return Integer.valueOf(defaultPort).intValue();
    }

    /**
     * Sets the value of the "IPv4 Address" field. Will not set if the address is invalid.
     * @param address The new address value.
     */
    public void setDesiredSocketAddress(String address) {
        if(Util.ipv4AddressIsValid(address)) {
            ipAddress.setText(address);
        }
    }

    /**
     * Sets the value of the "Port" field. Will not set if the port is invalid.
     * @param port The new port value.
     */
    public void setDesiredPort(int port) {
        if(Util.portIsValid(port)) {
            ipPort.setText(Integer.valueOf(port).toString());
        }
    }
}
