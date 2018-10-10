package io.opensphere.core.util.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

/**
 * The Class ActionListerJPanel.
 */
public abstract class AbstractActionListenerJPanel extends JPanel
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Action listeners. */
    private final Set<ActionListener> myActionListeners = new HashSet<>();

    /**
     * Adds the action listener.
     *
     * @param lstr the lstr
     */
    public void addActionListener(ActionListener lstr)
    {
        if (lstr != null)
        {
            synchronized (myActionListeners)
            {
                myActionListeners.add(lstr);
            }
        }
    }

    /**
     * Clear action listeners.
     */
    public void clearActionListeners()
    {
        synchronized (myActionListeners)
        {
            myActionListeners.clear();
        }
    }

    /**
     * Fire action performed.
     *
     * @param e the e
     */
    public void fireActionPerformed(final ActionEvent e)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            synchronized (myActionListeners)
            {
                myActionListeners.stream().forEach(a -> a.actionPerformed(e));
            }
        });
    }

    /**
     * Removes the action listener.
     *
     * @param lstr the lstr
     */
    public void removeActionListener(ActionListener lstr)
    {
        if (lstr != null)
        {
            synchronized (myActionListeners)
            {
                myActionListeners.remove(lstr);
            }
        }
    }
}
