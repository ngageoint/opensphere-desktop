package io.opensphere.core.cache.matcher;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.Accumulator;
import io.opensphere.core.model.time.ExtentAccumulator;
import io.opensphere.core.model.time.TimeSpan;

/**
 * A time span property matcher.
 */
public class TimeSpanMatcher extends AbstractPropertyMatcher<TimeSpan> implements IntervalPropertyMatcher<TimeSpan>
{
    /**
     * Construct the matcher.
     *
     * @param propertyName The name of the time span property.
     * @param timeSpan The time span.
     */
    public TimeSpanMatcher(String propertyName, TimeSpan timeSpan)
    {
        super(new PropertyDescriptor<TimeSpan>(propertyName, TimeSpan.class), timeSpan);
    }

    @Override
    public Accumulator<TimeSpan> getAccumulator()
    {
        return new ExtentAccumulator();
    }

    @Override
    public IntervalPropertyMatcher<TimeSpan> getGroupMatcher()
    {
        return this;
    }

    @Override
    public TimeSpan getMinimumOverlapInterval()
    {
        return getOperand();
    }

    @Override
    public TimeSpan getSimplifiedBounds()
    {
        return getOperand();
    }

    @Override
    public boolean isIndefinite(Object value)
    {
        return !((TimeSpan)value).isBounded();
    }

    @Override
    public boolean matches(Object operand)
    {
        if (operand instanceof TimeSpan)
        {
            TimeSpan ts = (TimeSpan)operand;
            return ts.overlaps(getOperand());
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean overlaps(IntervalPropertyMatcher<?> other)
    {
        return other.getPropertyDescriptor().equals(getPropertyDescriptor())
                && getOperand().overlaps((TimeSpan)other.getOperand());
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(getClass().getSimpleName()).append("[OVERLAPS ").append(getOperand()).append(']')
                .toString();
    }
}
