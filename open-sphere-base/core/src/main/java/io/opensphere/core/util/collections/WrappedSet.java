package io.opensphere.core.util.collections;

import java.util.Set;

/**
 * A set implementation that wraps another set and passes all methods to it.
 * This class may be extended to change the behavior of the set without having
 * the extend the set directly.
 *
 * @param <E> The type of the elements in the set.
 */
public class WrappedSet<E> extends AbstractProxySet<E>
{
    /** The wrapped set. */
    private final Set<E> mySet;

    /**
     * Construct the set.
     *
     * @param set The set to wrap.
     */
    public WrappedSet(Set<E> set)
    {
        mySet = set;
    }

    @Override
    protected final Set<E> getSet()
    {
        return mySet;
    }
}
