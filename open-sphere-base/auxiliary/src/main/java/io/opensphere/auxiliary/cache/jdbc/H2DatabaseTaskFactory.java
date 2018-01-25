package io.opensphere.auxiliary.cache.jdbc;

import io.opensphere.core.cache.jdbc.CacheUtilities;
import io.opensphere.core.cache.jdbc.DatabaseTaskFactory;
import io.opensphere.core.cache.jdbc.SQLGenerator;

/**
 * An extension to {@link DatabaseTaskFactory} that provides extended versions
 * of some tasks to support H2.
 */
public class H2DatabaseTaskFactory extends DatabaseTaskFactory
{
    /**
     * Constructor.
     *
     * @param cacheUtilities The cache utilities.
     * @param databaseState The database state.
     * @param sqlGenerator The SQL generator.
     * @param typeMapper The type mapper.
     */
    public H2DatabaseTaskFactory(CacheUtilities cacheUtilities, H2DatabaseState databaseState, SQLGenerator sqlGenerator,
            H2TypeMapper typeMapper)
    {
        super(cacheUtilities, databaseState, sqlGenerator, typeMapper);
    }

    @Override
    public H2DatabaseState getDatabaseState()
    {
        return (H2DatabaseState)super.getDatabaseState();
    }

    @Override
    public H2TypeMapper getTypeMapper()
    {
        return (H2TypeMapper)super.getTypeMapper();
    }
}
