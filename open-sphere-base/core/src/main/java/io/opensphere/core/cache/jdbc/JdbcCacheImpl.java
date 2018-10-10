package io.opensphere.core.cache.jdbc;

import java.io.NotSerializableException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import gnu.trove.list.TIntList;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.opensphere.core.cache.Cache;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheModificationListener;
import io.opensphere.core.cache.CacheRemovalListener;
import io.opensphere.core.cache.ClassProvider;
import io.opensphere.core.cache.PropertyValueMap;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.accessor.PersistentPropertyAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.jdbc.ConnectionAppropriator.ConnectionUser;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcherUtilities;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.model.Accumulator;
import io.opensphere.core.util.TimingMessageProvider;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.InlineExecutor;
import io.opensphere.core.util.lang.ImpossibleException;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * JDBC implementation of the {@link Cache} interface.
 */
@SuppressWarnings("PMD.GodClass")
public class JdbcCacheImpl implements Cache
{
    /** Schema version. */
    public static final String SCHEMA_VERSION = "17";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(JdbcCacheImpl.class);

    /** The SQL generator. */
    private static final SQLGenerator SQL_GENERATOR = new SQLGeneratorImpl();

    /** The type mapper responsible for mapping Java types to database types. */
    private static final TypeMapper TYPE_MAPPER = new TypeMapper();

    /** Cache utility class. */
    private final CacheUtilities myCacheUtil;

    /** Flag indicating if the cache is closed. */
    private volatile boolean myClosed = true;

    /** The connection appropriator. */
    private final ConnectionAppropriator myConnectionAppropriator;

    /** My connection source. */
    private final ConnectionSource myConnectionSource = this::getConnection;

    /** An in-memory cache of the database state. */
    private final DatabaseState myDatabaseState = new DatabaseState();

    /** The database task factory. */
    private volatile DatabaseTaskFactory myDatabaseTaskFactory;

    /** A data trimmer. */
    private volatile RowLimitDataTrimmer myDataTrimmer;

    /** Executor for background tasks. */
    private final Executor myExecutor;

    /** Exception indicating if the cache has failed to initialize. */
    private volatile CacheException myInitializationFailed;

    /**
     * Lock used for situations that require single-threaded database access.
     */
    private final ReadWriteLock myLock = new ReentrantReadWriteLock();

    /** The DB password. */
    private final String myPassword;

    /**
     * The maximum number of rows in the table before trimming occurs. A
     * negative number indicates no limit.
     */
    private final int myRowLimit;

    /** The DB url. */
    private final String myUrl;

    /** The DB username. */
    private final String myUsername;

    /**
     * Create the JDBC cache implementation.
     *
     * @param driver The name of the db driver class.
     * @param url The DB url.
     * @param username The DB username.
     * @param password The DB password.
     * @param rowLimit The maximum number of rows in a table before trimming
     *            occurs. A negative number indicates no limit.
     * @param executor An executor for background database tasks.
     * @throws ClassNotFoundException If the DB driver class cannot be found.
     */
    public JdbcCacheImpl(String driver, String url, String username, String password, int rowLimit,
            ScheduledExecutorService executor)
        throws ClassNotFoundException
    {
        Class.forName(driver);

        myUrl = url;
        myUsername = username;
        myPassword = password;
        myRowLimit = rowLimit;
        myCacheUtil = new CacheUtilities(getDbString(), getLock().readLock());
        myConnectionAppropriator = new ConnectionAppropriator(myConnectionSource);
        myExecutor = executor == null ? new InlineExecutor() : executor;
    }

    @Override
    public boolean acceptsPropertyDescriptor(PropertyDescriptor<?> desc)
    {
        return getTypeMapper().hasValueTranslator(desc);
    }

    @Override
    public void clear()
    {
        if (isClosed())
        {
            return;
        }

        Lock writeLock = getLock().writeLock();
        writeLock.lock();
        try
        {
            runTask(getDatabaseTaskFactory().getDeleteNonSessionGroupsTask());
        }
        catch (CacheException e)
        {
            LOGGER.error("Failed to clear cache: " + e, e);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void clear(DataModelCategory dmc, boolean returnIds, CacheRemovalListener listener) throws CacheException
    {
        int[] groupIds = getGroupIds(dmc);

        if (returnIds)
        {
            try
            {
                getIds(groupIds, (List<? extends PropertyMatcher<?>>)null, (List<? extends OrderSpecifier>)null, 0,
                        Integer.MAX_VALUE);
            }
            catch (NotSerializableException e)
            {
                throw new ImpossibleException(e);
            }
        }

        clearGroups(groupIds);

        listener.valuesRemoved(dmc, null);
    }

    @Override
    public void clear(final long[] ids) throws CacheException
    {
        if (isClosed() || ids == null || ids.length == 0)
        {
            return;
        }

        runTask(getDatabaseTaskFactory().getDeleteModelsTask(ids));
    }

    @Override
    public void clear(long[] ids, CacheRemovalListener listener) throws CacheException
    {
        clear(ids);

        // This implementation does not notify the listener.
    }

    @Override
    public void clearGroups(int[] groupIds) throws CacheException
    {
        if (isClosed() || groupIds.length == 0)
        {
            return;
        }

        runTask(getDatabaseTaskFactory().getDeleteGroupsTask(groupIds));
    }

    @Override
    public synchronized void close()
    {
        if (isClosed())
        {
            return;
        }

        try
        {
            runTask(getDatabaseTaskFactory().getDeleteSessionGroupsTask());
        }
        catch (CacheException e)
        {
            LOGGER.warn("Failed to remove old session groups: " + e, e);
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Closing cache.");
        }
        myClosed = true;
        if (myExecutor instanceof ScheduledExecutorService)
        {
            ((ScheduledExecutorService)myExecutor).shutdownNow();
            try
            {
                ((ScheduledExecutorService)myExecutor).awaitTermination(300, TimeUnit.SECONDS);
            }
            catch (InterruptedException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Interrupted while awaiting termination: " + e, e);
                }
            }
        }
    }

    @Override
    public DataModelCategory[] getDataModelCategories(final long[] ids) throws CacheException
    {
        if (isClosed() || ids.length == 0)
        {
            return New.emptyArray(DataModelCategory.class);
        }

        long t0 = System.nanoTime();

        int[] groupIds = getGroupIds(ids, false);
        int[] distinctGroupIds = Utilities.uniqueUnsorted(groupIds);
        List<DataModelCategory> dataModelCategories = getDataModelCategoriesByGroupId(distinctGroupIds, true, true, true, false);

        TIntObjectHashMap<DataModelCategory> map = new TIntObjectHashMap<>();
        for (int index = 0; index < distinctGroupIds.length; ++index)
        {
            map.put(distinctGroupIds[index], dataModelCategories.get(index));
        }

        DataModelCategory[] results = new DataModelCategory[ids.length];
        for (int index = 0; index < results.length;)
        {
            results[index] = map.get(groupIds[index++]);
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(StringUtilities.formatTimingMessage(
                    distinctGroupIds.length + " data model categories retrieved from cache in ", System.nanoTime() - t0));
        }

        return results;
    }

    @Override
    public List<DataModelCategory> getDataModelCategoriesByGroupId(final int[] groupIds, final boolean source,
            final boolean family, final boolean category, boolean distinct)
        throws CacheException
    {
        if (isClosed() || groupIds.length == 0 || !(source || family || category))
        {
            return Collections.<DataModelCategory>emptyList();
        }

        return runTask(getDatabaseTaskFactory().getRetrieveDataModelCategoriesTask(groupIds, source, family, category, distinct));
    }

    @Override
    public List<DataModelCategory> getDataModelCategoriesByModelId(final long[] ids, final boolean source, final boolean family,
            final boolean category)
        throws CacheException
    {
        int[] groupIds = getGroupIds(ids, true);
        return getDataModelCategoriesByGroupId(groupIds, source, family, category, true);
    }

    @Override
    public int[] getGroupIds(final DataModelCategory category) throws CacheException
    {
        if (isClosed())
        {
            return new int[0];
        }

        return runTask(getDatabaseTaskFactory().getRetrieveGroupIdsTask(category, (List<IntervalPropertyMatcher<?>>)null));
    }

    @Override
    public int[] getGroupIds(final long[] ids, final boolean distinct)
    {
        return getCacheUtil().getGroupIdsFromCombinedIds(ids, distinct);
    }

    @Override
    public long[] getIds(Collection<? extends Satisfaction> satisfactions, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit)
        throws CacheException, NotSerializableException
    {
        Utilities.checkNull(satisfactions, "satisfactions");
        if (isClosed() || satisfactions.isEmpty())
        {
            return new long[0];
        }

        int[] groupIds = new int[satisfactions.size()];
        int index = 0;
        for (Satisfaction satisfaction : satisfactions)
        {
            groupIds[index++] = ((JdbcSatisfaction)satisfaction).getGroupId();
        }

        return getIds(groupIds, parameters, orderSpecifiers, startIndex, limit);
    }

    @Override
    public long[] getIds(DataModelCategory category, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit)
        throws CacheException, NotSerializableException
    {
        Utilities.checkNull(category, "category");
        if (isClosed())
        {
            return new long[0];
        }

        final List<IntervalPropertyMatcher<?>> intervalParameters;
        if (CollectionUtilities.hasContent(parameters))
        {
            intervalParameters = PropertyMatcherUtilities.getGroupMatchers(parameters);

            // Replace the matchers with their group matchers.
            ListIterator<IntervalPropertyMatcher<?>> iterator = intervalParameters.listIterator();
            while (iterator.hasNext())
            {
                IntervalPropertyMatcher<?> matcher = iterator.next();
                IntervalPropertyMatcher<?> groupMatcher = matcher.getGroupMatcher();
                if (!Utilities.sameInstance(matcher, groupMatcher))
                {
                    iterator.remove();
                    iterator.add(groupMatcher);
                }
            }
        }
        else
        {
            intervalParameters = null;
        }

        try
        {
            int[] groupIds = runTask(getDatabaseTaskFactory().getRetrieveGroupIdsTask(category, intervalParameters));
            if (groupIds.length == 0)
            {
                return new long[0];
            }
            return runTask(getDatabaseTaskFactory().getRetrieveCombinedIdsTask(groupIds, parameters, orderSpecifiers, startIndex,
                    limit));
        }
        catch (CacheException e)
        {
            if (e.getCause() instanceof NotSerializableException)
            {
                throw (NotSerializableException)e.getCause();
            }
            throw e;
        }
    }

    @Override
    public long[] getIds(int[] groupIds, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit)
        throws NotSerializableException, CacheException
    {
        try
        {
            return runTask(getDatabaseTaskFactory().getRetrieveCombinedIdsTask(groupIds, parameters, orderSpecifiers, startIndex,
                    limit));
        }
        catch (CacheException e)
        {
            if (e.getCause() instanceof NotSerializableException)
            {
                throw (NotSerializableException)e.getCause();
            }
            throw e;
        }
    }

    @Override
    public Collection<? extends Satisfaction> getIntervalSatisfactions(DataModelCategory category,
            Collection<? extends IntervalPropertyMatcher<?>> parameters)
    {
        Utilities.checkNull(category, "category");
        Utilities.checkNull(parameters, "parameters");

        List<IntervalPropertyValueSet.Builder> ipvsBuilders = New.randomAccessList();

        Map<PropertyDescriptor<?>, List<Object>> resultMap = New.insertionOrderMap(parameters.size());
        for (IntervalPropertyMatcher<?> param : parameters)
        {
            resultMap.put(param.getPropertyDescriptor(), New.randomAccessList());
        }

        int[] groupIds;
        try
        {
            // Get the group ids that overlap the intervals.
            RetrieveGroupValuesTask task = getDatabaseTaskFactory().getRetrieveGroupValuesTask(category, parameters, resultMap);
            groupIds = runTask(task);

            // Now check to see if the group tables are empty, if so this
            // interval does not satisfy the query.
            RetrieveCombinedIdsTask idsTask = getDatabaseTaskFactory().getRetrieveCombinedIdsTask(groupIds, New.collection(),
                    New.list(), 0, 1);
            long[] combinedIds = runTask(idsTask);
            Collection<Satisfaction> results = null;
            if (combinedIds != null && combinedIds.length > 0)
            {
                // Create an ipvs builder for each group.
                for (int index = 0; index < task.getResultCount(); ++index)
                {
                    ipvsBuilders.add(new IntervalPropertyValueSet.Builder());
                }

                /* Check for the case where one of the groups has an indefinite
                 * interval. In this case, retrieve the values from the group
                 * table and replace the indefinite interval with the extent of
                 * the actual values. */
                Iterator<List<Object>> valueIter = resultMap.values().iterator();
                Iterator<? extends IntervalPropertyMatcher<?>> paramIter = parameters.iterator();
                while (valueIter.hasNext() && paramIter.hasNext())
                {
                    List<Object> values = valueIter.next();
                    IntervalPropertyMatcher<?> param = paramIter.next();
                    for (int groupIndex = 0; groupIndex < groupIds.length; ++groupIndex)
                    {
                        if (param.isIndefinite(values.get(groupIndex)))
                        {
                            int groupId = groupIds[groupIndex];
                            long[] ids = getIds(new int[] { groupId }, parameters, null, 0, Integer.MAX_VALUE);

                            if (ids.length > 0)
                            {
                                PropertyValueMap drillDownMap = new PropertyValueMap();
                                drillDownMap.addResultList(param.getPropertyDescriptor(), ids.length);
                                getValues(ids, drillDownMap, null);

                                List<?> resultList = drillDownMap.getResultList(param.getPropertyDescriptor());
                                @SuppressWarnings("unchecked")
                                Accumulator<Object> accumulator = (Accumulator<Object>)param.getAccumulator();
                                accumulator.addAll(resultList);
                                values.set(groupIndex, accumulator.getExtent());
                            }
                        }
                    }
                }

                // Put the group properties into the ipvs builders.
                for (Entry<PropertyDescriptor<?>, List<Object>> entry : resultMap.entrySet())
                {
                    for (int index = 0; index < entry.getValue().size(); ++index)
                    {
                        ipvsBuilders.get(index).add(entry.getKey(), entry.getValue().get(index));
                    }
                }

                // Create a satisfaction for each group.
                results = New.collection(ipvsBuilders.size());
                for (int index = 0; index < ipvsBuilders.size(); ++index)
                {
                    results.add(new JdbcSatisfaction(groupIds[index], ipvsBuilders.get(index).create()));
                }
            }
            else
            {
                results = New.collection();
            }

            return results;
        }
        catch (CacheException | NotSerializableException e)
        {
            LOGGER.error("Failed to retrieve satisfaction: " + e, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get the schema version required by the implementation.
     *
     * @return The schemaVersion.
     */
    public String getSchemaVersion()
    {
        return SCHEMA_VERSION;
    }

    @Override
    public void getValues(final long[] ids, final PropertyValueMap cacheResultMap, final TIntList failedIndices)
        throws CacheException
    {
        if (isClosed() || ids.length == 0)
        {
            return;
        }

        runTask(getDatabaseTaskFactory().getRetrieveValuesTask(ids, cacheResultMap, failedIndices));
    }

    @Override
    public long[] getValueSizes(long[] ids, PropertyDescriptor<?> desc) throws CacheException
    {
        if (isClosed() || ids.length == 0)
        {
            return new long[0];
        }

        return runTask(getDatabaseTaskFactory().getRetrieveValueSizesTask(ids, desc));
    }

    @Override
    public void initialize(final long millisecondsWait) throws CacheException
    {
        Runnable initTask = () ->
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Initializing cache with url: " + getUrl());
            }
            @SuppressWarnings("PMD.PrematureDeclaration")
            long t0 = System.nanoTime();
            try
            {
                getConnectionAppropriator().appropriateStatement((conn, stmt) ->
                {
                    initSchema(conn, stmt);
                    return null;
                });
            }
            catch (CacheException e)
            {
                setInitializationFailed(e);
                return;
            }

            createDataTrimmer();

            setOpen();

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(StringUtilities.formatTimingMessage("Cache initialized in ", System.nanoTime() - t0));
            }

            ScheduledExecutorService executor = getExecutor();
            if (executor != null)
            {
                getCacheUtil().startGarbageCollector(getDatabaseTaskFactory(), getSQLGenerator(), getConnectionAppropriator(),
                        executor);
            }
        };

        ScheduledExecutorService executor = getExecutor();
        if (executor == null)
        {
            initTask.run();
        }
        else
        {
            executor.execute(initTask);
            if (millisecondsWait > 0)
            {
                waitForInitialization(millisecondsWait);
            }
            else if (millisecondsWait < 0)
            {
                waitForInitialization(0);
            }
        }
    }

    /**
     * Get if the cache has been closed.
     *
     * @return If the cache has been closed.
     */
    public boolean isClosed()
    {
        if (myClosed)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Cache is closed.");
            }
            return true;
        }

        return false;
    }

    @Override
    public <T> long[] put(final CacheDeposit<T> insert, CacheModificationListener listener)
        throws CacheException, NotSerializableException
    {
        Utilities.checkNull(insert, "insert");
        Utilities.checkNull(insert.getCategory(), "insert.getCategory()");
        Utilities.checkNull(insert.getAccessors(), "insert.getAccessors()");
        Utilities.checkNull(insert.getInput(), "insert.getInput()");

        if (insert.isNew())
        {
            DataModelCategory category = insert.getCategory();
            Utilities.checkNull(category.getSource(), "insert.getCategory().getSource()");
            Utilities.checkNull(category.getFamily(), "insert.getCategory().getFamily()");
            Utilities.checkNull(category.getCategory(), "insert.getCategory().getCategory()");
        }

        if (isClosed())
        {
            return new long[0];
        }

        long[] ids = runTask(getDatabaseTaskFactory().getInsertTask(insert, listener));

        if (ids.length > 0 && myRowLimit >= 0)
        {
            scheduleDataTrimmer();
        }

        return ids;
    }

    @Override
    public void setClassProvider(ClassProvider provider)
    {
    }

    @Override
    public void setInMemorySizeBytes(long bytes) throws CacheException
    {
    }

    @Override
    public void setOnDiskSizeLimitBytes(long bytes)
    {
    }

    @Override
    public <T> void updateValues(final long[] ids, final Collection<? extends T> input,
            final Collection<? extends PropertyAccessor<? super T, ?>> accessors, Executor executor,
            CacheModificationListener listener)
        throws CacheException, NotSerializableException
    {
        Utilities.checkNull(ids, "ids");
        Utilities.checkNull(input, "input");
        Utilities.checkNull(accessors, "accessors");

        if (input.size() != 1 && ids.length != input.size())
        {
            throw new IllegalArgumentException(
                    "Either the input collection must be a singleton or must match the size of the id array.");
        }

        Collection<PersistentPropertyAccessor<? super T, ?>> persistentAccessors = New.collection();
        for (PropertyAccessor<? super T, ?> propertyAccessor : accessors)
        {
            if (propertyAccessor instanceof PersistentPropertyAccessor)
            {
                persistentAccessors.add((PersistentPropertyAccessor<? super T, ?>)propertyAccessor);
            }
        }
        if (!persistentAccessors.isEmpty())
        {
            runTask(getDatabaseTaskFactory().getUpdateTask(ids, input, persistentAccessors, listener));
        }
    }

    /**
     * Block until the cache is initialized or the thread is interrupted.
     *
     * @param timeout How long to wait, in milliseconds. If the timeout is
     *            <code>0</code>, the wait is indefinite.
     * @return <code>true</code> if initialization was completed before the
     *         timeout.
     * @throws CacheException If initialization fails.
     */
    public boolean waitForInitialization(long timeout) throws CacheException
    {
        try
        {
            while (myClosed)
            {
                synchronized (this)
                {
                    wait(timeout);
                    if (myInitializationFailed != null)
                    {
                        throw new CacheException("Cache initialization failed: " + myInitializationFailed.getMessage(),
                                myInitializationFailed);
                    }
                }
            }
        }
        catch (InterruptedException e)
        {
        }
        return !myClosed;
    }

    /**
     * Create the database task factory.
     *
     * @return The factory.
     */
    protected DatabaseTaskFactory createDatabaseTaskFactory()
    {
        return new DatabaseTaskFactory(getCacheUtil(), getDatabaseState(), getSQLGenerator(), getTypeMapper());
    }

    /**
     * Create the data trimmer.
     */
    protected void createDataTrimmer()
    {
        if (myRowLimit >= 0)
        {
            myDataTrimmer = new RowLimitDataTrimmer(myRowLimit, getCacheUtil(), getConnectionAppropriator(), getSQLGenerator(),
                    getLock().readLock());
        }
    }

    /**
     * Access the cache utilities.
     *
     * @return The cache utilities.
     */
    protected CacheUtilities getCacheUtil()
    {
        return myCacheUtil;
    }

    /**
     * Get a connection to the database.
     *
     * @return The database connection.
     * @throws CacheException If the connection could not be created.
     */
    protected Connection getConnection() throws CacheException
    {
        try
        {
            return DriverManager.getConnection(getUrl(), getUsername(), getPassword());
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to get database connection: " + e, e);
        }
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
     * Get the connection source.
     *
     * @return The connection source.
     */
    protected final ConnectionSource getConnectionSource()
    {
        return myConnectionSource;
    }

    /**
     * Get the in-memory cache of the database state.
     *
     * @return The database state.
     */
    protected DatabaseState getDatabaseState()
    {
        return myDatabaseState;
    }

    /**
     * Get the database task factory.
     *
     * @return The database task factory.
     */
    protected DatabaseTaskFactory getDatabaseTaskFactory()
    {
        if (myDatabaseTaskFactory == null)
        {
            synchronized (this)
            {
                if (myDatabaseTaskFactory == null)
                {
                    myDatabaseTaskFactory = createDatabaseTaskFactory();
                }
            }
        }
        return myDatabaseTaskFactory;
    }

    /**
     * Construct a string that identifies a database for logging purposes.
     *
     * @return The database string.
     */
    protected final String getDbString()
    {
        return "[" + getUsername() + "@" + getUrl() + "]";
    }

    /**
     * Get the executor service for background activities.
     *
     * @return The executor service.
     */
    protected ScheduledExecutorService getExecutor()
    {
        return (ScheduledExecutorService)(myExecutor instanceof ScheduledExecutorService ? myExecutor : null);
    }

    /**
     * Access the database lock. This is used to change the database structure
     * and for deleting data groups.
     *
     * @return The lock.
     */
    protected ReadWriteLock getLock()
    {
        return myLock;
    }

    /**
     * Get the DB password.
     *
     * @return The DB password.
     */
    protected String getPassword()
    {
        return myPassword;
    }

    /**
     * Get the SQL generator.
     *
     * @return The SQL generator.
     */
    protected SQLGenerator getSQLGenerator()
    {
        return SQL_GENERATOR;
    }

    /**
     * Get the type mapper. This is a hook to allow subclasses to override the
     * type mapper.
     *
     * @return The type mapper.
     */
    protected TypeMapper getTypeMapper()
    {
        return TYPE_MAPPER;
    }

    /**
     * Get the DB URL.
     *
     * @return The DB URL.
     */
    protected final String getUrl()
    {
        return myUrl;
    }

    /**
     * Get the DB username.
     *
     * @return The DB username.
     */
    protected final String getUsername()
    {
        return myUsername;
    }

    /**
     * Create the tables and sequences in the database.
     *
     * @param conn The DB connection.
     * @param stmt The DB statement.
     *
     * @throws CacheException If the schema cannot be initialized.
     */
    protected void initSchema(Connection conn, Statement stmt) throws CacheException
    {
        Lock writeLock = getLock().writeLock();
        writeLock.lock();
        try
        {
            String version;
            try
            {
                version = getDatabaseTaskFactory().getRetrieveSchemaVersionTask().run(conn, stmt);
            }
            catch (CacheException e)
            {
                // The version table may not have been created.
                version = null;
            }
            if (!getSchemaVersion().equals(version))
            {
                runTask(getDatabaseTaskFactory().getResetSchemaTask(getSchemaVersion()), conn, stmt);
            }

            runTask(getDatabaseTaskFactory().getInitSchemaTask(), conn, stmt);
            runTask(getDatabaseTaskFactory().getDeleteSessionGroupsTask(), conn, stmt);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Run a database task.
     *
     * @param <T> The return type of the task.
     * @param task The task.
     * @return The value from the task.
     * @throws CacheException If there is a database error.
     */
    protected <T> T runTask(final ConnectionUser<T> task) throws CacheException
    {
        Lock readLock = getLock().readLock();
        readLock.lock();
        try
        {
            return getConnectionAppropriator().appropriateConnection(c -> runTask(task, c), false);
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Run a database task with an existing connection.
     *
     * @param <T> The return type of the task.
     * @param task The task.
     * @param conn The database connection.
     * @return The value from the task.
     * @throws CacheException If there is a database error.
     */
    protected <T> T runTask(final ConnectionUser<T> task, Connection conn) throws CacheException
    {
        T result;
        long t0 = System.nanoTime();
        result = task.run(conn);
        if (LOGGER.isDebugEnabled() && task instanceof TimingMessageProvider)
        {
            LOGGER.debug(StringUtilities.formatTimingMessage((TimingMessageProvider)task, System.nanoTime() - t0));
        }

        return result;
    }

    /**
     * Run a database task that requires a statement.
     *
     * @param <T> The return type of the task.
     * @param task The task.
     * @return The value from the task.
     * @throws CacheException If there is a database error.
     */
    protected <T> T runTask(final StatementUser<T> task) throws CacheException
    {
        return runTask(c -> runTask(task, c));
    }

    /**
     * Run a database task that requires a statement, with an existing
     * connection.
     *
     * @param <T> The return type of the task.
     * @param task The task.
     * @param conn The database connection.
     * @return The value from the task.
     * @throws CacheException If there is a database error.
     */
    protected <T> T runTask(final StatementUser<T> task, Connection conn) throws CacheException
    {
        return new StatementAppropriator(conn).appropriateStatement((c, s) -> runTask(task, c, s));
    }

    /**
     * Run a database task that requires a statement, providing the statement.
     *
     * @param <T> The return type of the task.
     * @param task The task.
     * @param conn The database connection.
     * @param stmt The database statement.
     * @return The value from the task.
     * @throws CacheException If there is a database error.
     */
    protected <T> T runTask(StatementUser<T> task, Connection conn, Statement stmt) throws CacheException
    {
        T result;
        long t0 = System.nanoTime();
        result = task.run(conn, stmt);
        if (LOGGER.isDebugEnabled() && task instanceof TimingMessageProvider)
        {
            LOGGER.debug(StringUtilities.formatTimingMessage((TimingMessageProvider)task, System.nanoTime() - t0));
        }

        return result;
    }

    /**
     * Schedule the data trimmer.
     */
    protected void scheduleDataTrimmer()
    {
        getCacheUtil().scheduleDataTrimmer(myDataTrimmer, getExecutor());
    }

    /**
     * Set the initialization failed flag.
     *
     * @param e The exception that occurred.
     */
    protected void setInitializationFailed(CacheException e)
    {
        synchronized (this)
        {
            myInitializationFailed = e;
            notifyAll();
        }
    }

    /**
     * Mark the cache as open. This is called upon on the completion of the
     * initialize sequence and should not be called again.
     */
    protected void setOpen()
    {
        if (myExecutor instanceof ScheduledExecutorService && ((ScheduledExecutorService)myExecutor).isShutdown())
        {
            throw new IllegalStateException("Cannot reopen cache after it has been closed.");
        }
        synchronized (this)
        {
            myClosed = false;
            notifyAll();
        }
    }

    /**
     * Implementation of {@link io.opensphere.core.data.util.Satisfaction}.
     */
    protected static class JdbcSatisfaction extends SingleSatisfaction
    {
        /** The group id. */
        private final int myGroupId;

        /**
         * Constructor.
         *
         * @param groupId The group id.
         * @param intervalPropertyValueSet The interval property value set.
         */
        public JdbcSatisfaction(int groupId, IntervalPropertyValueSet intervalPropertyValueSet)
        {
            super(intervalPropertyValueSet);
            myGroupId = groupId;
        }

        /**
         * The id for the group that provides this satisfaction.
         *
         * @return The group id.
         */
        public int getGroupId()
        {
            return myGroupId;
        }
    }
}
