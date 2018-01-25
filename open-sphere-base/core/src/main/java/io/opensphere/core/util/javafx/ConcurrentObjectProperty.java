package io.opensphere.core.util.javafx;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Thread-safe {@link SimpleObjectProperty}.
 *
 * @param <T> the type of the wrapped {@code Object}
 */
public class ConcurrentObjectProperty<T> extends SimpleObjectProperty<T> implements Supplier<T>, Consumer<T>
{
    /**
     * The constructor of {@code ObjectProperty}.
     */
    public ConcurrentObjectProperty()
    {
        super();
    }

    /**
     * The constructor of {@code ObjectProperty}.
     *
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentObjectProperty(T initialValue)
    {
        super(initialValue);
    }

    /**
     * The constructor of {@code ObjectProperty}.
     *
     * @param bean the bean of this {@code ObjectProperty}
     * @param name the name of this {@code ObjectProperty}
     */
    public ConcurrentObjectProperty(Object bean, String name)
    {
        super(bean, name);
    }

    /**
     * The constructor of {@code ObjectProperty}.
     *
     * @param bean the bean of this {@code ObjectProperty}
     * @param name the name of this {@code ObjectProperty}
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentObjectProperty(Object bean, String name, T initialValue)
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
    public synchronized void addListener(ChangeListener<? super T> listener)
    {
        super.addListener(listener);
    }

    @Override
    public synchronized void removeListener(ChangeListener<? super T> listener)
    {
        super.removeListener(listener);
    }

    @Override
    public synchronized void fireValueChangedEvent()
    {
        super.fireValueChangedEvent();
    }

    @Override
    public synchronized T get()
    {
        return super.get();
    }

    @Override
    public synchronized void set(T newValue)
    {
        super.set(newValue);
    }

    @Override
    public synchronized boolean isBound()
    {
        return super.isBound();
    }

    @Override
    public synchronized void bind(ObservableValue<? extends T> newObservable)
    {
        super.bind(newObservable);
    }

    @Override
    public synchronized void unbind()
    {
        super.unbind();
    }

    @Override
    public void accept(T newValue)
    {
        set(newValue);
    }

    /**
     * Sets the value. If forceFire is true a change event will be fired even if
     * the value hasn't changed.
     *
     * @param value the value
     * @param forceFire whether to force a change event
     */
    public void set(T value, boolean forceFire)
    {
        if (forceFire && Objects.equals(get(), value))
        {
            fireValueChangedEvent();
        }
        else
        {
            set(value);
        }
    }
}
