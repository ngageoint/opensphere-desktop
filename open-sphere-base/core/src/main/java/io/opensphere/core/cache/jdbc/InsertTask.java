package io.opensphere.core.cache.jdbc;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheIdUtilities;
import io.opensphere.core.cache.CacheModificationListener;
import io.opensphere.core.cache.CacheModificationReport;
import io.opensphere.core.cache.accessor.IntervalPropertyAccessor;
import io.opensphere.core.cache.accessor.PersistentPropertyAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.jdbc.ConnectionAppropriator.ConnectionUser;
import io.opensphere.core.cache.jdbc.StatementAppropriator.PreparedStatementUser;
import io.opensphere.core.cache.jdbc.type.ValueTranslator;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Database task for putting models into the database.
 *
 * @param <T> The type of objects in the input collection.
 */
@SuppressWarnings("PMD.GodClass")
public class InsertTask<T> extends DatabaseTask implements ConnectionUser<long[]>
{
    /** A comparator used to sort accessors in repeatable order. */
    private static final Comparator<PropertyAccessor<?, ?>> ACCESSOR_COMPARATOR = (o1, o2) -> o1.getPropertyDescriptor().getType()
            .getName().compareTo(o2.getPropertyDescriptor().getType().getName());

    /**
     * When deciding to reuse groups, if the incoming data has an expiration
     * time after an existing group but within this buffer, the existing group
     * will be reused.
     */
    private static final Duration EXPIRATION_TIME_BUFFER_MILLISECONDS = new Milliseconds(
            Utilities.parseSystemProperty("opensphere.db.expirationTimeBufferMilliseconds", Constants.MILLIS_PER_HOUR));

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(InsertTask.class);

    /**
     * The limit on the number of models in an insert before a new group is
     * created.
     */
    private static final int MAX_GROUP_REUSE_SIZE = 100;

    /** The accessors for the property values. */
    private final Collection<? extends PersistentPropertyAccessor<? super T, ?>> myAccessors;

    /** Optional listener for cache modification reports. */
    private final CacheModificationListener myCacheModificationListener;

    /**
     * The data model category for the models. Either this is set or
     * {@link #myIds}.
     */
    private final DataModelCategory myCategory;

    /**
     * Flag indicating if this insert is critical (not eligible for clean up).
     */
    private final boolean myCritical;

    /**
     * The expiration date for the properties. Expiration is ignored for
     * updates.
     */
    private final Date myExpiration;

    /**
     * The combined ids of the models being updated. Either this is set or
     * {@link #myCategory}.
     */
    private long[] myIds;

    /** The objects providing the property values. */
    private final Iterable<? extends T> myInput;

    /** Flag indicating if this is a new insert (rather than an update). */
    private final boolean myNew;

    /**
     * Constructor for a category-based insert or update.
     *
     * @param insert An object that provides the property values to be
     *            persisted.
     * @param listener Optional listener for cache modification reports.
     * @param databaseTaskFactory The database task factory.
     */
    public InsertTask(CacheDeposit<T> insert, CacheModificationListener listener, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(insert, "insert");
        Utilities.checkNull(insert.getAccessors(), "insert.getAccessors()");
        Utilities.checkNull(insert.getCategory(), "insert.getCategory()");
        myInput = insert.getInput();
        myAccessors = getCacheUtilities().filterPersistentPropertyAccessors(insert.getAccessors());
        myCategory = insert.getCategory();
        final Date expirationDate = insert.getExpirationDate();
        myExpiration = Utilities.sameInstance(expirationDate, CacheDeposit.SESSION_END) ? null : expirationDate;
        myCacheModificationListener = listener;
        myNew = insert.isNew();
        myCritical = insert.isCritical();
    }

    /**
     * Constructor for an id-based update.
     *
     * @param ids The ids of the models being updated.
     * @param input The input objects that contain the new property values.
     * @param accessors The accessors for the property values.
     * @param listener Optional listener for cache modification reports.
     * @param databaseTaskFactory The database task factory.
     */
    public InsertTask(long[] ids, Collection<? extends T> input,
            Collection<? extends PersistentPropertyAccessor<? super T, ?>> accessors, CacheModificationListener listener,
                    DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(ids, "ids");
        Utilities.checkNull(input, "input");
        Utilities.checkNull(accessors, "accessors");

        myIds = ids.clone();
        myInput = input;
        myAccessors = accessors;
        myCacheModificationListener = listener;
        myCategory = null;
        myExpiration = null;
        myNew = false;
        myCritical = false;
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to write " + getIds().length + " models to cache with " + getAccessors().size() + " accessors: ";
    }

    @Override
    public long[] run(Connection conn) throws CacheException
    {
        if (getCategory() != null)
        {
            myIds = doPut(conn);
        }
        else
        {
            final Collection<? extends PropertyDescriptor<?>> descriptors = getCacheUtilities()
                    .extractPropertyDescriptorsFromAccessors(getAccessors());
            if (descriptors.isEmpty())
            {
                return new long[0];
            }

            final int[] distinctGroupIds = new StatementAppropriator(conn).appropriateStatement(getDatabaseTaskFactory()
                    .getRetrieveValidGroupIdsTask(getCacheUtilities().getGroupIdsFromCombinedIds(getIds(), false)));

            getDatabaseTaskFactory().getEnsureColumnsTask(distinctGroupIds, descriptors).run(conn);

            doUpdateValues(descriptors, distinctGroupIds, getIds(), conn);
        }
        return getIds();
    }

    /**
     * Create a data table for a new data group.
     *
     * @param propertyDescriptors The property descriptors.
     * @param columnNamesToTypes An ordered map of column names to SQL types.
     * @param conn The database connection.
     *
     * @return The group id associated with the table.
     * @throws CacheException If there is a database error.
     */
    protected int createDataTable(Collection<? extends PropertyDescriptor<?>> propertyDescriptors,
            final Map<String, String> columnNamesToTypes, Connection conn)
                    throws CacheException
    {
        return new StatementAppropriator(conn).appropriateStatement((unused, stmt) ->
        {
            final int groupId = getNextGroupId(stmt);

            final Map<String, String> mapWithId = New.insertionOrderMap();
            mapWithId.put(ColumnNames.DATA_ID, getTypeMapper().getSqlColumnDefinition(Integer.class, null, false, 1));
            mapWithId.putAll(columnNamesToTypes);

            final PrimaryKeyConstraint primaryKey = new PrimaryKeyConstraint(ColumnNames.DATA_ID);
            final String sql = getSQLGenerator().generateCreateTable(TableNames.getDataTableName(groupId), mapWithId, primaryKey);

            getCacheUtilities().execute(sql, stmt);

            return Integer.valueOf(groupId);
        }).intValue();
    }

    /**
     * Helper method for putting models into the database.
     *
     * @param conn The database connection to use for the transaction.
     *
     * @return The cache ids for the altered models.
     * @throws CacheException If there's a problem accessing the cache.
     */
    protected long[] doPut(Connection conn) throws CacheException
    {
        final Collection<? extends PropertyDescriptor<?>> propertyDescriptors = getCacheUtilities()
                .extractPropertyDescriptorsFromAccessors(getAccessors());

        if (isNew())
        {
            return doPutNew(conn, propertyDescriptors);
        }
        else if (!propertyDescriptors.isEmpty())
        {
            return doPutUpdate(conn, propertyDescriptors);
        }
        else
        {
            return new long[0];
        }
    }

    /**
     * Insert models for a data model category, either into a new group or an
     * existing group.
     *
     * @param conn The database connection to use for the transaction.
     * @param propertyDescriptors The property descriptors.
     * @return The cache ids for the altered models.
     * @throws CacheException If there's a problem accessing the cache.
     */
    protected long[] doPutNew(Connection conn, final Collection<? extends PropertyDescriptor<?>> propertyDescriptors)
            throws CacheException
    {
        int groupId = getReusableGroupId(propertyDescriptors, conn);
        final boolean newGroup = groupId == -1;

        Collection<String> columnNames;
        if (newGroup)
        {
            final Map<String, String> columnNamesToTypes = getTypeMapper().getColumnNamesToTypes(propertyDescriptors);
            columnNames = columnNamesToTypes.keySet();
            groupId = createDataTable(propertyDescriptors, columnNamesToTypes, conn);
        }
        else
        {
            columnNames = getTypeMapper().getColumnNames(propertyDescriptors);
        }

        boolean success = false;
        int[] dataIds;
        try
        {
            if (!CollectionUtilities.hasContent(getInput()))
            {
                dataIds = new int[0];
            }
            else
            {
                dataIds = insertData(TableNames.getDataTableName(groupId), columnNames, conn);
            }

            if (newGroup)
            {
                insertGroupData(conn, groupId);
            }
            success = true;
        }
        finally
        {
            if (!success && newGroup)
            {
                dropDataTable(conn, groupId);
            }
        }

        final long[] ids = postProcessPut(conn, propertyDescriptors, groupId, dataIds);

        if (!newGroup)
        {
            postPopulateDataTable(conn, TableNames.getDataTableName(groupId), propertyDescriptors, columnNames);
        }

        return ids;
    }

    /**
     * Update all the values in a data model category.
     *
     * @param conn The database connection to use for the transaction.
     * @param propertyDescriptors The property descriptors.
     * @return The cache ids for the altered models.
     * @throws CacheException If there's a problem accessing the cache.
     */
    protected long[] doPutUpdate(Connection conn, final Collection<? extends PropertyDescriptor<?>> propertyDescriptors)
            throws CacheException
    {
        final int[] distinctGroupIds = getDatabaseTaskFactory()
                .getRetrieveGroupIdsTask(getCategory(), (List<IntervalPropertyMatcher<?>>)null).run(conn);
        if (distinctGroupIds.length == 0)
        {
            return new long[0];
        }

        final long[] combinedIds = getDatabaseTaskFactory()
                .getRetrieveCombinedIdsTask(distinctGroupIds, Collections.<PropertyMatcher<? extends Serializable>>emptyList(),
                        Collections.<OrderSpecifier>emptyList(), 0, Integer.MAX_VALUE)
                .run(conn);

        // Do a quick sanity check.
        if (getInput() instanceof Collection && ((Collection<?>)getInput()).size() != 1
                && combinedIds.length != ((Collection<?>)getInput()).size())
        {
            throw new CacheException("Records could not be found for update attempt.");
        }

        doUpdateValues(propertyDescriptors, distinctGroupIds, combinedIds, conn);

        return combinedIds;
    }

    /**
     * Update property values.
     *
     * @param propertyDescriptors The property descriptors.
     * @param distinctGroupIds The distinct group ids for the objects.
     * @param combinedIds The combined ids for the objects, in the iteration
     *            order of the provided collection.
     * @param conn The database connection to use for the transaction.
     *
     * @throws CacheException If there's a problem accessing the cache.
     */
    protected void doUpdateValues(Collection<? extends PropertyDescriptor<?>> propertyDescriptors, int[] distinctGroupIds,
            long[] combinedIds, final Connection conn)
                    throws CacheException
    {
        getDatabaseTaskFactory().getEnsureColumnsTask(distinctGroupIds, propertyDescriptors).run(conn);

        final Map<Integer, CacheModificationReport> groupIdToReportMap;
        if (getCacheModificationListener() == null)
        {
            groupIdToReportMap = Collections.emptyMap();
        }
        else
        {
            final List<DataModelCategory> dataModelCategories = new StatementAppropriator(conn).appropriateStatement(
                    getDatabaseTaskFactory().getRetrieveDataModelCategoriesTask(distinctGroupIds, true, true, true, false));
            groupIdToReportMap = New.map(distinctGroupIds.length);
            for (int index = 0; index < distinctGroupIds.length; ++index)
            {
                groupIdToReportMap.put(Integer.valueOf(distinctGroupIds[index]),
                        new CacheModificationReport(dataModelCategories.get(index), new long[0], propertyDescriptors));
            }
        }

        insertIntervalPropertyData(getAccessors(), distinctGroupIds, conn);

        final Collection<String> columnNames = getTypeMapper().getColumnNames(propertyDescriptors);
        CacheIdUtilities.forEachGroup(combinedIds, (combinedIds0, groupId, dataIds) ->
        {
            updateData(groupId, dataIds, getAccessors(), columnNames, conn);

            if (getCacheModificationListener() != null)
            {
                final CacheModificationReport report = groupIdToReportMap.get(Integer.valueOf(groupId));

                groupIdToReportMap.put(Integer.valueOf(groupId), new CacheModificationReport(report.getDataModelCategory(),
                        Utilities.concatenate(report.getIds(), combinedIds0), report.getPropertyDescriptors()));
            }
        });

        if (getCacheModificationListener() != null)
        {
            for (final CacheModificationReport report : groupIdToReportMap.values())
            {
                if (report.getIds().length > 0)
                {
                    getCacheModificationListener().cacheModified(report);
                }
            }
        }
    }

    /**
     * Drop the data table. This happens if there is an error inserting the
     * data.
     *
     * @param conn The database connection to use for the transaction.
     * @param groupId The group id.
     */
    protected void dropDataTable(Connection conn, int groupId)
    {
        try
        {
            getCacheUtilities().execute(getSQLGenerator().generateDropTable(TableNames.getDataTableName(groupId)), conn);
        }
        catch (final CacheException ce)
        {
            LOGGER.warn("Failed to drop table: " + ce, ce);
        }
        catch (final RuntimeException re)
        {
            LOGGER.warn("Failed to drop table: " + re, re);
        }
    }

    /**
     * Get the accessors for the property values.
     *
     * @return The accessors.
     */
    protected Collection<? extends PersistentPropertyAccessor<? super T, ?>> getAccessors()
    {
        return myAccessors;
    }

    /**
     * Accessor for the cacheModifications.
     *
     * @return The cacheModifications.
     */
    protected CacheModificationListener getCacheModificationListener()
    {
        return myCacheModificationListener;
    }

    /**
     * The data model category for the models. This will be {@code null} if
     * {@link #getIds()} is set.
     *
     * @return The category.
     */
    protected DataModelCategory getCategory()
    {
        return myCategory;
    }

    /**
     * Accessor for the expiration. This may be {@code null} for updates.
     *
     * @return The expiration.
     */
    protected Date getExpiration()
    {
        return myExpiration;
    }

    /**
     * Get the ids of models being updated. This will be {@code null} if
     * {@link #getCategory()} is set.
     *
     * @return The ids.
     */
    protected long[] getIds()
    {
        return myIds;
    }

    /**
     * Get the objects containing the property values.
     *
     * @return The input.
     */
    protected Iterable<? extends T> getInput()
    {
        return myInput;
    }

    /**
     * Get the interval property accessors from a collection of property
     * accessors and order them.
     *
     * @param accessors The accessors.
     * @return The sorted accessors.
     */
    protected Iterable<IntervalPropertyAccessor<? super T, ?>> getIntervalAccessors(
            Collection<? extends PropertyAccessor<? super T, ?>> accessors)
    {
        final List<IntervalPropertyAccessor<? super T, ?>> list = new ArrayList<>(accessors.size());
        for (final PropertyAccessor<? super T, ?> propertyAccessor : accessors)
        {
            if (propertyAccessor instanceof IntervalPropertyAccessor<?, ?>)
            {
                list.add((IntervalPropertyAccessor<? super T, ?>)propertyAccessor);
            }
        }
        if (!list.isEmpty())
        {
            Collections.sort(list, ACCESSOR_COMPARATOR);
        }
        return list;
    }

    /**
     * Get the next available group id.
     *
     * @param stmt A database statement.
     * @return The group id.
     * @throws CacheException If there is a database error.
     */
    protected int getNextGroupId(Statement stmt) throws CacheException
    {
        return getCacheUtilities().executeSingleIntQuery(stmt,
                getSQLGenerator().generateGetNextSequenceValue(SQL.GROUP_ID_SEQUENCE));
    }

    /**
     * Create an error message for a prepared statement failure.
     *
     * @param sql The SQL text.
     * @param e The exception.
     * @return The message.
     */
    protected String getPrepareFailureMsg(final String sql, SQLException e)
    {
        return "Failed to prepare statement for sql [" + sql + "]: " + e;
    }

    /**
     * Determine if there's already a group in the database that's compatible
     * with this new insert. If a group is found, its id is returned.
     *
     * @param propertyDescriptors The property descriptors for the persistent
     *            accessors.
     * @param conn The database connection.
     *
     * @return The id of the group that can be reused, or <tt>-1</tt> if one was
     *         not found.
     * @throws CacheException If there is a database error.
     */
    protected int getReusableGroupId(Collection<? extends PropertyDescriptor<?>> propertyDescriptors, Connection conn)
            throws CacheException
    {
        int groupId = -1;
        if (getInput() instanceof Collection && ((Collection<?>)getInput()).size() < MAX_GROUP_REUSE_SIZE)
        {
            final Collection<IntervalPropertyMatcher<?>> matchers = getCacheUtilities()
                    .extractIntervalPropertyMatchersFromAccessors(getAccessors());
            TimeSpan expirationRange;
            if (getExpiration() == null)
            {
                expirationRange = null;
            }
            else if (getExpiration().getTime() - System.currentTimeMillis() > EXPIRATION_TIME_BUFFER_MILLISECONDS.longValue() * 5)
            {
                expirationRange = TimeSpan.get(EXPIRATION_TIME_BUFFER_MILLISECONDS, getExpiration());
            }
            else
            {
                expirationRange = TimeSpan.get(getExpiration());
            }
            final int[] groupIds = getDatabaseTaskFactory()
                    .getRetrieveGroupIdsTask(getCategory(), matchers, expirationRange, Boolean.valueOf(isCritical())).run(conn);
            if (groupIds.length > 0)
            {
                groupId = groupIds[0];

                if (!propertyDescriptors.isEmpty())
                {
                    getDatabaseTaskFactory().getEnsureColumnsTask(new int[] { groupId }, propertyDescriptors).run(conn);
                }
            }
        }
        return groupId;
    }

    /**
     * Insert the data for a collection of models.
     *
     * @param tableName The table name.
     * @param columnNames The column names.
     * @param conn The database connection.
     * @return The ids for the inserted rows.
     * @throws CacheException If the data cannot be inserted due to a database
     *             error.
     */
    protected int[] insertData(final String tableName, Collection<String> columnNames, Connection conn) throws CacheException
    {
        final String sql = getSQLGenerator().generateInsert(tableName, New.array(columnNames, String.class));
        final PreparedStatementUser<int[]> user = (unused, pstmt) -> executeDataInsert(sql, pstmt);
        return new StatementAppropriator(conn).appropriateStatement(user, sql, "DATA_ID");
    }

    /**
     * Executes a data insert using the supplied SQL Query and prepared
     * statement.
     *
     * @param sql the query with which to insert data.
     * @param pstmt the prepared statement with which to execute the SQL query.
     * @return an array of IDs inserted by the operation.
     * @throws CacheException if the insert cannot be performed.
     */
    protected int[] executeDataInsert(final String sql, PreparedStatement pstmt) throws CacheException
    {
        try
        {
            TIntArrayList ids;
            if (getInput() instanceof Collection)
            {
                ids = new TIntArrayList(((Collection<?>)getInput()).size());
            }
            else
            {
                ids = new TIntArrayList();
            }
            final ValueTranslator<?>[] translators = getTypeMapper().getValueTranslators(getAccessors());
            for (final T obj : getInput())
            {
                setValues(pstmt, getAccessors(), translators, obj, 1);

                final int result = getCacheUtilities().executeUpdate(pstmt, sql);
                if (result == 1)
                {
                    final ResultSet rs = pstmt.getGeneratedKeys();
                    try
                    {
                        if (rs.next())
                        {
                            ids.add(rs.getInt(1));
                        }
                        else
                        {
                            throw new CacheException("No keys were generated by sql [" + sql + "]");
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
                            if (LOGGER.isTraceEnabled())
                            {
                                LOGGER.trace("Failed to close result set: " + e, e);
                            }
                        }
                    }
                }
                else
                {
                    throw new CacheException("Number of updated rows was " + result + ", but expected 1.");
                }
            }
            return ids.toArray();
        }
        catch (final SQLException e)
        {
            throw new CacheException(getPrepareFailureMsg(sql, e), e);
        }
        catch (final NotSerializableException e)
        {
            throw new CacheException(e);
        }
    }

    /**
     * Lock the group tables and then insert the main data group and the
     * property value groups.
     *
     * @param conn The database connection to use for the transaction.
     * @param groupId The group id.
     * @throws CacheException If there's an error.
     */
    protected void insertGroupData(Connection conn, int groupId) throws CacheException
    {
        // Lock the group tables first because the group property tables need to
        // be locked and then the main group table to avoid
        // a deadlock, but we need to insert into the main group table first to
        // satisfy the foreign key.
        lockGroupTables(conn);

        insertMainDataGroup(groupId, conn);

        for (final IntervalPropertyAccessor<? super T, ?> propertyAccessor1 : getIntervalAccessors(getAccessors()))
        {
            insertPropertyValueGroups(propertyAccessor1, new int[] { groupId }, conn);
        }
    }

    /**
     * Insert group property data in the database.
     *
     * @param accessors The property accessors.
     * @param groupIds The group ids.
     * @param conn The database connection.
     * @throws CacheException If the data cannot be inserted due to a database
     *             error.
     */
    protected void insertIntervalPropertyData(Collection<? extends PropertyAccessor<?, ?>> accessors, int[] groupIds,
            Connection conn)
                    throws CacheException
    {
        for (final PropertyAccessor<?, ?> propertyAccessor : accessors)
        {
            if (propertyAccessor instanceof IntervalPropertyAccessor<?, ?>)
            {
                insertPropertyValueGroups((IntervalPropertyAccessor<?, ?>)propertyAccessor, groupIds, conn);
            }
        }
    }

    /**
     * Insert a data group into the database.
     *
     * @param groupId The unique id for the group.
     * @param conn The database connection.
     * @throws CacheException If a database error occurs.
     */
    protected void insertMainDataGroup(final int groupId, Connection conn) throws CacheException
    {
        final long t0 = System.nanoTime();
        final String sql = getSQLGenerator().generateInsert(TableNames.DATA_GROUP, ColumnNames.GROUP_ID, ColumnNames.SOURCE,
                ColumnNames.FAMILY, ColumnNames.CATEGORY, ColumnNames.CREATION_TIME, ColumnNames.EXPIRATION_TIME,
                ColumnNames.CRITICAL);
        new StatementAppropriator(conn).appropriateStatement((PreparedStatementUser<Void>)(conn1, pstmt) ->
        {
            try
            {
                pstmt.setInt(1, groupId);
                pstmt.setString(2, getCategory().getSource());
                pstmt.setString(3, getCategory().getFamily());
                pstmt.setString(4, getCategory().getCategory());

                // creation time
                pstmt.setLong(5, System.currentTimeMillis());

                if (getExpiration() == null)
                {
                    pstmt.setNull(6, Types.BIGINT);
                }
                else
                {
                    pstmt.setLong(6, getExpiration().getTime());
                }

                pstmt.setBoolean(7, isCritical());

                getCacheUtilities().executeUpdate(pstmt, sql);

                return null;
            }
            catch (final SQLException e)
            {
                throw new CacheException(getPrepareFailureMsg(sql, e), e);
            }
        }, sql);
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(StringUtilities.formatTimingMessage("Time to insert main data group: ", System.nanoTime() - t0));
        }
    }

    /**
     * Insert property value groups.
     *
     * @param <S> The type of the property values.
     * @param acc The property accessor.
     * @param groupIds The group ids.
     * @param conn The database connection.
     * @throws CacheException If there is a database error.
     */
    protected <S> void insertPropertyValueGroups(IntervalPropertyAccessor<?, S> acc, final int[] groupIds, Connection conn)
            throws CacheException
    {
        final PropertyDescriptor<S> desc = acc.getPropertyDescriptor();
        String[] columnNames;
        if (TimeSpan.class.isAssignableFrom(desc.getType()))
        {
            columnNames = new String[] { ColumnNames.GROUP_ID, ColumnNames.PROPERTY, ColumnNames.VALUE_START,
                ColumnNames.VALUE_END, };
        }
        else
        {
            columnNames = new String[] { ColumnNames.GROUP_ID, ColumnNames.PROPERTY, ColumnNames.VALUE };
        }
        final String sql = getSQLGenerator().generateInsert(TableNames.getGroupTableName(desc.getType()), columnNames);
        new StatementAppropriator(conn)
        .appropriateStatement((unused, pstmt) -> translateAndUpdate(acc, groupIds, desc, sql, pstmt), sql);
    }

    /**
     * Translates the supplied data, and executes an update operation.
     *
     * @param <S> The type of the property values.
     * @param acc The property accessor.
     * @param groupIds The group IDs.
     * @param desc the property descriptor extracted from the accessor.
     * @param sql the SQL query with which to perform the update operation.
     * @param pstmt the prepared statement used to execute the update operation.
     * @return the type of data modified by the update operation.
     * @throws CacheException if the update cannot be performed.
     */
    protected <S> Object translateAndUpdate(IntervalPropertyAccessor<?, S> acc, final int[] groupIds,
            final PropertyDescriptor<S> desc, final String sql, PreparedStatement pstmt)
                    throws CacheException
    {
        try
        {
            pstmt.setString(2, desc.getPropertyName());
            getTypeMapper().getValueTranslator(desc).setValue(pstmt, 3, acc.getExtent(), true);
            for (final int groupId : groupIds)
            {
                pstmt.setInt(1, groupId);
                getCacheUtilities().executeUpdate(pstmt, sql);
            }
            return null;
        }
        catch (final SQLException e)
        {
            throw new CacheException(getPrepareFailureMsg(sql, e), e);
        }
    }

    /**
     * Get if this is critical data.
     *
     * @return The critical flag.
     */
    protected boolean isCritical()
    {
        return myCritical;
    }

    /**
     * Get if this is a new insert.
     *
     * @return If this is a new insert, {@code true}.
     */
    protected boolean isNew()
    {
        return myNew;
    }

    /**
     * Lock the tables that will be updated by this task. The tables will be
     * locked in the iteration order of the accessors.
     *
     * @param conn The database connection.
     * @throws CacheException If there is a database error.
     */
    protected void lockGroupTables(Connection conn) throws CacheException
    {
        final Set<String> tableNames = new LinkedHashSet<>();
        for (final PropertyAccessor<?, ?> propertyAccessor : getAccessors())
        {
            if (propertyAccessor instanceof IntervalPropertyAccessor<?, ?>)
            {
                tableNames.add(TableNames.getGroupTableName(propertyAccessor.getPropertyDescriptor().getType()));
            }
        }
        if (!tableNames.isEmpty())
        {
            new StatementAppropriator(conn).appropriateStatement((unused, stmt) ->
            {
                tableNames.add(TableNames.DATA_GROUP);

                for (final String tableName : tableNames)
                {
                    getCacheUtilities().execute(getSQLGenerator().generateNullSelectForUpdate(tableName), stmt);
                }
                return null;
            });
        }
    }

    /**
     * Take any necessary actions after creating and populating a data table.
     *
     * @param conn The database connection.
     * @param tableName The name of the data table.
     * @param propertyDescriptors The property descriptors.
     * @param columnNames The column names, in the same order as the property
     *            descriptors.
     * @throws CacheException If there is a database error.
     */
    protected void postPopulateDataTable(Connection conn, String tableName,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, Collection<String> columnNames)
                    throws CacheException
    {
    }

    /**
     * Convert the data ids to combined ids, notify the cache modification
     * listener, and perform post-population tasks.
     *
     * @param conn The database connection to use for the transaction.
     * @param propertyDescriptors The property descriptors.
     * @param groupId The group id.
     * @param dataIds The data model ids.
     * @return The cache ids for the altered models.
     * @throws CacheException If there's a problem accessing the cache.
     */
    protected long[] postProcessPut(Connection conn, Collection<? extends PropertyDescriptor<?>> propertyDescriptors, int groupId,
            int[] dataIds)
                    throws CacheException
    {
        // Convert the data ids to be combined ids.
        final long[] ids = new long[dataIds.length];
        for (int index = 0; index < dataIds.length;)
        {
            ids[index] = getCacheUtilities().getCombinedId(groupId, dataIds[index++]);
        }

        if (getCacheModificationListener() != null)
        {
            getCacheModificationListener().cacheModified(new CacheModificationReport(getCategory(), ids, propertyDescriptors));
        }

        return ids;
    }

    /**
     * Set a value in the prepared statement.
     *
     * @param <S> The type of the property value.
     * @param pstmt The prepared statement.
     * @param propertyAccessor The property accessor.
     * @param translator The value translator.
     * @param columnIndex The column index to set.
     * @param value The value to set.
     * @return The next column index.
     * @throws CacheException If there is a database error.
     * @throws SQLException If there is a database error.
     * @throws NotSerializableException If the value cannot be serialized.
     */
    protected <S> int setValue(PreparedStatement pstmt, PropertyAccessor<?, S> propertyAccessor,
            ValueTranslator<? super S> translator, int columnIndex, S value)
                    throws CacheException, SQLException, NotSerializableException
    {
        try
        {
            return translator.setValue(pstmt, columnIndex, value, true);
        }
        catch (final IllegalArgumentException e)
        {
            StringBuilder sb = new StringBuilder();

            sb.append("Value provided by property accessor [" + value + "] is illegal for property descriptor ["
                    + propertyAccessor.getPropertyDescriptor() + "]");
            if (myCategory != null)
            {
                sb.append(" from data model " + myCategory);
            }
            sb.append(" : " + e);

            throw new CacheException(sb.toString(), e.getCause());
        }
    }

    /**
     * Set the values in a prepared statement.
     *
     * @param <X> The type of the property values. This is defined internally to
     *            this method.
     * @param pstmt The prepared statement.
     * @param propertyAccessors The accessors for the property values.
     * @param translators The value translators, one for each property.
     * @param obj The input object.
     * @param firstColumn The index of the first column.
     * @throws NotSerializableException If an object cannot be serialized.
     * @throws CacheException If there is a database error.
     * @throws SQLException If there is a database error.
     */
    protected <X> void setValues(PreparedStatement pstmt,
            final Collection<? extends PersistentPropertyAccessor<? super T, ?>> propertyAccessors,
                    ValueTranslator<?>[] translators, T obj, int firstColumn)
                            throws CacheException, SQLException, NotSerializableException
    {
        int accessorIndex = 0;
        int columnIndex = firstColumn;
        for (final PropertyAccessor<? super T, ?> propertyAccessor : propertyAccessors)
        {
            // Define T here to be the type of the property values of this
            // accessor.
            @SuppressWarnings("unchecked")
            final PropertyAccessor<? super T, X> pa = (PropertyAccessor<? super T, X>)propertyAccessor;

            final X value = pa.access(obj);
            @SuppressWarnings("unchecked")
            final ValueTranslator<? super X> translator = (ValueTranslator<? super X>)translators[accessorIndex++];

            columnIndex = setValue(pstmt, pa, translator, columnIndex, value);
        }
    }

    /**
     * Update existing data in the database. This assumes that the group id has
     * already been validated.
     *
     * @param groupId The group id.
     * @param dataIds The primary keys of the records to be updated in the
     *            database, one for each input object, in the same order as the
     *            iteration order of the input collection.
     * @param accessors The property accessors.
     * @param columnNames The column names.
     * @param conn The database connection.
     * @throws CacheException If the data cannot be inserted due to a database
     *             error.
     */
    protected void updateData(final int groupId, final int[] dataIds,
            final Collection<? extends PersistentPropertyAccessor<? super T, ?>> accessors, Collection<String> columnNames,
                    Connection conn)
                            throws CacheException
    {
        final List<String> columns = new ArrayList<>(columnNames);
        columns.add(0, ColumnNames.DATA_ID);
        final String sql = getSQLGenerator().generateMerge(TableNames.getDataTableName(groupId),
                new String[] { ColumnNames.DATA_ID }, New.array(columns, String.class));

        final PreparedStatementUser<Void> user = (unused, pstmt) -> executeDataUpdate(groupId, dataIds, accessors, sql, pstmt);
        new StatementAppropriator(conn).appropriateStatement(user, sql);
    }

    /**
     * Executes a cache update using the supplied parameters.
     *
     * @param groupId The group id.
     * @param dataIds The primary keys of the records to be updated in the
     *            database, one for each input object, in the same order as the
     *            iteration order of the input collection.
     * @param accessors The property accessors.
     * @param sql the SQL statement with which to update the data.
     * @param pstmt The database statement.
     * @return the datatype of the object returned by the statement.
     * @throws CacheException if the update fails.
     */
    protected Void executeDataUpdate(final int groupId, final int[] dataIds,
            final Collection<? extends PersistentPropertyAccessor<? super T, ?>> accessors, final String sql,
                    PreparedStatement pstmt)
                            throws CacheException
    {
        try
        {
            @SuppressWarnings("PMD.PrematureDeclaration")
            final long t0 = System.nanoTime();

            final Iterator<? extends T> iterator = getInput().iterator();
            if (!iterator.hasNext())
            {
                return null;
            }

            if (dataIds.length == 0)
            {
                throw new CacheException("Records could not be found for update attempt.");
            }

            final T first = iterator.next();
            final ValueTranslator<?>[] translators = getTypeMapper().getValueTranslators(accessors);

            // TODO if this is a slow update, it would be better to put data in
            // the database as we go rather than batching
            // it all.
            if (iterator.hasNext())
            {
                int index = 0;
                pstmt.setInt(1, dataIds[index++]);
                setValues(pstmt, accessors, translators, first, 2);
                pstmt.addBatch();
                do
                {
                    if (dataIds.length <= index)
                    {
                        throw new CacheException("Records could not be found for update attempt.");
                    }

                    pstmt.setInt(1, dataIds[index++]);
                    setValues(pstmt, accessors, translators, iterator.next(), 2);
                    pstmt.addBatch();
                }
                while (iterator.hasNext());
            }
            else
            {
                setValues(pstmt, accessors, translators, first, 2);

                for (int index = 0; index < dataIds.length;)
                {
                    pstmt.setInt(1, dataIds[index++]);
                    pstmt.addBatch();
                }
            }

            final int[] updateCounts = getCacheUtilities().executeBatch(pstmt, sql);
            if (updateCounts.length == dataIds.length)
            {
                for (int index = 0; index < updateCounts.length; ++index)
                {
                    if (updateCounts[index] != 1)
                    {
                        throw new CacheException(
                                "No record found with id: " + getCacheUtilities().getCombinedId(groupId, dataIds[index]));
                    }
                }
            }
            else
            {
                throw new CacheException("Update count was " + updateCounts.length + " but " + dataIds.length + " was expected.");
            }

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(StringUtilities.formatTimingMessage("Time to update " + dataIds.length + " rows in db: ",
                        System.nanoTime() - t0));
            }
            return null;
        }
        catch (final SQLException e)
        {
            throw new CacheException(getPrepareFailureMsg(sql, e), e);
        }
        catch (final NotSerializableException e)
        {
            throw new CacheException(e);
        }
    }
}
