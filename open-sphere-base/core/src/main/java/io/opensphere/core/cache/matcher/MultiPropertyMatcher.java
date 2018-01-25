package io.opensphere.core.cache.matcher;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * A property matcher that matches multiple serializable values. If a data model
 * has a property value that matches one of the values in this matcher, its id
 * will be returned by the query. The query results will be ordered by the first
 * multi-property matcher found.
 *
 * @param <T> The type of the property values.
 */
public final class MultiPropertyMatcher<T extends Serializable> extends AbstractPropertyMatcher<T>
{
    /** The values to match. */
    private final Collection<? extends T> myOperands;

    /**
     * Construct the property matcher.
     *
     * @param propertyDescriptor The property descriptor.
     * @param operands The values to be matched.
     */
    public MultiPropertyMatcher(PropertyDescriptor<T> propertyDescriptor, Collection<? extends T> operands)
    {
        super(propertyDescriptor, null);
        myOperands = Collections.unmodifiableSet(new LinkedHashSet<T>(operands));
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
        MultiPropertyMatcher<?> other = (MultiPropertyMatcher<?>)obj;
        return Objects.equals(myOperands, other.myOperands);
    }

    @Override
    public T getOperand()
    {
        throw new UnsupportedOperationException(
                "getOperand() is not supported for " + MultiPropertyMatcher.class.getSimpleName());
    }

    /**
     * The values to be matched.
     *
     * @return The value to be matched.
     */
    public Collection<? extends T> getOperands()
    {
        return myOperands;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myOperands == null ? 0 : myOperands.hashCode());
        return result;
    }

    @Override
    public boolean matches(Object operand)
    {
        throw new UnsupportedOperationException("matches() is not supported for " + MultiPropertyMatcher.class.getSimpleName());
    }
}
