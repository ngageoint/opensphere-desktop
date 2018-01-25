package io.opensphere.core.cache.accessor;

import java.io.InputStream;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * Accessor for {@link InputStream}s.
 *
 * @param <S> The type of object that provides the property values.
 */
public abstract class InputStreamAccessor<S> implements PersistentPropertyAccessor<S, InputStream>
{
    /** The property descriptor. */
    private final PropertyDescriptor<InputStream> myPropertyDescriptor;

    /**
     * Get a homogeneous accessor that simply returns the input objects.
     *
     * @param propertyDescriptor The property descriptor.
     *
     * @return The accessor.
     */
    public static InputStreamAccessor<InputStream> getHomogeneousAccessor(PropertyDescriptor<InputStream> propertyDescriptor)
    {
        return new InputStreamAccessor<InputStream>(propertyDescriptor)
        {
            @Override
            public InputStream access(InputStream input)
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
     *
     * @return The accessor.
     */
    public static InputStreamAccessor<InputStream> getHomogeneousAccessor(String propertyName)
    {
        return new InputStreamAccessor<InputStream>(propertyName)
        {
            @Override
            public InputStream access(InputStream input)
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
     * @param propertyDescriptor The property descriptor.
     * @param object The object to return.
     * @return The accessor.
     */
    public static <S> PropertyAccessor<S, InputStream> getSingletonAccessor(PropertyDescriptor<InputStream> propertyDescriptor,
            final InputStream object)
    {
        return new InputStreamAccessor<S>(propertyDescriptor)
        {
            @Override
            public InputStream access(S input)
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
     * @param propertyName The name of the property.
     * @param object The object to return.
     * @return The accessor.
     */
    public static <S> PropertyAccessor<S, InputStream> getSingletonAccessor(String propertyName, final InputStream object)
    {
        return new InputStreamAccessor<S>(propertyName)
        {
            @Override
            public InputStream access(S input)
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
    public InputStreamAccessor(PropertyDescriptor<InputStream> propertyDescriptor)
    {
        myPropertyDescriptor = propertyDescriptor;
    }

    /**
     * Construct the accessor with a custom property name.
     *
     * @param propertyName The property name.
     */
    public InputStreamAccessor(String propertyName)
    {
        myPropertyDescriptor = new PropertyDescriptor<>(propertyName, InputStream.class);
    }

    @Override
    public PropertyDescriptor<InputStream> getPropertyDescriptor()
    {
        return myPropertyDescriptor;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(InputStreamAccessor.class.getSimpleName()).append('[')
                .append(getPropertyDescriptor()).append(']').toString();
    }
}
