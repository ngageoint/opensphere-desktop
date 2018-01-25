package io.opensphere.core.cache.accessor;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * A property accessor. This provides a mechanism for getting properties from
 * objects without the property consumer knowing what kind of object the
 * properties are coming from. This also avoids the memory consumption of
 * constructing large collections of properties to pass to the property
 * consumer.
 *
 * @param <S> The type of object that provides the property values.
 * @param <T> The type of the property values.
 */
public interface PropertyAccessor<S, T>
{
    /**
     * Get the property value from an input object.
     *
     * @param input The input object.
     * @return The property value.
     */
    T access(S input);

    /**
     * Get a description of the property that this accessor provides.
     *
     * @return The property descriptor.
     */
    PropertyDescriptor<T> getPropertyDescriptor();
}
