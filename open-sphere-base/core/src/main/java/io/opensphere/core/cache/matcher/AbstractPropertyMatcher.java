package io.opensphere.core.cache.matcher;

import java.io.Serializable;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * Abstract property matcher that has a property descriptor.
 *
 * @param <T> The type of the property to be matched.
 */
public abstract class AbstractPropertyMatcher<T extends Serializable> implements PropertyMatcher<T>
{
    /** The operand. */
    private final T myOperand;

    /** The property descriptor. */
    private final PropertyDescriptor<T> myPropertyDescriptor;

    /**
     * Constructor.
     *
     * @param propertyDescriptor The property descriptor.
     * @param operand The operand.
     */
    protected AbstractPropertyMatcher(PropertyDescriptor<T> propertyDescriptor, T operand)
    {
        myPropertyDescriptor = propertyDescriptor;
        myOperand = operand;
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
        AbstractPropertyMatcher<?> other = (AbstractPropertyMatcher<?>)obj;
        return EqualsHelper.equals(myPropertyDescriptor, other.myPropertyDescriptor, myOperand, other.myOperand);
    }

    @Override
    public T getOperand()
    {
        return myOperand;
    }

    @Override
    public PropertyDescriptor<T> getPropertyDescriptor()
    {
        return myPropertyDescriptor;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myPropertyDescriptor == null ? 0 : myPropertyDescriptor.hashCode());
        result = prime * result + (myOperand == null ? 0 : myOperand.hashCode());
        return result;
    }

    /**
     * A method to get direct access to the operand. If a sub-class overrides
     * {@link #getOperand()} to make a copy, this method may be used to get at
     * the operand directly.
     *
     * @return The operand.
     */
    protected T getOperandDirect()
    {
        return myOperand;
    }
}
