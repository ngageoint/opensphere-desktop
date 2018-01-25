package io.opensphere.server.display;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXBusyLabel;

/**
 * Class that displays one of several icons based on the current status. The
 * icons are a Button that triggers a validate action, a "Valid" JLabel, an
 * "Invalid" JLabel, and spinning Busy Label while waiting for status.
 */
public class UrlStatusWidget extends JPanel
{
    /** The serial version ID. */
    private static final long serialVersionUID = 1L;

    /** URL editor states. */
    /** Unknown state. */
    public static final String UNKNOWN = "Unknown";

    /** Unvalidated state. */
    public static final String UNVALIDATED = "Unvalidated";

    /** Valid state. */
    public static final String VALID = "Valid";

    /** Invalid state. */
    public static final String INVALID = "Invalid";

    /** Waiting state. */
    public static final String WAITING = "Waiting";

    /** GUI Components */
    /** Label to display when URL is Invalid. */
    private final JLabel myNotValidLabel = new JLabel(" Not-Valid");

    /** Label to display when URL is Valid. */
    private final JLabel myIsValidLabel = new JLabel(" Valid");

    /** Spinning graphic to display while waiting for response from server. */
    private final JXBusyLabel myValidateBusy = new JXBusyLabel(new Dimension(15, 15));

    /** Button to prompt validation to the server. */
    private final JButton myValidateButton = new JButton("Validate");

    /** The URL's current state. */
    private String myCurrentState = UNKNOWN;

    /** The CardLayout used by this panel. */
    private CardLayout myLayout;

    /**
     * Constructor.
     */
    public UrlStatusWidget()
    {
        initialize();
        myCurrentState = UNVALIDATED;
    }

    /**
     * Adds an <code>ActionListener</code> to the validate button.
     *
     * @param listener the <code>ActionListener</code> to be added
     */
    public void addActionListener(ActionListener listener)
    {
        myValidateButton.addActionListener(listener);
    }

    /**
     * Gets the widget's current state.
     *
     * @return the current state
     */
    public String getState()
    {
        return myCurrentState;
    }

    @Override
    /** Lock/Unlock the validate button to prevent/allow validation. */
    public void setEnabled(boolean enabled)
    {
        myValidateButton.setEnabled(enabled);
    }

    /**
     * Set the URL state and switch to the appropriate Component.
     *
     * @param state The state to switch to
     */
    public void setState(String state)
    {
        if (!state.equals(myCurrentState))
        {
            myCurrentState = state;
            myValidateBusy.setBusy(state.equals(WAITING));
            myLayout.show(this, state);
        }
    }

    /**
     * Perform basic startup, initialization, and layout of the panel and its
     * sub-components.
     */
    private void initialize()
    {
        myNotValidLabel.setFont(myNotValidLabel.getFont().deriveFont(Font.BOLD));
        myNotValidLabel.setForeground(Color.RED);
        myIsValidLabel.setFont(myIsValidLabel.getFont().deriveFont(Font.BOLD));
        myIsValidLabel.setForeground(Color.GREEN);
        myValidateButton.setMargin(new Insets(3, 3, 3, 3));
        myValidateButton.setFocusPainted(false);

        myLayout = new CardLayout();
        setLayout(myLayout);
        this.add(myValidateButton, UNVALIDATED);
        this.add(myIsValidLabel, VALID);
        this.add(myNotValidLabel, INVALID);
        this.add(myValidateBusy, WAITING);
    }
}
