package io.opensphere.core.util.collections.observable;

import java.util.Arrays;

import javafx.beans.InvalidationListener;
import javafx.collections.SetChangeListener;

/**
 * A generic implementation of the {@link SetListenerHelper} providing common
 * operations for managing listeners.
 *
 * @param <E> The data type stored in the underlying set helped by the helper.
 */
public class Generic<E> extends SetListenerHelper<E>
{
    /**
     * An array of invalidation listeners to be notified when references are
     * removed.
     */
    private InvalidationListener[] myInvalidationListeners;

    /** An array of listeners to be notified when set changes are made. */
    private SetChangeListener<? super E>[] myChangeListeners;

    /** The size of the listener array to be invalidated. */
    private int myInvalidationSize;

    /** The size of the change listener array. */
    private int myChangeSize;

    /** A flag used to reflect the locked state of the helper. */
    private boolean myLocked;

    /**
     * Creates a new generic helper with the supplied listeners.
     *
     * @param listener0 a listener to notify of invalidation.
     * @param listener1 a listener to notify of invalidation.
     */
    public Generic(InvalidationListener listener0, InvalidationListener listener1)
    {
        myInvalidationListeners = new InvalidationListener[] { listener0, listener1 };
        myInvalidationSize = 2;
    }

    /**
     * Creates a new generic helper with the supplied listeners.
     *
     * @param listener0 a listener to notify of changes.
     * @param listener1 a listener to notify of changes.
     */
    public Generic(SetChangeListener<? super E> listener0, SetChangeListener<? super E> listener1)
    {
        myChangeListeners = new SetChangeListener[] { listener0, listener1 };
        myChangeSize = 2;
    }

    /**
     * Creates a new generic helper with the supplied listeners.
     *
     * @param invalidationListener the listener to notify of invalidation.
     * @param changeListener the listener to notify of changes.
     */
    public Generic(InvalidationListener invalidationListener, SetChangeListener<? super E> changeListener)
    {
        myInvalidationListeners = new InvalidationListener[] { invalidationListener };
        myInvalidationSize = 1;
        myChangeListeners = new SetChangeListener[] { changeListener };
        myChangeSize = 1;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#addListener(javafx.beans.InvalidationListener)
     */
    @Override
    protected Generic<E> addListener(InvalidationListener listener)
    {
        if (myInvalidationListeners == null)
        {
            myInvalidationListeners = new InvalidationListener[] { listener };
            myInvalidationSize = 1;
        }
        else
        {
            final int oldCapacity = myInvalidationListeners.length;
            if (myLocked)
            {
                final int newCapacity = (myInvalidationSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                myInvalidationListeners = Arrays.copyOf(myInvalidationListeners, newCapacity);
            }
            else if (myInvalidationSize == oldCapacity)
            {
                myInvalidationSize = trim(myInvalidationSize, myInvalidationListeners);
                if (myInvalidationSize == oldCapacity)
                {
                    final int newCapacity = (oldCapacity * 3) / 2 + 1;
                    myInvalidationListeners = Arrays.copyOf(myInvalidationListeners, newCapacity);
                }
            }
            myInvalidationListeners[myInvalidationSize++] = listener;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#removeListener(javafx.beans.InvalidationListener)
     */
    @Override
    protected SetListenerHelper<E> removeListener(InvalidationListener listener)
    {
        if (myInvalidationListeners != null)
        {
            for (int index = 0; index < myInvalidationSize; index++)
            {
                if (listener.equals(myInvalidationListeners[index]))
                {
                    if (myInvalidationSize == 1)
                    {
                        if (myChangeSize == 1)
                        {
                            return new SingleChange<>(myChangeListeners[0]);
                        }
                        myInvalidationListeners = null;
                        myInvalidationSize = 0;
                    }
                    else if ((myInvalidationSize == 2) && (myChangeSize == 0))
                    {
                        return new SingleInvalidation<>(myInvalidationListeners[1 - index]);
                    }
                    else
                    {
                        final int numMoved = myInvalidationSize - index - 1;
                        final InvalidationListener[] oldListeners = myInvalidationListeners;
                        if (myLocked)
                        {
                            myInvalidationListeners = new InvalidationListener[myInvalidationListeners.length];
                            System.arraycopy(oldListeners, 0, myInvalidationListeners, 0, index);
                        }
                        if (numMoved > 0)
                        {
                            System.arraycopy(oldListeners, index + 1, myInvalidationListeners, index, numMoved);
                        }
                        myInvalidationSize--;
                        if (!myLocked)
                        {
                            // Let gc do its work:
                            myInvalidationListeners[myInvalidationSize] = null;
                        }
                    }
                    break;
                }
            }
        }
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
        if (myChangeListeners == null)
        {
            myChangeListeners = new SetChangeListener[] { listener };
            myChangeSize = 1;
        }
        else
        {
            final int oldCapacity = myChangeListeners.length;
            if (myLocked)
            {
                final int newCapacity = (myChangeSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                myChangeListeners = Arrays.copyOf(myChangeListeners, newCapacity);
            }
            else if (myChangeSize == oldCapacity)
            {
                myChangeSize = trim(myChangeSize, myChangeListeners);
                if (myChangeSize == oldCapacity)
                {
                    final int newCapacity = (oldCapacity * 3) / 2 + 1;
                    myChangeListeners = Arrays.copyOf(myChangeListeners, newCapacity);
                }
            }
            myChangeListeners[myChangeSize++] = listener;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.collections.observable.SetListenerHelper#removeListener(javafx.collections.SetChangeListener)
     */
    @Override
    protected SetListenerHelper<E> removeListener(SetChangeListener<? super E> listener)
    {
        if (myChangeListeners != null)
        {
            for (int index = 0; index < myChangeSize; index++)
            {
                if (listener.equals(myChangeListeners[index]))
                {
                    if (myChangeSize == 1)
                    {
                        if (myInvalidationSize == 1)
                        {
                            return new SingleInvalidation<>(myInvalidationListeners[0]);
                        }
                        myChangeListeners = null;
                        myChangeSize = 0;
                    }
                    else if ((myChangeSize == 2) && (myInvalidationSize == 0))
                    {
                        return new SingleChange<>(myChangeListeners[1 - index]);
                    }
                    else
                    {
                        final int numMoved = myChangeSize - index - 1;
                        final SetChangeListener<? super E>[] oldListeners = myChangeListeners;
                        if (myLocked)
                        {
                            myChangeListeners = new SetChangeListener[myChangeListeners.length];
                            System.arraycopy(oldListeners, 0, myChangeListeners, 0, index);
                        }
                        if (numMoved > 0)
                        {
                            System.arraycopy(oldListeners, index + 1, myChangeListeners, index, numMoved);
                        }
                        myChangeSize--;
                        if (!myLocked)
                        {
                            // Let gc do its work
                            myChangeListeners[myChangeSize] = null;
                        }
                    }
                    break;
                }
            }
        }
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
        final InvalidationListener[] curInvalidationList = myInvalidationListeners;
        final int curInvalidationSize = myInvalidationSize;
        final SetChangeListener<? super E>[] curChangeList = myChangeListeners;
        final int curChangeSize = myChangeSize;

        try
        {
            myLocked = true;
            for (int i = 0; i < curInvalidationSize; i++)
            {
                try
                {
                    curInvalidationList[i].invalidated(change.getSet());
                }
                catch (Exception e)
                {
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
            for (int i = 0; i < curChangeSize; i++)
            {
                try
                {
                    curChangeList[i].onChanged(change);
                }
                catch (Exception e)
                {
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        }
        finally
        {
            myLocked = false;
        }
    }
}
