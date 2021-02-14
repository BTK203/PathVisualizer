package BTK203.util;

/**
 * A simple class containing an x-coordinate, y-coordinate, width, and height
 */
public class Rectangle {
    private double
        x,
        y,
        width,
        height;

    /**
     * Creates a new Rectangle at position (x, y) with size (width, height)
     */
    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the x-coordinate of the Rectangle.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the rectangle.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the width of the rectangle.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the height of the rectangle.
     */
    public double getHeight() {
        return height;
    }

    public String toString() {
        return "(" +
            Double.valueOf(x).toString() + ", " +
            Double.valueOf(y).toString() + ", " +
            Double.valueOf(width).toString() + ", " +
            Double.valueOf(height).toString() + ")";
    }
}
