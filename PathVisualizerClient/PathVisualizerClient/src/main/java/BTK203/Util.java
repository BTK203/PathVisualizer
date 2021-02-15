package BTK203;

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
}
