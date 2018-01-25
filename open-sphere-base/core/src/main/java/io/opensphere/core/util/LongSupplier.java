package io.opensphere.core.util;

/**
 * Represents a supplier of results.
 *
 * <p>
 * There is no requirement that a new or distinct result be returned each time
 * the supplier is invoked.
 */
@FunctionalInterface
public interface LongSupplier
{
    /**
     * Gets a result.
     *
     * @return A result.
     */
    long get();
}
