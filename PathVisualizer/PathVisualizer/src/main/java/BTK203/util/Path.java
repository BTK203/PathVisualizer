package BTK203.util;

import java.io.IOException;
import java.nio.file.Files;

import BTK203.Constants;

import java.awt.Color;

/**
 * Represents a Path that can be rendered on the screen.
 */
public class Path implements IRenderable {
    private static float currentHue = 0;

    private Point2D[] points;
    private Color color;
    private boolean
        valid,
        visible;

    /**
     * Creates a new path from the given file path.
     * @param file The path to the file to read from.
     * @param color The color of the path.
     */
    public Path(String file, Color color) {
        this.color = color;
        this.valid = false;
        this.visible = true;
        try {
            String fileContents = Files.readString(java.nio.file.Path.of(file));
            String[] pointStrings = fileContents.split("\n");
            points = new Point2D[pointStrings.length];
            for(int i=0; i<pointStrings.length; i++) {
                points[i] = Point2D.fromString(pointStrings[i]);
            }

            valid = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NumberFormatException ex) {
            System.out.println("Invalid File!");
        }
    }

    /**
     * Creates a new Path.
     * @param points An array of points describing the path.
     * @param color The color of the path.
     */
    public Path(Point2D[] points, Color color) {
        this.points = points;
        this.color = color;
        this.valid = true;
        this.visible = true;
    }

    /**
     * Returns the Path's points.
     */
    public Point2D[] getPoints() {
        return points;
    }

    /**
     * Returns true if this Path was initalized correctly, false otherwise.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets whether or not the Path is visible.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Returns whether or not the path is visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Returns the color of the path.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the next color for a new path.
     */
    public static Color getNextColor() {
        Color nextColor = new Color(Color.HSBtoRGB(currentHue, Constants.NEXTCOLOR_SATURATION, Constants.NEXTCOLOR_BRIGHTNESS));
        currentHue += Constants.NEXTCOLOR_HUE_INCREMENT;
        return nextColor;
    }
}
