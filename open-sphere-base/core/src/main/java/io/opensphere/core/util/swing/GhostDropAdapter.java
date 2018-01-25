package io.opensphere.core.util.swing;

import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The Class GhostDropAdapter.
 */
public class GhostDropAdapter extends MouseAdapter
{
    /** The action. */
    private final String myAction;

    /** The glass pane. */
    private final GhostGlassPane myGlassPane;

    /** The listeners. */
    private final List<GhostDropListener> myListeners;

    /**
     * Instantiates a new ghost drop adapter.
     *
     * @param glassPane the glass pane
     * @param action the action
     */
    public GhostDropAdapter(GhostGlassPane glassPane, String action)
    {
        myGlassPane = glassPane;
        myAction = action;
        myListeners = new ArrayList<>();
    }

    /**
     * Adds the ghost drop listener.
     *
     * @param listener the listener
     */
    public void addGhostDropListener(GhostDropListener listener)
    {
        if (listener != null)
        {
            myListeners.add(listener);
        }
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    public String getAction()
    {
        return myAction;
    }

    /**
     * Gets the glass pane.
     *
     * @return the glass pane
     */
    public GhostGlassPane getGlassPane()
    {
        return myGlassPane;
    }

    /**
     * Removes the ghost drop listener.
     *
     * @param listener the listener
     */
    public void removeGhostDropListener(GhostDropListener listener)
    {
        if (listener != null)
        {
            myListeners.remove(listener);
        }
    }

    /**
     * Fire ghost drop event.
     *
     * @param evt the evt
     */
    protected void fireGhostDropEvent(GhostDropEvent evt)
    {
        Iterator<GhostDropListener> it = myListeners.iterator();
        while (it.hasNext())
        {
            it.next().ghostDropped(evt);
        }
    }
}
