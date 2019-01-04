package io.opensphere.core.util.collections.observable;

import java.util.AbstractSet;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 * A base class for an observable set. An observable set adheres to all of the
 * rules defined in {@link java.util.Set}, and also allows observers to track
 * changes when they occur.
 *
 * @param <E> The element types stored within the set.
 */
public abstract class ObservableSetBase<E> extends AbstractSet<E> implements ObservableSet<E>
{
    /**
     * The helper used to manage listeners that need to be notified of set
     * changes.
     */
    private SetListenerHelper<E> myListenerHelper;

    /**
     * {@inheritDoc}
     *
     * @see javafx.beans.Observable#addListener(javafx.beans.InvalidationListener)
     */
    @Override
    public final void addListener(InvalidationListener listener)
    {
        myListenerHelper = SetListenerHelper.addListener(myListenerHelper, listener);
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.beans.Observable#removeListener(javafx.beans.InvalidationListener)
     */
    @Override
    public final void removeListener(InvalidationListener listener)
    {
        myListenerHelper = SetListenerHelper.removeListener(myListenerHelper, listener);
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.collections.ObservableSet#addListener(javafx.collections.SetChangeListener)
     */
    @Override
    public final void addListener(SetChangeListener<? super E> listener)
    {
        myListenerHelper = SetListenerHelper.addListener(myListenerHelper, listener);
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.collections.ObservableSet#removeListener(javafx.collections.SetChangeListener)
     */
    @Override
    public final void removeListener(SetChangeListener<? super E> listener)
    {
        myListenerHelper = SetListenerHelper.removeListener(myListenerHelper, listener);
    }

    /**
     * Propagates a change out to all registered listeners.
     *
     * @param change the change to send to registered listeners.
     */
    protected final void fireChange(SetChangeListener.Change<? extends E> change)
    {
        SetListenerHelper.fireValueChangedEvent(myListenerHelper, change);
    }

    /**
     * Returns <code>true</code> if there are at least one listener registered
     * for change notifications from this list.
     *
     * @return <code>true</code> if there is at least one listener registered
     *         for change notifications from this list.
     */
    protected final boolean hasListeners()
    {
        return SetListenerHelper.hasListeners(myListenerHelper);
    }
}
