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
        STANDALONE_POINT_DIAMETER = 14;

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
        ROBOT_POSITION_COLOR = new Color(0, 0, 0);

    public static final float
        NEXTCOLOR_HUE_INCREMENT = 1 / 6f,
        NEXTCOLOR_SATURATION    = 1f,
        NEXTCOLOR_BRIGHTNESS    = 0.75f;

    /**
     * File Locations
     */
    public static final String
        PREFERENCES_LOCATION = "prefs.txt";

    /**
     * Timing
     */
    public static final long
        UPDATE_RATE = 50, //50 ms or 20 hz
        PING_RATE = 500;

    /**
     * Socket constants
     */
    public static final int
        SOCKET_TIMEOUT = 20,
        SOCKET_BUFFER_SIZE = 128000; //128 kB

    /**
     * Other constants
     */
    public static final String
        ROBOT_POSITION_NAME = "Robot Position",
        FILE_SUFFIX         = ".hpt";
}
