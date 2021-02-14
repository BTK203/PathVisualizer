package BTK203.ui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import BTK203.App;
import BTK203.Constants;
import BTK203.util.Path;
import BTK203.util.Util;

/**
 * A widget that represents 
 */
public class PathWidget extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private Path path;
    private String name;
    private JButton
        toggleWidget,
        deleteWidget;

    /**
     * Creates a new PathWidget
     * @param path The path that the widget represents
     * @param name The name of the widget
     */
    public PathWidget(Path path, String name) {
        super();
        this.path = path;
        this.name = name;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentX(RIGHT_ALIGNMENT);

        //text to identify the name of the path
        JLabel nameLabel = new JLabel(name);
        nameLabel.setBorder(Util.generateHorizontalMargin());
        add(nameLabel);

        //color sample panel to help user identify the color of the line
        JPanel colorSample = new JPanel();
        colorSample.setBackground(path.getColor());
        colorSample.setMaximumSize(Constants.COLOR_SAMPLE_SIZE);
        colorSample.setAlignmentX(LEFT_ALIGNMENT);
        add(colorSample);

        //toggle widget button
        toggleWidget = new JButton("-");
        toggleWidget.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean pathVisible = !path.isVisible();
                App.getManager().setPathVisible(path, pathVisible);
                path.setVisible(pathVisible);
                toggleWidget.setText(pathVisible ? "-" : "+");
            }
        });
        add(toggleWidget);

        //delete widget button
        deleteWidget = new JButton("X");
        deleteWidget.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.getManager().deletePath(path);
            }
        });
        add(deleteWidget);
    }

    /**
     * Returns the path that this widget represents.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns the name of this widget.
     */
    public String getName() {
        return name;
    }
}
