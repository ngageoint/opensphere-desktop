package io.opensphere.auxiliary.cache.jdbc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheModificationListener;
import io.opensphere.core.cache.jdbc.CacheUtilities;
import io.opensphere.core.cache.jdbc.DatabaseTaskFactory;
import io.opensphere.core.cache.jdbc.PurgeGroupsTask;
import io.opensphere.core.cache.jdbc.RetrieveGroupIdsTask;
import io.opensphere.core.cache.jdbc.SQLGenerator;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.lang.Nulls;

/**
 * An extension to {@link DatabaseTaskFactory} that provides extended versions
 * of some tasks to support Hatbox.
 */
public class HatboxDatabaseTaskFactory extends H2DatabaseTaskFactory
{
    /**
     * Constructor.
     *
     * @param cacheUtilities The cache utilities.
     * @param databaseState The database state.
     * @param sqlGenerator The SQL generator.
     * @param typeMapper The type mapper.
     */
    public HatboxDatabaseTaskFactory(CacheUtilities cacheUtilities, H2DatabaseState databaseState, SQLGenerator sqlGenerator,
            H2TypeMapper typeMapper)
    {
        super(cacheUtilities, databaseState, sqlGenerator, typeMapper);
    }

    @Override
    public HatboxDeleteAllTask getDeleteAllTask()
    {
        return new HatboxDeleteAllTask(this, Nulls.STRING);
    }

    @Override
    public HatboxEnsureColumnsTask getEnsureColumnsTask(int[] groupIds,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors)
    {
        return new HatboxEnsureColumnsTask(groupIds, propertyDescriptors, this);
    }

    @Override
    public HatboxInitSchemaTask getInitSchemaTask()
    {
        return new HatboxInitSchemaTask(this);
    }

    @Override
    public <T> HatboxInsertTask<T> getInsertTask(CacheDeposit<T> insert, CacheModificationListener listener)
    {
        return new HatboxInsertTask<T>(insert, listener, this);
    }

    @Override
    public PurgeGroupsTask getPurgeGroupsTask(int[] groupIds)
    {
        return new HatboxPurgeGroupsTask(groupIds, this);
    }

    @Override
    public RetrieveGroupIdsTask getRetrieveGroupIdsTask(DataModelCategory category,
            Collection<? extends IntervalPropertyMatcher<?>> parameters, TimeSpan expirationRange, Boolean critical)
    {
        return new HatboxRetrieveGroupIdsTask(category, parameters, this, expirationRange, critical);
    }

    @Override
    public HatboxRetrieveGroupValuesTask getRetrieveGroupValuesTask(DataModelCategory category,
            Collection<? extends IntervalPropertyMatcher<?>> parameters,
            Map<? extends PropertyDescriptor<?>, ? extends List<?>> resultMap)
    {
        return new HatboxRetrieveGroupValuesTask(category, parameters, resultMap, this);
    }
}
