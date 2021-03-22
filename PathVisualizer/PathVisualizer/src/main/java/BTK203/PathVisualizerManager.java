package BTK203;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import BTK203.comm.SocketHelper;
import BTK203.enumeration.FileOperation;
import BTK203.enumeration.MessageType;
import BTK203.ui.PathVisualizerGUI;
import BTK203.util.IRenderable;
import BTK203.util.Path;
import BTK203.util.Point2D;

/**
 * This class' only job is basically to sit around and pass method calls
 * around. And also run a timer. And also start the program.
 */
public class PathVisualizerManager {
    private PathVisualizerGUI gui;
    private SocketHelper socketHelper;
    private HashMap<String, String> preferences;
    private HashMap<String, Integer> names;
    private boolean updatedBefore;

    /**
     * Initalizes all needed components of the program.
     */
    public PathVisualizerManager() {
        gui = new PathVisualizerGUI();
        preferences = new HashMap<String, String>();
        readPreferences();

        names = new HashMap<String, Integer>();
        updatedBefore = false;

        //resolve default IP address and port. Keep this section of code below the call to readPreferences().
        String defaultIPAddress = getPreference("defaultIPAddress", "10.36.95.2");
        int defaultPort         = Integer.valueOf(getPreference("defaultPort", "3695"));
        socketHelper = new SocketHelper(defaultIPAddress, defaultPort);

        gui.setDesiredIPAddress(defaultIPAddress);
        gui.setDesiredPort(defaultPort);
    }

    /**
     * Starts the program.
     */
    public void start() {
        gui.start();

        //do main loop
        Timer timer = new Timer();
        TimerTask updateTask = new TimerTask() {
            public void run() {
                try {
                    update();
                } catch(Exception ex) {
                    if(Constants.SHOW_LOWKEY_ERRORS) {
                        System.err.println("Process encountered an error!");
                        ex.printStackTrace();
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(updateTask, 1, Constants.UPDATE_RATE);
    }

    /**
     * Returns the Manager's SocketHelper.
     * @return A SocketHelper.
     */
    public SocketHelper getSocketHelper() {
        return socketHelper;
    }

    /**
     * Gets the value of a preference, stored in a string.
     */
    public String getPreference(String key, String backup) {
        if(preferences.containsKey(key)) {
            return preferences.get(key);
        }

        preferences.put(key, backup);
        return backup;
    }

    /**
     * Sets a user preference.
     * @param key The name of the pref to set.
     * @param value The new value of the pref.
     */
    public void setPreference(String key, String value) {
        preferences.put(key, value);
    }

    /**
     * Returns whether or not the application is listening for live path data from the robot.
     * @return True if live is enabled, false otherwise.
     */
    public boolean dataIsLive() {
        return gui.liveButtonSelected();
    }

    /**
     * Sets the visibility of a path.
     * @param path The path to change.
     * @param visible True if the path should be visible, false otherwise.
     */
    public void setPathVisible(IRenderable path, boolean visible) {
        gui.setPathVisible(path, visible);
    }

    /**
     * Deletes a path.
     * @param path The path to delete.
     */
    public void deletePath(IRenderable path) {
        gui.deletePath(path);
    }
    
    /**
     * Prompts the user to select a path, and then loads the path.
     */
    public void loadPath() {
        gui.openFile();
    }

    /**
     * Prompts the user to select a path to save and a location to save it to.
     */
    public void saveRenderable() {
        gui.promptSaveRenderable();
    }

    /**
     * Prompts the user to save a file to the robot.
     */
    public void doRobotFileOperation(FileOperation operation) {
        //resolve from preferences the last file location that the user used. This will be the starting location for the directory
        String startingDirectory = "/";
        if(operation == FileOperation.LOAD) {
            startingDirectory = getPreference("defaultRobotLoadDirectory", Constants.DEFAULT_ROBOT_ROOT_DIR);
        } else {
            startingDirectory = getPreference("defaultRobotSaveDirectory", Constants.DEFAULT_ROBOT_ROOT_DIR);
        }

        //if the user wants a save, we need to know what path to save
        IRenderable thingToSave = null;
        if(operation == FileOperation.SAVE) {
            thingToSave = gui.promptRenderableToSave();

            if(thingToSave == null) {
                return;
            }
        }

        final IRenderable finalThingToSave = thingToSave; //this is needed because java complains about using thingToSave in the Thread

        //get the file path from the user and return if its wack
        String filePath = gui.runRobotFileDialog(operation, startingDirectory);
        if(filePath == null || !filePath.contains("/") || filePath.equals("")) {
            return;
        }

        //saves last choice to preferences for convenience
        String directoryName = filePath.substring(0, filePath.lastIndexOf("/"));
        if(operation == FileOperation.LOAD) {
            setPreference("defaultRobotLoadDirectory", directoryName);
        } else {
            setPreference("defaultRobotSaveDirectory", directoryName);
        }

        //do the operation now (separate thread because this involves sendMessageAndGetResponse)
        new Thread(
            () -> {
                if(operation == FileOperation.LOAD) {
                    String fileName = "Robot: " + filePath.substring(filePath.lastIndexOf("/") + 1);
                    String pathString = socketHelper.sendMessageAndGetResponse(MessageType.LOAD, filePath);
                    
                    if(pathString.isEmpty()) {
                        return;
                    }

                    if(pathString.contains("ERR")) {
                        gui.showGeneralAlert("An Error occurred while grabbing the file.");
                        return;
                    }

                    Path newPath = Path.fromString(pathString, fileName);
                    if(newPath == null) {
                        gui.showGeneralAlert("Invalid File!");
                        return;
                    }

                    gui.putPath(newPath);
                } else { //FileOperation.SAVE
                    if(finalThingToSave != null) {
                        String response = socketHelper.sendMessageAndGetResponse(MessageType.SAVE, finalThingToSave.toString(), filePath);
                        if(!response.equals("OK")) {
                            gui.showGeneralAlert("An Error occurred while saving \"" + finalThingToSave.getName() + "\" To the robot.");
                        }
                    }
                }
            }
        ).start();
    }

    /**
     * Forwards data from the robot to its appropriate location for handling.
     * The value of contents must correlate with the value of subject.
     * Quick reference (value of subject : type of contents)
     * PATH : Path
     * POSITION: Point2D
     * @param subject The subject of the message that the robot sent
     * @param contents The contents of the message, ready for handling.
     */
    public void forwardData(MessageType subject, Object contents) {
        switch(subject) {
        case PATH: {
                if(contents instanceof Path) {
                    Path path = (Path) contents;
                    path.setName(getNextName(path.getName()));
                    gui.putPath(path);
                } else {
                    System.out.println("Manager tried to forward path data to GUI, but it was not a path!"); //hopefully this code is never run, but its nice to have
                    System.out.println("Contents: " + contents.toString());
                }
            }            
            break;
        case POSITION: {
                if(contents instanceof Point2D) {
                    gui.updateRobotPosition((Point2D) contents);
                } else {
                    System.out.println("Manager tried to forward position to GUI, but it was not a Point2D!"); //same here
                    System.out.println("Contents: " + contents.toString());
                }
            }
            break;
        default:
            return;
        }
    }

    /**
     * Gets the desired address and port of the socket, and applies it.
     */
    public void connectSocket() {
        String address = gui.getDesiredIPAddress();
        int    port    = gui.getDesiredPort();

        socketHelper.startConnectingTo(address, port);

        //set preference so that the address is reloaded the next time the app is launched
        setPreference("defaultIPAddress", address);
        setPreference("defaultPort", Integer.valueOf(port).toString());
    }

    /**
     * Saves the user preferences to PREFERENCES_LOCATION
     */
    public void savePreferences() {
        String fileContents = "";
        Iterator<String> keys = preferences.keySet().iterator();
        if(keys != null) {
            while(keys.hasNext()) {
                String key = keys.next();
                String value = preferences.get(key);
                fileContents += key + ":\"" + value + "\"\n"; //only inserting quotes for readability. Line formatted as such: [key]:"[value]"
            }
        }

        try {
            Files.write(java.nio.file.Path.of(Constants.PREFERENCES_LOCATION), fileContents.getBytes());
        } catch(IOException ex) {
            System.out.println("Failed to save preferences!");
        }
    }

    /**
     * Gets the next available name for something.
     * This is similar to how the windows file explorer renames duplicate files to "name (1)" or "name (2)"
     * @param Name The base name to use.
     * @return The next available name.
     */
    private String getNextName(String name) {
        if(names.get(name) != null) {
            //name has been used before, rename it
            int nameNumber = names.get(name).intValue();
            nameNumber++;
            names.put(name, Integer.valueOf(nameNumber));
            return name + " (" + Integer.toString(nameNumber) + ")";
        }

        names.put(name, Integer.valueOf(0));
        return name;
    }

    /**
     * Reads the user preferences off of PREFERENCES_LOCATION and sets necessary variables.
     */
    private void readPreferences() {
        java.nio.file.Path prefsLocation = java.nio.file.Path.of(Constants.PREFERENCES_LOCATION);
        this.preferences = new HashMap<String, String>();

        //check to see if the prefs file exists
        if(!Files.exists(prefsLocation)) {
            System.out.println("No Preferences manifest found.");
            return;
        }

        //read raw preferences into our prefs hashmap
        try {
            List<String> lines = Files.readAllLines(prefsLocation);

            //each line should look like this: [key]:"[value]"
            for(int i=0; i<lines.size(); i++) {
                String line = lines.get(i);
                if(line.contains(":")) {
                    int firstColonIndex = line.indexOf(":"); //gets index of first colon in string
                    String key = line.substring(0, firstColonIndex);
                    String value = line.substring(firstColonIndex + 2, line.length() - 1);
                    preferences.put(key, value);
                }
            }
        } catch(IOException ex) {
            System.out.println("An error occurred while reading the preferences manifest.");
        }
    }

    /**
     * Updates the components of the program that need to be updated
     */
    private void update() {
        socketHelper.update();
        if(updatedBefore && !socketHelper.getUpdated()) {
            socketHelper.startConnectingTo(gui.getDesiredIPAddress(), gui.getDesiredPort());
        }
        
        updatedBefore = socketHelper.getUpdated();
        gui.updateSocketStatus(socketHelper.getConnecting(), socketHelper.getInitalizedAndConnected());
    }
}
