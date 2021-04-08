package BTK203.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import BTK203.Constants;
import BTK203.util.IRenderable;
import BTK203.util.Util;

/**
 * A graphical manifest of all paths currently on display.
 */
public class PathManifest extends JPanel {
    private static final long serialVersionUID = 1L;

    private ArrayList<RenderableWidget> widgets;

    /**
     * Creates a new PathManifest.
     */
    public PathManifest() {
        super();
        setPreferredSize(Constants.DEFAULT_MANIFEST_SIZE);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        widgets = new ArrayList<RenderableWidget>();
    }

    /**
     * Adds a PathWidget to the list being displayed.
     * @param widget The widget to add.
     */
    public void addWidget(RenderableWidget widget) {
        widgets.add(widget);
        add(widget);
        validate(); //this forces an immediate repainting of the window. Without this line, the new widget does not render until the window is manually resized.
        resolveSize();
    }

    /**
     * removes a PathWidget from the list being displayed
     * @param widget The widget to remove.
     */
    public void removeWidget(RenderableWidget widget) {
        if(widgets.contains(widget)) {
            widgets.remove(widget);
            remove(widget);
            resolveSize();
        }
    }

    /**
     * Removes a PathWidget by name.
     * @param name The name of the widget to delete.
     */
    public void removeWidgetByPath(IRenderable path) {
        for(int i=0; i<widgets.size(); i++) {
            if(widgets.get(i).getRenderable().equals(path)) {
                removeWidget(widgets.get(i));
            }
        }
    }

    /**
     * Queries the existence of a widget by its name.
     * @param name The name of the widget to check.
     * @return true if the widget exists, false otherwise.
     */
    public boolean widgetExists(String name) {
        for(int i=0; i<widgets.size(); i++) {
            if(widgets.get(i).getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns an array containing the names of every IRenderable on the manifest.
     * @return Names of all renderables on the manifest.
     */
    public String[] getWidgetNames() {
        String[] array = new String[widgets.size()];
        for(int i=0; i<widgets.size(); i++) {
            array[i] = widgets.get(i).getName();
        }

        return array;
    }

    /**
     * Sets the size of the widget to be big enough for all widgets to fit comfortably.
     */
    private void resolveSize() {
        //find the width of the widest widget so we know what to set the manifest size to.
        int biggestWidth = Integer.MIN_VALUE;
        for(int i=0; i<widgets.size(); i++) {
            if(widgets.get(i).getWidth() > biggestWidth) {
                biggestWidth = widgets.get(i).getWidth();
            }
        }

        int newWidth = biggestWidth + Constants.MANIFEST_MARGIN;
        if(newWidth < 0) {
            newWidth = (int) Constants.DEFAULT_MANIFEST_SIZE.getWidth();
        }

        Dimension newSize = new Dimension(newWidth, getParent().getHeight());
        setPreferredSize(newSize);
        setMaximumSize(newSize);
        setMinimumSize(newSize);

        revalidate();
    }
}
