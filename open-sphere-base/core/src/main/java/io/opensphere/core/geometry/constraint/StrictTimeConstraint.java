package io.opensphere.core.geometry.constraint;

import net.jcip.annotations.Immutable;

import io.opensphere.core.model.time.TimeSpan;

/**
 * A strict time constraint that requires that the active time span exactly
 * matches (or doesn't match) a certain time span.
 */
@Immutable
public class StrictTimeConstraint extends TimeConstraint
{
    /**
     * Constructor.
     *
     * @param key The key associated with this constraint. The key must be an
     *            immutable object.
     * @param startTime The start time.
     * @param endTime The end time.
     */
    public StrictTimeConstraint(Object key, long startTime, long endTime)
    {
        super(key, startTime, endTime);
    }

    /**
     * Constructor.
     *
     * @param key The key associated with this constraint. The key must be an
     *            immutable object.
     * @param startTime The start time.
     * @param endTime The end time.
     * @param negate If {@code true}, the check is negated.
     */
    public StrictTimeConstraint(Object key, long startTime, long endTime, boolean negate)
    {
        super(key, startTime, endTime, negate, false);
    }

    /**
     * Constructor.
     *
     * @param key The key associated with this constraint. The key must be an
     *            immutable object.
     * @param span The time span that must be covered.
     */
    public StrictTimeConstraint(Object key, TimeSpan span)
    {
        super(key, span, false);
    }

    /**
     * Constructor.
     *
     * @param key The key associated with this constraint. The key must be an
     *            immutable object.
     * @param span The time span that must be covered.
     * @param negate If {@code true}, the check is negated.
     */
    public StrictTimeConstraint(Object key, TimeSpan span, boolean negate)
    {
        super(key, span, negate);
    }

    @Override
    public boolean check(TimeSpan span)
    {
        return isNegative() ^ getTimeSpan().equals(span);
    }
}
