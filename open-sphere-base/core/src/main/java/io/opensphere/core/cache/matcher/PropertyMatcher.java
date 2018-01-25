package io.opensphere.core.cache.matcher;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * An object that knows how to match a property value.
 *
 * @param <T> The type of the property to be matched.
 */
public interface PropertyMatcher<T>
{
    /**
     * Get the property value to be matched.
     *
     * @return The property value.
     */
    T getOperand();

    /**
     * Get the description of the property to be matched.
     *
     * @return The property descriptor.
     */
    PropertyDescriptor<T> getPropertyDescriptor();

    /**
     * Determine if this property matcher matches an operand.
     *
     * @param operand The operand under test.
     * @return If the operand matches.
     */
    boolean matches(Object operand);
}
