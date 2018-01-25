package io.opensphere.core.util.javafx;

import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Thread-safe {@link SimpleIntegerProperty}.
 */
public class ConcurrentIntegerProperty extends SimpleIntegerProperty implements Consumer<Integer>
{
    /**
     * The constructor of {@code IntegerProperty}.
     */
    public ConcurrentIntegerProperty()
    {
        super();
    }

    /**
     * The constructor of {@code IntegerProperty}.
     *
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentIntegerProperty(int initialValue)
    {
        super(initialValue);
    }

    /**
     * The constructor of {@code IntegerProperty}.
     *
     * @param bean the bean of this {@code IntegerProperty}
     * @param name the name of this {@code IntegerProperty}
     */
    public ConcurrentIntegerProperty(Object bean, String name)
    {
        super(bean, name);
    }

    /**
     * The constructor of {@code IntegerProperty}.
     *
     * @param bean the bean of this {@code IntegerProperty}
     * @param name the name of this {@code IntegerProperty}
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentIntegerProperty(Object bean, String name, int initialValue)
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
    public synchronized int get()
    {
        return super.get();
    }

    @Override
    public synchronized void set(int newValue)
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
    public void accept(Integer newValue)
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
    public void set(int value, boolean forceFire)
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
