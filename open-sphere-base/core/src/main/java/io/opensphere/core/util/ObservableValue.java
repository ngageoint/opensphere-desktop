package io.opensphere.core.util;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Observable value interface.
 *
 * @param <T> The type of the value
 */
public interface ObservableValue<T> extends Supplier<T>, Consumer<T>
{
    /**
     * Adds the change listener.
     *
     * @param listener the listener
     */
    void addListener(ChangeListener<? super T> listener);

    /**
     * Bind two models together so that if either model is changed, the other is
     * updated. Initially both values will be set to the value of the input.
     *
     * @param other The other model.
     * @param listeners Optional return collection of created listeners.
     */
    void bindBidirectional(final ObservableValue<T> other, @Nullable Collection<? super ChangeListener<? super T>> listeners);

    /**
     * Get the cause of the error.
     *
     * @return The error cause or {@code null} if none.
     */
    Throwable getErrorCause();

    /**
     * Get the error message.
     *
     * @return The error message or {@code null} if none.
     */
    String getErrorMessage();

    /**
     * Removes the change listener.
     *
     * @param listener the listener
     */
    void removeListener(ChangeListener<? super T> listener);

    /**
     * Sets the value.
     *
     * @param value the new value
     * @return {@code true} if the value changed
     */
    boolean set(T value);

    /**
     * Sets the value. If forceFire is true a change event will be fired even if
     * the value hasn't changed.
     *
     * @param value the value
     * @param forceFire whether to force a change event
     * @return {@code true} if the value changed
     */
    boolean set(T value, boolean forceFire);

    /**
     * Indicate that an error has occurred. This will notify change listeners.
     *
     * @param message The error message.
     */
    void setError(String message);

    /**
     * Indicate that an error has occurred. This will notify change listeners.
     *
     * @param message The error message.
     * @param cause The cause of the error, which may be {@code null}.
     */
    void setError(String message, Throwable cause);
}
