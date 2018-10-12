package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import io.opensphere.core.TimeManager;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * This checks geometry constraints against the current environment.
 */
public class ConstraintChecker
{
    /**
     * The optional group time constraint, which, when satisfied, indicates that
     * all the individual constraints are <b>not</b> satisfied.
     */
    private final TimeConstraint myNegativeGroupTimeConstraint;

    /** Flag indicating if the on-screen geometries need to be recalculated. */
    private final AtomicBoolean myOnscreenDirty = new AtomicBoolean();

    /**
     * The optional group time constraint, which, when satisfied, indicates that
     * all the individual constraints are also satisfied.
     */
    private final TimeConstraint myPositiveGroupTimeConstraint;

    /**
     * Enum indicating if all geometries are inside or outside the current
     * active time spans.
     */
    private volatile TimeConstraintStatus myTimeConstraintStatus;

    /**
     * Constructor.
     *
     * @param positiveGroupConstraints If these constraints are satisfied,
     *            individual constraints are all satisfied.
     * @param negativeGroupConstraints If these constraints are satisfied,
     *            individual constraints are all <b>not</b> satisfied.
     */
    public ConstraintChecker(Constraints positiveGroupConstraints, Constraints negativeGroupConstraints)
    {
        myPositiveGroupTimeConstraint = positiveGroupConstraints == null ? null : positiveGroupConstraints.getTimeConstraint();
        myNegativeGroupTimeConstraint = negativeGroupConstraints == null ? null : negativeGroupConstraints.getTimeConstraint();
    }

    /**
     * Check the provided constraints.
     *
     * @param geom The geometry.
     * @param mapContext The map context.
     * @param timeManager The time manager.
     * @return <code>true</code> if the constraints are satisfied.
     */
    public boolean checkConstraints(Geometry geom, MapContext<? extends Viewer> mapContext, TimeManager timeManager)
    {
        if (geom instanceof ConstrainableGeometry)
        {
            Constraints constraints = ((ConstrainableGeometry)geom).getConstraints();
            if (constraints != null)
            {
                if (constraints.getViewerPositionConstraint() != null && mapContext != null)
                {
                    Vector3d viewerPosition = mapContext.getStandardViewer().getPosition().getLocation();
                    if (viewerPosition != null
                            && !constraints.getViewerPositionConstraint().check(viewerPosition, mapContext.getProjection()))
                    {
                        return false;
                    }
                }

                if (timeManager != null && constraints.getTimeConstraint() != null
                        && !constraints.getTimeConstraint().check(timeManager.getPrimaryActiveTimeSpans()))
                {
                    if (constraints.getTimeConstraint().getKey() == null)
                    {
                        return false;
                    }
                    Collection<? extends TimeSpan> secondary = timeManager
                            .getSecondaryActiveTimeSpans(constraints.getTimeConstraint().getKey());
                    if (secondary == null || !constraints.getTimeConstraint().check(secondary))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Check to see if the negative group time constraint is satisfied. If the
     * negative group time constraint is satisfied, it means that none of the
     * individual time constraints are satisfied.
     *
     * @param timeManager The time manager.
     * @return {@code true} if the constraint is satisfied.
     */
    public boolean checkNegativeGroupTimeConstraint(TimeManager timeManager)
    {
        if (myNegativeGroupTimeConstraint == null)
        {
            return false;
        }
        else if (myNegativeGroupTimeConstraint.check(timeManager.getPrimaryActiveTimeSpans()))
        {
            return true;
        }
        else if (myNegativeGroupTimeConstraint.getKey() == null)
        {
            return false;
        }
        else
        {
            Collection<? extends TimeSpan> secondary = timeManager
                    .getSecondaryActiveTimeSpans(myNegativeGroupTimeConstraint.getKey());
            return secondary != null && myNegativeGroupTimeConstraint.check(secondary);
        }
    }

    /**
     * Check to see if the positive group time constraint is satisfied. If the
     * positive group time constraint is satisfied, it means that all of the
     * individual time constraints are also satisfied.
     *
     * @param timeManager The time manager.
     * @return {@code true} if the constraint is satisfied.
     */
    public boolean checkPositiveGroupTimeConstraint(TimeManager timeManager)
    {
        if (myPositiveGroupTimeConstraint == null)
        {
            return false;
        }
        else if (myPositiveGroupTimeConstraint.check(timeManager.getPrimaryActiveTimeSpans()))
        {
            return true;
        }
        else if (myPositiveGroupTimeConstraint.getKey() == null)
        {
            return false;
        }
        else
        {
            Collection<? extends TimeSpan> secondary = timeManager
                    .getSecondaryActiveTimeSpans(myPositiveGroupTimeConstraint.getKey());
            return secondary != null && myPositiveGroupTimeConstraint.check(secondary);
        }
    }

    /**
     * Get the negative group time constraint.
     *
     * @return The constraint.
     */
    public TimeConstraint getNegativeGroupTimeConstraint()
    {
        return myNegativeGroupTimeConstraint;
    }

    /**
     * Get the positive group time constraint.
     *
     * @return The constraint.
     */
    public TimeConstraint getPositiveGroupTimeConstraint()
    {
        return myPositiveGroupTimeConstraint;
    }

    /**
     * Get the current time constraint status.
     *
     * @return The timeConstraintStatus.
     */
    public TimeConstraintStatus getTimeConstraintStatus()
    {
        return myTimeConstraintStatus;
    }

    /**
     * Determine the time constraint status based on the new active time spans.
     *
     * @param timeManager The time manager.
     */
    public void handleTimeSpansChanged(TimeManager timeManager)
    {
        TimeConstraintStatus newStatus;
        if (checkPositiveGroupTimeConstraint(timeManager))
        {
            newStatus = TimeConstraintStatus.ALL_SATISFIED;
        }
        else if (checkNegativeGroupTimeConstraint(timeManager))
        {
            newStatus = TimeConstraintStatus.NONE_SATISFIED;
        }
        else
        {
            newStatus = TimeConstraintStatus.UNKNOWN;
        }

        // If the status is UNKNOWN or used to be UNKNOWN, check the geometries.
        if (Utilities.sameInstance(myTimeConstraintStatus, TimeConstraintStatus.UNKNOWN)
                || Utilities.sameInstance(newStatus, TimeConstraintStatus.UNKNOWN))
        {
            setOnscreenDirty();
        }
        myTimeConstraintStatus = newStatus;
    }

    /**
     * Get if the on-screen dirty flag is set, and set it to {@code false}.
     *
     * @return The flag.
     */
    public boolean pollOnscreenDirty()
    {
        return myOnscreenDirty.getAndSet(false);
    }

    /**
     * Set the time constraint status.
     *
     * @param timeConstraintStatus The new status.
     */
    public void setTimeConstraintStatus(TimeConstraintStatus timeConstraintStatus)
    {
        myTimeConstraintStatus = timeConstraintStatus;
    }

    /**
     * Set the on-screen dirty flag to {@code true}.
     */
    private void setOnscreenDirty()
    {
        myOnscreenDirty.set(true);
    }

    /** Status of the time constraints. */
    protected enum TimeConstraintStatus
    {
        /** All time constraints are satisfied. */
        ALL_SATISFIED,

        /** No time constraints are satisfied. */
        NONE_SATISFIED,

        /** Time constraint satisfaction is unknown. */
        UNKNOWN,
    }
}
