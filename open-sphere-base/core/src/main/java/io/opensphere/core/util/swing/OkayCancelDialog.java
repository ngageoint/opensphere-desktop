package io.opensphere.core.util.swing;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * A simple reusable dialog that prompts the user to okay or cancel an action.
 */
public abstract class OkayCancelDialog extends JDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The OK button. */
    private JButton myOkButton;

    /** Indicates the user's selected option. */
    private int myUserOption = JOptionPane.CLOSED_OPTION;

    /**
     * Default constructor.
     */
    protected OkayCancelDialog()
    {
    }

    /**
     * Constructor that takes an owner.
     *
     * @param owner The <code>Window</code> from which the dialog is displayed
     *            or <code>null</code> if this dialog has no owner.
     */
    protected OkayCancelDialog(Window owner)
    {
        super(owner);
    }

    /**
     * Accessor for the OK button.
     *
     * @return The OK button.
     */
    public JButton getOkButton()
    {
        return myOkButton;
    }

    /**
     * Indicates if the user selected the OK option.
     *
     * @return <code>true</code> If the user selected the OK option.
     */
    public boolean wasOKSelected()
    {
        return myUserOption == JOptionPane.OK_OPTION;
    }

    /**
     * Creates the OK/Cancel button panel.
     *
     * @return The OK/Cancel button panel.
     */
    protected final Container getOkayCancelPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(2, 3, 2, 3);
        gbc.weightx = 1.0;
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(Box.createHorizontalGlue());

        myOkButton = new JButton("OK");
        buttonBox.add(myOkButton);

        JButton cancelButton = new JButton("Cancel");
        buttonBox.add(Box.createHorizontalStrut(4));
        buttonBox.add(cancelButton);

        panel.add(buttonBox, gbc);

        myOkButton.addActionListener(e ->
        {
            myUserOption = JOptionPane.OK_OPTION;
            setVisible(false);
            dispose();
        });
        cancelButton.addActionListener(e ->
        {
            myUserOption = JOptionPane.CANCEL_OPTION;
            setVisible(false);
            dispose();
        });

        // set the user option when the close button is selected
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                myUserOption = JOptionPane.CLOSED_OPTION;
            }
        });

        // press the OK button when the enter key is pressed
        getRootPane().registerKeyboardAction(e -> myOkButton.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        return panel;
    }
}
