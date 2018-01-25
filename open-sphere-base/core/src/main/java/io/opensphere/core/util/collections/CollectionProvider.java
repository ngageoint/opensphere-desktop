package io.opensphere.core.util.collections;

import java.util.Collection;

/**
 * Interface for a facility that provides collections.
 *
 * @param <E> The type of elements in the provided collections.
 */
public interface CollectionProvider<E>
{
    /**
     * Get a collection.
     *
     * @return The collection.
     */
    Collection<E> get();

    /**
     * Get a collection, specifying the contents of the collection. The returned
     * collection must contain the provided contents at a minimum, but may also
     * have other contents.
     *
     * @param contents The contents for the collection.
     * @return The collection.
     */
    Collection<E> get(Collection<? extends E> contents);

    /**
     * Get a collection with a suggested size. The size may be ignored at the
     * discretion of the implementation.
     *
     * @param size The suggested size for the collection.
     * @return The collection.
     */
    Collection<E> get(int size);

    /**
     * Get a collection with the intention that the collection will not need to
     * contain anything.
     *
     * @return The collection.
     */
    Collection<E> getEmpty();
}
