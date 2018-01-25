package io.opensphere.core.cache.accessor;

import io.opensphere.core.cache.util.PropertyArrayDescriptor;

/**
 * Property array accessor.
 *
 * @param <S> The type of object that provides the property values.
 */
public abstract class PropertyArrayAccessor<S> implements PersistentPropertyAccessor<S, Object[]>
{
    /** The property descriptor. */
    private final PropertyArrayDescriptor myPropertyDescriptor;

    /**
     * Construct the property array accessor.
     *
     * @param descriptor The property descriptor.
     */
    public PropertyArrayAccessor(PropertyArrayDescriptor descriptor)
    {
        myPropertyDescriptor = descriptor;
    }

    /**
     * Get the property value from an input object. The array values must match
     * the types returned by {@link PropertyArrayDescriptor#getColumnTypes()}
     * for the indices of the active columns as specified by
     * {@link PropertyArrayDescriptor#getActiveColumns()}.
     * <p>
     * The length of the array must be equal to the number of active columns.
     * <p>
     * The following statement should be <code>true</code>:
     * <p>
     * <code>
     * columnType[activeColumn[index]].isAssignableFrom(access(input)[index]);
     * </code>
     *
     * @param input The input object.
     * @return The property value.
     */
    @Override
    public abstract Object[] access(S input);

    @Override
    public PropertyArrayDescriptor getPropertyDescriptor()
    {
        return myPropertyDescriptor;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(128).append(PropertyArrayAccessor.class.getSimpleName()).append('[')
                .append(getPropertyDescriptor()).append(']').toString();
    }
}
