package io.opensphere.core.util.javafx;

import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Thread-safe {@link SimpleDoubleProperty}.
 */
public class ConcurrentDoubleProperty extends SimpleDoubleProperty implements Consumer<Double>
{
    /**
     * The constructor of {@code DoubleProperty}.
     */
    public ConcurrentDoubleProperty()
    {
        super();
    }

    /**
     * The constructor of {@code DoubleProperty}.
     *
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentDoubleProperty(double initialValue)
    {
        super(initialValue);
    }

    /**
     * The constructor of {@code DoubleProperty}.
     *
     * @param bean the bean of this {@code DoubleProperty}
     * @param name the name of this {@code DoubleProperty}
     */
    public ConcurrentDoubleProperty(Object bean, String name)
    {
        super(bean, name);
    }

    /**
     * The constructor of {@code DoubleProperty}.
     *
     * @param bean the bean of this {@code DoubleProperty}
     * @param name the name of this {@code DoubleProperty}
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentDoubleProperty(Object bean, String name, double initialValue)
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
    public synchronized double get()
    {
        return super.get();
    }

    @Override
    public synchronized void set(double newValue)
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
    public void accept(Double newValue)
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
    public void set(double value, boolean forceFire)
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
