package BTK203;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import BTK203.comm.SocketHelper;
import BTK203.ui.PathVisualizerGUI;
import BTK203.util.Path;

/**
 * This class' only job is basically to sit in a chair and pass method calls
 * around. And also run a timer. And also start the program.
 */
public class PathVisualizerManager {
    private PathVisualizerGUI gui;
    private SocketHelper socketHelper;
    private HashMap<String, String> preferences;

    /**
     * Initalizes all needed components of the program.
     */
    public PathVisualizerManager() {
        gui = new PathVisualizerGUI();
        preferences = new HashMap<String, String>();
        readPreferences();

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
                update();
            }
        };
        timer.scheduleAtFixedRate(updateTask, 1, Constants.UPDATE_RATE);
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
     * Sets the visibility of a path.
     * @param path The path to change.
     * @param visible True if the path should be visible, false otherwise.
     */
    public void setPathVisible(Path path, boolean visible) {
        gui.setPathVisible(path, visible);
    }

    /**
     * Deletes a path.
     * @param path The path to delete.
     */
    public void deletePath(Path path) {
        gui.deletePath(path);
    }
    
    /**
     * Prompts the user to select a path, and then loads the path.
     */
    public void loadPath() {
        gui.openFile();
    }

    /**
     * Gets the desired address and port of the socket, and applies it.
     */
    public void connectSocket() {
        String address = gui.getDesiredIPAddress();
        int    port    = gui.getDesiredPort();

        socketHelper.startSearchOn(address, port);

        //set preference so that the address is reloaded the next time the app is launched
        setPreference("defaultIPAddress", address);
        setPreference("defaultPort", Integer.valueOf(port).toString());
    }

    /**
     * Saves the user preferences to PREFERENCES_LOCATION
     */
    public void savePreferences() {
        System.out.println("Saving Preferences...");
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
        gui.updateSocketStatus(socketHelper.isDatagramSocketConnected(), socketHelper.isStreamSocketConnected());
    }
}
