package io.opensphere.core.data;

import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;

/**
 * Interface for objects that receive {@link CacheDeposit}s.
 */
@FunctionalInterface
public interface CacheDepositReceiver
{
    /**
     * Method called when a new deposit is available.
     *
     * @param <T> The type of objects in the deposit.
     * @param deposit The deposit.
     * @return The ids for the deposited models, or {@code null} if this
     *         receiver does not perform a deposit.
     * @throws CacheException If there is a problem processing the cache
     *             deposit.
     */
    <T> long[] receive(CacheDeposit<T> deposit) throws CacheException;
}
