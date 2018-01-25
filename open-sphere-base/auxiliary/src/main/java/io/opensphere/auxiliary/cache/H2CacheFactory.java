package io.opensphere.auxiliary.cache;

import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.auxiliary.cache.jdbc.H2CacheImpl;
import io.opensphere.core.cache.Cache;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheFactory;

/**
 * Factory for {@link H2CacheImpl}s.
 */
public class H2CacheFactory implements CacheFactory
{
    @Override
    public Cache create(String path, int rowLimit, ScheduledExecutorService executor)
        throws ClassNotFoundException, CacheException
    {
        return new H2CacheImpl(path, rowLimit, executor);
    }
}
