package io.opensphere.core.util;

import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.WeakReference;

/**
 * Support for notifying interested parties of generic changes. This
 * implementation maintains only weak references to the listeners, to reduce
 * memory leaks.
 *
 * @param <T> The supported listener type.
 */
@javax.annotation.concurrent.ThreadSafe
public class WeakChangeSupport<T> extends AbstractChangeSupport<T>
{
    /**
     * Convenience factory for new change supports.
     *
     * @param <T> The type of the listener.
     * @return The change support object.
     */
    public static <T> WeakChangeSupport<T> create()
    {
        return new WeakChangeSupport<T>();
    }

    @Override
    protected Reference<T> createReference(T listener)
    {
        return new WeakReference<T>(listener);
    }
}
