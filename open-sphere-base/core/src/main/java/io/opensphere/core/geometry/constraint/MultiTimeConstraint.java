package io.opensphere.core.geometry.constraint;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.jcip.annotations.Immutable;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;

/**
 * A composite time constraint that has a list of child time constraints.
 */
@Immutable
public class MultiTimeConstraint extends TimeConstraint
{
    /** The child time constraints. */
    private final List<? extends TimeConstraint> myChildren;

    /**
     * Construct a time constraint.
     *
     * @param key The key associated with the constraint.
     * @param timeSpan The time span.
     * @param children The child time constraints.
     * @return A time constraint.
     */
    public static synchronized MultiTimeConstraint getTimeConstraint(Object key, TimeSpan timeSpan,
            Collection<? extends TimeConstraint> children)
    {
        return new MultiTimeConstraint(key, timeSpan.isUnboundedStart() ? Long.MIN_VALUE : timeSpan.getStart(),
                timeSpan.isUnboundedEnd() ? Long.MAX_VALUE : timeSpan.getEnd(), children);
    }

    /**
     * Construct a time constraint.
     *
     * @param timeSpan The time span.
     * @param children The child time constraints.
     * @return A time constraint.
     */
    public static synchronized MultiTimeConstraint getTimeConstraint(TimeSpan timeSpan,
            Collection<? extends TimeConstraint> children)
    {
        return getTimeConstraint(null, timeSpan, children);
    }

    /**
     * Construct a time constraint.
     *
     * @param key The key associated with this constraint.
     * @param minTime The early time boundary in milliseconds since Java epoch.
     * @param maxTime The late time boundary in milliseconds since Java epoch.
     * @param children The child time constraints.
     */
    protected MultiTimeConstraint(Object key, long minTime, long maxTime, Collection<? extends TimeConstraint> children)
    {
        super(key, minTime, maxTime);
        myChildren = New.unmodifiableList(children);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        MultiTimeConstraint other = (MultiTimeConstraint)obj;
        return Objects.equals(myChildren, other.myChildren);
    }

    /**
     * Accessor for the children.
     *
     * @return The children.
     */
    public List<? extends TimeConstraint> getChildren()
    {
        return myChildren;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myChildren == null ? 0 : myChildren.hashCode());
        return result;
    }
}
