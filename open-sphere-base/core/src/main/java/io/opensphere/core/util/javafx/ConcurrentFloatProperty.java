package io.opensphere.core.util.javafx;

import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Thread-safe {@link SimpleFloatProperty}.
 */
public class ConcurrentFloatProperty extends SimpleFloatProperty implements Consumer<Float>
{
    /**
     * The constructor of {@code FloatProperty}.
     */
    public ConcurrentFloatProperty()
    {
        super();
    }

    /**
     * The constructor of {@code FloatProperty}.
     *
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentFloatProperty(float initialValue)
    {
        super(initialValue);
    }

    /**
     * The constructor of {@code FloatProperty}.
     *
     * @param bean the bean of this {@code FloatProperty}
     * @param name the name of this {@code FloatProperty}
     */
    public ConcurrentFloatProperty(Object bean, String name)
    {
        super(bean, name);
    }

    /**
     * The constructor of {@code FloatProperty}.
     *
     * @param bean the bean of this {@code FloatProperty}
     * @param name the name of this {@code FloatProperty}
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentFloatProperty(Object bean, String name, float initialValue)
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
    public synchronized float get()
    {
        return super.get();
    }

    @Override
    public synchronized void set(float newValue)
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
    public void accept(Float newValue)
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
    public void set(float value, boolean forceFire)
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
