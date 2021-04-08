package BTK203.ui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import BTK203.App;
import BTK203.Constants;
import BTK203.util.IRenderable;
import BTK203.util.Util;

/**
 * A widget that represents an IRenderable.
 */
public class RenderableWidget extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private IRenderable renderable;
    private JLabel nameLabel;
    private JButton
        toggleWidget,
        deleteWidget;

    /**
     * Creates a new PathWidget
     * @param renderable The path that the widget represents
     * @param name The name of the widget
     */
    public RenderableWidget(IRenderable renderable) {
        super();
        this.renderable = renderable;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentX(RIGHT_ALIGNMENT);

        //text to identify the name of the path
        nameLabel = new JLabel(renderable.getName());
        nameLabel.setBorder(Util.generateHorizontalMargin());
        add(nameLabel);

        //color sample panel to help user identify the color of the line
        JPanel colorSample = new JPanel();
        colorSample.setBackground(renderable.getColor());
        colorSample.setMaximumSize(Constants.COLOR_SAMPLE_SIZE);
        colorSample.setAlignmentX(LEFT_ALIGNMENT);
        add(colorSample);

        //toggle widget button
        toggleWidget = new JButton("-");
        toggleWidget.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean pathVisible = !renderable.isVisible();
                App.getManager().setPathVisible(renderable, pathVisible); //set visualizer's path 
                renderable.setVisible(pathVisible); //set our path for reference
                toggleWidget.setText(pathVisible ? "-" : "+");
            }
        });
        add(toggleWidget);

        //delete widget button
        deleteWidget = new JButton("X");
        deleteWidget.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.getManager().deletePath(renderable);
            }
        });
        add(deleteWidget);
    }

    /**
     * Returns the path that this widget represents.
     */
    public IRenderable getRenderable() {
        return renderable;
    }

    /**
     * Returns the name of this widget.
     */
    public String getName() {
        return renderable.getName();
    }

    /**
     * Returns the width of the Widget.
     */
    @Override
    public int getWidth() {
        return nameLabel.getWidth() + Constants.MANIFEST_WIDGET_NONLABEL_WIDTH;
    }
}
