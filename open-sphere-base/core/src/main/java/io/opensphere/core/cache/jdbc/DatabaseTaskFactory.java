package io.opensphere.core.cache.jdbc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import gnu.trove.list.TIntList;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheModificationListener;
import io.opensphere.core.cache.PropertyValueMap;
import io.opensphere.core.cache.accessor.PersistentPropertyAccessor;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.Nulls;

/**
 * A factory for database tasks.
 */
public class DatabaseTaskFactory
{
    /** The cache utilities. */
    private final CacheUtilities myCacheUtilities;

    /** The database state. */
    private final DatabaseState myDatabaseState;

    /** The SQL generator. */
    private final SQLGenerator mySQLGenerator;

    /** The type mapper. */
    private final TypeMapper myTypeMapper;

    /**
     * Constructor.
     *
     * @param cacheUtilities The cache utilities.
     * @param databaseState The database state.
     * @param sqlGenerator The SQL generator.
     * @param typeMapper The type mapper.
     */
    public DatabaseTaskFactory(CacheUtilities cacheUtilities, DatabaseState databaseState, SQLGenerator sqlGenerator,
            TypeMapper typeMapper)
    {
        Utilities.checkNull(cacheUtilities, "cacheUtilities");
        Utilities.checkNull(databaseState, "databaseState");
        Utilities.checkNull(sqlGenerator, "sqlGenerator");
        Utilities.checkNull(typeMapper, "typeMapper");
        myCacheUtilities = cacheUtilities;
        myDatabaseState = databaseState;
        mySQLGenerator = sqlGenerator;
        myTypeMapper = typeMapper;
    }

    /**
     * Accessor for the cacheUtilities.
     *
     * @return The cacheUtilities.
     */
    public final CacheUtilities getCacheUtilities()
    {
        return myCacheUtilities;
    }

    /**
     * Get a database task that creates a temporary table containing some ids.
     *
     * @param ids The ids.
     * @param idColumnName The name of the column for the ids.
     * @return The task.
     */
    public CreateIdJoinTableTask getCreateIdJoinTableTask(int[] ids, String idColumnName)
    {
        return new CreateIdJoinTableTask(ids, idColumnName, Nulls.STRING, this);
    }

    /**
     * Get a database task that creates a temporary table containing some ids.
     *
     * @param ids The ids.
     * @param idColumnName The name of the column for the ids.
     * @param orderColumnName The name of a column to create on the temporary
     *            table that will contain the index of each row.
     * @return The task.
     */
    public CreateIdJoinTableTask getCreateIdJoinTableTask(int[] ids, String idColumnName, String orderColumnName)
    {
        return new CreateIdJoinTableTask(ids, idColumnName, orderColumnName, this);
    }

    /**
     * Get the in-memory cache of the database state.
     *
     * @return The database state.
     */
    public DatabaseState getDatabaseState()
    {
        return myDatabaseState;
    }

    /**
     * Get a database task for deleting all data from all tables.
     *
     * @return The task.
     */
    public DeleteAllTask getDeleteAllTask()
    {
        return new DeleteAllTask(Nulls.STRING, this);
    }

    /**
     * Get a database task for deleting some groups.
     *
     * @param groupIds The group ids.
     * @return The task.
     */
    public DeleteGroupsTask getDeleteGroupsTask(int[] groupIds)
    {
        return new DeleteGroupsTask(groupIds, this);
    }

    /**
     * Get a database task for deleting some individual models.
     *
     * @param combinedIds The combined ids for the models.
     * @return The task.
     */
    public DeleteModelsTask getDeleteModelsTask(long[] combinedIds)
    {
        return new DeleteModelsTask(combinedIds, this);
    }

    /**
     * Get a database task for deleting non-session groups.
     *
     * @return The task.
     */
    public DeleteNonSessionGroupsTask getDeleteNonSessionGroupsTask()
    {
        return new DeleteNonSessionGroupsTask(this);
    }

    /**
     * Get a database task for deleting session groups.
     *
     * @return The task.
     */
    public DeleteSessionGroupsTask getDeleteSessionGroupsTask()
    {
        return new DeleteSessionGroupsTask(this);
    }

    /**
     * Get a database task for ensuring necessary columns exist.
     *
     * @param groupIds The group ids.
     * @param propertyDescriptors The property descriptors defining the
     *            necessary columns.
     * @return The task.
     */
    public EnsureColumnsTask getEnsureColumnsTask(int[] groupIds, Collection<? extends PropertyDescriptor<?>> propertyDescriptors)
    {
        return new EnsureColumnsTask(groupIds, propertyDescriptors, this);
    }

    /**
     * Get a database task for ensuring necessary indices exist.
     *
     * @param groupIds The group ids.
     * @param propertyMatchers The property matchers defining the necessary
     *            columns.
     * @return The task.
     */
    public EnsureIndicesTask getEnsureIndicesTask(int[] groupIds, Collection<? extends PropertyMatcher<?>> propertyMatchers)
    {
        return new EnsureIndicesTask(groupIds, propertyMatchers, this);
    }

    /**
     * Get a task for initializing the database schema.
     *
     * @return The task.
     */
    public InitSchemaTask getInitSchemaTask()
    {
        return new InitSchemaTask(this);
    }

    /**
     * Get a database task for inserting property values.
     *
     * @param <T> The type of the objects in the insert.
     * @param insert An object that provides the properties to be inserted.
     * @param listener Optional listener for cache modification reports.
     * @return The task.
     */
    public <T> InsertTask<T> getInsertTask(CacheDeposit<T> insert, CacheModificationListener listener)
    {
        return new InsertTask<T>(insert, listener, this);
    }

    /**
     * Get a database task for purging groups from the database.
     *
     * @param groupIds The group ids to be removed.
     * @return The task.
     */
    public PurgeGroupsTask getPurgeGroupsTask(int[] groupIds)
    {
        return new PurgeGroupsTask(groupIds, this);
    }

    /**
     * Get a database task for resetting the schema.
     *
     * @param schemaVersion The latest schema version.
     * @return The task.
     */
    public ResetSchemaTask getResetSchemaTask(String schemaVersion)
    {
        return new ResetSchemaTask(schemaVersion, this);
    }

    /**
     * Get a database task that retrieves the combined ids that match the input
     * parameters.
     *
     * @param groupIds The group ids.
     * @param parameters An optional list of property matchers to limit the
     *            query.
     * @param orderSpecifiers Optional specifiers of how the ids should be
     *            ordered. Note that order specifiers will slow down the query.
     * @param startIndex The first row index to return, <code>0</code> being the
     *            first row.
     * @param limit A limit on the number of ids returned.
     * @return The array of ids.
     */
    public RetrieveCombinedIdsTask getRetrieveCombinedIdsTask(int[] groupIds, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit)
    {
        return new RetrieveCombinedIdsTask(groupIds, parameters, orderSpecifiers, startIndex, limit, this);
    }

    /**
     * Get a database task for retrieving data model categories.
     *
     * @param groupIds The group ids.
     * @param selectSource Flag indicating that the source of the data model
     *            categories needs to be populated.
     * @param selectFamily Flag indicating that the family of the data model
     *            categories needs to be populated.
     * @param selectCategory Flag indicating that the category of the data model
     *            categories needs to be populated.
     * @param distinct Flag indicating if only distinct categories should be
     *            returned.
     * @return The task.
     */
    public RetrieveDataModelCategoriesTask getRetrieveDataModelCategoriesTask(int[] groupIds, boolean selectSource,
            boolean selectFamily, boolean selectCategory, boolean distinct)
    {
        return new RetrieveDataModelCategoriesTask(groupIds, selectSource, selectFamily, selectCategory, distinct, this);
    }

    /**
     * Get a database task for retrieving group ids.
     *
     * @param category The data model category. Any <code>null</code> values are
     *            treated as wildcards.
     * @param parameters Interval parameters for the query.
     * @return The task.
     */
    public RetrieveGroupIdsTask getRetrieveGroupIdsTask(DataModelCategory category,
            Collection<? extends IntervalPropertyMatcher<?>> parameters)
    {
        return getRetrieveGroupIdsTask(category, parameters, TimeSpan.newUnboundedEndTimeSpan(System.currentTimeMillis()),
                (Boolean)null);
    }

    /**
     * Get a database task for retrieving group ids.
     *
     * @param category The data model category. Any <code>null</code> values are
     *            treated as wildcards.
     * @param parameters Interval parameters for the query.
     * @param expirationRange If not {@code null}, the range that the groups'
     *            expiration times must lie within. If {@code null}, the groups'
     *            expiration times must be {@code null}.
     * @param critical If not {@code null}, the required criticality of the
     *            groups.
     * @return The task.
     */
    public RetrieveGroupIdsTask getRetrieveGroupIdsTask(DataModelCategory category,
            Collection<? extends IntervalPropertyMatcher<?>> parameters, TimeSpan expirationRange, Boolean critical)
    {
        return new RetrieveGroupIdsTask(category, parameters, this, expirationRange, critical);
    }

    /**
     * Get a database task for retrieving group property values.
     *
     * @param category The data model category. Any <code>null</code> values are
     *            treated as wildcards.
     * @param parameters An optional collection of property matchers to limit
     *            the query.
     * @param resultMap An input/output map of property descriptors to lists of
     *            results. The property descriptors in this map define which
     *            properties are to be retrieved from the database.
     * @return The task.
     */
    public RetrieveGroupValuesTask getRetrieveGroupValuesTask(DataModelCategory category,
            Collection<? extends IntervalPropertyMatcher<?>> parameters,
            Map<? extends PropertyDescriptor<?>, ? extends List<?>> resultMap)
    {
        return new RetrieveGroupValuesTask(category, parameters, resultMap, this);
    }

    /**
     * Get a database task for retrieving the schema version.
     *
     * @return The task.
     */
    public RetrieveSchemaVersionTask getRetrieveSchemaVersionTask()
    {
        return new RetrieveSchemaVersionTask(this);
    }

    /**
     * Get a database task for getting valid group ids.
     *
     * @param groupIds The group ids to be validated.
     * @return The task.
     */
    public RetrieveValidGroupIdsTask getRetrieveValidGroupIdsTask(int[] groupIds)
    {
        return new RetrieveValidGroupIdsTask(groupIds, this);
    }

    /**
     * Get a database task for retrieving property value sizes.
     *
     * @param ids The combined ids of the models.
     * @param desc The descriptor for the property whose value sizes are to be
     *            retrieved.
     * @return The task.
     */
    public RetrieveValueSizesTask getRetrieveValueSizesTask(long[] ids, PropertyDescriptor<?> desc)
    {
        return new RetrieveValueSizesTask(ids, desc, this);
    }

    /**
     * Get a database task for retrieving property values.
     *
     * @param ids The combined ids of the models.
     * @param resultMap An input/output map of property descriptors to lists of
     *            results. The property descriptors in this map define which
     *            properties are to be retrieved from the database.
     * @param failedIndices Return collection of indices into the {@code ids}
     *            array of elements that could not be retrieved. This may be
     *            {@code null} if failed indices do not need to be collected.
     * @return The task.
     */
    public RetrieveValuesTask getRetrieveValuesTask(long[] ids, PropertyValueMap resultMap, TIntList failedIndices)
    {
        return new RetrieveValuesTask(ids, resultMap, failedIndices, this);
    }

    /**
     * Accessor for the sqlGenerator.
     *
     * @return The sqlGenerator.
     */
    public SQLGenerator getSQLGenerator()
    {
        return mySQLGenerator;
    }

    /**
     * Accessor for the typeMapper.
     *
     * @return The typeMapper.
     */
    public TypeMapper getTypeMapper()
    {
        return myTypeMapper;
    }

    /**
     * Get a database task for updating property values.
     *
     * @param <T> The type of the input objects.
     * @param ids The combined ids for the models being updated.
     * @param input The objects providing the new values.
     * @param persistentAccessors Accessors that extract the new values from the
     *            input objects.
     * @param listener Optional return collection of cache modification reports.
     * @return The task.
     */
    public <T> InsertTask<T> getUpdateTask(long[] ids, Collection<? extends T> input,
            Collection<PersistentPropertyAccessor<? super T, ?>> persistentAccessors, CacheModificationListener listener)
    {
        return new InsertTask<T>(ids, input, persistentAccessors, listener, this);
    }
}
