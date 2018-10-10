package io.opensphere.core.cache.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;

/**
 * A runnable that determines if the number of rows in the database is over a
 * threshold, and expires the oldest groups until the database is under the
 * threshold.
 */
public class RowLimitDataTrimmer implements Runnable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RowLimitDataTrimmer.class);

    /** The cache utilities instance. */
    private final CacheUtilities myCacheUtilities;

    /** The connection appropriator. */
    private final ConnectionAppropriator myConnectionAppropriator;

    /** A lock to use when accessing the database. */
    private final Lock myLock;

    /** The threshold on the number of rows in the database. */
    private final int myRowLimit;

    /** The SQL generator. */
    private final SQLGenerator mySQLGenerator;

    /**
     * Constructor.
     *
     * @param rowLimit The threshold on the number of rows in the database.
     * @param cacheUtilities The cache utilities instance.
     * @param connectionAppropriator A database connection appropriator.
     * @param sqlGenerator A generator for SQL.
     * @param lock A lock to use when accessing the database.
     */
    public RowLimitDataTrimmer(int rowLimit, CacheUtilities cacheUtilities, ConnectionAppropriator connectionAppropriator,
            SQLGenerator sqlGenerator, Lock lock)
    {
        myRowLimit = rowLimit;
        myCacheUtilities = cacheUtilities;
        myConnectionAppropriator = connectionAppropriator;
        mySQLGenerator = sqlGenerator;
        myLock = lock;
    }

    @Override
    public void run()
    {
        try
        {
            trimDataTables();
        }
        catch (CacheException e)
        {
            LOGGER.warn("Failed to trim database tables: " + e, e);
        }
    }

    /**
     * Trim excess rows from a data table.
     *
     * @param rowLimit The maximum number of rows in the table.
     * @param stmt A database statement.
     * @throws CacheException If there's a database error.
     */
    public void trimDataTable(final int rowLimit, Statement stmt) throws CacheException
    {
        try
        {
            TIntList groupIds = new TIntArrayList();
            int count = getGroupsToRemove(rowLimit, stmt, groupIds);
            if (count >= rowLimit)
            {
                String sql = mySQLGenerator.generateExpireGroups(groupIds.toArray());
                myCacheUtilities.execute(sql, stmt);
            }
        }
        catch (SQLException | CacheException e)
        {
            throw new CacheException("Failed to trim database: " + e, e);
        }
    }

    /**
     * Determine which groups to remove if the database table is beyond the row
     * limit.
     *
     * @param rowLimit The limit on the number of rows in the table.
     * @param stmt The database statement to use.
     * @param groupsToRemove The return collection.
     * @return The number of rows in the table.
     * @throws CacheException If there is a database error.
     * @throws SQLException If there is a database error.
     */
    @SuppressFBWarnings("SQL_NONCONSTANT_STRING_PASSED_TO_EXECUTE")
    protected int getGroupsToRemove(int rowLimit, Statement stmt, TIntList groupsToRemove) throws CacheException, SQLException
    {
        StringBuilder sql = new StringBuilder(132);
        sql.append("select GROUP_ID from ").append(TableNames.DATA_GROUP).append(SQL.WHERE).append(SQL.NOT_CRITICAL_QUERY)
        .append(SQL.AND).append(SQL.EXPIRATION_TIME_QUERY).append(" > 0 ").append(SQL.ORDER_BY)
        .append(ColumnNames.EXPIRATION_TIME).append(", ").append(ColumnNames.CREATION_TIME).append(" DESC");
        ResultSet rs = myCacheUtilities.executeQuery(stmt, sql.toString());
        int[] groupIds;
        try
        {
            groupIds = myCacheUtilities.convertResultSetToIntArray(rs);
        }
        finally
        {
            try
            {
                rs.close();
            }
            catch (SQLException e)
            {
                myCacheUtilities.handleResultSetCloseException(e);
            }
        }

        int count = 0;
        final float targetRatio = .8f;
        for (int groupId : groupIds)
        {
            sql.setLength(0);
            sql.append(SQL.SELECT).append("count(*)").append(SQL.FROM).append(TableNames.getDataTableName(groupId));
            count += myCacheUtilities.executeSingleIntQuery(stmt, sql.toString());

            if (count >= rowLimit * targetRatio)
            {
                groupsToRemove.add(groupId);
            }
        }
        return count;
    }

    /**
     * Trim a database table if it is up to or beyond the row limit.
     *
     * @throws CacheException If there is a database problem.
     */
    protected synchronized void trimDataTables() throws CacheException
    {
        // Only run if the lock is immediately available.
        if (myLock.tryLock())
        {
            try
            {
                myConnectionAppropriator.appropriateStatement((StatementUser<Void>)(conn, stmt) ->
                {
                    trimDataTable(myRowLimit, stmt);
                    return null;
                }, false);
            }
            finally
            {
                myLock.unlock();
            }
        }
    }
}
