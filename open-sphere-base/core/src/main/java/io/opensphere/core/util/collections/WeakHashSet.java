package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * A weak hash set that wraps a {@link WeakHashMap}, using only the keys.
 *
 * @param <E> The type of the elements in the set.
 */
public class WeakHashSet<E> extends WrappedMapSet<E, Void>
{
    /**
     * Construct an empty weak hash set.
     */
    public WeakHashSet()
    {
        super(new WeakHashMap<E, Void>(), null);
    }

    /**
     * Construct a weak hash set containing the values from another collection.
     *
     * @param c The other collection.
     */
    public WeakHashSet(Collection<? extends E> c)
    {
        super(new WeakHashMap<E, Void>(c.size()), null);
        addAll(c);
    }

    /**
     * Construct a weak hash set with an initial capacity. See {@link HashMap}
     * for a discussion on initial capacity and performance considerations.
     *
     * @param initialCapacity The initial capacity.
     * @see HashMap
     */
    public WeakHashSet(int initialCapacity)
    {
        super(new WeakHashMap<E, Void>(initialCapacity), null);
    }

    /**
     * Construct a weak hash set with an initial capacity and load factor. See
     * {@link HashMap} for a discussion on initial capacity, load factor, and
     * performance considerations.
     *
     * @param initialCapacity The initial capacity.
     * @param loadFactor The load factor.
     * @see HashMap
     */
    public WeakHashSet(int initialCapacity, float loadFactor)
    {
        super(new WeakHashMap<E, Void>(initialCapacity, loadFactor), null);
    }
}
