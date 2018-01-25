package io.opensphere.mantle.data.cache.impl;

import java.io.Serializable;
import java.util.Comparator;

import io.opensphere.mantle.data.cache.Priority;

/**
 * The Class RunnablePriorityComparator.
 */
public class RunnablePriorityComparator implements Comparator<Runnable>, Serializable
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new runnable priority comparator.
     */
    public RunnablePriorityComparator()
    {
        super();
    }

    @Override
    public int compare(Runnable o1, Runnable o2)
    {
        Integer i1 = Integer.valueOf(o1 == null ? Integer.MAX_VALUE : o1 instanceof Priority ? ((Priority)o1).getPriority() : 10);
        Integer i2 = Integer.valueOf(o2 == null ? Integer.MAX_VALUE : o2 instanceof Priority ? ((Priority)o2).getPriority() : 10);
        return i1.compareTo(i2);
    }
}
