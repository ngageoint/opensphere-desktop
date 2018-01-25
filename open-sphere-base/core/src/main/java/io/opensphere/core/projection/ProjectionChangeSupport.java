package io.opensphere.core.projection;

import java.util.concurrent.Executor;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.WeakChangeSupport;

/**
 * Support for notifying interested parties when the projection changes.
 */
public class ProjectionChangeSupport
{
    /** Change support helper. */
    private final ChangeSupport<ProjectionChangeListener> myChangeSupport = new WeakChangeSupport<>();

    /**
     * Add a listener for projection changes.
     *
     * @param listener The listener.
     */
    public void addProjectionChangeListener(ProjectionChangeSupport.ProjectionChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Notify the projection-change listeners that the projection has changed.
     *
     * @param evt An event describing the change to the projection.
     * @param executor Call the projection changed methods using this executor.
     */
    public void notifyProjectionChangeListeners(final ProjectionChangedEvent evt, Executor executor)
    {
        ChangeSupport.Callback<ProjectionChangeListener> callback = new ChangeSupport.Callback<ProjectionChangeListener>()
        {
            @Override
            public void notify(ProjectionChangeListener listener)
            {
                listener.projectionChanged(evt);
            }
        };
        myChangeSupport.notifyListeners(callback, executor);
    }

    /**
     * Remove a projection-changed listener.
     *
     * @param listener The listener to be removed.
     */
    public void removeProjectionChangeListener(ProjectionChangeSupport.ProjectionChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Listener interface for projection changes.
     */
    @FunctionalInterface
    public interface ProjectionChangeListener
    {
        /**
         * Method called when the projection changes.
         *
         * @param evt The event describing the projection change.
         */
        void projectionChanged(ProjectionChangedEvent evt);
    }
}
