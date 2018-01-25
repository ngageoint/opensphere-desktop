package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.Statement;

import io.opensphere.core.cache.CacheException;

/**
 * Database task to delete all non-session data groups.
 */
public class DeleteNonSessionGroupsTask extends AbstractDeleteGroupsTask
{
    /**
     * Constructor.
     *
     * @param databaseTaskFactory The database task factory.
     */
    public DeleteNonSessionGroupsTask(DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to delete all non-session groups: ";
    }

    @Override
    protected String getWhereExpression(Connection conn, Statement stmt) throws CacheException
    {
        return ColumnNames.EXPIRATION_TIME + SQL.IS_NOT_NULL;
    }
}
