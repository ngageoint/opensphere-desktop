package io.opensphere.core.cache.jdbc;

import io.opensphere.core.util.TimingMessageProvider;
import io.opensphere.core.util.Utilities;

/**
 * Abstract base class for database tasks.
 */
public abstract class DatabaseTask implements TimingMessageProvider
{
    /** The database task factory. */
    private final DatabaseTaskFactory myDatabaseTaskFactory;

    /**
     * Constructor.
     *
     * @param databaseTaskFactory The database task factory.
     */
    protected DatabaseTask(DatabaseTaskFactory databaseTaskFactory)
    {
        Utilities.checkNull(databaseTaskFactory, "databaseTaskFactory");
        myDatabaseTaskFactory = databaseTaskFactory;
    }

    /**
     * Get the in-memory cache of the database state.
     *
     * @return The database state.
     */
    public DatabaseState getDatabaseState()
    {
        return myDatabaseTaskFactory.getDatabaseState();
    }

    /**
     * Accessor for the cacheUtilities.
     *
     * @return The cacheUtilities.
     */
    protected final CacheUtilities getCacheUtilities()
    {
        return myDatabaseTaskFactory.getCacheUtilities();
    }

    /**
     * Accessor for the databaseTaskFactory.
     *
     * @return The databaseTaskFactory.
     */
    protected DatabaseTaskFactory getDatabaseTaskFactory()
    {
        return myDatabaseTaskFactory;
    }

    /**
     * Accessor for the sqlGenerator.
     *
     * @return The sqlGenerator.
     */
    protected final SQLGenerator getSQLGenerator()
    {
        return myDatabaseTaskFactory.getSQLGenerator();
    }

    /**
     * Accessor for the typeMapper.
     *
     * @return The typeMapper.
     */
    protected final TypeMapper getTypeMapper()
    {
        return myDatabaseTaskFactory.getTypeMapper();
    }
}
