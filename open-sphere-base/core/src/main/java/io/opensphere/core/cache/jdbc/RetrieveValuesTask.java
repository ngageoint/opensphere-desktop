package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import gnu.trove.list.TIntList;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheIdUtilities;
import io.opensphere.core.cache.PropertyValueMap;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Database task that retrieves property values from the database for specific
 * models.
 */
public class RetrieveValuesTask extends DatabaseTask implements StatementUser<Void>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RetrieveValuesTask.class);

    /** The optional collection of failed indices into {@link #myIds}. */
    private final TIntList myFailedIndices;

    /** The combined models ids. */
    private final long[] myIds;

    /** The map of property descriptors to lists of values. */
    private final PropertyValueMap myResultMap;

    /**
     * Constructor.
     *
     * @param ids The combined ids of the models.
     * @param resultMap An input/output map of property descriptors to lists of
     *            results. The property descriptors in this map define which
     *            properties are to be retrieved from the database.
     * @param failedIndices Return collection of indices into the {@code ids}
     *            array of elements that could not be retrieved. This may be
     *            {@code null} if failed indices do not need to be collected.
     * @param databaseTaskFactory The database task factory.
     */
    protected RetrieveValuesTask(long[] ids, final PropertyValueMap resultMap, final TIntList failedIndices,
            DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(ids, "ids");
        Utilities.checkNull(resultMap, "resultMap");
        myIds = ids.clone();
        myResultMap = resultMap;
        myFailedIndices = failedIndices;
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to get values for " + getResultMap().size() + " properties over " + getIds().length + " ids: ";
    }

    @Override
    public Void run(final Connection conn, final Statement stmt) throws CacheException
    {
        if (getResultMap().isEmpty())
        {
            return null;
        }

        Set<? extends PropertyDescriptor<?>> descriptors = getResultMap().getPropertyDescriptors();
        Map<String, String> columnNamesToTypes = getTypeMapper().getColumnNamesToTypes(descriptors);
        if (columnNamesToTypes.isEmpty())
        {
            return null;
        }

        int[] distinctGroupIds = getCacheUtilities().getGroupIdsFromCombinedIds(getIds(), true);
        getDatabaseTaskFactory().getEnsureColumnsTask(distinctGroupIds, descriptors).run(conn);

        final Collection<String> columnNames = columnNamesToTypes.keySet();
        CacheIdUtilities.forEachGroup(getIds(), new CacheIdUtilities.DatabaseGroupFunctor()
        {
            @Override
            public void run(long[] combinedIds, int groupId, int[] dataIds) throws CacheException
            {
                doGetValues(conn, stmt, groupId, dataIds, columnNames);
            }
        });

        return null;
    }

    /**
     * Extract property values from the database.
     *
     * @param conn The database connection.
     * @param stmt The database statement.
     * @param groupId The data group id.
     * @param dataIds The data ids.
     * @param columnNames The database column names.
     *
     * @throws CacheException If there is a database error.
     */
    protected void doGetValues(Connection conn, Statement stmt, int groupId, int[] dataIds, final Collection<String> columnNames)
        throws CacheException
    {
        final PropertyDescriptor<?>[] props = New.array(getResultMap().getPropertyDescriptors(), PropertyDescriptor.class);

        String tableName = TableNames.getDataTableName(groupId);

        final String sql;
        if (dataIds.length == 1)
        {
            sql = getSQLGenerator().generateRetrieveValues(dataIds[0], tableName, columnNames);
        }
        else
        {
            String joinTableName = getDatabaseTaskFactory()
                    .getCreateIdJoinTableTask(dataIds, ColumnNames.JOIN_ID, ColumnNames.SEQUENCE).run(conn, stmt);
            sql = getSQLGenerator().generateRetrieveValues(joinTableName, tableName, columnNames);
        }

        List<?>[] results = New.array(getResultMap().values(), List.class);

        ResultSet rs = getCacheUtilities().executeQuery(stmt, sql);
        try
        {
            readValues(rs, props, results);
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
     * Accessor for the failedIndices.
     *
     * @return The failedIndices.
     */
    protected TIntList getFailedIndices()
    {
        return myFailedIndices;
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
     * Accessor for the resultMap.
     *
     * @return The resultMap.
     */
    protected PropertyValueMap getResultMap()
    {
        return myResultMap;
    }

    /**
     * Read property values from a result set.
     *
     * @param rs The result set.
     * @param props The properties expected in the result set.
     * @param results Array of result lists, one for each property.
     * @throws CacheException If the objects could not be read from the result
     *             set.
     */
    protected void readValues(ResultSet rs, PropertyDescriptor<?>[] props, List<?>[] results) throws CacheException
    {
        getCacheUtilities().convertResultSetToPropertyValues(rs, props, results, getTypeMapper(),
                (Collection<? extends IntervalPropertyMatcher<?>>)null, getFailedIndices());
    }
}
