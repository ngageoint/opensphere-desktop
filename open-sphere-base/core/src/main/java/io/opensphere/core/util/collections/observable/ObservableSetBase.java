package io.opensphere.core.util.collections.observable;

import java.util.AbstractSet;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 * @author Andres Almiray
 * @since 2.9.0
 */
public abstract class ObservableSetBase<E> extends AbstractSet<E> implements ObservableSet<E>
{
    private SetListenerHelper<E> listenerHelper;

    @Override
    public final void addListener(InvalidationListener listener)
    {
        listenerHelper = SetListenerHelper.addListener(listenerHelper, listener);
    }

    @Override
    public final void removeListener(InvalidationListener listener)
    {
        listenerHelper = SetListenerHelper.removeListener(listenerHelper, listener);
    }

    @Override
    public final void addListener(SetChangeListener<? super E> listener)
    {
        listenerHelper = SetListenerHelper.addListener(listenerHelper, listener);
    }

    @Override
    public final void removeListener(SetChangeListener<? super E> listener)
    {
        listenerHelper = SetListenerHelper.removeListener(listenerHelper, listener);
    }

    protected final void fireChange(SetChangeListener.Change<? extends E> change)
    {
        SetListenerHelper.fireValueChangedEvent(listenerHelper, change);
    }

    /**
     * Returns true if there are some listeners registered for this list.
     */
    protected final boolean hasListeners()
    {
        return SetListenerHelper.hasListeners(listenerHelper);
    }
}