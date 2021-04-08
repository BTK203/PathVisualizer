package BTK203.ui;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import BTK203.Constants;
import BTK203.util.IRenderable;
import BTK203.util.Util;

/**
 * A dialog that prompts the user to select a path.
 */
public class PathChooser extends JDialog {
    private static final long serialVersionUID = 1L;

    private Visualizer visualizer;
    private ArrayList<IRenderable> optionList;
    private ArrayList<JRadioButton> radioButtons;
    private boolean initalized;

    private IRenderable result;
    
    public PathChooser(JFrame parent, IRenderable[] options, boolean pathsOnly) {
        super(parent, true);
        setTitle("Save");
        setSize(Constants.DEFAULT_SAVE_DIALOG_SIZE);

        initalized = false;
        optionList = new ArrayList<IRenderable>();
        radioButtons = new ArrayList<JRadioButton>();

        // generate a list of options for the user to choose from.
        ArrayList<IRenderable> optionsList = new ArrayList<IRenderable>();
        ArrayList<String> namesList = new ArrayList<String>();
        // populate optionsList with the contents of options, while maybe filtering out
        // non-paths
        for (int i=0; i<options.length; i++) {
            IRenderable option = options[i];
            String name = option.getName();
            if (pathsOnly) {
                if (option.getPoints().length > 1) {
                    optionsList.add(option);
                    namesList.add(name);
                }
            } else {
                optionsList.add(option);
                namesList.add(name);
            }
        }

        // init graphical elements
        JPanel contents = new JPanel();
            contents.setLayout(new BoxLayout(contents, BoxLayout.X_AXIS));

            this.visualizer = new Visualizer();
                contents.add(visualizer);

            JPanel manifest = new JPanel();
                manifest.setLayout(new BorderLayout());

                JPanel optionsPanel = new JPanel();
                    optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
                    
                    //header moment
                    optionsPanel.add(Util.boldify(new JLabel("Save Path or Position")));

                    //if there is nothing to save, say that too
                    if(options.length == 0) {
                        optionsPanel.add(new JLabel("There is nothing to save."));
                    }

                    //assess options and generate a button for each of them
                    ButtonGroup buttonGroup = new ButtonGroup();
                    for(int i=0; i<optionsList.size(); i++) {
                        IRenderable option = optionsList.get(i);
                        String name = namesList.get(i);
                        JRadioButton radioButton = new JRadioButton(name, i == 0); //create new radio button with name that will be selected if i is equal to 0
                        radioButton.addActionListener(new ActionListener() { //if radio button is pressed, the visualizer will update
                            public void actionPerformed(ActionEvent e) {
                                update();
                            }
                        });

                        //configure all of the lists and stuff
                        buttonGroup.add(radioButton);
                        radioButtons.add(radioButton);
                        optionsPanel.add(radioButton);
                        optionList.add(option);
                    }

                    manifest.add(optionsPanel, BorderLayout.NORTH);

                JPanel buttonPanel = new JPanel();
                    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

                    JButton cancelButton = new JButton("Cancel");
                        cancelButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                complete(null);
                            }
                        });

                        buttonPanel.add(cancelButton);

                    JButton saveButton = new JButton("Save");
                        saveButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                complete(getSelectedRenderable());
                            }
                        });
                        
                        buttonPanel.add(saveButton);

                    manifest.add(buttonPanel, BorderLayout.SOUTH);
                contents.add(manifest);

                
            setContentPane(contents);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                complete(null);
            }
        });
        initalized = true;
    }

    /**
     * Updates the dialog.
     * This method ensures that the selected path is rendered by the Visualizer.
     */
    public void update() {
        IRenderable selectedRenderable = getSelectedRenderable();
        visualizer.stopRenderingAll();
        visualizer.render(selectedRenderable);
    }

    /**
     * Runs the dialog and returns the result
     * @return The user-selected IRenderable.
     */
    public IRenderable run() {
        update();
        setVisible(true);
        return result;
    }

    /**
     * Gets the renderable that the user selected with the radio buttons.
     * @return The user's selected renderable.
     */
    private IRenderable getSelectedRenderable() {
        if(initalized && optionList.size() > 0) {
            for(int i=0; i<radioButtons.size(); i++) {
                if(radioButtons.get(i).isSelected()) {
                    return optionList.get(i);
                }
            }
        }

        return null;
    }

    private void complete(IRenderable result) {
        this.result = result;
        dispose();
    }
}
