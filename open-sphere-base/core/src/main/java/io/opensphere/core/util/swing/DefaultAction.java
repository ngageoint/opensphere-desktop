package io.opensphere.core.util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

/** An action that delegates to another action listener. */
public class DefaultAction extends AbstractAction
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The action listener. */
    private final ActionListener myActionListener;

    /**
     * Constructor.
     *
     * @param name The name for the action.
     * @param listener The listener.
     */
    public DefaultAction(String name, ActionListener listener)
    {
        super(name);
        myActionListener = listener;
    }

    /**
     * Constructor for use with boolean controls.
     *
     * @param name The name for the action.
     * @param selected If the action is selected.
     * @param listener The listener.
     */
    public DefaultAction(String name, boolean selected, ActionListener listener)
    {
        super(name);
        putValue(Action.SELECTED_KEY, Boolean.valueOf(selected));
        myActionListener = listener;
    }

    /**
     * Constructor.
     *
     * @param name The name for the action.
     * @param icon The icon for the action.
     * @param listener The listener.
     */
    public DefaultAction(String name, Icon icon, ActionListener listener)
    {
        super(name, icon);
        myActionListener = listener;
    }

    /**
     * Constructor.
     *
     * @param name The name for the action.
     * @param icon The icon for the action.
     * @param selected If the action is selected.
     * @param listener The listener.
     */
    public DefaultAction(String name, Icon icon, boolean selected, ActionListener listener)
    {
        super(name, icon);
        putValue(Action.SELECTED_KEY, Boolean.valueOf(selected));
        myActionListener = listener;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        myActionListener.actionPerformed(e);
    }
}
