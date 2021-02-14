package BTK203.util;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

import BTK203.Constants;

public class Util {
    /**
     * Rounds a number to n places.
     * @param number The number to round.
     */
    public static double roundTo(double number, int places) {
        int multiplier = (int) Math.pow(10, places);
        double expanded = number * multiplier;
        double rounded = (int) expanded;
        return rounded / (double) multiplier;
    }

    /**
     * Makes a label bold.
     * @param label The label to make bold.
     * @return The boldified label.
     */
    public static JLabel boldify(JLabel label) {
        Font font = label.getFont();
        Font newFont = font.deriveFont(font.getStyle() | Font.BOLD);
        label.setFont(newFont);
        return label;
    }

    /**
     * Creates a new Border to acheive a margin
     */
    public static Border generateHorizontalMargin() {
        return BorderFactory.createEmptyBorder(0, Constants.DEFAULT_HORIZONTAL_MARGIN, 0, Constants.DEFAULT_HORIZONTAL_MARGIN);
    }

    /**
     * Creates a new Border to acheive a margin.
     */
    public static Border generateVerticalMargin() {
        return BorderFactory.createEmptyBorder(Constants.DEFAULT_VERTICAL_MARGIN, 0, Constants.DEFAULT_VERTICAL_MARGIN, 0);
    }

    /**
     * Tests a number to see if it is between two bounds inclusively.
     * @param num The number to check.
     * @param lowerBound The lowest that the number is allowed to be.
     * @param upperBound The highest that the number is allowed to be.
     * @return True if lowerBound <= num <= upperBound, false otherwise.
     */
    public static boolean isAtOrBetween(int num, int lowerbound, int upperBound) {
        return num >= lowerbound && num <= upperBound;
    }

    /**
     * Tests the validity of an IPv4 address.
     * @param address The ipv4 address to check.
     * @return true if the address is valid, false otherwise.
     */
    public static boolean ipv4AddressIsValid(String address) {
        String[] segments = address.split("\\.");
        if(segments.length == 4) { //all IPv4 addresses have 4 segments.
            int[] segmentsInt = new int[4];
            for(int i=0; i<segments.length; i++) {
                try {
                    segmentsInt[i] = Integer.valueOf(segments[i]);
                } catch(NumberFormatException ex) { //segment is non-numeric and therefore the IP address is not valid.
                    return false;
                }
            }

            //all IPv4 addresses' segments are between the bounds of 0-255, inclusive.
            for(int i=0; i<segments.length; i++) {
                if(!isAtOrBetween(segmentsInt[i], 0, 255)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Tests an IP port for validity.
     * @param port The port to test.
     * @return True if the port is valid, false otherwise.
     */
    public static boolean portIsValid(int port) {
        return isAtOrBetween(port, 1, 65535);
    }

    /**
     * Tests an IP port for validity.
     * @param port the port to test.
     * @return True if the port is valid, false otherwise.
     */
    public static boolean portIsValid(String port) {
        try {
            int portInt = Integer.valueOf(port);
            return portIsValid(portInt);
        } catch(NumberFormatException ex) {
            return false;
        }
    }

    /**
	 * Tests the equality of two values, then prints and returns the result.
	 * Print string will be displayed on RioLog as an error reading: "Assertion [assertionID] SUCCEEDED/FAILED."
	 * @param assertionName The informational name of the assertion. Will be used in the printout.
	 * @param item1       The first item to test.
	 * @param item2       The second item to test.
	 * @return            True if item1 equals item2. False otherwise.
	 */
	public static boolean assertEquals(String assertionName, Object item1, Object item2) {
		boolean success = item1.equals(item2);
		String message = "Assertion " + assertionName + " " + (success ? "SUCCEEDED" : "FAILED") + "." + " Item 1: " + item1.toString() + " | Item 2: " + item2.toString();
		System.out.println(message);
		return success;
	}
}
