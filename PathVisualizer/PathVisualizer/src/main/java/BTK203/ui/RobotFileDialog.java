package BTK203.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;

import BTK203.App;
import BTK203.Constants;
import BTK203.enumeration.FileOperation;
import BTK203.util.RobotFileSystem;
import BTK203.util.Util;

public class RobotFileDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTextField
        nameBox,
        directoryBox;
    
    private JPanel
        contents,
        fileListPanel;

    private ArrayList<JPanel> fileOptions;
    private long lastClickTime;
    private String result; //the path of the file to pull from / push to the robot
    private RobotFileSystem fileSystem;

    /**
     * Creates a new RobotFileDialog.
     * @param operation The mode to open files with. Can either be RobotFileDialog.SAVING or RobotFileDialog.READING
     */
    public RobotFileDialog(JFrame parent, FileOperation operation, String startingDirectory) {
        super(parent, true);
        setSize(Constants.DEFAULT_SAVE_DIALOG_SIZE);
        setTitle(operation == FileOperation.SAVE? "Save To Robot" : "Load From Robot");
        fileSystem = new RobotFileSystem(startingDirectory);

        fileOptions   = new ArrayList<JPanel>();
        lastClickTime = System.currentTimeMillis();

        contents = new JPanel();
            contents.setLayout(new BorderLayout());
            contents.setBorder(Util.generateHorizontalMargin());

            //header area where it says "Load / Save file" and prompt a directory
            JPanel header = new JPanel();
                header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
                header.setBorder(Util.generateVerticalMargin());

                //header label
                header.add(Util.boldify(new JLabel(operation == FileOperation.SAVE ? "Save File" : "Load File")));

                //area where you put in a directory for it to search
                JPanel directoryPanel = new JPanel();
                    directoryPanel.setLayout(new BoxLayout(directoryPanel, BoxLayout.X_AXIS));
                    directoryPanel.add(new JLabel("Directory: "));

                    directoryBox = new JTextField(startingDirectory);
                        directoryBox.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                populateList(directoryBox.getText());
                            }
                        });

                        directoryPanel.add(directoryBox);

                    Icon backIcon = new ImageIcon(Constants.BACK_ICON);
                    JButton backButton = new JButton(backIcon);
                        backButton.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                String currentDir = fileSystem.getCurrentDirectory();
                                if(currentDir.contains("/")) {
                                    String newDir = currentDir.substring(0, currentDir.lastIndexOf("/"));
                                    populateList(newDir);
                                }
                            }
                        });
                        backButton.setPreferredSize(new Dimension(backIcon.getIconWidth() + 5, backIcon.getIconHeight() + 5));
                        directoryPanel.add(backButton);

                    header.add(directoryPanel);
                contents.add(header, BorderLayout.NORTH);
            
            //Broswer where all of the files show up
            fileListPanel = new JPanel();
                generateFileList(new String[0]); //generate an empty area for the files
                contents.add(fileListPanel, BorderLayout.CENTER);

            //populate the file browser. This will generate the UI elements and place them where they need to go
            populateList(startingDirectory);
            
            //Area with the file name box and the Save / Load and Cancel buttons
            JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
                buttonPanel.setBorder(Util.generateVerticalMargin());
                buttonPanel.add(new JLabel("File Name: "));

                nameBox = new JTextField(Constants.DEFAULT_ROBOT_FILE_NAME);
                    buttonPanel.add(nameBox);

                JButton continueButton = new JButton(operation == FileOperation.SAVE ? "Save" : "Load");
                    continueButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            complete(directoryBox.getText() + "/" + nameBox.getText());
                        }
                    });

                    buttonPanel.add(continueButton);

                JButton cancelButton = new JButton("Cancel");
                    cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            complete(null);
                        }
                    });

                    buttonPanel.add(cancelButton);
                contents.add(buttonPanel, BorderLayout.SOUTH);
            setContentPane(contents);
    }

    /**
     * Runs the dialog and returns the user-selected path.
     * @return The user-selected robot file path, or null if the dialog was canceled.
     */
    public String run() {
        setVisible(true);
        return result;
    }

    /**
     * Completes the dialog, closing it and unblocking other UI processes.
     * @param result The result of the dialog to return to the parent.
     */
    private void complete(String result) {
        this.result = result;
        dispose();
    }

    /**
     * Populates fileList by making contact with the robot and querying it's path files.
     * This method should be run in a separate thread.
     */
    private void populateList(String directory) {
        directoryBox.setText(directory);

        if(!App.getManager().getSocketHelper().getInitalizedAndConnected()) {
            JOptionPane.showMessageDialog(this, "Please connect the Robot to see its Files.");
            return;
        }

        new Thread(
            () -> {
                fileSystem.setCurrentDirectory(directory);
                String[] paths = fileSystem.onlyNames(fileSystem.getPaths()); //returns only the names of the files in the directory.
                generateFileList(paths);
            }
        ).start();
    }

    /**
     * Generates a graphical browser showing the given paths.
     * @param paths An array of file paths.
     */
    private void generateFileList(String[] paths) {
        contents.remove(fileListPanel);
        fileListPanel = new JPanel();
            fileListPanel.setLayout(new BorderLayout());
            fileListPanel.setBackground(Constants.WHITE);
            fileListPanel.setBorder(BorderFactory.createLineBorder(Constants.BLACK));

        fileOptions.clear();

        JPanel scrollPanePanel = new JPanel();
        scrollPanePanel.setBackground(Constants.WHITE);
        scrollPanePanel.setLayout(new BoxLayout(scrollPanePanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(scrollPanePanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBackground(Constants.WHITE);
        scrollPane.setBounds(fileListPanel.getBounds());

        //path format: [file path]:[dir/file]
        for(String path : paths) {
            if(!path.contains(":")) {
                continue;
            }

            boolean isDirectory = path.endsWith(Constants.ROBOTBROWSER_DIRECTORY_SUFFIX);
            String pathName = path.substring(0, path.indexOf(":"));

            JPanel panel = new JPanel();
                panel.setBackground(Constants.WHITE);
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.setAlignmentX(LEFT_ALIGNMENT);
                panel.setBorder(Util.generateHorizontalMargin());

                JLabel icon = new JLabel(new ImageIcon(isDirectory ? Constants.DIRECTORY_ICON : Constants.FILE_ICON));
                    icon.setPreferredSize(new Dimension(20, 20));
                    panel.add(icon);

                JLabel newLabel = new JLabel(pathName);
                    panel.add(newLabel);

                //add mouse hover and click events 
                panel.addMouseListener(new MouseListener() {
                    public void mouseClicked(MouseEvent e) {
                        //reset all panels to white so there wont be any other dark blue ones
                        for(int i=0; i<fileOptions.size(); i++) {
                            fileOptions.get(i).setBackground(Constants.WHITE);
                        }
                        
                        panel.setBackground(Constants.SELECT_BLUE);

                        //decide if it's a doubleclick
                        long currentTime = System.currentTimeMillis();
                        if(currentTime - lastClickTime < Constants.DOUBLE_CLICK_TIME) {
                            if(isDirectory) {
                                populateList(fileSystem.getCurrentDirectory() + "/" + pathName);
                            } else {
                                complete(directoryBox.getText() + "/" + nameBox.getText());
                            }

                            lastClickTime = 0; //reset the double click by setting to 0
                        } else {
                            lastClickTime = currentTime;
                        }

                        //set value of name box even on a single click
                        if(!isDirectory) { //only if its a file tho
                            nameBox.setText(pathName);
                        }
                    }

                    public void mouseEntered(MouseEvent e) {
                        if(!panel.getBackground().equals(Constants.SELECT_BLUE)) { //if panel is not selected...
                            panel.setBackground(Constants.HOVER_BLUE);
                        }
                    }

                    public void mouseExited(MouseEvent e) {
                        if(!panel.getBackground().equals(Constants.SELECT_BLUE)) { //if panel is not selected...
                            panel.setBackground(Constants.WHITE);
                        }
                    }

                    public void mousePressed(MouseEvent e) {
                    }

                    public void mouseReleased(MouseEvent e) {
                    }
                });

                scrollPanePanel.add(panel);
                fileOptions.add(panel);
                
        }

        fileListPanel.add(scrollPane, BorderLayout.CENTER);
        contents.add(fileListPanel, BorderLayout.CENTER);

        //forces a repaint of the entire dialog so that the new widgets show up on screen
        validate();
    }
}
