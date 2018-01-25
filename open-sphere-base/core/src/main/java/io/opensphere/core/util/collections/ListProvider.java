package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.List;

/**
 * Interface for a facility that provides sets.
 *
 * @param <E> The type of elements in the provided collections.
 */
public interface ListProvider<E> extends CollectionProvider<E>
{
    @Override
    List<E> get();

    @Override
    List<E> get(Collection<? extends E> contents);

    @Override
    List<E> get(int size);

    @Override
    List<E> getEmpty();
}
