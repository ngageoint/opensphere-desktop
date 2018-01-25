package io.opensphere.core.util;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import io.opensphere.core.util.ref.Reference;

/**
 * Default implementation of ObservableValue.
 *
 * @param <T> The type of the value
 */
public abstract class AbstractObservableValue<T> implements ObservableValue<T>
{
    /** The cause of the error. */
    @GuardedBy("this")
    private Throwable myErrorCause;

    /** The error message. */
    @GuardedBy("this")
    private String myErrorMessage;

    /** The value. */
    @GuardedBy("this")
    private T myValue;

    @Override
    public void accept(T t)
    {
        set(t);
    }

    @Override
    public void addListener(ChangeListener<? super T> listener)
    {
        getChangeSupport().addListener(listener);
    }

    @Override
    public void bindBidirectional(final ObservableValue<T> other,
            @Nullable Collection<? super ChangeListener<? super T>> listeners)
    {
        ChangeListener<T> listener1 = (observable, oldValue, newValue) -> other.set(get());
        addListener(listener1);

        ChangeListener<T> listener2 = (observable, oldValue, newValue) -> set(other.get());
        other.addListener(listener2);

        if (listeners != null)
        {
            listeners.add(listener1);
            listeners.add(listener2);
        }

        set(other.get());
    }

    /**
     * Fires a change.
     */
    public void fireChangeEvent()
    {
        T value = get();
        fireChangeEvent(value, value);
    }

    /**
     * Fires a change.
     *
     * @param oldValue The old value
     * @param newValue The new value
     */
    public void fireChangeEvent(final T oldValue, final T newValue)
    {
        getChangeSupport().notifyListeners(listener -> listener.changed(this, oldValue, newValue));
    }

    @Override
    public synchronized T get()
    {
        return myValue;
    }

    @Override
    public synchronized Throwable getErrorCause()
    {
        return myErrorCause;
    }

    @Override
    public synchronized String getErrorMessage()
    {
        return myErrorMessage;
    }

    /**
     * Removes all listeners.
     */
    public void removeAllListeners()
    {
        for (Reference<ChangeListener<? super T>> reference : getChangeSupport().getListeners())
        {
            getChangeSupport().removeListener(reference.get());
        }
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener)
    {
        getChangeSupport().removeListener(listener);
    }

    @Override
    public boolean set(T value)
    {
        return set(value, false);
    }

    @Override
    public boolean set(T value, boolean forceFire)
    {
        boolean different;
        T oldValue;
        synchronized (this)
        {
            oldValue = myValue;
            different = forceFire || !Objects.equals(myValue, value);
            if (different)
            {
                myValue = value;
            }
        }

        if (different)
        {
            fireChangeEvent(oldValue, value);
        }
        return different;
    }

    @Override
    public synchronized void setError(String message)
    {
        setError(message, (Throwable)null);
    }

    @Override
    public void setError(String errorMessage, Throwable errorCause)
    {
        boolean different;
        synchronized (this)
        {
            different = !Objects.equals(myErrorMessage, errorMessage);
            if (different)
            {
                myErrorMessage = errorMessage;
                myErrorCause = errorCause;
            }
        }

        if (different)
        {
            fireChangeEvent();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(64);
        builder.append(getClass().getSimpleName()).append(" [value=").append(get()).append(']');
        return builder.toString();
    }

    /**
     * Gets the change support.
     *
     * @return the change support
     */
    protected abstract AbstractChangeSupport<ChangeListener<? super T>> getChangeSupport();
}
