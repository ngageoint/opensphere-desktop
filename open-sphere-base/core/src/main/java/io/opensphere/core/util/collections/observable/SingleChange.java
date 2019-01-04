package io.opensphere.core.util.collections.observable;

import javafx.beans.InvalidationListener;
import javafx.collections.SetChangeListener;

/**
 * A helper designed to propagate changes to a single listener.
 *
 * @param <E> the type of the set handled by the helper.
 */
public class SingleChange<E> extends SetListenerHelper<E>
{
    /** The listener registered to receive changes from the helper. */
    private final SetChangeListener<? super E> myListener;

    /**
     * Creates a new change handler with the supplied listener.
     *
     * @param listener the listener to register for change notification.
     */
    public SingleChange(SetChangeListener<? super E> listener)
    {
        myListener = listener;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#addListener(javafx.beans.InvalidationListener)
     */
    @Override
    protected SetListenerHelper<E> addListener(InvalidationListener listener)
    {
        return new Generic<>(listener, myListener);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#removeListener(javafx.beans.InvalidationListener)
     */
    @Override
    protected SetListenerHelper<E> removeListener(InvalidationListener listener)
    {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#addListener(javafx.collections.SetChangeListener)
     */
    @Override
    protected SetListenerHelper<E> addListener(SetChangeListener<? super E> listener)
    {
        return new Generic<>(myListener, listener);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#removeListener(javafx.collections.SetChangeListener)
     */
    @Override
    protected SetListenerHelper<E> removeListener(SetChangeListener<? super E> listener)
    {
        return (listener.equals(myListener)) ? null : this;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#fireValueChangedEvent(javafx.collections.SetChangeListener.Change)
     */
    @Override
    protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change)
    {
        try
        {
            myListener.onChanged(change);
        }
        catch (Exception e)
        {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
    }
}