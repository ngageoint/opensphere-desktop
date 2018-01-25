package io.opensphere.core.projection;

import java.util.Collection;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.collections.New;

/**
 * Event indicating the projection has changed.
 */
public class ProjectionChangedEvent
{
    /** The bounds for the change. */
    private final Collection<? extends GeographicBoundingBox> myBounds;

    /**
     * Indicates if the new projection invalidates everything currently
     * on-screen.
     */
    private final boolean myFullClear;

    /** The new projection. */
    private final Projection myProjection;

    /** A snapshot of the current state of the projection. */
    private final Projection myProjectionSnapshot;

    /**
     * Construct the event.
     *
     * @param proj The new projection.
     * @param projSnap A snapshot of the current state of the projection
     * @param fullClear Indicates if the new projection invalidates everything
     *            currently on-screen.
     */
    public ProjectionChangedEvent(Projection proj, Projection projSnap, boolean fullClear)
    {
        myProjection = proj;
        myProjectionSnapshot = projSnap;
        myFullClear = fullClear;
        myBounds = null;
    }

    /**
     * Construct the event.
     *
     * @param proj The active projection.
     * @param projSnap A snapshot of the current state of the projection
     * @param bounds The bounds for the change.
     */
    public ProjectionChangedEvent(Projection proj, Projection projSnap, Collection<GeographicBoundingBox> bounds)
    {
        myProjection = proj;
        myProjectionSnapshot = projSnap;
        myFullClear = false;
        myBounds = New.unmodifiableCollection(bounds);
    }

    /**
     * Get the bounds.
     *
     * @return the bounds
     */
    public Collection<? extends GeographicBoundingBox> getBounds()
    {
        return myBounds;
    }

    /**
     * Get the new projection.
     *
     * @return The new projection.
     */
    public Projection getProjection()
    {
        return myProjection;
    }

    /**
     * Get the projectionSnapshot.
     *
     * @return the projectionSnapshot
     */
    public Projection getProjectionSnapshot()
    {
        return myProjectionSnapshot;
    }

    /**
     * Get if this event signals a full screen clear.
     *
     * @return <code>true</code> if the screen should be cleared.
     */
    public boolean isFullClear()
    {
        return myFullClear;
    }
}
