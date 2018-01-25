package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for a facility that provides sets.
 *
 * @param <E> The type of elements in the provided collections.
 */
public interface SetProvider<E> extends CollectionProvider<E>
{
    @Override
    Set<E> get();

    @Override
    Set<E> get(Collection<? extends E> contents);

    @Override
    Set<E> get(int size);

    @Override
    Set<E> getEmpty();
}
