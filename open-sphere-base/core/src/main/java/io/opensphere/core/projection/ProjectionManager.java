package io.opensphere.core.projection;

import java.util.concurrent.Executor;

/**
 * Manager for the projections in the application.
 */
public class ProjectionManager
{
    /**
     * The current projection. This is synchronized using the ProjectionManager
     * monitor.
     */
    private Projection myCurrentProjection;

    /** Support for projection changes. */
    private final ProjectionChangeSupport myProjectionChangeSupport = new ProjectionChangeSupport();

    /**
     * Get the current projection.
     *
     * @return The current projection.
     */
    public synchronized Projection getCurrentProjection()
    {
        return myCurrentProjection;
    }

    /**
     * Access the projection change support.
     *
     * @return The projection change support.
     */
    public ProjectionChangeSupport getProjectionChangeSupport()
    {
        return myProjectionChangeSupport;
    }

    /**
     * Notify the projection-change listeners that the projection has changed.
     *
     * @param evt An event describing the change to the projection.
     * @param executor Call the projection changed methods using this executor.
     */
    public void notifyProjectionChangeListeners(ProjectionChangedEvent evt, Executor executor)
    {
        myProjectionChangeSupport.notifyProjectionChangeListeners(evt, executor);
    }

    /**
     * Set the current projection.
     *
     * @param currentProjection The new current projection.
     */
    public synchronized void setCurrentProjection(Projection currentProjection)
    {
        myCurrentProjection = currentProjection;
    }
}
