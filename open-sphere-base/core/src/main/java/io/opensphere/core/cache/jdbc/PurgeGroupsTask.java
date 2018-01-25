package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.Statement;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;

/**
 * Database task for removing data groups from the database.
 */
public class PurgeGroupsTask extends DatabaseTask implements StatementUser<Void>
{
    /** The group ids. */
    private final int[] myGroupIds;

    /**
     * Constructor.
     *
     * @param groupIds The group ids.
     * @param databaseTaskFactory The database task factory.
     */
    protected PurgeGroupsTask(int[] groupIds, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        myGroupIds = groupIds;
    }

    @Override
    public String getTimingMessage()
    {
        return "Purged " + getGroupIds().length + " groups from the database in ";
    }

    @Override
    public Void run(Connection conn, Statement stmt) throws CacheException
    {
        for (int groupId : getGroupIds())
        {
            purgeDataGroupRow(stmt, groupId);
            purgeGroupTable(groupId, conn, stmt);
        }
        return null;
    }

    /**
     * Accessor for the group ids.
     *
     * @return The group ids.
     */
    protected int[] getGroupIds()
    {
        return myGroupIds;
    }

    /**
     * Purge a single group.
     *
     * @param stmt The database statement.
     * @param groupId The group id.
     * @throws CacheException If there's a database error.
     */
    protected void purgeDataGroupRow(Statement stmt, int groupId) throws CacheException
    {
        // This should only be done once all the references
        // have been removed, to avoid locking multiple tables and risking
        // deadlock.
        String sql = getSQLGenerator().generateDeleteGroup(groupId);
        getCacheUtilities().execute(sql, stmt);
    }

    /**
     * Purge a data group from the database.
     *
     * @param groupId The group id.
     * @param conn The database connection.
     * @param stmt The database statement.
     * @throws CacheException If there is a database error.
     */
    protected void purgeGroupTable(int groupId, Connection conn, Statement stmt) throws CacheException
    {
        String tableName = TableNames.getDataTableName(groupId);
        getCacheUtilities().execute(SQL.DROP_TABLE + tableName, stmt);

        getDatabaseState().removeGroup(groupId);
    }
}
