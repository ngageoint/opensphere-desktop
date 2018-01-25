package io.opensphere.core.cache.jdbc;

import java.sql.Connection;

import io.opensphere.core.cache.CacheException;

/**
 * A facility for getting a connection to the database.
 */
@FunctionalInterface
public interface ConnectionSource
{
    /**
     * Get a connection to the database.
     *
     * @return The connection.
     * @throws CacheException If there's an error connecting to the database.
     */
    Connection getConnection() throws CacheException;
}
