package io.opensphere.core.util.swing;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import io.opensphere.core.util.collections.New;

/**
 * Generic button panel.
 */
public class ButtonPanel extends JPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Button insets that match those of a default JButton. */
    public static final Insets INSETS_DEFAULT = new Insets(5, 14, 5, 14);

    /** Button insets that are moderate. */
    public static final Insets INSETS_MEDIUM = new Insets(4, 12, 4, 12);

    /** Button insets that match those of a JOptionPane. */
    public static final Insets INSETS_JOPTIONPANE = new Insets(2, 8, 2, 8);

    /** OK. */
    public static final String OK = "OK";

    /** Cancel. */
    public static final String CANCEL = "Cancel";

    /** Yes. */
    public static final String YES = "Yes";

    /** No. */
    public static final String NO = "No";

    /** Close. */
    public static final String CLOSE = "Close";

    /** The OK/Cancel options. */
    public static final Collection<String> OK_CANCEL = New.unmodifiableList(OK, CANCEL);

    /** The Yes/No options. */
    public static final Collection<String> YES_NO = New.unmodifiableList(YES, NO);

    /** The button margin. */
    private Insets myButtonMargin = INSETS_MEDIUM;

    /** The selection that was made. */
    private volatile String mySelection;

    /** Map of button label to button. */
    private final Map<String, JButton> myButtonMap = new HashMap<>();

    /**
     * Constructor.
     */
    public ButtonPanel()
    {
        this(OK_CANCEL);
    }

    /**
     * Constructor.
     *
     * @param buttonLabels The button labels
     */
    public ButtonPanel(Collection<String> buttonLabels)
    {
        initialize(buttonLabels);
    }

    /**
     * Constructor.
     *
     * @param buttonLabels The button labels
     */
    public ButtonPanel(String... buttonLabels)
    {
        this(Arrays.asList(buttonLabels));
    }

    /**
     * Adds an <code>ActionListener</code> to the button.
     *
     * @param l the <code>ActionListener</code> to be added
     */
    public void addActionListener(ActionListener l)
    {
        listenerList.add(ActionListener.class, l);
    }

    /**
     * Gets the button for the given button label.
     *
     * @param buttonLabel The button label
     * @return The button, or null
     */
    public JButton getButton(String buttonLabel)
    {
        return myButtonMap.get(buttonLabel);
    }

    /**
     * Getter for selection.
     *
     * @return the selection
     */
    public String getSelection()
    {
        return mySelection;
    }

    /**
     * Setter for buttonMargin.
     *
     * @param buttonMargin the buttonMargin
     */
    public void setButtonMargin(Insets buttonMargin)
    {
        myButtonMargin = buttonMargin;
        for (JButton button : myButtonMap.values())
        {
            button.setMargin(myButtonMargin);
        }
    }

    /**
     * Initializes the GUI.
     *
     * @param buttonLabels The button labels
     */
    private void initialize(Collection<String> buttonLabels)
    {
        setLayout(new GridLayout(1, buttonLabels.size(), 6, 0));
        for (String label : buttonLabels)
        {
            final IconButton button = new IconButton(label);
            button.setFocusPainted(true);
            if (OK.equals(label) || YES.equals(label))
            {
                button.setIcon("/images/check_12x12.png");
            }
            else if (CANCEL.equals(label) || NO.equals(label))
            {
                button.setIcon("/images/cancel_14x14.png");
            }
            button.addActionListener(e ->
            {
                mySelection = e.getActionCommand();
                for (ActionListener listener : getListeners(ActionListener.class))
                {
                    listener.actionPerformed(e);
                }
            });
            button.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusGained(FocusEvent e)
                {
                    JRootPane root = SwingUtilities.getRootPane(button);
                    if (root != null)
                    {
                        root.setDefaultButton(button);
                    }
                }
            });
            add(button);
            myButtonMap.put(label, button);
        }
    }
}
