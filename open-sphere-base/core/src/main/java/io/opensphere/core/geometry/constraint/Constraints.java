package io.opensphere.core.geometry.constraint;

import java.util.Objects;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * Constraints on when a geometry is visible.
 */
public class Constraints implements Cloneable
{
    /**
     * The location constraint.
     */
    private LatLonAlt myLocationConstraint;

    /** A time constraint. */
    private TimeConstraint myTimeConstraint;

    /** A viewer position constraint. */
    private ViewerPositionConstraint myViewerPositionConstraint;

    /**
     * Creates the time only constraint.
     *
     * @param key The key associated with the constraint.
     * @param t The {@link TimeSpan} for the constraint.
     * @return The constraints.
     */
    public static Constraints createTimeOnlyConstraint(Object key, TimeSpan t)
    {
        return new Constraints(TimeConstraint.getTimeConstraint(key, t));
    }

    /**
     * Creates the time only constraint.
     *
     * @param t the {@link TimeSpan} for the constraint
     * @return the constraints
     */
    public static Constraints createTimeOnlyConstraint(TimeSpan t)
    {
        return new Constraints(TimeConstraint.getTimeConstraint(t));
    }

    /**
     * Creates a location only constraint.
     *
     * @param location The location.
     * @return The constraints.
     */
    public static Constraints createLocationOnlyConstraint(LatLonAlt location)
    {
        return new Constraints(null, null, location);
    }

    /**
     * Construct the constraints object with only a time constraint.
     *
     * @param timeConstraint The optional time constraint.
     */
    public Constraints(TimeConstraint timeConstraint)
    {
        this(timeConstraint, null);
    }

    /**
     * Construct the constraints object with a time constraint and a viewer
     * position constraint.
     *
     * @param timeConstraint The optional time constraint.
     * @param viewerPositionConstraint The optional viewer position constraint.
     */
    public Constraints(TimeConstraint timeConstraint, ViewerPositionConstraint viewerPositionConstraint)
    {
        myTimeConstraint = timeConstraint;

        myViewerPositionConstraint = viewerPositionConstraint;
    }

    /**
     * Constructs the constraints object with time constraints position
     * constraints and location constraints.
     *
     * @param timeConstraint The optional time constraint.
     * @param viewerPositionConstraint The optional viewer position constraint.
     * @param locationConstraint The optional location constraint.
     */
    public Constraints(TimeConstraint timeConstraint, ViewerPositionConstraint viewerPositionConstraint,
            LatLonAlt locationConstraint)
    {
        this(timeConstraint, viewerPositionConstraint);
        myLocationConstraint = locationConstraint;
    }

    /**
     * Construct the constraints object with only viewer position constraint.
     *
     * @param viewerPositionConstraint The optional viewer position constraint.
     */
    public Constraints(ViewerPositionConstraint viewerPositionConstraint)
    {
        this(null, viewerPositionConstraint);
    }

    @Override
    public Constraints clone()
    {
        try
        {
            return (Constraints)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        Constraints other = (Constraints)obj;
        return Objects.equals(myTimeConstraint, other.myTimeConstraint)
                && Objects.equals(myViewerPositionConstraint, other.myViewerPositionConstraint);
    }

    /**
     * Gets the location constraint.
     *
     * @return The location constraint.
     */
    public LatLonAlt getLocationConstraint()
    {
        return myLocationConstraint;
    }

    /**
     * Get the time constraint.
     *
     * @return The time constraint.
     */
    public TimeConstraint getTimeConstraint()
    {
        return myTimeConstraint;
    }

    /**
     * Get the viewer position constraint.
     *
     * @return The viewer position constraint.
     */
    public ViewerPositionConstraint getViewerPositionConstraint()
    {
        return myViewerPositionConstraint;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myTimeConstraint == null ? 0 : myTimeConstraint.hashCode());
        result = prime * result + (myViewerPositionConstraint == null ? 0 : myViewerPositionConstraint.hashCode());
        result = prime * result + (myLocationConstraint == null ? 0 : myLocationConstraint.hashCode());
        return result;
    }

    /**
     * Sets the location constraint.
     *
     * @param locationConstraint The location constraint.
     */
    protected void setLocationConstraint(LatLonAlt locationConstraint)
    {
        myLocationConstraint = locationConstraint;
    }

    /**
     * Set the timeConstraint.
     *
     * @param timeConstraint the timeConstraint to set
     */
    protected void setTimeConstraint(TimeConstraint timeConstraint)
    {
        myTimeConstraint = timeConstraint;
    }

    /**
     * Set the viewerPositionConstraint.
     *
     * @param viewerPositionConstraint the viewerPositionConstraint to set
     */
    protected void setViewerPositionConstraint(ViewerPositionConstraint viewerPositionConstraint)
    {
        myViewerPositionConstraint = viewerPositionConstraint;
    }
}
