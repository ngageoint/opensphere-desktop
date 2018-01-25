package io.opensphere.core.cache.accessor;

/**
 * Abstract implementation of {@link IntervalPropertyAccessor} that provides a
 * field for the extent.
 *
 * @param <S> The type of object that provides the property values.
 * @param <T> The type of the property values.
 */
public abstract class AbstractIntervalPropertyAccessor<S, T> implements IntervalPropertyAccessor<S, T>
{
    /** The overall extent for all the property values. */
    private final T myExtent;

    /**
     * Construct the accessor.
     *
     * @param extent The overall extent of all the property values being
     *            accessed.
     */
    protected AbstractIntervalPropertyAccessor(T extent)
    {
        myExtent = extent;
    }

    @Override
    public T getExtent()
    {
        return myExtent;
    }
}
