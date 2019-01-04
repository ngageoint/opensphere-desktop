package io.opensphere.core.util.collections.observable;

import javafx.beans.InvalidationListener;
import javafx.collections.SetChangeListener;

/**
 * A helper class used in creation of observable sets.
 *
 * @param <E> the elements contained within the set.
 */
public abstract class SetListenerHelper<E> extends ExpressionHelperBase
{
    /**
     * Registers the supplied listener for notification with the supplied
     * helper. If the supplied helper is <code>null</code>, a new instance is
     * created.
     *
     * @param helper the optional helper in which the listener will be
     *            registered (if null, a new instance is created).
     * @param listener the required invalidation listener to register for
     *            notification.
     * @return a reference to the helper created during this operation.
     */
    public static <E> SetListenerHelper<E> addListener(SetListenerHelper<E> helper, InvalidationListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException();
        }
        return (helper == null) ? new SingleInvalidation<>(listener) : helper.addListener(listener);
    }

    /**
     * Unregisters the supplied listener for notification with the supplied
     * helper. If the supplied helper is <code>null</code>, no action is taken.
     *
     * @param helper the optional helper in which the listener will be
     *            unregistered (if null, no action is taken).
     * @param listener the required invalidation listener to unregister for
     *            notification.
     * @return a reference to the helper used during this operation (null if the
     *         supplied helper is null).
     */
    public static <E> SetListenerHelper<E> removeListener(SetListenerHelper<E> helper, InvalidationListener listener)
    {
        if (listener == null)
        {
            throw new NullPointerException();
        }
        return (helper == null) ? null : helper.removeListener(listener);
    }

    /**
     * Registers the supplied listener for notification with the supplied
     * helper. If the supplied helper is <code>null</code>, a new instance is
     * created.
     *
     * @param helper the optional helper in which the listener will be
     *            registered (if null, a new instance is created).
     * @param listener the required change listener to register for
     *            notification.
     * @return a reference to the helper created during this operation.
     */
    public static <E> SetListenerHelper<E> addListener(SetListenerHelper<E> helper, SetChangeListener<? super E> listener)
    {
        if (listener == null)
        {
            throw new NullPointerException();
        }
        return (helper == null) ? new SingleChange<>(listener) : helper.addListener(listener);
    }

    /**
     * Unregisters the supplied listener for notification with the supplied
     * helper. If the supplied helper is <code>null</code>, no action is taken.
     *
     * @param helper the optional helper in which the listener will be
     *            unregistered (if null, no action is taken).
     * @param listener the required change listener to unregister for
     *            notification.
     * @return a reference to the helper used during this operation (null if the
     *         supplied helper is null).
     */
    public static <E> SetListenerHelper<E> removeListener(SetListenerHelper<E> helper, SetChangeListener<? super E> listener)
    {
        if (listener == null)
        {
            throw new NullPointerException();
        }
        return (helper == null) ? null : helper.removeListener(listener);
    }

    /**
     * Through the supplied helper, propagates the supplied change event to the
     * helper's registered listeners.
     *
     * @param helper the helper through which the event will be sent.
     * @param change the object describing the changes made to the set.
     */
    public static <E> void fireValueChangedEvent(SetListenerHelper<E> helper, SetChangeListener.Change<? extends E> change)
    {
        if (helper != null)
        {
            helper.fireValueChangedEvent(change);
        }
    }

    /**
     * Tests to determine if the supplied helper has listeners.
     *
     * @param helper the helper to test.
     * @return <code>true</code> if the helper has listeners, <code>false</code>
     *         otherwise.
     */
    public static <E> boolean hasListeners(SetListenerHelper<E> helper)
    {
        return helper != null;
    }

    /**
     * Registers the supplied listener for invalidation event notifications.
     *
     * @param listener the listener to register.
     * @return a reference to this instance for call chaining.
     */
    protected abstract SetListenerHelper<E> addListener(InvalidationListener listener);

    /**
     * Unregisters the supplied listener from invalidation event notifications.
     *
     * @param listener the listener to unregister.
     * @return a reference to this instance for call chaining.
     */
    protected abstract SetListenerHelper<E> removeListener(InvalidationListener listener);

    /**
     * Registers the supplied listener for change event notifications.
     *
     * @param listener the listener to register.
     * @return a reference to this instance for call chaining.
     */
    protected abstract SetListenerHelper<E> addListener(SetChangeListener<? super E> listener);

    /**
     * Unregisters the supplied listener from change event notifications.
     *
     * @param listener the listener to unregister.
     * @return a reference to this instance for call chaining.
     */
    protected abstract SetListenerHelper<E> removeListener(SetChangeListener<? super E> listener);

    /**
     * Fires the supplied value change event to all registered change listeners.
     *
     * @param change the event object describing the change event on the
     *            underlying set.
     */
    protected abstract void fireValueChangedEvent(SetChangeListener.Change<? extends E> change);
}
