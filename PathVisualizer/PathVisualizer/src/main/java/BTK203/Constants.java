package BTK203;

import java.awt.Dimension;
import java.awt.Color;

public class Constants {
    /**
     * Size Constants
     */
    public static final Dimension 
        DEFAULT_WINDOW_SIZE      = new Dimension(850, 420),
        DEFAULT_MANIFEST_SIZE    = new Dimension(225, 420),
        DEFAULT_SAVE_DIALOG_SIZE = new Dimension(500, 300),
        MINIMUM_VISUALIZER_SIZE  = new Dimension(200, 200),
        COLOR_SAMPLE_SIZE        = new Dimension(18, 18);

    public static final int
        DEFAULT_HORIZONTAL_MARGIN = 8,
        DEFAULT_VERTICAL_MARGIN   = 8,
        POINT_MARK_DIAMETER       = 5,
        ENDPOINT_MARK_DIAMETER    = 10,
        STANDALONE_POINT_DIAMETER = 14,
        MANIFEST_MARGIN           = 0,
        MANIFEST_WIDGET_NONLABEL_WIDTH = 100;

    /**
     * Color Constants
     */
    public static final Color
        PRIMARY_TEXT         = new Color(0, 0, 0),
        SECONDARY_BACKGROUND = new Color(230, 230, 230),
        START_POINT_COLOR    = new Color(0, 160, 0),
        END_POINT_COLOR      = new Color(200, 20, 20),
        ERROR_RED_COLOR      = new Color(255, 50, 50),
        WARNING_YELLOW_COLOR = new Color(255, 255, 50),
        GOOD_GREEN_COLOR     = new Color(50, 255, 50),
        ROBOT_POSITION_COLOR = new Color(0, 0, 0),
        BLACK                = new Color(0, 0, 0),
        WHITE                = new Color(255, 255, 255),
        HOVER_BLUE           = new Color(229, 243, 255),
        SELECT_BLUE          = new Color(204, 232, 255);

    public static final float
        NEXTCOLOR_HUE_INCREMENT = 1 / 6f,
        NEXTCOLOR_SATURATION    = 1f,
        NEXTCOLOR_BRIGHTNESS    = 0.75f;

    /**
     * File Locations
     */
    public static final String
        PREFERENCES_LOCATION = "prefs.txt",
        BACK_ICON            = "src/main/java/BTK203/ui/icons/back.png",
        FILE_ICON            = "src/main/java/BTK203/ui/icons/file.png",
        DIRECTORY_ICON       = "src/main/java/BTK203/ui/icons/directory.png";

    /**
     * Timing
     */
    public static final long
        UPDATE_RATE = 50, //50 ms or 20 hz
        DOUBLE_CLICK_TIME = 400,
        PING_RATE = 500,
        MESSAGE_TIMEOUT = 5000, //time for robot to respond before processes give up (in ms).
        STABLE_UPDATE_THRESHOLD = 250,
        RECONNECT_INTERVAL = 1000;

    /**
     * Settings (debug)
     */
    public static final boolean
        SHOW_LOWKEY_ERRORS = true; //if set to true, program will print out errors that aren't considered to be serious.

    /**
     * Socket constants
     */
    public static final int
        SOCKET_TIMEOUT = 20,
        SOCKET_BUFFER_SIZE = 128000, //128 kB
        MAX_UNCLAIMED_MESSAGES = 256;

    /**
     * Message Format constants. These must exactly match their counterparts in the robot host.
     */
    public static final String
        START_SEQUENCE = "[",
        END_SEQUENCE = "]",
        SPLIT_SEQUENCE = ":::",
        SUBJECT_SEQUENCE = "---";

    /**
     * Other constants
     */
    public static final String
        ROBOT_POSITION_NAME          = "Robot Position",
        FILE_SUFFIX                  = ".hpt",
        ROBOTBROWSER_DIRECTORY_SUFFIX = ":dir",
        DEFAULT_ROBOT_FILE_NAME      = "points" + FILE_SUFFIX,
        DEFAULT_PC_FILE_NAME         = "path" + FILE_SUFFIX,
        DEFAULT_ROBOT_ROOT_DIR       = "/home/lvuser";
}
