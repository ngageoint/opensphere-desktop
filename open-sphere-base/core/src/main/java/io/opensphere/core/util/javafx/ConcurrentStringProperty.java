package io.opensphere.core.util.javafx;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Thread-safe {@link SimpleStringProperty}.
 */
@SuppressFBWarnings(value = "UG_SYNC_SET_UNSYNC_GET", justification = "FindBugs must be confused")
public class ConcurrentStringProperty extends SimpleStringProperty implements Supplier<String>, Consumer<String>
{
    /**
     * The constructor of {@code StringProperty}.
     */
    public ConcurrentStringProperty()
    {
        super();
    }

    /**
     * The constructor of {@code StringProperty}.
     *
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentStringProperty(String initialValue)
    {
        super(initialValue);
    }

    /**
     * The constructor of {@code StringProperty}.
     *
     * @param bean the bean of this {@code StringProperty}
     * @param name the name of this {@code StringProperty}
     */
    public ConcurrentStringProperty(Object bean, String name)
    {
        super(bean, name);
    }

    /**
     * The constructor of {@code StringProperty}.
     *
     * @param bean the bean of this {@code StringProperty}
     * @param name the name of this {@code StringProperty}
     * @param initialValue the initial value of the wrapped value
     */
    public ConcurrentStringProperty(Object bean, String name, String initialValue)
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
    public synchronized void addListener(ChangeListener<? super String> listener)
    {
        super.addListener(listener);
    }

    @Override
    public synchronized void removeListener(ChangeListener<? super String> listener)
    {
        super.removeListener(listener);
    }

    @Override
    public synchronized void fireValueChangedEvent()
    {
        super.fireValueChangedEvent();
    }

    @Override
    public synchronized String get()
    {
        return super.get();
    }

    @Override
    public synchronized void set(String newValue)
    {
        super.set(newValue);
    }

    @Override
    public synchronized boolean isBound()
    {
        return super.isBound();
    }

    @Override
    public synchronized void bind(ObservableValue<? extends String> newObservable)
    {
        super.bind(newObservable);
    }

    @Override
    public synchronized void unbind()
    {
        super.unbind();
    }

    @Override
    public void accept(String newValue)
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
    public void set(String value, boolean forceFire)
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
