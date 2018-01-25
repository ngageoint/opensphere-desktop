package io.opensphere.auxiliary.cache.jdbc;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheModificationListener;
import io.opensphere.core.cache.accessor.PersistentPropertyAccessor;
import io.opensphere.core.cache.jdbc.DatabaseTaskFactory;
import io.opensphere.core.cache.jdbc.InsertTask;
import io.opensphere.core.cache.jdbc.TableNames;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.util.collections.CollectionUtilities;

/**
 * Extension to {@link InsertTask} with specialization for Hatbox.
 *
 * @param <T> The type of objects in the input collection.
 */
public class HatboxInsertTask<T> extends InsertTask<T>
{
    /**
     * Constructor for a category-based insert or update.
     *
     * @param insert An object that provides the property values to be
     *            persisted.
     * @param listener Optional listener for cache modification reports.
     * @param databaseTaskFactory The database task factory.
     */
    public HatboxInsertTask(CacheDeposit<T> insert, CacheModificationListener listener, DatabaseTaskFactory databaseTaskFactory)
    {
        super(insert, listener, databaseTaskFactory);
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
    public HatboxInsertTask(long[] ids, Collection<? extends T> input,
            Collection<? extends PersistentPropertyAccessor<? super T, ?>> accessors, CacheModificationListener listener,
            DatabaseTaskFactory databaseTaskFactory)
    {
        super(ids, input, accessors, listener, databaseTaskFactory);
    }

    @Override
    protected int createDataTable(Collection<? extends PropertyDescriptor<?>> propertyDescriptors,
            Map<String, String> columnNamesToTypes, Connection conn) throws CacheException
    {
        int groupId = super.createDataTable(propertyDescriptors, columnNamesToTypes, conn);

        int geomIndex = getCacheUtilities().indexOfProperty(propertyDescriptors, Geometry.class);
        if (geomIndex > -1)
        {
            String geometryColumnName = CollectionUtilities.getItem(columnNamesToTypes.keySet(), geomIndex);

            HatboxUtilities.spatialize(conn, "PUBLIC", TableNames.getDataTableName(groupId), geometryColumnName);
        }

        return groupId;
    }

    @Override
    protected void postPopulateDataTable(Connection conn, String tableName,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, Collection<String> columnNames) throws CacheException
    {
        super.postPopulateDataTable(conn, tableName, propertyDescriptors, columnNames);

        int geomIndex = getCacheUtilities().indexOfProperty(propertyDescriptors, Geometry.class);
        if (geomIndex > -1)
        {
            String geometryColumnName = CollectionUtilities.getItem(columnNames, geomIndex);

            HatboxUtilities.buildSpatialIndex((H2DatabaseState)getDatabaseState(), getCacheUtilities(), conn, tableName,
                    geometryColumnName);
        }
    }
}
