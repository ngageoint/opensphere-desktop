package io.opensphere.core.cache.accessor;

import java.io.Serializable;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * Accessor for {@link Serializable} objects.
 *
 * @param <S> The type of object that provides the property values.
 * @param <T> The type of the property values, which must be
 *            {@link Serializable}.
 */
public abstract class SerializableAccessor<S, T extends Serializable> implements PersistentPropertyAccessor<S, T>
{
    /** The property descriptor. */
    private final PropertyDescriptor<T> myPropertyDescriptor;

    /**
     * Get a homogeneous accessor that simply returns the input serializable
     * objects.
     *
     * @param propertyDescriptor The property descriptor.
     *
     * @param <T> The type of the input objects.
     * @return The accessor.
     */
    public static <T extends Serializable> SerializableAccessor<T, T> getHomogeneousAccessor(
            PropertyDescriptor<T> propertyDescriptor)
    {
        return new SerializableAccessor<T, T>(propertyDescriptor)
        {
            @Override
            public T access(T input)
            {
                return input;
            }
        };
    }

    /**
     * Get a homogeneous accessor that simply returns the input serializable
     * objects.
     *
     * @param propertyName The name of the property.
     * @param type The type of the input objects.
     *
     * @param <T> The type of the input objects.
     * @return The accessor.
     */
    public static <T extends Serializable> SerializableAccessor<T, T> getHomogeneousAccessor(String propertyName, Class<T> type)
    {
        return new SerializableAccessor<T, T>(propertyName, type)
        {
            @Override
            public T access(T input)
            {
                return input;
            }
        };
    }

    /**
     * Get a singleton accessor that returns the same serializable object,
     * regardless of the input object.
     *
     * @param <S> The type of the input objects.
     *
     * @param <T> The type of the returned object.
     * @param propertyDescriptor The property descriptor.
     * @param object The object to return.
     * @return The accessor.
     */
    public static <S, T extends Serializable> SerializableAccessor<S, T> getSingletonAccessor(
            PropertyDescriptor<T> propertyDescriptor, final T object)
    {
        return new SerializableAccessor<S, T>(propertyDescriptor)
        {
            @Override
            public T access(S input)
            {
                return object;
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
     * @param propertyName The name of the property.
     * @param type The type of the returned object.
     * @param object The object to return.
     * @return The accessor.
     */
    public static <S, T extends Serializable> PropertyAccessor<S, T> getSingletonAccessor(String propertyName, Class<T> type,
            final T object)
    {
        return new SerializableAccessor<S, T>(propertyName, type)
        {
            @Override
            public T access(S input)
            {
                return object;
            }
        };
    }

    /**
     * Construct the accessor from a property descriptor.
     *
     * @param propertyDescriptor The property descriptor.
     */
    public SerializableAccessor(PropertyDescriptor<T> propertyDescriptor)
    {
        myPropertyDescriptor = propertyDescriptor;
    }

    /**
     * Construct the accessor with a custom property name.
     *
     * @param propertyName The property name.
     * @param type The type of object (which must be serializable).
     */
    public SerializableAccessor(String propertyName, Class<T> type)
    {
        myPropertyDescriptor = new PropertyDescriptor<>(propertyName, type);
    }

    @Override
    public PropertyDescriptor<T> getPropertyDescriptor()
    {
        return myPropertyDescriptor;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(SerializableAccessor.class.getSimpleName()).append('[')
                .append(getPropertyDescriptor()).append(']').toString();
    }
}
