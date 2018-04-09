package io.opensphere.core.geometry.constraint;

import java.util.Collection;
import java.util.Collections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.WeakHashSet;

/**
 * Extension to {@link Constraints} that allows the constraints to be changed.
 */
@SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
public class MutableConstraints extends Constraints
{
    /** Listeners for changes to this constraints object. */
    private Collection<ConstraintsChangedListener> myChangeListeners = Collections
            .synchronizedCollection(new WeakHashSet<ConstraintsChangedListener>());

    /** The original time constraint. */
    private final TimeConstraint myOriginalTimeConstraint;

    /**
     * Construct the constraints object with only a time constraint.
     *
     * @param timeConstraint The optional time constraint.
     */
    public MutableConstraints(TimeConstraint timeConstraint)
    {
        super(timeConstraint);
        myOriginalTimeConstraint = timeConstraint;
    }

    /**
     * Construct the constraints object with a time constraint and a viewer
     * position constraint.
     *
     * @param timeConstraint The optional time constraint.
     * @param viewerPositionConstraint The optional viewer position constraint.
     */
    public MutableConstraints(TimeConstraint timeConstraint, ViewerPositionConstraint viewerPositionConstraint)
    {
        super(timeConstraint, viewerPositionConstraint);
        myOriginalTimeConstraint = timeConstraint;
    }

    /**
     * Construct the constraints object with a time constraint and a viewer
     * position constraint and a location constraint.
     *
     * @param timeConstraint The optional time constraint.
     * @param viewerPositionConstraint The optional viewer position constraint.
     * @param locationConstraint The optional location constraint.
     *
     */
    public MutableConstraints(TimeConstraint timeConstraint, ViewerPositionConstraint viewerPositionConstraint,
            LatLonAlt locationConstraint)
    {
        super(timeConstraint, viewerPositionConstraint, locationConstraint);
        myOriginalTimeConstraint = timeConstraint;
    }

    /**
     * Construct the constraints object with only viewer position constraint.
     *
     * @param viewerPositionConstraint The optional viewer position constraint.
     */
    public MutableConstraints(ViewerPositionConstraint viewerPositionConstraint)
    {
        super(viewerPositionConstraint);
        myOriginalTimeConstraint = null;
    }

    /**
     * Add a listener for changes to my properties.
     *
     * @param listen The listener to add.
     */
    public void addListener(ConstraintsChangedListener listen)
    {
        myChangeListeners.add(listen);
    }

    @Override
    public MutableConstraints clone()
    {
        MutableConstraints clone = (MutableConstraints)super.clone();
        clone.myChangeListeners = Collections.synchronizedCollection(new WeakHashSet<ConstraintsChangedListener>());
        return clone;
    }

    /** Notify listeners when one or more of my properties has changed. */
    public void notifyChanged()
    {
        ConstraintsChangedEvent evt = new ConstraintsChangedEvent(this);
        synchronized (myChangeListeners)
        {
            for (ConstraintsChangedListener listen : myChangeListeners)
            {
                listen.constraintsChanged(evt);
            }
        }
    }

    /**
     * Remove a listener for changes to my properties..
     *
     * @param listen The listener to remove.
     */
    public void removeListener(ConstraintsChangedListener listen)
    {
        myChangeListeners.remove(listen);
    }

    /* (non-Javadoc)
     * 
     * @see
     * io.opensphere.core.geometry.constraint.Constraints#setLocationConstraint(
     * io.opensphere.core.model.LatLonAlt) */
    @Override
    public void setLocationConstraint(LatLonAlt locationConstraint)
    {
        super.setLocationConstraint(locationConstraint);
        notifyChanged();
    }

    @Override
    public void setTimeConstraint(TimeConstraint timeConstraint)
    {
        super.setTimeConstraint(timeConstraint);
        notifyChanged();
    }

    /**
     * Set the viewerPositionConstraint.
     *
     * @param viewerPositionConstraint the viewerPositionConstraint to set
     */
    @Override
    public void setViewerPositionConstraint(ViewerPositionConstraint viewerPositionConstraint)
    {
        super.setViewerPositionConstraint(viewerPositionConstraint);
        notifyChanged();
    }

    /**
     * Resets the time constraint to the original constraint.
     */
    public void resetTimeConstraint()
    {
        setTimeConstraint(myOriginalTimeConstraint);
    }
}
