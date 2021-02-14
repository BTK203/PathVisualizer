package BTK203.ui;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import BTK203.App;
import BTK203.Constants;
import BTK203.util.Path;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.File;

/**
 * The main Graphical User Interface of the program.
 */
public class PathVisualizerGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private Visualizer visualizer;
    private Ribbon ribbon;
    private PathManifest manifest;

    /**
     * Creates a new PathVisualizerGUI
     */
    public PathVisualizerGUI() {
        super("PathVisualizer");
        System.out.println("Building UI...");

        //make the app look like those nice and easy to use windows apps.
        try {
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            System.out.println("Unsupported L&F! Reverting to default Java Swing L&F.");
        }

        // create our window
        JPanel contents = new JPanel(new BorderLayout());
            visualizer = new Visualizer();
            contents.add(visualizer, BorderLayout.CENTER);

            //ribbon
            ribbon = new Ribbon();
            contents.add(ribbon, BorderLayout.NORTH);

            //manifest
            manifest = new PathManifest();
            contents.add(manifest, BorderLayout.EAST);
        
            setContentPane(contents);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(Constants.DEFAULT_WINDOW_SIZE);
        setSize(Constants.DEFAULT_WINDOW_SIZE);

        //add a listener to listen for when the window closes. When that happens, we want to write preferences.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                App.getManager().savePreferences();
            }
        });
    }

    /**
     * Starts the GUI.
     */
    public void start() {
        toFront();
        setVisible(true);
    }

    /**
     * Prompts the user to select a path file to open, and then displays it.
     */
    public void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);

        //resolve default file path from prefs. Use the main user directory as a fallback.
        String filePath = App.getManager().getPreference("defaultOpenFilePath", System.getenv("USERPROFILE"));
        File defaultDirectory = new File(filePath);
        fileChooser.setCurrentDirectory(defaultDirectory);

        int result = fileChooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String pathString = selectedFile.getAbsolutePath();
            String pathName = pathString.substring(pathString.lastIndexOf("\\") + 1);

            App.getManager().setPreference("defaultOpenFilePath", pathString.substring(0, pathString.lastIndexOf("\\"))); //set the default directory to the directory of the file (not the file though)

            if(!manifest.widgetExists(pathName)) {
                Path newPath = new Path(pathString, Path.getNextColor());

                //render the path on the visualizer and add to the manifest
                visualizer.render(newPath);
                manifest.addWidget(new PathWidget(newPath, pathName));
            }
        }
    }

    /**
     * Sets the visibility of a path.
     * @param path the path to change.
     * @param visible True if the path should be visible, false otherwise.
     */
    public void setPathVisible(Path path, boolean visible) {
        visualizer.setPathVisible(path, visible);
    }

    /**
     * Deletes a path.
     * @param path Path to delete.
     */
    public void deletePath(Path path) {
        visualizer.stopRendering(path);
        manifest.removeWidgetByPath(path);
    }

    /**
     * Updates the status of the socket widget.
     * @param datagramConnected True if the datagram is connected, false otherwise.
     * @param streamConnected True if the stream is connected, false otherwise.
     */
    public void updateSocketStatus(boolean datagramConnected, boolean streamConnected) {
        ribbon.setSocketStatus(datagramConnected, streamConnected);
    }

    /**
     * Returns the user's desired IPv4 socket address for the robot.
     * See Ribbon.getDesiredSocketAddress() for more information.
     */
    public String getDesiredIPAddress() {
        return ribbon.getDesiredSocketAddress();
    }

    /**
     * Returns the user's desired socket port.
     * See Ribbon.getDesiredPort() for more information.
     */
    public int getDesiredPort() {
        return ribbon.getDesiredPort();
    }

    /**
     * Sets the value of the "IPv4 Address" field in the ribbon.
     * @param newAddress The value of the new address to set.
     */
    public void setDesiredIPAddress(String newAddress) {
        ribbon.setDesiredSocketAddress(newAddress);
    }

    /**
     * Sets the value of the "Port" field in the ribbon.
     * @param newPort The value of the new port to set.
     */
    public void setDesiredPort(int newPort) {
        ribbon.setDesiredPort(newPort);
    }
}
