package io.opensphere.core.cache.accessor;

import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.TimeSpanMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Accessor for time span property values.
 *
 * @param <S> The type of object that provides the property values.
 */
public abstract class TimeSpanAccessor<S> extends AbstractIntervalPropertyAccessor<S, TimeSpan>
implements PersistentPropertyAccessor<S, TimeSpan>
{
    /** The property descriptor. */
    public static final PropertyDescriptor<TimeSpan> PROPERTY_DESCRIPTOR;

    /** The standard name of the time span property. */
    public static final String TIME_PROPERTY_NAME = "time";

    static
    {
        PROPERTY_DESCRIPTOR = new PropertyDescriptor<>(TIME_PROPERTY_NAME, TimeSpan.class);
    }

    /**
     * Construct the time span accessor.
     *
     * @param extent A time span that comprises all of the spans provided by
     *            this accessor.
     */
    public TimeSpanAccessor(TimeSpan extent)
    {
        super(extent);
    }

    @Override
    public IntervalPropertyMatcher<?> createMatcher()
    {
        return new TimeSpanMatcher(TIME_PROPERTY_NAME, getExtent());
    }

    @Override
    public PropertyDescriptor<TimeSpan> getPropertyDescriptor()
    {
        return PROPERTY_DESCRIPTOR;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(TimeSpanAccessor.class.getSimpleName()).append('[').append(getPropertyDescriptor())
                .append(']').toString();
    }
}
