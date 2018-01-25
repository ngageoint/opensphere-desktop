package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheIdUtilities;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.Utilities;

/**
 * Database task that retrieves property value sizes from the database for
 * specific models.
 */
public class RetrieveValueSizesTask extends DatabaseTask implements StatementUser<long[]>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RetrieveValueSizesTask.class);

    /** The combined models ids. */
    private final long[] myIds;

    /** Descriptors for the properties. */
    private final Collection<? extends PropertyDescriptor<?>> myPropertyDescriptors;

    /**
     * Constructor.
     *
     * @param ids The combined ids of the models.
     * @param desc The descriptor for the property.
     * @param databaseTaskFactory The database task factory.
     */
    protected RetrieveValueSizesTask(long[] ids, PropertyDescriptor<?> desc, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        myIds = Utilities.checkNull(ids, "ids").clone();
        myPropertyDescriptors = Collections.<PropertyDescriptor<?>>singleton(Utilities.checkNull(desc, "desc"));
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to get value sizes for property values over " + getIds().length + " ids: ";
    }

    @Override
    public long[] run(final Connection conn, final Statement stmt) throws CacheException
    {
        final long[] result = new long[getIds().length];
        Collection<? extends PropertyDescriptor<?>> descriptors = getPropertyDescriptors();
        final List<String> columnNames = getTypeMapper().getColumnNames(descriptors);
        if (columnNames.isEmpty())
        {
            return result;
        }
        int[] distinctGroupIds = getCacheUtilities().getGroupIdsFromCombinedIds(getIds(), true);
        getDatabaseTaskFactory().getEnsureColumnsTask(distinctGroupIds, descriptors).run(conn);

        CacheIdUtilities.forEachGroup(getIds(), new CacheIdUtilities.DatabaseGroupFunctor()
        {
            @Override
            public void run(long[] combinedIds, int groupId, int[] dataIds) throws CacheException
            {
                long[] sizes = doGetValueSizes(conn, stmt, groupId, dataIds, columnNames);

                boolean found = false;
                long firstId = getCacheUtilities().getCombinedId(groupId, dataIds[0]);
                for (int index = 0; index < getIds().length && !found; ++index)
                {
                    if (getIds()[index] == firstId)
                    {
                        System.arraycopy(sizes, 0, result, index, sizes.length);
                        found = true;
                    }
                }
                if (!found)
                {
                    throw new CacheException("Could not find matching combined id.");
                }
            }
        });

        return result;
    }

    /**
     * Extract property values from the database.
     *
     * @param conn The database connection.
     * @param stmt The database statement.
     * @param groupId The data group id.
     * @param dataIds The data ids.
     * @param columnNames The database column names.
     * @return The sizes
     *
     * @throws CacheException If there is a database error.
     */
    protected long[] doGetValueSizes(Connection conn, Statement stmt, int groupId, int[] dataIds,
            final Collection<String> columnNames) throws CacheException
    {
        String tableName = TableNames.getDataTableName(groupId);

        final String sql;
        if (dataIds.length == 1)
        {
            sql = getSQLGenerator().generateRetrieveValueSizes(dataIds[0], tableName, columnNames);
        }
        else
        {
            String joinTableName = getDatabaseTaskFactory()
                    .getCreateIdJoinTableTask(dataIds, ColumnNames.JOIN_ID, ColumnNames.SEQUENCE).run(conn, stmt);
            sql = getSQLGenerator().generateRetrieveValueSizes(joinTableName, tableName, columnNames);
        }

        ResultSet rs = getCacheUtilities().executeQuery(stmt, sql);
        try
        {
            return getCacheUtilities().convertResultSetToLongArray(rs);
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to convert result set to long array: " + e, e);
        }
        finally
        {
            try
            {
                rs.close();
            }
            catch (SQLException e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Failed to close result set: " + e, e);
                }
            }
        }
    }

    /**
     * Accessor for the ids.
     *
     * @return The ids.
     */
    protected long[] getIds()
    {
        return myIds;
    }

    /**
     * Get the property descriptors.
     *
     * @return The property descriptors.
     */
    protected Collection<? extends PropertyDescriptor<?>> getPropertyDescriptors()
    {
        return myPropertyDescriptors;
    }
}
