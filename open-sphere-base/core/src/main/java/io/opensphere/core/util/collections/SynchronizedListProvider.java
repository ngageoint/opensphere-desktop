package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A {@link ListProvider} that provides synchronized lists.
 *
 * @param <E> The type of objects.
 */
public class SynchronizedListProvider<E> implements ListProvider<E>
{
    /** The nested provider. */
    private final ListProvider<E> myNestedProvider;

    /**
     * Construct the provider from another list provider.
     *
     * @param nestedProvider The provider that this provider will use to create
     *            the (presumably unsynchronized) lists.
     */
    public SynchronizedListProvider(ListProvider<E> nestedProvider)
    {
        myNestedProvider = nestedProvider;
    }

    @Override
    public List<E> get()
    {
        return Collections.synchronizedList(myNestedProvider.get());
    }

    @Override
    public List<E> get(Collection<? extends E> contents)
    {
        return Collections.synchronizedList(myNestedProvider.get(contents));
    }

    @Override
    public List<E> get(int size)
    {
        return Collections.synchronizedList(myNestedProvider.get(size));
    }

    @Override
    public List<E> getEmpty()
    {
        return myNestedProvider.getEmpty();
    }
}
