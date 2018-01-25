package io.opensphere.core.util;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.GuardedBy;

import io.opensphere.core.util.ref.Reference;

/**
 * Support for notifying interested parties of generic changes.
 *
 * @param <T> The supported listener type.
 */
@javax.annotation.concurrent.ThreadSafe
public abstract class AbstractChangeSupport<T> implements ChangeSupport<T>
{
    /** The listeners. */
    @SuppressWarnings("unchecked")
    @GuardedBy("this")
    private volatile Reference<T>[] myListeners = (Reference<T>[])new Reference<?>[0];

    /**
     * Add a listener for changes. Only a weak reference to the listener is
     * held.
     *
     * @param listener The listener.
     */
    @Override
    public synchronized void addListener(T listener)
    {
        checkReferences();

        @SuppressWarnings("unchecked")
        Reference<T>[] arr = (Reference<T>[])new Reference<?>[myListeners.length + 1];
        System.arraycopy(myListeners, 0, arr, 0, myListeners.length);
        arr[arr.length - 1] = createReference(listener);
        myListeners = arr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clearListeners()
    {
        myListeners = (Reference<T>[])new Reference<?>[0];
    }

    @Override
    public int getListenerCount()
    {
        return myListeners.length;
    }

    /**
     * Get a copy of the listeners.
     *
     * @return A copy of the listeners.
     */
    public synchronized Reference<T>[] getListeners()
    {
        return myListeners.clone();
    }

    @Override
    public ReferenceService<T> getListenerService(T listener)
    {
        return new ReferenceService<T>(listener)
        {
            @Override
            public void close()
            {
                removeListener(listener);
            }

            @Override
            public void open()
            {
                addListener(listener);
            }
        };
    }

    @Override
    public boolean isEmpty()
    {
        return myListeners.length == 0;
    }

    @Override
    public void notifyListeners(ChangeSupport.Callback<T> callback)
    {
        notifyListeners(callback, (Executor)null);
    }

    @Override
    public void notifyListeners(final ChangeSupport.Callback<T> callback, Executor executor)
    {
        boolean checkReferences = false;

        Reference<T>[] listeners = myListeners;
        if (executor == null)
        {
            for (Reference<T> ref : listeners)
            {
                T listener = ref.get();
                if (listener == null)
                {
                    checkReferences = true;
                }
                else
                {
                    callback.notify(listener);
                }
            }
        }
        else
        {
            for (Reference<T> ref : listeners)
            {
                final T listener = ref.get();
                if (listener == null)
                {
                    checkReferences = true;
                }
                else
                {
                    executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            synchronized (AbstractChangeSupport.this)
                            {
                                callback.notify(listener);
                            }
                        }
                    });
                }
            }
        }
        if (checkReferences)
        {
            checkReferences();
        }
    }

    @Override
    public void notifyListenersSingle(final ChangeSupport.Callback<T> callback, Executor executor)
    {
        executor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                notifyListeners(callback);
            }
        });
    }

    @Override
    public synchronized boolean removeListener(T listener)
    {
        checkReferences();

        boolean removed = false;
        for (int index = 0; index < myListeners.length; ++index)
        {
            if (myListeners[index].get() == listener)
            {
                removeListener(index--);
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        if (myListeners.length == 0)
        {
            sb.append(']');
        }
        else
        {
            for (Reference<T> listener : myListeners)
            {
                sb.append(listener.get()).append(", ");
            }
            sb.replace(sb.length() - 2, sb.length(), "]");
        }

        return sb.toString();
    }

    /**
     * Create a reference to a listener.
     *
     * @param listener The listener.
     * @return The reference.
     */
    protected abstract Reference<T> createReference(T listener);

    /**
     * Check the listener references and remove any that have been cleared.
     */
    private synchronized void checkReferences()
    {
        for (int index = 0; index < myListeners.length; ++index)
        {
            if (myListeners[index].get() == null)
            {
                removeListener(index--);
            }
        }
    }

    /**
     * Remove a listener from the array.
     *
     * @param index The index to remove.
     */
    private synchronized void removeListener(int index)
    {
        @SuppressWarnings("unchecked")
        Reference<T>[] arr = (Reference<T>[])new Reference<?>[myListeners.length - 1];
        if (arr.length > 0)
        {
            if (index > 0)
            {
                System.arraycopy(myListeners, 0, arr, 0, index);
            }
            if (index < arr.length)
            {
                System.arraycopy(myListeners, index + 1, arr, index, arr.length - index);
            }
        }
        myListeners = arr;
    }
}
