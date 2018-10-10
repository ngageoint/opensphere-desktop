package io.opensphere.core.cache.accessor;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * Accessor for objects that should not be serialized.
 *
 * @param <S> The type of object that provides the property values.
 * @param <T> The type of the property values.
 */
public abstract class UnserializableAccessor<S, T> implements PropertyAccessor<S, T>
{
    /** The property descriptor. */
    private final PropertyDescriptor<T> myPropertyDescriptor;

    /**
     * Get a homogeneous accessor that simply returns the input objects.
     *
     * @param <T> The type of the input objects.
     * @param propertyDescriptor The description of the property being accessed.
     * @return The accessor.
     */
    public static <T> UnserializableAccessor<T, T> getHomogeneousAccessor(PropertyDescriptor<T> propertyDescriptor)
    {
        return new UnserializableAccessor<>(propertyDescriptor)
        {
            @Override
            public T access(T input)
            {
                return input;
            }
        };
    }

    /**
     * Get a singleton accessor that returns the same object, regardless of the
     * input object.
     *
     * @param <S> The type of the input objects.
     *
     * @param <T> The type of the returned object.
     * @param propertyDescriptor The property descriptor.
     * @param object The object to return.
     * @return The accessor.
     */
    public static <S, T> UnserializableAccessor<S, T> getSingletonAccessor(PropertyDescriptor<T> propertyDescriptor,
            final T object)
    {
        return new UnserializableAccessor<>(propertyDescriptor)
        {
            @Override
            public T access(S input)
            {
                return object;
            }
        };
    }

    /**
     * Construct the accessor.
     *
     * @param propertyDescriptor The description of the property being accessed.
     */
    public UnserializableAccessor(PropertyDescriptor<T> propertyDescriptor)
    {
        myPropertyDescriptor = propertyDescriptor;
    }

    @Override
    public PropertyDescriptor<T> getPropertyDescriptor()
    {
        return myPropertyDescriptor;
    }
}
