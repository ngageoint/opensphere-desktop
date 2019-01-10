package io.opensphere.core.util.collections.observable;

import javafx.beans.InvalidationListener;
import javafx.collections.SetChangeListener;

/**
 * A listener helper implementation used to handle a single invalidation
 * listener.
 *
 * @param <E> the type handled by the helper.
 */
public class SingleInvalidation<E> extends SetListenerHelper<E>
{
    /** The listener used to receive invalidation events. */
    private final InvalidationListener myListener;

    /**
     * Creates a new invalidation handler with the single supplied listener.
     *
     * @param listener the listener to use for invalidation events.
     */
    public SingleInvalidation(InvalidationListener listener)
    {
        this.myListener = listener;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#addListener(javafx.beans.InvalidationListener)
     */
    @Override
    protected SetListenerHelper<E> addListener(InvalidationListener listener)
    {
        return new Generic<>(this.myListener, listener);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#removeListener(javafx.beans.InvalidationListener)
     */
    @Override
    protected SetListenerHelper<E> removeListener(InvalidationListener listener)
    {
        return (listener.equals(this.myListener)) ? null : this;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#addListener(javafx.collections.SetChangeListener)
     */
    @Override
    protected SetListenerHelper<E> addListener(SetChangeListener<? super E> listener)
    {
        return new Generic<>(this.myListener, listener);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#removeListener(javafx.collections.SetChangeListener)
     */
    @Override
    protected SetListenerHelper<E> removeListener(SetChangeListener<? super E> listener)
    {
        return this;
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
            myListener.invalidated(change.getSet());
        }
        catch (Exception e)
        {
            Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }
    }
}
