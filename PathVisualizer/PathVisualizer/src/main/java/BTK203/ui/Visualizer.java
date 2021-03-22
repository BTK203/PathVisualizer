package BTK203.ui;

import BTK203.util.IRenderable;
import BTK203.util.Point2D;
import BTK203.util.Rectangle;
import BTK203.Constants;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;

/**
 * A class that renders paths onto an area of the screen.
 */
public class Visualizer extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private ArrayList<IRenderable> renderables;

    /**
     * Creates a new Visualizer.
     */
    public Visualizer() {
        super(new BorderLayout());
        setMinimumSize(Constants.MINIMUM_VISUALIZER_SIZE);
        renderables = new ArrayList<IRenderable>();
    }

    /**
     * Adds the passed renderable to the list of renderables to render.
     * @param renderable The IRenderable to render.
     */
    public void render(IRenderable renderable) {
        if(renderable != null && renderable.isValid()) {
            renderables.add(renderable);
            repaint();
        }
    }

    /**
     * Removes the passed renderable from the list of renderables to render.
     * @param renderable The IRenderable to stop rendering.
     */
    public void stopRendering(IRenderable renderable) {
        if(renderables.contains(renderable)) {
            renderables.remove(renderable);
            repaint();
        }
    }

    /**
     * Removes all renderables from the list of renderables to render.
     */
    public void stopRenderingAll() {
        renderables.clear();
    }

    /**
     * Returns the running list of renderables that the Visualizer is rendering.
     * @return All renderables currently being rendered.
     */
    public ArrayList<IRenderable> getRenderables() {
        return renderables;
    }

    /**
     * Paints the object. This method called by Swing internal methods.
     */
    public void paint(Graphics g) {
        //fill in the background
        g.setColor(Constants.SECONDARY_BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Constants.PRIMARY_TEXT);

        if(renderables.size() == 0) {
            byte[] message = new String("Nothing to Draw!").getBytes();
            g.drawBytes(message, 0, message.length, 25, 25);
            return;
        }

        //figure out scale of rendering (pixels per unit)
        Rectangle bounds = getBounds(renderables);
        double xScale = (getWidth() - (3 * Constants.DEFAULT_HORIZONTAL_MARGIN)) / bounds.getWidth();
        double yScale = (getHeight() - (3 * Constants.DEFAULT_VERTICAL_MARGIN)) / bounds.getHeight();
        double finalScale =  (xScale < yScale ? xScale : yScale); //the final scale is the smaller of the x and y scales

        for(int i=0; i<renderables.size(); i++) {
            IRenderable renderable = renderables.get(i);
            if(renderable.isVisible()) {
                Point2D[] points = renderable.getPoints();
                g.setColor(renderable.getColor());

                if(points.length > 1) {
                    for(int k=1; k<points.length; k++) {
                        Point2D p1 = points[k-1];
                        Point2D p2 = points[k];

                        if(p1 == null || p2 == null) {
                            continue;
                        }
                        
                        //define locations of points in image space
                        double 
                            p1ImageX = ((p1.getX() - bounds.getX()) * finalScale) + Constants.DEFAULT_HORIZONTAL_MARGIN,
                            p1ImageY = ((p1.getY() - bounds.getY()) * finalScale) + Constants.DEFAULT_VERTICAL_MARGIN,
                            p2ImageX = ((p2.getX() - bounds.getX()) * finalScale) + Constants.DEFAULT_HORIZONTAL_MARGIN,
                            p2ImageY = ((p2.getY() - bounds.getY()) * finalScale) + Constants.DEFAULT_VERTICAL_MARGIN;

                        g.drawLine((int) p1ImageX, (int) p1ImageY, (int) p2ImageX, (int) p2ImageY);

                        //highlight points with a filled circle
                        int pointHighLightX = (int) p2ImageX - (Constants.POINT_MARK_DIAMETER / 2);
                        int pointHighLightY = (int) p2ImageY - (Constants.POINT_MARK_DIAMETER / 2);
                        g.fillOval(pointHighLightX, pointHighLightY, Constants.POINT_MARK_DIAMETER, Constants.POINT_MARK_DIAMETER);
                    }

                    //mark start point with special dot
                    g.setColor(Constants.START_POINT_COLOR);
                    int startMarkX = (int) ((points[0].getX() - bounds.getX()) * finalScale) + Constants.DEFAULT_HORIZONTAL_MARGIN;
                    int startMarkY = (int) ((points[0].getY() - bounds.getY()) * finalScale) + Constants.DEFAULT_VERTICAL_MARGIN;
                    int endpointRadius = Constants.ENDPOINT_MARK_DIAMETER / 2;
                    startMarkX -= endpointRadius;
                    startMarkY -= endpointRadius;
                    g.fillOval(startMarkX, startMarkY, Constants.ENDPOINT_MARK_DIAMETER, Constants.ENDPOINT_MARK_DIAMETER);

                    //mark end point with special dot
                    g.setColor(Constants.END_POINT_COLOR);
                    int endMarkX = (int) ((points[points.length - 1].getX() - bounds.getX()) * finalScale) + Constants.DEFAULT_HORIZONTAL_MARGIN;
                    int endMarkY = (int) ((points[points.length - 1].getY() - bounds.getY()) * finalScale) + Constants.DEFAULT_VERTICAL_MARGIN;
                    endMarkX -= endpointRadius;
                    endMarkY -= endpointRadius;
                    g.fillOval(endMarkX, endMarkY, Constants.ENDPOINT_MARK_DIAMETER, Constants.ENDPOINT_MARK_DIAMETER);
                } else if(points.length == 1) {
                    int 
                        standalonePointRadius = Constants.STANDALONE_POINT_DIAMETER / 2,
                        pointX = (int) ((points[0].getX() - bounds.getX()) * finalScale) + Constants.DEFAULT_HORIZONTAL_MARGIN,
                        pointY = (int) ((points[0].getY() - bounds.getY()) * finalScale) + Constants.DEFAULT_VERTICAL_MARGIN;

                    pointX -= standalonePointRadius;
                    pointY -= standalonePointRadius;
                    
                    g.fillOval(pointX, pointY, Constants.STANDALONE_POINT_DIAMETER, Constants.STANDALONE_POINT_DIAMETER);
                }
            }
        }
    }

    /**
     * Sets whether or not a Path is visible.
     * @param path The path to set visibility of.
     * @param visible True if the path is visible, false otherwise.
     */
    public void setPathVisible(IRenderable path, boolean visible) {
        for(int i=0; i<renderables.size(); i++) {
            if(renderables.get(i).equals(path)) {
                renderables.get(i).setVisible(visible);
                repaint();
            }
        }
    }

    /**
     * Returns a Rectangle that denotes the bounds of the passed paths
     * @param renderables A list of paths.
     * @return A bounding rectangle that fits all points in paths.
     */
    private Rectangle getBounds(ArrayList<IRenderable> renderables) {
        double
            smallestX = Double.MAX_VALUE,
            greatestX = Double.MIN_VALUE,
            smallestY = Double.MAX_VALUE,
            greatestY = Double.MIN_VALUE;

        for(int i=0; i<renderables.size(); i++) {
            if(renderables.get(i).isVisible()) {
                Point2D[] points = renderables.get(i).getPoints();
                if(points == null) {
                    continue;
                }

                for(int k=0; k<points.length; k++) {
                    if(points[k] == null) {
                        continue;
                    }

                    double
                        x = points[k].getX(),
                        y = points[k].getY();

                    if(x < smallestX) {
                        smallestX = x;
                    }

                    if(x > greatestX) {
                        greatestX = x;
                    }

                    if(y < smallestY) {
                        smallestY = y;
                    }

                    if(y > greatestY) {
                        greatestY = y;
                    }
                }
            }
        }

        //figure out the width and height and return
        double 
            width  = greatestX - smallestX,
            height = greatestY - smallestY;

        return new Rectangle(smallestX, smallestY, width, height);
    }
}
