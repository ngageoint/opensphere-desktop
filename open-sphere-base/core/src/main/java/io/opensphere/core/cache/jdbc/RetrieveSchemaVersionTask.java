package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.Statement;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;

/**
 * Task for getting the current schema version in the database.
 */
public class RetrieveSchemaVersionTask extends DatabaseTask implements StatementUser<String>
{
    /**
     * Constructor.
     *
     * @param databaseTaskFactory The database task factory.
     */
    public RetrieveSchemaVersionTask(DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to get schema version: ";
    }

    @Override
    public String run(Connection conn, Statement stmt) throws CacheException
    {
        return getCacheUtilities().executeSingleStringQuery(getSQLGenerator().generateRetrieveSchemaVersion(), stmt);
    }
}
