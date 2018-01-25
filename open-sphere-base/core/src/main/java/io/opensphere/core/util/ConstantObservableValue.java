package io.opensphere.core.util;

import java.util.Collection;

/**
 * An observable value that returns a constant value.
 *
 * @param <T> The type of the value
 */
public class ConstantObservableValue<T> implements ObservableValue<T>
{
    /** The value. */
    private final T myValue;

    /**
     * Constructor.
     *
     * @param value the value
     */
    public ConstantObservableValue(T value)
    {
        myValue = value;
    }

    @Override
    public T get()
    {
        return myValue;
    }

    @Override
    public void accept(T t)
    {
        throw new UnsupportedOperationException("accept is not supported for ConstantObservableValue");
    }

    @Override
    public void addListener(ChangeListener<? super T> listener)
    {
    }

    @Override
    public void bindBidirectional(ObservableValue<T> other, Collection<? super ChangeListener<? super T>> listeners)
    {
        if (!(other instanceof ConstantObservableValue))
        {
            throw new IllegalArgumentException("Cannot bind a constant observable to a variable observable.");
        }
    }

    @Override
    public Throwable getErrorCause()
    {
        return null;
    }

    @Override
    public String getErrorMessage()
    {
        return null;
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener)
    {
    }

    @Override
    public boolean set(T value)
    {
        throw new UnsupportedOperationException("set is not supported for ConstantObservableValue");
    }

    @Override
    public boolean set(T value, boolean forceFire)
    {
        throw new UnsupportedOperationException("set is not supported for ConstantObservableValue");
    }

    @Override
    public void setError(String message)
    {
    }

    @Override
    public void setError(String message, Throwable cause)
    {
    }
}
