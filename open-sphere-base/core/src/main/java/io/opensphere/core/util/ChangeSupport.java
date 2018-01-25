package io.opensphere.core.util;

import java.util.concurrent.Executor;

/**
 * Support for notifying interested parties of generic changes.
 *
 * @param <T> The supported listener type.
 */
public interface ChangeSupport<T>
{
    /**
     * Add a listener for changes.
     *
     * @param listener The listener.
     */
    void addListener(T listener);

    /**
     * Clear all listeners.
     */
    void clearListeners();

    /**
     * Gets the number of listeners.
     *
     * @return the number of listeners
     */
    int getListenerCount();

    /**
     * Get a service that handles adding and removing a listener. When
     * {@link Service#open()} is called, the listener will be added to this
     * change support. When {@link Service#close()} is called, the listener will
     * be removed. The service holds a strong reference to the listener, but no
     * reference is held to the service.
     *
     * @param listener The listener.
     * @return The service.
     */
    ReferenceService<T> getListenerService(T listener);

    /**
     * Get if this change support has no listeners.
     *
     * @return {@code true} if this change support is empty.
     */
    boolean isEmpty();

    /**
     * Notify the listeners of a change.
     *
     * @param callback The callback to invoke for each listener.
     */
    void notifyListeners(final ChangeSupport.Callback<T> callback);

    /**
     * Notify the listeners of a change.
     *
     * @param callback The callback to invoke for each listener.
     * @param executor Optional executor to use for each callback.
     */
    void notifyListeners(final ChangeSupport.Callback<T> callback, Executor executor);

    /**
     * Notify the listeners of a change, submitting a single {@link Runnable} to
     * the {@link Executor}.
     *
     * @param callback The callback to invoke for each listener.
     * @param executor Optional executor to use for all callbacks.
     */
    void notifyListenersSingle(final ChangeSupport.Callback<T> callback, Executor executor);

    /**
     * Remove a listener.
     *
     * @param listener The listener to be removed.
     * @return {@code true} if the listener was removed.
     */
    boolean removeListener(T listener);

    /**
     * Interface for the object called when a notification occurs.
     *
     * @param <T> The supported listener type.
     */
    @FunctionalInterface
    public interface Callback<T>
    {
        /**
         * Notify the listener.
         *
         * @param listener The listener object.
         */
        void notify(T listener);
    }
}
