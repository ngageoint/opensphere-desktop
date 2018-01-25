package io.opensphere.core.util.ref;

/**
 * Abstract reference class that implements {@link #toString()}.
 *
 * @param <T> The type of object that this object refers to.
 */
public abstract class AbstractReference<T> implements Reference<T>
{
    @Override
    public String toString()
    {
        return new StringBuilder(32).append(getClass().getSimpleName()).append('[').append(get()).append(']').toString();
    }
}
