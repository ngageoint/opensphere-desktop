package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;

/**
 * Database task for deleting data groups.
 */
public abstract class AbstractDeleteGroupsTask extends DatabaseTask implements StatementUser<Void>
{
    /**
     * Constructor.
     *
     * @param databaseTaskFactory The database task factory.
     */
    protected AbstractDeleteGroupsTask(DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
    }

    @Override
    public Void run(Connection conn, Statement stmt) throws CacheException
    {
        String sql = buildDeleteGroupsQuery(conn, stmt);
        getCacheUtilities().execute(sql, stmt);
        return null;
    }

    /**
     * Build the SQL for deleting data groups from the database. This doesn't
     * actually delete the groups, but rather changes them to be expired.
     * Deleting a group can take some time, so it's better to defer to a
     * background process.
     *
     * @param conn The database connection.
     * @param stmt The database statement.
     * @return The SQL string.
     * @throws CacheException If there's a database error.
     */
    protected String buildDeleteGroupsQuery(Connection conn, Statement stmt) throws CacheException
    {
        return getSQLGenerator().generateUpdate(TableNames.DATA_GROUP, Collections.singletonMap(ColumnNames.EXPIRATION_TIME, "0"),
                getWhereExpression(conn, stmt));
    }

    /**
     * Get the where expression for the delete.
     *
     * @param conn The database connection.
     * @param stmt The database statement.
     *
     * @return The where expression.
     * @throws CacheException If there's a database error.
     */
    protected abstract String getWhereExpression(Connection conn, Statement stmt) throws CacheException;
}
