package BTK203.util;

import java.awt.Color;

/**
 * This interface should be implemented by anything that can be rendered by the {@code Visualizer}.
 */
public interface IRenderable {
    public Point2D[] getPoints();
    public Color     getColor();
    public boolean   isVisible();
    public boolean   isValid();
    public void      setVisible(boolean visible);
    public String    getName();
}
