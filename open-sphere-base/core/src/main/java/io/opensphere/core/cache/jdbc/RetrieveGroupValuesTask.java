package io.opensphere.core.cache.jdbc;

import java.io.NotSerializableException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import gnu.trove.list.TIntList;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.PreparedStatementUser;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Database task for retrieving group property values.
 */
public class RetrieveGroupValuesTask extends RetrieveGroupIdsTask
{
    /** The result map. */
    private final Map<? extends PropertyDescriptor<?>, ? extends List<?>> myResultMap;

    /**
     * Constructor.
     *
     * @param category The optional data model category. Null values in the
     *            category indicate any value is acceptable.
     * @param parameters The optional parameters on the query.
     * @param resultMap An input/output map that contains the descriptors for
     *            the properties to be retrieved, and a list of results to be
     *            populated for each one.
     * @param databaseTaskFactory The database task factory.
     */
    public RetrieveGroupValuesTask(DataModelCategory category, Collection<? extends IntervalPropertyMatcher<?>> parameters,
            Map<? extends PropertyDescriptor<?>, ? extends List<?>> resultMap, DatabaseTaskFactory databaseTaskFactory)
    {
        super(category, parameters, databaseTaskFactory, TimeSpan.newUnboundedEndTimeSpan(System.currentTimeMillis()),
                (Boolean)null);
        myResultMap = resultMap;
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to get values for " + getResultMap().size() + " properties over " + getResultCount() + " ids: ";
    }

    @Override
    public int[] run(Connection conn) throws CacheException
    {
        final List<PropertyDescriptor<?>> selectProperties = New.list(getResultMap().keySet());
        final List<List<?>> values = New.list(getResultMap().values());

        final Collection<? extends IntervalPropertyMatcher<?>> resultFilterParameters = getResultFilterParameters();
        final Collection<PropertyDescriptor<?>> filterProperties = getCacheUtilities()
                .extractPropertyDescriptorsFromMatchers(resultFilterParameters);

        for (PropertyDescriptor<?> filterProperty : filterProperties)
        {
            if (!getResultMap().keySet().contains(filterProperty))
            {
                selectProperties.add(filterProperty);
                values.add(New.list());
            }
        }

        final String sql = getSQLGenerator().generateRetrieveGroupValuesSql((int[])null, (JoinTableColumn)null, getCategory(),
                getParameters(), selectProperties, getExpirationRange(), isCritical());
        return new StatementAppropriator(conn).appropriateStatement((PreparedStatementUser<int[]>)(conn1, pstmt) ->
        {
            try
            {
                prepareGroupQueryStatement(conn1, pstmt, sql);

                PropertyDescriptor<?>[] props = New.array(selectProperties, PropertyDescriptor.class, 1, 0);
                props[0] = new PropertyDescriptor<>("", Integer.class);
                List<?>[] results = New.array(values, List.class, 1, 0);
                List<Integer> ids = New.list();
                results[0] = ids;

                ResultSet rs = getCacheUtilities().executeQuery(pstmt, sql);
                try
                {
                    getCacheUtilities().convertResultSetToPropertyValues(rs, props, results, getTypeMapper(),
                            resultFilterParameters, (TIntList)null);

                    setResultCount(results[0].size());
                    return CollectionUtilities.toIntArray(ids);
                }
                finally
                {
                    rs.close();
                }
            }
            catch (SQLException e1)
            {
                throw new CacheException("Failed to read group ids from cache: " + e1, e1);
            }
            catch (NotSerializableException e)
            {
                throw new CacheException(e);
            }
        }, sql);
    }

    /**
     * Accessor for the resultMap.
     *
     * @return The resultMap.
     */
    protected Map<? extends PropertyDescriptor<?>, ? extends List<?>> getResultMap()
    {
        return myResultMap;
    }
}
