package io.opensphere.core.viewer;

import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Support for notifying interested parties when the view changes.
 */
public class ViewChangeSupport
{
    /** How long to allow for view updates before a warning is issued. */
    private static final long VIEW_UPDATE_THRESHOLD_MS = Long.getLong("opensphere.viewUpdateThresholdMilliseconds", 20).longValue();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ViewChangeSupport.class);

    /** Change support helper. */
    private final ChangeSupport<ViewChangeListener> myChangeSupport = new WeakChangeSupport<>();

    /**
     * Add a listener for view changes.
     *
     * @param listener The listener.
     */
    public void addViewChangeListener(ViewChangeSupport.ViewChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Notify the view-change listeners that the view has changed.
     *
     * @param viewer The new viewer.
     * @param executor Call the view changed methods using this executor.
     * @param type The type of view update.
     */
    public void notifyViewChangeListeners(final Viewer viewer, Executor executor, final ViewChangeSupport.ViewChangeType type)
    {
        final ChangeSupport.Callback<ViewChangeSupport.ViewChangeListener> callback = listener ->
        {
            final long t0 = System.nanoTime();
            listener.viewChanged(viewer, type);
            final long elapsed = System.nanoTime() - t0;
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(StringUtilities.formatTimingMessage("Time to notify view change listener [" + listener + "]: ",
                        elapsed));
            }
            if (elapsed > VIEW_UPDATE_THRESHOLD_MS * Constants.NANO_PER_UNIT / Constants.MILLI_PER_UNIT)
            {
                LOGGER.warn(StringUtilities.formatTimingMessage("Time to notify view change listener [" + listener
                        + "] over threshold of " + VIEW_UPDATE_THRESHOLD_MS + " ms: ", elapsed));
            }
        };
        myChangeSupport.notifyListeners(callback, executor);
    }

    /**
     * Remove a view-change listener.
     *
     * @param listener The listener to be removed.
     */
    public void removeViewChangeListener(ViewChangeSupport.ViewChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Creates a service that can be used to add/remove the given listener.
     *
     * @param listener the listener
     * @return the service
     */
    public Service getViewChangeListenerService(final ViewChangeSupport.ViewChangeListener listener)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                addViewChangeListener(listener);
            }

            @Override
            public void close()
            {
                removeViewChangeListener(listener);
            }
        };
    }

    /**
     * Listener interface for view changes.
     */
    @FunctionalInterface
    public interface ViewChangeListener
    {
        /**
         * Method called when the view changes.
         *
         * @param viewer The new viewer.
         * @param type The type of view update.
         */
        void viewChanged(Viewer viewer, ViewChangeSupport.ViewChangeType type);
    }

    /**
     * Different types of viewer updates.
     */
    public enum ViewChangeType
    {
        /** The viewer is being replaced. */
        NEW_VIEWER,

        /** The viewer has changed. */
        VIEW_CHANGE,

        /** The window size has changed. */
        WINDOW_RESIZE
    }
}
