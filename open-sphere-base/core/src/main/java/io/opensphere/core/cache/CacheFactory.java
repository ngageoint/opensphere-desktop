package io.opensphere.core.cache;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Interface for a factory that creates {@link Cache}s.
 */
@FunctionalInterface
public interface CacheFactory
{
    /**
     * Create the cache.
     *
     * @param path The path to the database file.
     * @param rowLimit The maximum number of rows in a table before trimming
     *            occurs. A negative number indicates no limit.
     * @param executor An executor service for background cache tasks.
     * @return The cache implementation.
     * @throws ClassNotFoundException If the database driver cannot be loaded.
     * @throws CacheException If the tables cannot be created.
     */
    Cache create(String path, int rowLimit, ScheduledExecutorService executor) throws ClassNotFoundException, CacheException;
}
