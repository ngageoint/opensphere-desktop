package io.opensphere.core.viewer.impl;

import java.util.concurrent.Executor;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.viewer.impl.MapContext.DrawEnableListener;

/**
 * Support for notifying interested parties when drawing is enabled or disabled.
 */
public class DrawEnableSupport
{
    /** Change support helper. */
    private final ChangeSupport<DrawEnableListener> myChangeSupport = new WeakChangeSupport<>();

    /** The optional executor to use for notifications. */
    private final Executor myExecutor;

    /**
     * Construct without an executor.
     */
    public DrawEnableSupport()
    {
        myExecutor = null;
    }

    /**
     * Construct with an executor to use for notifications.
     *
     * @param executor The executor.
     */
    public DrawEnableSupport(Executor executor)
    {
        myExecutor = executor;
    }

    /**
     * Add a listener for projection changes.
     *
     * @param listener The listener.
     */
    public void addDrawEnableListener(MapContext.DrawEnableListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Remove a projection-changed listener.
     *
     * @param listener The listener to be removed.
     */
    public void removeDrawEnableListener(MapContext.DrawEnableListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Notify the draw-enable listeners that drawing is enabled or disabled.
     *
     * @param drawEnable <code>true</code> if drawing is enabled.
     */
    protected void notifyDrawEnableListeners(final boolean drawEnable)
    {
        ChangeSupport.Callback<DrawEnableListener> callback = listener -> listener.drawEnabled(drawEnable);
        myChangeSupport.notifyListeners(callback, myExecutor);
    }
}
