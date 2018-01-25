package io.opensphere.core.util.collections;

import java.util.Collection;
import java.util.Collections;

/**
 * Interface for a facility that provides a collection lazily. Only one
 * collection is ever created, and it is only created if one of the {@code get}
 * methods is called (but not {@link #getEmpty} or {@link #getUnmodifiable}).
 *
 * @param <E> The type of elements in the provided collections.
 */
public interface LazyCollectionProvider<E> extends CollectionProvider<E>
{
    /**
     * Get an unmodifiable version of the wrapped collection. If no collection
     * has been created, {@link Collections#emptySet()} is returned.
     *
     * @return The collection.
     */
    Collection<? extends E> getUnmodifiable();
}
