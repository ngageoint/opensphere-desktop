package io.opensphere.core.util.collections.observable;

import java.util.function.Predicate;

import javafx.beans.WeakListener;

/**
 * Base class for expression helpers, which contains utility methods useful in
 * concrete implementations.
 */
public abstract class ExpressionHelperBase
{
    /**
     * Trims the size of the listeners, removing any garbage collected weak
     * references.
     *
     * @param size the initial size of the array (note that the actual array may
     *            be larger) to examine.
     * @param listeners the listeners to examine for obsolete references.
     * @return the new size, after removing obsolete references.
     */
    protected static int trim(int size, Object[] listeners)
    {
        Predicate<Object> p = t -> t instanceof WeakListener && ((WeakListener)t).wasGarbageCollected();
        int index = 0;
        for (; index < size; index++)
        {
            if (p.test(listeners[index]))
            {
                break;
            }
        }
        if (index < size)
        {
            for (int src = index + 1; src < size; src++)
            {
                if (!p.test(listeners[src]))
                {
                    listeners[index++] = listeners[src];
                }
            }
            int oldSize = size;
            size = index;
            for (; index < oldSize; index++)
            {
                listeners[index] = null;
            }
        }

        return size;
    }

}
