package io.opensphere.auxiliary.cache.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.CacheUtilities;
import io.opensphere.core.cache.jdbc.ColumnNames;
import io.opensphere.core.cache.jdbc.ConnectionAppropriator;
import io.opensphere.core.cache.jdbc.SQL;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.cache.jdbc.TableNames;
import io.opensphere.core.util.lang.ThreadUtilities;

/**
 * A runnable that determines if the amount of data in the database is over a
 * threshold, and deletes some portion of data, starting with the oldest groups.
 */
public class DatabaseSizeDataTrimmer implements Runnable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DatabaseSizeDataTrimmer.class);

    /** The cache utilities instance. */
    private final CacheUtilities myCacheUtilities;

    /** A runnable that will compact the database. */
    private final DatabaseCompactor myCompactor;

    /** The connection appropriator. */
    private final ConnectionAppropriator myConnectionAppropriator;

    /** A lock to use when accessing the database. */
    private final Lock myLock;

    /** The threshold on the number of bytes in the database. */
    private volatile long mySizeLimitBytes;

    /** The SQL generator. */
    private final H2SQLGeneratorImpl mySQLGenerator;

    /**
     * Constructor.
     *
     * @param sizeLimitBytes The threshold on the number of bytes in the
     *            database.
     * @param cacheUtilities The cache utilities instance.
     * @param sqlGenerator The SQL generator.
     * @param connectionAppropriator A database connection appropriator.
     * @param lock A lock to use when accessing the database.
     * @param compactor A runnable that will compact the database.
     */
    public DatabaseSizeDataTrimmer(long sizeLimitBytes, CacheUtilities cacheUtilities, H2SQLGeneratorImpl sqlGenerator,
            ConnectionAppropriator connectionAppropriator, Lock lock, DatabaseCompactor compactor)
    {
        mySizeLimitBytes = sizeLimitBytes;
        myCacheUtilities = cacheUtilities;
        mySQLGenerator = sqlGenerator;
        myConnectionAppropriator = connectionAppropriator;
        myLock = lock;
        myCompactor = compactor;
    }

    /**
     * Get the current size limit.
     *
     * @return The size limit in bytes.
     */
    public long getSizeLimitBytes()
    {
        return mySizeLimitBytes;
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
     * Set the current size limit.
     *
     * @param sizeLimitBytes The size limit in bytes.
     */
    public void setSizeLimitBytes(long sizeLimitBytes)
    {
        LOGGER.info("Setting database size threshold to " + sizeLimitBytes + "B");
        mySizeLimitBytes = sizeLimitBytes;
    }

    /**
     * Drop data tables if the current database size is over the limit.
     *
     * @param sizeLimitBytes The maximum number of bytes in the database.
     * @param stmt A database statement.
     * @return The ids of the groups to be deleted.
     * @throws CacheException If there's a database error.
     */
    public int[] trimDataTables(final long sizeLimitBytes, Statement stmt) throws CacheException
    {
        try
        {
            long currentSize = getCurrentSize(stmt);
            if (currentSize > sizeLimitBytes)
            {
                getCacheUtil().getGarbageCollector().reset();
                int[] groupIds = getGroupsToRemove(stmt);
                if (groupIds.length > 0)
                {
                    LOGGER.info("Current database size is " + currentSize + "B, which is greater than the limit of "
                            + sizeLimitBytes + "B. Trimming/compacting database.");
                    String sql = mySQLGenerator.generateExpireGroups(groupIds);
                    getCacheUtil().execute(sql, stmt);

                    // Sleep a couple seconds to let any users of the expired
                    // groups finish.
                    ThreadUtilities.sleep(2000);

                    return groupIds;
                }
            }
            return new int[0];
        }
        catch (SQLException | CacheException e)
        {
            throw new CacheException("Failed to compact database: " + e, e);
        }
    }

    /**
     * Get the cache utilities.
     *
     * @return The cache utilities.
     */
    protected CacheUtilities getCacheUtil()
    {
        return myCacheUtilities;
    }

    /**
     * Get the connection appropriator.
     *
     * @return The connection appropriator.
     */
    protected ConnectionAppropriator getConnectionAppropriator()
    {
        return myConnectionAppropriator;
    }

    /**
     * Get the current database size in bytes.
     *
     * @param stmt The database statement.
     * @return The size of the database.
     * @throws CacheException If there's a database error.
     */
    protected long getCurrentSize(Statement stmt) throws CacheException
    {
        int pageCount = getCacheUtil().executeSingleIntQuery(stmt, mySQLGenerator.generateGetPageCount());
        int pageSize = getCacheUtil().executeSingleIntQuery(stmt, mySQLGenerator.generateGetPageSize());
        long currentSize = (long)pageSize * pageCount;
        return currentSize;
    }

    /**
     * Determine which groups to remove if the database table is beyond the size
     * limit.
     *
     * @param stmt The database statement to use.
     *
     * @return An array of group ids.
     * @throws CacheException If there is a database error.
     * @throws SQLException If there is a database error.
     */
    protected int[] getGroupsToRemove(Statement stmt) throws CacheException, SQLException
    {
        StringBuilder sql = new StringBuilder(132);
        sql.append("select GROUP_ID from ").append(TableNames.DATA_GROUP).append(SQL.WHERE).append(SQL.NOT_CRITICAL_QUERY)
                .append(SQL.ORDER_BY).append(ColumnNames.EXPIRATION_TIME).append(", ").append(ColumnNames.CREATION_TIME)
                .append(" DESC");
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

        return Arrays.copyOfRange(groupIds, 0, groupIds.length / 2);
    }

    /**
     * Trim a database table if it is up to or beyond the size limit.
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
                int[] groupIds = myConnectionAppropriator.appropriateStatement(new StatementUser<int[]>()
                {
                    @Override
                    public int[] run(Connection conn, Statement stmt) throws CacheException
                    {
                        return trimDataTables(getSizeLimitBytes(), stmt);
                    }
                }, false);
                if (groupIds.length > 0)
                {
                    myCompactor.compact(groupIds);

                    myConnectionAppropriator.appropriateStatement(new StatementUser<Void>()
                    {
                        @Override
                        public Void run(Connection conn, Statement stmt) throws CacheException
                        {
                            LOGGER.info("Database size is now " + getCurrentSize(stmt) + "B");
                            return null;
                        }
                    });
                }
            }
            finally
            {
                myLock.unlock();
            }
        }
    }
}
