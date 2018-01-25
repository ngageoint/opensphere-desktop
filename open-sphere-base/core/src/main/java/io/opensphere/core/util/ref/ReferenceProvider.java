package io.opensphere.core.util.ref;

/**
 * Interface for classes that provide references.
 *
 * @param <T> The referent type.
 */
@FunctionalInterface
public interface ReferenceProvider<T>
{
    /**
     * Get the reference.
     *
     * @return The reference.
     */
    Reference<T> getReference();
}
