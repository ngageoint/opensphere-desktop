package io.opensphere.core.util;

/**
 * Represents a supplier of results.
 *
 * <p>
 * There is no requirement that a new or distinct result be returned each time
 * the supplier is invoked.
 *
 * @param <T> the type of results supplied by this supplier
 * @param <E> the type of exception that can be thrown
 */
@FunctionalInterface
public interface SupplierX<T, E extends Throwable>
{
    /**
     * Gets a result.
     *
     * @return A result.
     * @throws E if there's an error.
     */
    T get() throws E;
}
