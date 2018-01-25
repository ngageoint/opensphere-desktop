package io.opensphere.core.util.ref;

/**
 * Interface for an object that refers to another object.
 *
 * @param <T> The type of object that this object refers to.
 */
public interface Reference<T>
{
    /**
     * Clear the reference.
     */
    void clear();

    /**
     * Get the referent.
     *
     * @return The object, or <code>null</code> if the reference has been
     *         cleared.
     */
    T get();
}
