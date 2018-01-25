package io.opensphere.core.util.javafx;

import java.util.function.Consumer;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Thread-safe {@link SimpleBooleanProperty}.
 */
public class ConcurrentBooleanProperty extends SimpleBooleanProperty implements Consumer<Boolean>
{
    /**
     * The constructor of {@code BooleanProperty}.
     */
    public ConcurrentBooleanProperty()
    {
        super();
    }

    /**
     * The constructor of {@code BooleanProperty}.
     *
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentBooleanProperty(boolean initialValue)
    {
        super(initialValue);
    }

    /**
     * The constructor of {@code BooleanProperty}.
     *
     * @param bean the bean of this {@code BooleanProperty}
     * @param name the name of this {@code BooleanProperty}
     */
    public ConcurrentBooleanProperty(Object bean, String name)
    {
        super(bean, name);
    }

    /**
     * The constructor of {@code BooleanProperty}.
     *
     * @param bean the bean of this {@code BooleanProperty}
     * @param name the name of this {@code BooleanProperty}
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentBooleanProperty(Object bean, String name, boolean initialValue)
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
    public synchronized void addListener(ChangeListener<? super Boolean> listener)
    {
        super.addListener(listener);
    }

    @Override
    public synchronized void removeListener(ChangeListener<? super Boolean> listener)
    {
        super.removeListener(listener);
    }

    @Override
    public synchronized void fireValueChangedEvent()
    {
        super.fireValueChangedEvent();
    }

    @Override
    @SuppressWarnings("PMD.BooleanGetMethodName")
    public synchronized boolean get()
    {
        return super.get();
    }

    @Override
    public synchronized void set(boolean newValue)
    {
        super.set(newValue);
    }

    @Override
    public synchronized boolean isBound()
    {
        return super.isBound();
    }

    @Override
    public synchronized void bind(ObservableValue<? extends Boolean> newObservable)
    {
        super.bind(newObservable);
    }

    @Override
    public synchronized void unbind()
    {
        super.unbind();
    }

    @Override
    public void accept(Boolean newValue)
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
    public void set(boolean value, boolean forceFire)
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
