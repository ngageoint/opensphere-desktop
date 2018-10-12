package io.opensphere.core.cache.util;

import java.io.Serializable;

import io.opensphere.core.cache.Cache;
import io.opensphere.core.util.Utilities;

/**
 * A description of a model property, comprising the property name and the class
 * of the property values. There's also an optional hint as to the size of the
 * property values.
 *
 * @param <T> The type of the property values.
 * @see Cache#getValues(long[], io.opensphere.core.cache.PropertyValueMap,
 *      gnu.trove.list.TIntList)
 */
public class PropertyDescriptor<T> implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates if values of this property can be cached either in memory or to
     * disk.
     */
    private final boolean myCacheable;

    /** The estimated size of the property values. */
    private final long myEstimatedValueSizeBytes;

    /** The name of the property. */
    private final String myPropertyName;

    /** The type of the property values. */
    private final Class<T> myType;

    /**
     * Convenience factory method.
     *
     * @param <T> The type of the property values.
     * @param propertyName The name of the property.
     * @param type The type of the property values.
     * @return The property descriptor.
     */
    public static <T> PropertyDescriptor<T> create(String propertyName, Class<T> type)
    {
        return new PropertyDescriptor<>(propertyName, type);
    }

    /**
     * Convenience factory method.
     *
     * @param <T> The type of the property values.
     * @param propertyName The name of the property.
     * @param type The type of the property values.
     * @param estimatedValueSizeBytes The estimated size of the property values.
     * @return The property descriptor.
     */
    public static <T> PropertyDescriptor<T> create(String propertyName, Class<T> type, long estimatedValueSizeBytes)
    {
        return new PropertyDescriptor<>(propertyName, type, estimatedValueSizeBytes);
    }

    /**
     * Convenience factory method.
     *
     * @param <T> The type of the property values.
     * @param propertyName The name of the property.
     * @param type The type of the property values.
     * @param estimatedValueSizeBytes The estimated size of the property values.
     * @param cacheable Indicates if values of this property can be cached
     *            either in memory or to disk.
     * @return The property descriptor.
     */
    public static <T> PropertyDescriptor<T> create(String propertyName, Class<T> type, long estimatedValueSizeBytes,
            boolean cacheable)
    {
        return new PropertyDescriptor<>(propertyName, type, estimatedValueSizeBytes, cacheable);
    }

    /**
     * Construct the property descriptor.
     *
     * @param propertyName The name of the property.
     * @param type The type of the property values.
     */
    public PropertyDescriptor(String propertyName, Class<T> type)
    {
        this(propertyName, type, -1L);
    }

    /**
     * Construct the property descriptor.
     *
     * @param propertyName The name of the property.
     * @param type The type of the property values.
     * @param estimatedValueSizeBytes The estimated size of the property values.
     */
    public PropertyDescriptor(String propertyName, Class<T> type, long estimatedValueSizeBytes)
    {
        this(propertyName, type, estimatedValueSizeBytes, true);
    }

    /**
     * Construct the property descriptor.
     *
     * @param propertyName The name of the property.
     * @param type The type of the property values.
     * @param estimatedValueSizeBytes The estimated size of the property values.
     * @param cacheable Indicates if values of this property can be cached
     *            either in memory or to disk.
     */
    public PropertyDescriptor(String propertyName, Class<T> type, long estimatedValueSizeBytes, boolean cacheable)
    {
        Utilities.checkNull(propertyName, "propertyName");
        Utilities.checkNull(type, "type");
        myPropertyName = propertyName;
        myType = type;
        myEstimatedValueSizeBytes = estimatedValueSizeBytes;
        myCacheable = cacheable;
    }

    @Override
    public final boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof PropertyDescriptor))
        {
            return false;
        }
        PropertyDescriptor<?> other = (PropertyDescriptor<?>)obj;
        if (!myPropertyName.equals(other.myPropertyName))
        {
            return false;
        }
        return Utilities.sameInstance(myType, other.myType);
    }

    /**
     * Get the estimated size of the property values, in bytes. This may return
     * <tt>-1</tt> if the size is unknown.
     *
     * @return The estimated size of the property values, or -1 if the size is
     *         unknown.
     */
    public long getEstimatedValueSizeBytes()
    {
        return myEstimatedValueSizeBytes;
    }

    /**
     * Get the property name.
     *
     * @return The property name.
     */
    public String getPropertyName()
    {
        return myPropertyName;
    }

    /**
     * Get the type of the property values.
     *
     * @return The type of the property values.
     */
    public Class<T> getType()
    {
        return myType;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myPropertyName == null ? 0 : myPropertyName.hashCode());
        result = prime * result + (myType == null ? 0 : myType.hashCode());
        return result;
    }

    /**
     * Get if values of this property can be cached either in memory or to disk.
     *
     * @return If this property is cache-able.
     */
    public boolean isCacheable()
    {
        return myCacheable;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(PropertyDescriptor.class.getSimpleName()).append("[name[").append(getPropertyName())
                .append("] type[").append(getType().getSimpleName()).append("]]").toString();
    }
}
