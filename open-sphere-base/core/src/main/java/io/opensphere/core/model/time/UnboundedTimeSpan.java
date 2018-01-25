package io.opensphere.core.model.time;

import io.opensphere.core.units.duration.Duration;

/** Base class for unbounded time spans. */
abstract class UnboundedTimeSpan extends TimeSpan
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    @Override
    public Duration getDuration() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Cannot get duration of unbounded time span.");
    }

    @Override
    public long getDurationMs() throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("Cannot get duration of unbounded time span.");
    }

    @Override
    public boolean isBounded()
    {
        return false;
    }

    @Override
    public boolean isInstantaneous()
    {
        return false;
    }

    @Override
    public boolean isZero()
    {
        return false;
    }
}
