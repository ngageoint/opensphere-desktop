package io.opensphere.core.util;

import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.StrongReference;

/**
 * Support for notifying interested parties of generic changes. This
 * implementation maintains strong references to the listeners.
 *
 * @param <T> The supported listener type.
 */
@javax.annotation.concurrent.ThreadSafe
public class StrongChangeSupport<T> extends AbstractChangeSupport<T>
{
    /**
     * Convenience factory for new change supports.
     *
     * @param <T> The type of the listener.
     * @return The change support object.
     */
    public static <T> StrongChangeSupport<T> create()
    {
        return new StrongChangeSupport<T>();
    }

    @Override
    protected Reference<T> createReference(T listener)
    {
        return new StrongReference<T>(listener);
    }
}
