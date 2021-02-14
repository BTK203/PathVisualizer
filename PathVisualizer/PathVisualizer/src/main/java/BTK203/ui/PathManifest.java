package BTK203.ui;

import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import BTK203.Constants;
import BTK203.util.Path;
import BTK203.util.Util;

/**
 * A graphical manifest of all paths currently on display.
 */
public class PathManifest extends JPanel {
    private static final long serialVersionUID = 1L;

    private ArrayList<PathWidget> widgets;

    /**
     * Creates a new PathManifest.
     */
    public PathManifest() {
        super();
        setPreferredSize(Constants.DEFAULT_MANIFEST_SIZE);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        widgets = new ArrayList<PathWidget>();

        JLabel header = Util.boldify(new JLabel("Paths"));
        add(header);
    }

    /**
     * Adds a PathWidget to the list being displayed.
     * @param widget The widget to add.
     */
    public void addWidget(PathWidget widget) {
        widgets.add(widget);
        add(widget);

        validate(); //this forces an immediate repainting of the window. Without this line, the new widget does not render until the window is manual resized.
    }

    /**
     * removes a PathWidget from the list being displayed
     * @param widget The widget to remove.
     */
    public void removeWidget(PathWidget widget) {
        if(widgets.contains(widget)) {
            widgets.remove(widget);
            remove(widget);
            repaint();
        }
    }

    /**
     * Removes a PathWidget by name.
     * @param name The name of the widget to delete.
     */
    public void removeWidgetByPath(Path path) {
        for(int i=0; i<widgets.size(); i++) {
            if(widgets.get(i).getPath().equals(path)) {
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
}
