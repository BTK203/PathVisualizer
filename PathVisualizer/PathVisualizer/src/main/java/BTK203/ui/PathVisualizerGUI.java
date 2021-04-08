package BTK203.ui;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import BTK203.App;
import BTK203.Constants;
import BTK203.enumeration.FileOperation;
import BTK203.util.IRenderable;
import BTK203.util.Path;
import BTK203.util.Point2D;
import BTK203.util.Position;
import BTK203.util.Util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * The main Graphical User Interface of the program.
 */
public class PathVisualizerGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private Visualizer visualizer;
    private Ribbon ribbon;
    private PathManifest manifest;
    private Position robotPosition;
    private boolean robotPositionInitalized;

    /**
     * Creates a new PathVisualizerGUI
     */
    public PathVisualizerGUI() {
        super("PathVisualizer");
        robotPosition = new Position(new Point2D(0, 0, 0), new Color(0, 0, 0), Constants.ROBOT_POSITION_NAME);
        robotPositionInitalized = false;

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
            Path newPath = new Path(pathString, Path.getNextColor());

            App.getManager().setPreference("defaultOpenFilePath", pathString.substring(0, pathString.lastIndexOf("\\"))); //set the default directory to the directory of the file (not the file though)

            if(!manifest.widgetExists(newPath.getName())) { //I dont trust .equals() for something as big as an IRenderable, so we're using the name
                if(newPath.isValid()) {
                    //render the path on the visualizer and add to the manifest
                    visualizer.render(newPath);
                    manifest.addWidget(new RenderableWidget(newPath));
                } else {
                    JOptionPane.showMessageDialog(this, "Could not read File!");
                }
            }
        }
    }

    public IRenderable promptRenderableToSave() {
        ArrayList<IRenderable> renderableList = visualizer.getRenderables();
        IRenderable[] renderableArray = new IRenderable[renderableList.size()];
        for(int i=0; i<renderableList.size(); i++) {
            renderableArray[i] = renderableList.get(i);
        }

        //prompt the user for the path to save
        PathChooser pathChooser = new PathChooser(this, renderableArray, false);
        IRenderable desiredRenderable = pathChooser.run();
        return desiredRenderable;
    }

    /**
     * Prompts the user to select a renderable to save to file.
     * @return The IRenderable to save to file.
     */
    public void promptSaveRenderable() {
        IRenderable desiredRenderable = promptRenderableToSave();

        if(desiredRenderable == null) {
            JOptionPane.showMessageDialog(this, "No Path or Point Selected.");
            return;
        }

        //prompt the user for the path to the new file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
        fileChooser.setDialogTitle("File Location");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File(Constants.DEFAULT_PC_FILE_NAME));

        //resolve default file path from prefs. Use the main user directory as a fallback.
        String filePath = App.getManager().getPreference("defaultSaveFilePath", System.getenv("USERPROFILE"));
        File defaultDirectory = new File(filePath);
        fileChooser.setCurrentDirectory(defaultDirectory);

        int result = fileChooser.showSaveDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            //save the file
            String saveFilePath = fileChooser.getSelectedFile().getAbsolutePath();
            String directory = saveFilePath.substring(0, saveFilePath.lastIndexOf("\\"));
            App.getManager().setPreference("defaultSaveFilePath", directory);
            if(!saveFilePath.endsWith(Constants.FILE_SUFFIX)) {
                saveFilePath += Constants.FILE_SUFFIX;
            }

            String fileContents = desiredRenderable.toString();

            try {
                Files.writeString(java.nio.file.Path.of(saveFilePath), fileContents);
            } catch(IOException ex) {
                System.out.println("Could not write file!");
                ex.printStackTrace();
            }
        }
    }

    /**
     * Prompts the user to select a file to load from or save to the robot.
     * @param operation The operation (save / load) that will take place
     * @return The absolute robot file path that the user selects
     */
    public String runRobotFileDialog(FileOperation operation, String startingDirectory) {
        RobotFileDialog fileDialog = new RobotFileDialog(this, operation, startingDirectory);
        return fileDialog.run();
    }

    /**
     * Shows an information alert box to the user.
     * @param message The message to put in the information box.
     */
    public void showGeneralAlert(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    /**
     * Renders a path on the Visualizer.
     * @param path The path to render.
     * @param name The name of the path.
     */
    public void putPath(Path path) {
        if(!manifest.widgetExists(path.getName()) && path.isValid()) {
            visualizer.render(path);
            manifest.addWidget(new RenderableWidget(path));
        }
    }

    /**
     * Updates the robot position on the Visualizer.
     * @param newPosition The position to render.
     */
    public void updateRobotPosition(Point2D newPosition) {
        if(!robotPositionInitalized) {
            robotPosition = new Position(newPosition, Constants.ROBOT_POSITION_COLOR, Constants.ROBOT_POSITION_NAME);
            robotPositionInitalized = true;
        }

        if(App.getManager().dataIsLive() && !manifest.widgetExists(Constants.ROBOT_POSITION_NAME)) {
            visualizer.render(robotPosition);
            manifest.addWidget(new RenderableWidget(robotPosition));
        }

        robotPosition.setPosition(newPosition);
        visualizer.repaint();
    }

    /**
     * Sets the visibility of a path.
     * @param path the path to change.
     * @param visible True if the path should be visible, false otherwise.
     */
    public void setPathVisible(IRenderable path, boolean visible) {
        visualizer.setPathVisible(path, visible);
    }

    /**
     * Deletes a path.
     * @param path Path to delete.
     */
    public void deletePath(IRenderable path) {
        visualizer.stopRendering(path);
        manifest.removeWidgetByPath(path);
    }

    /**
     * Updates the status of the socket widget.
     * @param socketConnecting True if the datagram is connected, false otherwise.
     * @param socketInitalized True if the stream is connected, false otherwise.
     */
    public void updateSocketStatus(boolean socketConnecting, boolean socketInitalized) {
        ribbon.setSocketStatus(socketConnecting, socketInitalized);
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

    /**
     * Returns whether or not the "Live" button is selected.
     * @return True if the "Live" button is selected, False otherwise.
     */
    public boolean liveButtonSelected() {
        return ribbon.liveButtonSelected();
    }
}
