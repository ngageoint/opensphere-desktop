package io.opensphere.core.util;

/**
 * A {@code ChangeListener} is notified whenever the value of an
 * {@link ObservableValue} changes. It can be registered and unregistered with
 * {@link ObservableValue#addListener(ChangeListener)} respectively
 * {@link ObservableValue#removeListener(ChangeListener)}
 *
 * @param <T> The type of the wrapped value.
 * @deprecated use javafx.beans.value.ChangeListener<T> instead.
 */
@Deprecated
@FunctionalInterface
public interface ChangeListener<T>
{
    /**
     * This method needs to be provided by an implementation of
     * {@code ChangeListener}. It is called if the value of an
     * {@link ObservableValue} changes.
     * <p>
     * In general is is considered bad practice to modify the observed value in
     * this method.
     *
     * @param observable The {@code ObservableValue} which value changed
     * @param oldValue The old value
     * @param newValue The new value
     */
    void changed(ObservableValue<? extends T> observable, T oldValue, T newValue);
}
