package io.opensphere.core.util.swing.input.model;

import io.opensphere.core.util.Service;

/**
 * Listener handle for PropertyChangeListeners.
 */
public class PropertyChangeListenerHandle implements Service
{
    /** Whether to handle the event. */
    private volatile boolean myHandleEvent = true;

    /** The listener. */
    private final PropertyChangeListener myListener;

    /** The observable value. */
    private final AbstractViewModel<?> myModel;

    /**
     * Constructor.
     *
     * @param observable the observable value
     * @param listener the listener
     */
    public PropertyChangeListenerHandle(AbstractViewModel<?> observable, final PropertyChangeListener listener)
    {
        myModel = observable;
        myListener = new PropertyChangeListener()
        {
            @Override
            public void stateChanged(PropertyChangeEvent e)
            {
                if (myHandleEvent)
                {
                    listener.stateChanged(e);
                }
            }
        };
    }

    @Override
    public void close()
    {
        myModel.removePropertyChangeListener(myListener);
    }

    @Override
    public void open()
    {
        myModel.addPropertyChangeListener(myListener);
    }

    /**
     * Causes events to be suppressed until resume is called.
     */
    public void pause()
    {
        myHandleEvent = false;
    }

    /**
     * Causes events to be handled.
     */
    public void resume()
    {
        myHandleEvent = true;
    }
}
