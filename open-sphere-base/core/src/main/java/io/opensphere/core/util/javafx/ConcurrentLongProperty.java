package io.opensphere.core.util.javafx;

import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Thread-safe {@link SimpleLongProperty}.
 */
public class ConcurrentLongProperty extends SimpleLongProperty implements Consumer<Long>
{
    /**
     * The constructor of {@code LongProperty}.
     */
    public ConcurrentLongProperty()
    {
        super();
    }

    /**
     * The constructor of {@code LongProperty}.
     *
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentLongProperty(long initialValue)
    {
        super(initialValue);
    }

    /**
     * The constructor of {@code LongProperty}.
     *
     * @param bean the bean of this {@code LongProperty}
     * @param name the name of this {@code LongProperty}
     */
    public ConcurrentLongProperty(Object bean, String name)
    {
        super(bean, name);
    }

    /**
     * The constructor of {@code LongProperty}.
     *
     * @param bean the bean of this {@code LongProperty}
     * @param name the name of this {@code LongProperty}
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentLongProperty(Object bean, String name, long initialValue)
    {
        super(bean, name, initialValue);
    }

    @Override
    public synchronized void addListener(InvalidationListener listener)
    {
        super.addListener(listener);
    }

    @Override
    public synchronized void removeListener(InvalidationListener listener)
    {
        super.removeListener(listener);
    }

    @Override
    public synchronized void addListener(ChangeListener<? super Number> listener)
    {
        super.addListener(listener);
    }

    @Override
    public synchronized void removeListener(ChangeListener<? super Number> listener)
    {
        super.removeListener(listener);
    }

    @Override
    public synchronized void fireValueChangedEvent()
    {
        super.fireValueChangedEvent();
    }

    @Override
    public synchronized long get()
    {
        return super.get();
    }

    @Override
    public synchronized void set(long newValue)
    {
        super.set(newValue);
    }

    @Override
    public synchronized boolean isBound()
    {
        return super.isBound();
    }

    @Override
    public synchronized void bind(ObservableValue<? extends Number> newObservable)
    {
        super.bind(newObservable);
    }

    @Override
    public synchronized void unbind()
    {
        super.unbind();
    }

    @Override
    public void accept(Long newValue)
    {
        setValue(newValue);
    }

    /**
     * Sets the value. If forceFire is true a change event will be fired even if
     * the value hasn't changed.
     *
     * @param value the value
     * @param forceFire whether to force a change event
     */
    public void set(long value, boolean forceFire)
    {
        if (forceFire && get() == value)
        {
            fireValueChangedEvent();
        }
        else
        {
            set(value);
        }
    }
}
