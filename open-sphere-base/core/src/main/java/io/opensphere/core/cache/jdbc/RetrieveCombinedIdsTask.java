package io.opensphere.core.cache.jdbc;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.ConnectionAppropriator.ConnectionUser;
import io.opensphere.core.cache.jdbc.StatementAppropriator.PreparedStatementUser;
import io.opensphere.core.cache.jdbc.type.ValueTranslator;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.MultiPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Database task that retrieves the combined ids that match the input
 * parameters.
 */
public class RetrieveCombinedIdsTask extends DatabaseTask implements ConnectionUser<long[]>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RetrieveCombinedIdsTask.class);

    /**
     * The group ids.
     */
    private final int[] myGroupIds;

    /**
     * A limit on the number of ids returned.
     */
    private final int myLimit;

    /**
     * Optional specifiers of how the ids should be ordered.
     */
    private final List<? extends OrderSpecifier> myOrderSpecifiers;

    /**
     * An optional list of property matchers to limit the query.
     */
    private final Collection<? extends PropertyMatcher<?>> myParameters;

    /** The result count. */
    private transient int myResultCount;

    /**
     * The first row index to return, <code>0</code> being the first row.
     */
    private final int myStartIndex;

    /**
     * Constructor.
     *
     * @param groupIds The group ids.
     * @param parameters An optional list of property matchers to limit the
     *            query.
     * @param orderSpecifiers Optional specifiers of how the ids should be
     *            ordered. Note that order specifiers will slow down the query.
     * @param startIndex The first row index to return, <code>0</code> being the
     *            first row.
     * @param limit A limit on the number of ids returned.
     * @param databaseTaskFactory The database task factory.
     */
    public RetrieveCombinedIdsTask(int[] groupIds, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(groupIds, "groupIds");
        myGroupIds = groupIds.clone();
        myParameters = parameters == null || parameters.isEmpty() ? null : parameters;
        myOrderSpecifiers = orderSpecifiers;
        myStartIndex = startIndex;
        myLimit = limit;
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to retrieve " + getResultCount() + " ids: ";
    }

    @Override
    public long[] run(Connection conn) throws CacheException
    {
        if (getGroupIds().length == 0)
        {
            setResultCount(0);
            return new long[0];
        }

        List<String> joinTableNames = getJoinTableNames(conn);

        final String sql = getSQLGenerator().generateRetrieveIds(getGroupIds(), getParameters(), joinTableNames,
                getOrderSpecifiers(), getStartIndex(), getLimit(), getTypeMapper());
        PreparedStatementUser<long[]> user = (unused, pstmt) ->
        {
            try
            {
                prepareGetIdStatement(sql, getGroupIds().length, getParameters(), pstmt);
            }
            catch (NotSerializableException e1)
            {
                throw new CacheException(e1);
            }
            catch (SQLException e2)
            {
                throw getCacheUtilities().createCacheException(sql, e2);
            }

            ResultSet rs = getCacheUtilities().executeQuery(pstmt, sql);
            try
            {
                long[] arr;
                if (rs.next())
                {
                    int size = 0;
                    arr = new long[16];
                    do
                    {
                        if (arr.length == size)
                        {
                            arr = Arrays.copyOf(arr, size << 1);
                        }
                        arr[size++] = getCacheUtilities().getCombinedId(rs.getInt(1), rs.getInt(2));
                    }
                    while (rs.next());
                    if (arr.length > size)
                    {
                        arr = Arrays.copyOf(arr, size);
                    }
                }
                else
                {
                    arr = new long[0];
                }
                setResultCount(arr.length);
                return arr;
            }
            catch (SQLException e3)
            {
                throw getCacheUtilities().createCacheException(sql, e3);
            }
            finally
            {
                try
                {
                    rs.close();
                }
                catch (SQLException e4)
                {
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Failed to close result set: " + e4, e4);
                    }
                }
            }
        };
        return new StatementAppropriator(conn).appropriateStatement(user, sql);
    }

    /**
     * Accessor for the groupIds.
     *
     * @return The groupIds.
     */
    protected int[] getGroupIds()
    {
        return myGroupIds;
    }

    /**
     * Accessor for the limit.
     *
     * @return The limit.
     */
    protected int getLimit()
    {
        return myLimit;
    }

    /**
     * Accessor for the orderSpecifiers.
     *
     * @return The orderSpecifiers.
     */
    protected List<? extends OrderSpecifier> getOrderSpecifiers()
    {
        return myOrderSpecifiers;
    }

    /**
     * Accessor for the parameters.
     *
     * @return The parameters.
     */
    protected Collection<? extends PropertyMatcher<?>> getParameters()
    {
        return myParameters;
    }

    /**
     * Accessor for the resultCount.
     *
     * @return The resultCount.
     */
    protected int getResultCount()
    {
        return myResultCount;
    }

    /**
     * Accessor for the startIndex.
     *
     * @return The startIndex.
     */
    protected int getStartIndex()
    {
        return myStartIndex;
    }

    /**
     * Prepare the statement for the get id query.
     *
     * @param sql The SQL string.
     * @param groupCount The number of groups being queried.
     * @param parameters The optional query parameters.
     * @param pstmt The prepared statement.
     * @return The prepared statement.
     * @throws SQLException If the statement cannot be prepared.
     * @throws CacheException If the statement cannot be prepared.
     * @throws NotSerializableException If one of the parameters is not
     *             serializable.
     */
    protected PreparedStatement prepareGetIdStatement(String sql, int groupCount,
            Collection<? extends PropertyMatcher<?>> parameters, PreparedStatement pstmt)
                    throws SQLException, NotSerializableException, CacheException
    {
        int index = 1;

        if (parameters != null)
        {
            for (int groupIndex = 0; groupIndex < groupCount; ++groupIndex)
            {
                for (PropertyMatcher<?> param : parameters)
                {
                    index = setGetIdStatementParameter(pstmt, index, param);
                }
            }
        }
        return pstmt;
    }

    /**
     * Set a parameter in a get id prepared statement.
     *
     * @param <T> The parameter type.
     * @param pstmt The prepared statement.
     * @param startIndex The current parameter index.
     * @param param The parameter.
     * @return The next parameter index.
     * @throws SQLException If there is a database error.
     * @throws CacheException If there is a database error.
     * @throws NotSerializableException If the parameter value is not
     *             serializable.
     */
    protected <T> int setGetIdStatementParameter(PreparedStatement pstmt, int startIndex, PropertyMatcher<T> param)
            throws SQLException, CacheException, NotSerializableException
    {
        int index;
        if (param instanceof MultiPropertyMatcher)
        {
            // This is handled as a join rather than a where, so there are no
            // parameters to set.
            index = startIndex;
        }
        else if (param instanceof GeometryMatcher)
        {
            // Spatial operations are handled in the post-query filtering, so
            // there are no parameters to set.
            index = startIndex;
        }
        else
        {
            PropertyDescriptor<T> desc = param.getPropertyDescriptor();
            ValueTranslator<? super T> valueTranslator = getTypeMapper().getValueTranslator(desc);

            index = valueTranslator.setValue(pstmt, startIndex, param.getOperand(), false);
        }

        return index;
    }

    /**
     * Mutator for the resultCount.
     *
     * @param resultCount The resultCount to set.
     */
    protected void setResultCount(int resultCount)
    {
        myResultCount = resultCount;
    }

    /**
     * Set up a join table for a multi-property matcher.
     *
     * @param <T> The type of the matcher property values.
     * @param conn A database connection.
     * @param parameter The multi-property matcher.
     * @return The name of the join table.
     * @throws CacheException If there's a database error.
     */
    protected <T extends Serializable> String setupValueJoinTable(Connection conn, MultiPropertyMatcher<T> parameter)
            throws CacheException
    {
        Class<?> parameterPropertyType = parameter.getPropertyDescriptor().getType();
        if (parameterPropertyType == null)
        {
            throw new IllegalArgumentException("Parameter type is null for [" + parameter + "]");
        }
        ValueTranslator<? super T> translator = getTypeMapper().getValueTranslator(parameter.getPropertyDescriptor());
        String sqlType = getTypeMapper().getSqlType(parameterPropertyType);
        return setupValueJoinTable(conn, translator, sqlType, parameter.getOperands());
    }

    /**
     * Set up a join table that contains a row for each of the given values.
     *
     * @param <T> The type of the values.
     * @param conn The DB connection.
     * @param translator A translator from Java types to database types.
     * @param sqlType The SQL type.
     * @param values The values.
     * @return The name of the join table.
     * @throws CacheException If there's a database error.
     */
    protected <T> String setupValueJoinTable(Connection conn, final ValueTranslator<T> translator, final String sqlType,
            final Collection<? extends T> values) throws CacheException
    {
        StatementAppropriator app = new StatementAppropriator(conn);

        String tempTableName = getDatabaseState().getNextTempTableName();

        Map<String, String> columnNamesToTypes = New.insertionOrderMap();
        columnNamesToTypes.put(ColumnNames.VALUE, sqlType);
        columnNamesToTypes.put(ColumnNames.SEQUENCE, getTypeMapper().getSqlColumnDefinition(Integer.class, null, false, 1));
        PrimaryKeyConstraint primaryKey = new PrimaryKeyConstraint(ColumnNames.VALUE);
        String createTableSql = getSQLGenerator().generateCreateTemporaryTable(tempTableName, columnNamesToTypes, primaryKey);
        getCacheUtilities().execute(createTableSql, conn);

        final String sql = getSQLGenerator().generateInsert(tempTableName, ColumnNames.VALUE);
        PreparedStatementUser<Void> user = (unused, pstmt) ->
        {
            CacheUtilities cacheUtil = getCacheUtilities();
            try
            {
                for (T value : values)
                {
                    translator.setValue(pstmt, 1, value, true);
                    if (cacheUtil.executeUpdate(pstmt, sql) != 1)
                    {
                        throw new CacheException("Failed to insert values into join table.");
                    }
                }

                return null;
            }
            catch (SQLException e)
            {
                throw cacheUtil.createCacheException(sql, e);
            }
        };
        app.appropriateStatement(user, sql);

        return tempTableName;
    }

    /**
     * Get the join table names for the parameters on the query.
     *
     * @param conn The connection.
     * @return The join table names.
     * @throws CacheException If there is a problem setting up the join tables.
     */
    private List<String> getJoinTableNames(Connection conn) throws CacheException
    {
        List<String> joinTableNames = null;
        if (CollectionUtilities.hasContent(getParameters()))
        {
            Collection<PropertyDescriptor<?>> descriptors = getCacheUtilities()
                    .extractPropertyDescriptorsFromMatchers(getParameters());

            getDatabaseTaskFactory().getEnsureColumnsTask(getGroupIds(), descriptors).run(conn);
            getDatabaseTaskFactory().getEnsureIndicesTask(getGroupIds(), getParameters()).run(conn);

            for (PropertyMatcher<?> parameter : getParameters())
            {
                if (parameter instanceof MultiPropertyMatcher)
                {
                    String tableName = setupValueJoinTable(conn, (MultiPropertyMatcher<?>)parameter);
                    joinTableNames = CollectionUtilities.lazyAdd(tableName, joinTableNames);
                }
            }
        }
        return joinTableNames;
    }
}
