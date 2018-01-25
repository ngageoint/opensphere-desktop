package io.opensphere.core.help;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.help.JHelp;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/** This class is responsible for creating a frame to display help in. */
public class HelpFrame extends JDialog
{
    /** Default serial id. */
    private static final long serialVersionUID = 1L;

    /** The title of this frame. */
    private static final String TITLE = "Help";

    /**
     * Default Constructor.
     *
     * @param help The java help to display.
     */
    public HelpFrame(JHelp help)
    {
        super();
        initialize(help);
    }

    /**
     * Initialize the dialog that will hold help contents.
     *
     * @param help The main java help.
     */
    private void initialize(JHelp help)
    {
        Dimension size = new Dimension(800, 600);
        setSize(size);
        setPreferredSize(size);
        setTitle(TITLE);
        setAlwaysOnTop(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(help, BorderLayout.CENTER);
        setContentPane(panel);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }
}
