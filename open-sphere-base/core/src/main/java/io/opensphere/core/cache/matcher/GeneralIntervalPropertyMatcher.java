package io.opensphere.core.cache.matcher;

import java.io.Serializable;
import java.util.Objects;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.Accumulator;

/**
 * General interval matcher for a {@link Serializable}. It is assumed that these
 * values never overlap.
 *
 * @param <T> The operand type.
 */
public class GeneralIntervalPropertyMatcher<T extends Serializable> extends GeneralPropertyMatcher<T>
        implements IntervalPropertyMatcher<T>
{
    /**
     * Constructor.
     *
     * @param propertyDescriptor The descriptor of the property.
     * @param operand The operand.
     */
    public GeneralIntervalPropertyMatcher(PropertyDescriptor<T> propertyDescriptor, T operand)
    {
        super(propertyDescriptor, operand);
    }

    @Override
    public Accumulator<T> getAccumulator()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntervalPropertyMatcher<T> getGroupMatcher()
    {
        return this;
    }

    @Override
    public T getMinimumOverlapInterval()
    {
        return getOperand();
    }

    @Override
    public T getSimplifiedBounds()
    {
        return getOperand();
    }

    @Override
    public boolean isIndefinite(Object value)
    {
        return false;
    }

    @Override
    public boolean matches(Object operand)
    {
        return Objects.equals(getOperand(), operand);
    }

    @Override
    public boolean overlaps(IntervalPropertyMatcher<?> other)
    {
        return matches(other);
    }
}
