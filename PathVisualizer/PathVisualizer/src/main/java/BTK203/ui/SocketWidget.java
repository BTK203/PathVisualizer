package BTK203.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;

import BTK203.Constants;

import java.awt.BorderLayout;

/**
 * Represents a Socket.
 */
public class SocketWidget extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final String
        DATAGRAM_NOT_CONNECTED = "Status: Not Connected!",
        DATAGRAM_CONNECTED = "Status: Searching...",
        STREAM_CONNECTED     = "Status: Connected";

    private JLabel statusLabel;

    public SocketWidget() {
        super();
        setLayout(new BorderLayout());
        setBackground(Constants.ERROR_RED_COLOR);

        statusLabel = new JLabel(DATAGRAM_NOT_CONNECTED);
        add(statusLabel, BorderLayout.CENTER);
    }

    /**
     * Updates the widget.
     * @param bound True if the socket is bound, false otherwise.
     * @param connected True if the socket is connected, false otherwise.
     */
    public void update(boolean datagramConnected, boolean streamConnected) {
        if(!datagramConnected) { //unbound, unconnected
            setBackground(Constants.ERROR_RED_COLOR);
            statusLabel.setText(DATAGRAM_NOT_CONNECTED);
        } else if(datagramConnected && !streamConnected) {
            setBackground(Constants.WARNING_YELLOW_COLOR);
            statusLabel.setText(DATAGRAM_CONNECTED);
        } else if(datagramConnected && streamConnected) {
            setBackground(Constants.GOOD_GREEN_COLOR);
            statusLabel.setText(STREAM_CONNECTED);
        }
    }
}
