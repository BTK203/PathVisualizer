package BTK203.util;

import java.awt.Color;

/**
 * An IRenderable representing a single position in the XY plane.
 */
public class Position implements IRenderable {
    private Point2D position;
    private Color color;
    private boolean visible;
    private String name;

    /**
     * Creates a new Position object.
     * @param position The position of the point.
     * @param color The color of the point.
     */
    public Position(Point2D position, Color color, String name) {
        this.position = position;
        this.color = color;
        this.name = name;
        this.visible = true;
    }

    /**
     * Sets the position of the point.
     * @param newPosition The new position of the point.
     */
    public void setPosition(Point2D newPosition) {
        this.position = newPosition;
    }

    /**
     * Returns an array whose only object is the point of this Position.
     * This method returns an array because the IRenderable interface demands it.
     * @return An array containing the position.
     */
    public Point2D[] getPoints() {
        return new Point2D[] { position };
    }

    /**
     * Gets the color of the point.
     * @return The color of the Position
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns true if the point is visible, false otherwise.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Returns whether or not the object is valid. For the Position class, this will always be true.
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Sets the visibility of the object.
     * @param visible True if the object should be visible, false otherwise.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Returns the name of the Renderable as it would appear on the Manifest.
     * @return Name of the Renderable.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a human (and computer) readable String representation of this Position.
     * @return A string representing position.
     */
    public String toString() {
        return position.toString();
    }
}
