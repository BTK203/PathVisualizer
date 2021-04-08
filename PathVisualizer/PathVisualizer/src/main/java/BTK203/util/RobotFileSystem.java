package BTK203.util;

import BTK203.App;
import BTK203.enumeration.MessageType;

/**
 * A class that can peek through a robot's file system.
 */
public class RobotFileSystem {
    private final String[] EMPTY = new String[0];
    private String currentDirectory;
    private String[] paths;

    /**
     * Creates a new RobotFileSystem, starting its search on directory.
     * @param directory The absolute directory to search first.
     */
    public RobotFileSystem(String directory) {
        currentDirectory = directory;
        paths = EMPTY;
    }

    /**
     * Sets the file system's directory and queries the robot's files on that directory.
     * @param directory The absolute directory to change to.
     */
    public void setCurrentDirectory(String directory) {
        currentDirectory = directory;
        paths = EMPTY;

        //query the robot's files and directories on the new directory. The robot should return a message with all files and directories separated by newlines.
        String directoryContents = App.getManager().getSocketHelper().sendMessageAndGetResponse(MessageType.DIRECTORY_REQUEST, directory);
        if(directoryContents.length() > 0) {
            paths = directoryContents.split("\n");
        }
    }

    /**
     * Returns an array of all file paths in the current directory.
     * @return All file paths in the current directory.
     */
    public String[] getPaths() {
        return paths;
    }

    public String[] onlyNames(String[] paths) {
        String[] nameArray = new String[paths.length];
        for(int i=0; i<paths.length; i++) {
            String name = paths[i].substring(paths[i].lastIndexOf("/") + 1); //works when client is running on Unix. If no forward slash found, will return the old string.
            name = name.substring(name.lastIndexOf("\\") + 1); //works when client is running on windows. If no back slash found, will return the old string.
            nameArray[i] = name;
        }

        return nameArray;
    }

    /**
     * Returns the file system's current directory.
     * @return The current directory.
     */
    public String getCurrentDirectory() {
        return currentDirectory;
    }
}
