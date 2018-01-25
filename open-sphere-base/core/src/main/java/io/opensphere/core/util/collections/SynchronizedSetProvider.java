package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A {@link SetProvider} that provides synchronized sets.
 *
 * @param <E> The type of objects.
 */
public class SynchronizedSetProvider<E> implements SetProvider<E>
{
    /** The nested provider. */
    private final SetProvider<E> myNestedProvider;

    /**
     * Construct the provider from another set provider.
     *
     * @param nestedProvider The provider that this provider will use to create
     *            the (presumably unsynchronized) sets.
     */
    public SynchronizedSetProvider(SetProvider<E> nestedProvider)
    {
        myNestedProvider = nestedProvider;
    }

    @Override
    public Set<E> get()
    {
        return Collections.synchronizedSet(myNestedProvider.get());
    }

    @Override
    public Set<E> get(Collection<? extends E> contents)
    {
        return Collections.synchronizedSet(myNestedProvider.get(contents));
    }

    @Override
    public Set<E> get(int size)
    {
        return Collections.synchronizedSet(myNestedProvider.get(size));
    }

    @Override
    public Set<E> getEmpty()
    {
        return myNestedProvider.getEmpty();
    }
}
