package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;

import gnu.trove.list.TIntList;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheIdUtilities;
import io.opensphere.core.cache.CacheRuntimeException;
import io.opensphere.core.cache.JTSHelper;
import io.opensphere.core.cache.accessor.IntervalPropertyAccessor;
import io.opensphere.core.cache.accessor.PersistentPropertyAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.cache.jdbc.type.ValueTranslator;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.TimeSpanMatcher;
import io.opensphere.core.cache.util.PropertyArrayDescriptor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.time.SpaceTime;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.SharedObjectPool;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Database utilities for the cache implementation.
 */
@SuppressWarnings("PMD.GodClass")
public class CacheUtilities
{
    /**
     * Constant indicating the number of ids being queried before a temporary
     * join table is used rather than an 'IN' statement.
     *
     * TODO: move this somewhere it can be more database-specific.
     */
    public static final int ID_JOIN_THRESHOLD = 400;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CacheUtilities.class);

    /** A description of the database connection. */
    private final String myDbString;

    /** The garbage collector. */
    private GarbageCollector myGarbageCollector;

    /** A helper for working with JTS. */
    private final JTSHelper myJTSHelper = new JTSHelper();

    /** A lock to use when accessing the database. */
    private final Lock myLock;

    /** An executor that will put off tasks a few seconds. */
    private volatile Executor myProcrastinatingExecutor;

    /**
     * Get the connection from a statement.
     *
     * @param stmt The statement.
     * @return The connection.
     * @throws CacheRuntimeException If there is a database error.
     */
    public static Connection getConnectionFromStatement(Statement stmt) throws CacheRuntimeException
    {
        try
        {
            return stmt.getConnection();
        }
        catch (final SQLException e)
        {
            throw new CacheRuntimeException("Failed to get database connection from statement: " + e, e);
        }
    }

    /**
     * Construct the cache utilities.
     *
     * @param dbString A description of the database connection.
     * @param lock A lock to use when accessing the database.
     */
    public CacheUtilities(String dbString, Lock lock)
    {
        myDbString = dbString;
        myLock = lock;
    }

    /**
     * Extract {@link DataModelCategory}s from a result set.
     *
     * @param rs The result set.
     * @return The {@link DataModelCategory}s.
     * @throws SQLException If a   database error occurs.
     */
    public DataModelCategory[] convertResultSetToDataModelCategories(ResultSet rs) throws SQLException
    {
        DataModelCategory[] arr;
        if (rs.next())
        {
            final SharedObjectPool<DataModelCategory> pool = new SharedObjectPool<>();
            int size = 0;
            arr = new DataModelCategory[16];
            do
            {
                if (arr.length == size)
                {
                    arr = Arrays.copyOf(arr, size << 1);
                }
                final String sourceResult = rs.getString(1);
                final String familyResult = rs.getString(2);
                final String categoryResult = rs.getString(3);
                arr[size++] = pool.get(new DataModelCategory(sourceResult, familyResult, categoryResult));
            }
            while (rs.next());
            if (arr.length > size)
            {
                arr = Arrays.copyOf(arr, size);
            }
        }
        else
        {
            arr = new DataModelCategory[0];
        }
        return arr;
    }

    /**
     * Extract integers from a result set.
     *
     * @param rs The result set.
     * @return The array of integers.
     * @throws SQLException If a   database error occurs.
     */
    public int[] convertResultSetToIntArray(ResultSet rs) throws SQLException
    {
        return convertResultSetToIntArray(rs, (Collection<? extends IntervalPropertyMatcher<?>>)null, (TypeMapper)null);
    }

    /**
     * Extract integers from a result set, optionally filtering.
     *
     * @param rs The result set.
     * @param resultFilterParameters Optional parameters that need to be used to
     *            filter the results.
     * @param typeMapper A type mapper. Only required if there are filter
     *            parameters.
     * @return The array of integers.
     * @throws SQLException If a   database error occurs.
     */
    public int[] convertResultSetToIntArray(ResultSet rs, Collection<? extends IntervalPropertyMatcher<?>> resultFilterParameters,
            TypeMapper typeMapper)
        throws SQLException
    {
        int[] arr;
        if (rs.next())
        {
            int size = 0;
            arr = new int[16];
            if (CollectionUtilities.hasContent(resultFilterParameters))
            {
                final PropertyDescriptor<?>[] props = New.array(extractPropertyDescriptorsFromMatchers(resultFilterParameters),
                        PropertyDescriptor.class);
                final IntervalPropertyMatcher<?>[] matchers = New.array(resultFilterParameters, IntervalPropertyMatcher.class);
                @SuppressWarnings("unchecked")
                final ValueTranslator<Object>[] valueTranslators = (ValueTranslator<Object>[])typeMapper
                        .getValueTranslators(props);

                final List<Object> results = New.list(1);
                do
                {
                    if (arr.length == size)
                    {
                        arr = Arrays.copyOf(arr, size << 1);
                    }
                    final int id = rs.getInt(1);
                    int rsIndex = 2;
                    for (int propIndex = 0; propIndex < props.length; ++propIndex)
                    {
                        try
                        {
                            rsIndex = valueTranslators[propIndex].getValue(props[propIndex], rsIndex, rs, null, results);
                            final Object result = results.get(0);
                            if (matchers[propIndex].matches(result))
                            {
                                arr[size++] = id;
                            }
                        }
                        catch (final CacheException e)
                        {
                            LOGGER.warn("Failed to get property value: " + e, e);
                        }
                    }
                }
                while (rs.next());
            }
            else
            {
                do
                {
                    if (arr.length == size)
                    {
                        arr = Arrays.copyOf(arr, size << 1);
                    }
                    arr[size++] = rs.getInt(1);
                }
                while (rs.next());
            }
            if (arr.length > size)
            {
                arr = Arrays.copyOf(arr, size);
            }
        }
        else
        {
            arr = new int[0];
        }
        return arr;
    }

    /**
     * Extract longs from a result set.
     *
     * @param rs The result set.
     * @return The array of longs.
     * @throws SQLException If a   database error occurs.
     */
    public long[] convertResultSetToLongArray(ResultSet rs) throws SQLException
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
                arr[size++] = rs.getLong(1);
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
        return arr;
    }

    /**
     * Extract property values from a result set.
     *
     * @param rs The result set.
     * @param props The property descriptors that define the columns in the
     *            result set.
     * @param results An array of lists to contain the results, one for each
     *            column.
     * @param typeMapper A type mapper.
     * @param resultFilterParameters Optional parameters that need to be used to
     *            filter the results.
     * @param failedIndices An optional output list to contain row numbers that
     *            could not be retrieved.
     * @throws CacheException If there is a database error.
     */
    public void convertResultSetToPropertyValues(ResultSet rs, PropertyDescriptor<?>[] props, List<?>[] results,
            TypeMapper typeMapper, Collection<? extends IntervalPropertyMatcher<?>> resultFilterParameters,
            TIntList failedIndices)
        throws CacheException
    {
        final IntervalPropertyMatcher<?>[] filters = new IntervalPropertyMatcher<?>[props.length];
        if (CollectionUtilities.hasContent(resultFilterParameters))
        {
            for (final IntervalPropertyMatcher<?> param : resultFilterParameters)
            {
                int foundAtIndex = -1;
                for (int index = 0; index < props.length; ++index)
                {
                    if (param.getPropertyDescriptor().equals(props[index]))
                    {
                        foundAtIndex = index;
                        break;
                    }
                }
                if (foundAtIndex == -1)
                {
                    throw new IllegalArgumentException(
                            "Filter parameter [" + param + "] was not found in selected columns: " + Arrays.toString(props));
                }
                else
                {
                    filters[foundAtIndex] = param;
                }
            }
        }

        @SuppressWarnings("unchecked")
        final ValueTranslator<Object>[] valueTranslators = (ValueTranslator<Object>[])typeMapper.getValueTranslators(props);

        try
        {
            int row = 0;
            while (rs.next())
            {
                int rsIndex = 1;
                for (int propIndex = 0; propIndex < props.length; ++propIndex)
                {
                    @SuppressWarnings("unchecked")
                    final List<Object> list = (List<Object>)results[propIndex];
                    try
                    {
                        if (filters[propIndex] == null)
                        {
                            rsIndex = valueTranslators[propIndex].getValue(props[propIndex], rsIndex, rs, null, list);
                        }
                        else
                        {
                            final int sizeBefore = list.size();
                            rsIndex = valueTranslators[propIndex].getValue(props[propIndex], rsIndex, rs, filters[propIndex],
                                    list);

                            if (list.size() == sizeBefore)
                            {
                                // Remove the earlier portion of the row.
                                for (int propIndex2 = propIndex - 1; propIndex2 >= 0;)
                                {
                                    final List<?> list2 = results[propIndex2--];
                                    list2.remove(list2.size() - 1);
                                }
                                // Skip the rest of the columns.
                                break;
                            }
                        }
                    }
                    catch (final CacheException e)
                    {
                        LOGGER.warn("Failed to get property value: " + e, e);
                        if (failedIndices != null)
                        {
                            failedIndices.add(row);
                        }
                        for (; propIndex < props.length; ++propIndex)
                        {
                            results[propIndex].add(null);
                        }
                    }
                }
                row++;
            }
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to read object from cache: " + e, e);
        }
    }

    /**
     * Create a {@link CacheException} from an {@link SQLException}.
     *
     * @param sql The SQL string.
     * @param e The exception.
     * @return A cache exception.
     */
    public CacheException createCacheException(String sql, SQLException e)
    {
        return new CacheException(sql, myDbString, e);
    }

    /**
     * Execute a single SQL command.
     *
     * @param sql The SQl command.
     * @param connection A database connection.
     * @throws CacheException If the command cannot be executed.
     */
    public void execute(final String sql, Connection connection) throws CacheException
    {
        new StatementAppropriator(connection).appropriateStatement(new StatementUser<Void>()
        {
            @Override
            public Void run(Connection conn, Statement stmt) throws CacheException
            {
                execute(sql, stmt);
                return null;
            }
        });
    }

    /**
     * Execute a single SQL command.
     *
     * @param sql The SQl command.
     * @param connectionAppropriator A connection appropriator.
     * @throws CacheException If the command cannot be executed.
     */
    public void execute(final String sql, ConnectionAppropriator connectionAppropriator) throws CacheException
    {
        connectionAppropriator.appropriateStatement(new StatementUser<Void>()
        {
            @Override
            public Void run(Connection conn, Statement stmt) throws CacheException
            {
                execute(sql, stmt);
                return null;
            }
        });
    }

    /**
     * Execute a single SQL command.
     *
     * @param sql The SQl command.
     * @param stmt A database statement.
     * @throws CacheException If the command cannot be executed.
     */
    public void execute(String sql, Statement stmt) throws CacheException
    {
        final Lock lock = getLock();
        lock.lock();
        try
        {
            logSql(sql);
            final long t0 = System.nanoTime();
            try
            {
                stmt.execute(sql);
                final long t1 = System.nanoTime();

                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace(StringUtilities.formatTimingMessage("Time to execute sql [" + sql + "]: ", t1 - t0));
                }
            }
            catch (final SQLException e)
            {
                throw new CacheException("Failed to execute sql [" + sql + "]: " + e, e);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Execute a single SQL command.
     *
     * @param sql The SQl command.
     * @param statementAppropriator A statement appropriator.
     * @throws CacheException If the command cannot be executed.
     */
    public void execute(final String sql, StatementAppropriator statementAppropriator) throws CacheException
    {
        statementAppropriator.appropriateStatement(new StatementUser<Void>()
        {
            @Override
            public Void run(Connection conn, Statement stmt) throws CacheException
            {
                execute(sql, stmt);
                return null;
            }
        });
    }

    /**
     * Execute an sql statement batch.
     *
     * @param stmt The database statement.
     * @param sql The sql statement.
     * @return The array of counts of updated rows.
     *
     * @throws CacheException If an  error occurs.
     */
    public int[] executeBatch(Statement stmt, String sql) throws CacheException
    {
        logSql(sql);
        getLock().lock();
        try
        {
            return stmt.executeBatch();
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to execute batch: " + e, e);
        }
        finally
        {
            getLock().unlock();
        }
    }

    /**
     * Execute a query and convert the results into an array of ints.
     *
     * @param stmt The database statement to use.
     * @param sql The SQL command.
     * @return The array of ints.
     * @throws CacheException If there is a database error.
     */
    @SuppressWarnings("PMD.CheckResultSet")
    public int[] executeIntArrayQuery(Statement stmt, String sql) throws CacheException
    {
        final ResultSet rs = executeQuery(stmt, sql);
        try
        {
            return convertResultSetToIntArray(rs);
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to execute int array query [" + sql + "]: " + e, e);
        }
        finally
        {
            try
            {
                rs.close();
            }
            catch (final SQLException e)
            {
                handleResultSetCloseException(e);
            }
        }
    }

    /**
     * Execute an sql statement.
     *
     * @param pstmt The database statement.
     * @param sql The sql statement.
     * @return The result set.
     *
     * @throws CacheException If an  error occurs.
     */
    public ResultSet executeQuery(PreparedStatement pstmt, String sql) throws CacheException
    {
        logSql(sql, pstmt);
        getLock().lock();
        try
        {
            pstmt.execute();
            return pstmt.getResultSet();
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to execute query: " + e, e);
        }
        finally
        {
            getLock().unlock();
        }
    }

    /**
     * Execute a query.
     *
     * @param stmt The DB statement.
     * @param sql The SQL command.
     * @return The result set.
     * @throws CacheException If the query cannot be executed.
     */
    public ResultSet executeQuery(Statement stmt, String sql) throws CacheException
    {
        logSql(sql);
        getLock().lock();
        try
        {
            return stmt.executeQuery(sql);
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to execute query [" + sql + "]: " + e, e);
        }
        finally
        {
            getLock().unlock();
        }
    }

    /**
     * Execute an SQL query for a single int. If there are no results,
     * <code>0</code> is returned.
     *
     * @param stmt A database statement.
     * @param sql The SQl command.
     * @return The result.
     * @throws CacheException If the command cannot be executed.
     */
    public int executeSingleIntQuery(Statement stmt, String sql) throws CacheException
    {
        ResultSet rs = null;
        try
        {
            rs = executeQuery(stmt, sql);
            return rs.next() ? rs.getInt(1) : 0;
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to get result for int query [" + sql + "]: " + e, e);
        }
        finally
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (final SQLException e)
                {
                    handleResultSetCloseException(e);
                }
            }
        }
    }

    /**
     * Execute an SQL query for a single string. If there are no results,
     * <code>null</code> is returned.
     *
     * @param sql The SQl command.
     * @param stmt A database statement.
     *
     * @return The result string.
     * @throws CacheException If the command cannot be executed.
     */
    public String executeSingleStringQuery(String sql, Statement stmt) throws CacheException
    {
        ResultSet rs = null;
        try
        {
            rs = executeQuery(stmt, sql);
            return rs.next() ? rs.getString(1) : null;
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to get result for string query [" + sql + "]: " + e, e);
        }
        finally
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (final SQLException e)
                {
                    handleResultSetCloseException(e);
                }
            }
        }
    }

    /**
     * Execute an sql statement.
     *
     * @param pstmt The database statement.
     * @param sql The sql statement.
     * @return The count of updated rows.
     *
     * @throws CacheException If an  error occurs.
     */
    public int executeUpdate(PreparedStatement pstmt, String sql) throws CacheException
    {
        logSql(sql, pstmt);
        final Lock readLock = getLock();
        readLock.lock();
        try
        {
            return pstmt.executeUpdate();
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to execute update: " + e, e);
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Execute an sql statement.
     *
     * @param stmt The database statement.
     * @param sql The sql statement.
     * @return The count of updated rows.
     *
     * @throws CacheException If an  error occurs.
     */
    public int executeUpdate(Statement stmt, String sql) throws CacheException
    {
        logSql(sql);
        final Lock readLock = getLock();
        readLock.lock();
        try
        {
            return stmt.executeUpdate(sql);
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to execute update: " + e, e);
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Extract the interval property matchers from the interval property
     * accessors in the provided collection.
     *
     * @param accessors The accessors.
     * @return The property descriptors.
     */
    public Collection<IntervalPropertyMatcher<?>> extractIntervalPropertyMatchersFromAccessors(
            Collection<? extends PropertyAccessor<?, ?>> accessors)
    {
        final Collection<IntervalPropertyMatcher<?>> matchers = New.collection(accessors.size());
        for (final PropertyAccessor<?, ?> acc : accessors)
        {
            if (acc instanceof IntervalPropertyAccessor)
            {
                matchers.add(((IntervalPropertyAccessor<?, ?>)acc).createMatcher());
            }
        }
        return matchers;
    }

    /**
     * Extract the property descriptors from the property accessors in the
     * provided collection.
     *
     * @param accessors The accessors.
     * @return The property descriptors.
     */
    public Collection<? extends PropertyDescriptor<?>> extractPropertyDescriptorsFromAccessors(
            Collection<? extends PersistentPropertyAccessor<?, ?>> accessors)
    {
        final Collection<PropertyDescriptor<?>> descriptors = New.collection(accessors.size());
        for (final PropertyAccessor<?, ?> acc : accessors)
        {
            descriptors.add(acc.getPropertyDescriptor());
        }
        return descriptors;
    }

    /**
     * Extract the property descriptors from some property matchers.
     *
     * @param matchers The matchers.
     * @return The property descriptors.
     */
    public Collection<PropertyDescriptor<?>> extractPropertyDescriptorsFromMatchers(
            Collection<? extends PropertyMatcher<?>> matchers)
    {
        if (matchers.isEmpty())
        {
            return Collections.emptySet();
        }
        else
        {
            final Collection<PropertyDescriptor<?>> descriptors = New.collection();
            for (final PropertyMatcher<?> param : matchers)
            {
                descriptors.add(param.getPropertyDescriptor());
            }
            return descriptors;
        }
    }

    /**
     * Extract the persistent property accessors from some property accessors.
     *
     * @param <T> The type of the objects that the accessors access.
     * @param accessors The accessors.
     * @return The interval property matchers.
     */
    public <T> Collection<? extends PersistentPropertyAccessor<? super T, ?>> filterPersistentPropertyAccessors(
            Collection<? extends PropertyAccessor<? super T, ?>> accessors)
    {
        @SuppressWarnings("rawtypes")
        final Collection<PersistentPropertyAccessor> filtered = CollectionUtilities.filterDowncast(accessors,
                PersistentPropertyAccessor.class);
        final Collection<PersistentPropertyAccessor<T, ?>> result = New.collection(filtered.size());
        @SuppressWarnings("rawtypes")
        final Iterator<PersistentPropertyAccessor> iter = filtered.iterator();
        while (iter.hasNext())
        {
            @SuppressWarnings("unchecked")
            final PersistentPropertyAccessor<T, ?> cast = iter.next();
            result.add(cast);
        }

        return result;
    }

    /**
     * Get the current columns for a table. If the table does not exist, an
     * empty collection is returned.
     *
     * @param conn The database connection.
     * @param tableName The table name.
     * @param columnPattern Optional SQL pattern to limit the column names
     *            returned.
     * @return The column names.
     * @throws CacheException If there is a database error.
     */
    public Set<String> getColumns(Connection conn, String tableName, String columnPattern) throws CacheException
    {
        final Set<String> existingColumns = New.set();
        ResultSet rs = null;
        try
        {
            rs = conn.getMetaData().getColumns(null, null, tableName, columnPattern);
            while (rs.next())
            {
                existingColumns.add(rs.getString(4));
            }
            return existingColumns;
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to get database metadata: " + e, e);
        }
        finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
            }
            catch (final SQLException e)
            {
                handleResultSetCloseException(e);
            }
        }
    }

    /**
     * Compile a combined id from a group id and data id.
     *
     * @param groupId The group id.
     * @param dataId The data id.
     * @return The combined id.
     */
    public long getCombinedId(int groupId, int dataId)
    {
        return CacheIdUtilities.getCombinedId(groupId, dataId);
    }

    /**
     * Extract a data id from a combined id.
     *
     * @param combinedId The combined id.
     * @return The data id.
     */
    public int getDataIdFromCombinedId(long combinedId)
    {
        return CacheIdUtilities.getDataIdFromCombinedId(combinedId);
    }

    /**
     * Get the data ids from some combined ids.
     *
     * @param ids The combined ids.
     * @return The data ids.
     */
    public int[] getDataIds(final long[] ids)
    {
        return CacheIdUtilities.getDataIdsFromCombinedIds(ids);
    }

    /**
     * Get the garbage collector.
     *
     * @return The garbage collector.
     */
    public GarbageCollector getGarbageCollector()
    {
        return myGarbageCollector;
    }

    /**
     * Extract a group id from a combined id.
     *
     * @param combinedId The combined id.
     * @return The group id.
     */
    public int getGroupIdFromCombinedId(long combinedId)
    {
        return CacheIdUtilities.getGroupIdFromCombinedId(combinedId);
    }

    /**
     * Extract some group ids from some combined ids.
     *
     * @param combinedIds The combined ids.
     * @param distinct Indicates if only distinct group ids should be returned.
     * @return
     *         <ul>
     *         <li>If <code>distinct</code> is <code>false</code>, the group
     *         ids, one for each element. If there is no group, the value is set
     *         to <code>0</code>.</li>
     *         <li>If <code>distinct</code> is <code>true</code>, the distinct
     *         group ids.</li>
     *         </ul>
     */
    public int[] getGroupIdsFromCombinedIds(long[] combinedIds, boolean distinct)
    {
        return CacheIdUtilities.getGroupIdsFromCombinedIds(combinedIds, distinct);
    }

    /**
     * Get the names of the indices on a database table.
     *
     * @param conn A connection to use.
     * @param catalog The catalog name, or <code>null</code> for all catalogs.
     * @param schema The schema name, or <code>null</code> for all schemata.
     * @param tableName The table name.
     * @return The matching index names.
     * @throws CacheException If the index names cannot be retrieved.
     */
    public Collection<String> getIndexNames(Connection conn, String catalog, String schema, String tableName)
        throws CacheException
    {
        final Collection<String> indexNames = New.collection();
        try
        {
            final ResultSet rs = conn.getMetaData().getIndexInfo(catalog, schema, tableName, false, false);
            try
            {
                while (rs.next())
                {
                    indexNames.add(rs.getString("INDEX_NAME"));
                }
            }
            finally
            {
                try
                {
                    rs.close();
                }
                catch (final SQLException e)
                {
                    handleResultSetCloseException(e);
                }
            }
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to get indices: " + e, e);
        }
        return indexNames;
    }

    /**
     * Get table names in the database.
     *
     * @param conn A connection to use.
     * @param catalog The catalog name, or <code>null</code> for all catalogs.
     * @param schemaPattern A schema pattern, or <code>null</code> for all
     *            schemata.
     * @param tableNamePattern A table name pattern, or <code>null</code> for
     *            all tables.
     * @return The matching table names.
     * @throws CacheException If the table names cannot be retrieved.
     */
    public Collection<String> getTableNames(Connection conn, String catalog, String schemaPattern, String tableNamePattern)
        throws CacheException
    {
        final Collection<String> tableNames = New.collection();
        try
        {
            final ResultSet rs = conn.getMetaData().getTables(catalog, schemaPattern, tableNamePattern, new String[] { "TABLE" });
            try
            {
                while (rs.next())
                {
                    tableNames.add(rs.getString("TABLE_NAME"));
                }
            }
            finally
            {
                try
                {
                    rs.close();
                }
                catch (final SQLException e)
                {
                    handleResultSetCloseException(e);
                }
            }
        }
        catch (final SQLException e)
        {
            throw new CacheException("Failed to get tables: " + e, e);
        }
        return tableNames;
    }

    /**
     * Method called if there's an error closing a result set.
     *
     * @param e The exception.
     */
    public void handleResultSetCloseException(SQLException e)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Failed to close result set: " + e, e);
        }
    }

    /**
     * Get the index of the property with a certain type in a collection. If any
     * of the property descriptors are {@link PropertyArrayDescriptor}s, the
     * nested active columns are also considered. For example, if the first
     * property descriptor is an ordinary one, but the second property
     * descriptor is a property array descriptor, and the property being
     * searched for is the third active column in the property array descriptor,
     * the returned index will be <tt>3</tt>.
     *
     * @param propertyDescriptors The property descriptors.
     * @param type The property value type.
     * @return The index, or -1 if the property type was not found.
     */
    public int indexOfProperty(Collection<? extends PropertyDescriptor<?>> propertyDescriptors, Class<?> type)
    {
        int index = 0;
        for (final PropertyDescriptor<?> propertyDescriptor : propertyDescriptors)
        {
            if (propertyDescriptor instanceof PropertyArrayDescriptor)
            {
                final PropertyArrayDescriptor pad = (PropertyArrayDescriptor)propertyDescriptor;
                for (final int column : pad.getActiveColumns())
                {
                    if (type.isAssignableFrom(pad.getColumnTypes()[column]))
                    {
                        return index;
                    }
                    else
                    {
                        ++index;
                    }
                }
            }
            else
            {
                if (type.isAssignableFrom(propertyDescriptor.getType()))
                {
                    return index;
                }
                else
                {
                    if (propertyDescriptor.getType().equals(TimeSpan.class))
                    {
                        index += 2;
                    }
                    else
                    {
                        ++index;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Log an SQL statement.
     *
     * @param sql The SQL statement.
     */
    public void logSql(String sql)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Executing sql [" + sql + "] on db " + myDbString);
        }
    }

    /**
     * Log an SQL statement and a prepared statement.
     *
     * @param sql The SQL statement.
     * @param pstmt The prepared statement.
     */
    public void logSql(String sql, PreparedStatement pstmt)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(new StringBuilder(256).append("Executing sql [").append(sql).append("] using statement [").append(pstmt)
                    .append("] on db ").append(myDbString).toString());
        }
    }

    /**
     * Read space-times from a result set.
     *
     * @param rs The result set.
     * @param parameters The query parameters.
     * @return The collection of geometries.
     * @throws CacheException If a   geometry is corrupt.
     * @throws SQLException If a   database error occurs.
     */
    public Collection<SpaceTime> readSpaceTimes(ResultSet rs, List<? extends PropertyMatcher<?>> parameters)
        throws CacheException, SQLException
    {
        int startIndex = -1;
        int endIndex = -1;
        int geomIndex = -1;

        int index = 1;
        for (final PropertyMatcher<?> param : parameters)
        {
            if (param instanceof TimeSpanMatcher)
            {
                startIndex = index++;
                endIndex = index++;
            }
            else if (param instanceof GeometryMatcher)
            {
                geomIndex = index++;
            }
        }

        if (geomIndex == -1)
        {
            throw new UnsupportedOperationException("Getting unsatisfied without a geometry parameter is currently unsupported.");
        }

        final Collection<SpaceTime> results = New.collection();
        if (rs.next())
        {
            do
            {
                final Geometry geometry = geomIndex == -1 ? null : myJTSHelper.readGeometry(rs.getBytes(geomIndex));
                final TimeSpan timeSpan = startIndex == -1 ? TimeSpan.TIMELESS
                        : TimeSpan.get(rs.getLong(startIndex), rs.getLong(endIndex));
                final SpaceTime result = new SpaceTime(geometry, timeSpan);
                results.add(result);
            }
            while (rs.next());
        }
        return results;
    }

    /**
     * Schedule the data trimmer to run.
     *
     * @param dataTrimmer The data trimmer.
     * @param executor The executor to use for data trimming. This is only used
     *            the first time the data trimmer is scheduled.
     */
    public void scheduleDataTrimmer(Runnable dataTrimmer, ScheduledExecutorService executor)
    {
        if (myProcrastinatingExecutor == null)
        {
            final int delayMilliseconds = Integer.getInteger("opensphere.db.dataTrimmerDelayMilliseconds", 5000).intValue();
            myProcrastinatingExecutor = new ProcrastinatingExecutor(executor, delayMilliseconds);
        }
        myProcrastinatingExecutor.execute(dataTrimmer);
    }

    /**
     * Start the garbage collector.
     *
     * @param taskFactory The database task factory.
     * @param sqlGenerator An SQL generator.
     * @param connectionAppropriator A connection appropriator.
     * @param executor The cache executor.
     */
    public void startGarbageCollector(DatabaseTaskFactory taskFactory, SQLGenerator sqlGenerator,
            ConnectionAppropriator connectionAppropriator, final ScheduledExecutorService executor)
    {
        final long delayMilliseconds = 1000L;
        final long defaultPeriodMilliseconds = 5000L;
        final long defaultBudgetNanoseconds = 100000000L;
        final long periodMilliseconds = Long.getLong("opensphere.db.gcPeriodMilliseconds", defaultPeriodMilliseconds).longValue();
        final long budgetNanoseconds = Long.getLong("opensphere.db.gcBudgetNanoseconds", defaultBudgetNanoseconds).longValue();
        myGarbageCollector = new GarbageCollector(budgetNanoseconds, taskFactory, this, sqlGenerator, connectionAppropriator,
                executor);
        executor.scheduleWithFixedDelay(myGarbageCollector, delayMilliseconds, periodMilliseconds * Constants.NANO_PER_MILLI,
                TimeUnit.NANOSECONDS);
    }

    /**
     * Get the lock to use when accessing the database.
     *
     * @return The lock.
     */
    private Lock getLock()
    {
        return myLock;
    }
}
