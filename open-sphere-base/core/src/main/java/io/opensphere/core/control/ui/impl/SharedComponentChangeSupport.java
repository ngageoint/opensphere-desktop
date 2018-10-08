package io.opensphere.core.control.ui.impl;

import java.util.concurrent.Executor;

import io.opensphere.core.control.ui.SharedComponentListener;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.WeakChangeSupport;

/**
 * Support for notify interested parties when a shared component has been added
 * or removed.
 */
public class SharedComponentChangeSupport
{
    /** The change support helper. */
    private final ChangeSupport<SharedComponentListener> myChangeSupport = new WeakChangeSupport<>();

    /**
     * Add a listener for shared component changes.
     *
     * @param listener The listener.
     */
    public void addListener(SharedComponentListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Remove a listener from receiving shared component changes.
     *
     * @param listener The listener.
     */
    public void removeListener(SharedComponentListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Notify the shared component listeners that a change has been made.
     *
     * @param name The name of the shared component.
     * @param type The type of update (component added or removed).
     * @param executor The optional executor.
     */
    protected void notifyComponentListeners(final String name, final ComponentChangeType type, Executor executor)
    {
        WeakChangeSupport.Callback<SharedComponentListener> callback = new WeakChangeSupport.Callback<>()
        {
            @Override
            public void notify(SharedComponentListener listener)
            {
                if (type == ComponentChangeType.ADDED)
                {
                    listener.componentAdded(name);
                }
                else if (type == ComponentChangeType.REMOVED)
                {
                    listener.componentRemoved(name);
                }
            }
        };
        myChangeSupport.notifyListeners(callback, executor);
    }
}
