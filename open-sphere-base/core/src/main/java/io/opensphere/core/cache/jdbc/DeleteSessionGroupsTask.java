package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Database task for deleting session groups (groups with {@code null}
 * expiration).
 */
public class DeleteSessionGroupsTask extends AbstractDeleteGroupsTask
{
    /**
     * Constructor.
     *
     * @param databaseTaskFactory The database task factory.
     */
    public DeleteSessionGroupsTask(DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to delete session-only groups: ";
    }

    @Override
    protected String getWhereExpression(Connection conn, Statement stmt)
    {
        return ColumnNames.EXPIRATION_TIME + " is null";
    }
}
