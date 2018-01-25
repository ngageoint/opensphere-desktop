package io.opensphere.auxiliary.cache.jdbc;

import java.sql.Connection;
import java.util.Collection;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.DatabaseTaskFactory;
import io.opensphere.core.cache.jdbc.EnsureColumnsTask;
import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * Extension to {@link EnsureColumnsTask} that creates spatial indices for
 * geometry columns.
 */
public class HatboxEnsureColumnsTask extends EnsureColumnsTask
{
    /**
     * Constructor.
     *
     * @param groupIds The group ids.
     * @param propertyDescriptors The property descriptors.
     * @param databaseTaskFactory The database task factory.
     */
    public HatboxEnsureColumnsTask(int[] groupIds, Collection<? extends PropertyDescriptor<?>> propertyDescriptors,
            DatabaseTaskFactory databaseTaskFactory)
    {
        super(groupIds, propertyDescriptors, databaseTaskFactory);
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this is a geometry column, spatialize the table.
     */
    @Override
    protected void addColumn(String tableName, String columnName, String columnType, Connection conn) throws CacheException
    {
        super.addColumn(tableName, columnName, columnType, conn);

        Collection<? extends PropertyDescriptor<?>> propertyDescriptors = getPropertyDescriptors();
        for (PropertyDescriptor<?> propertyDescriptor : propertyDescriptors)
        {
            if (Geometry.class.isAssignableFrom(propertyDescriptor.getType())
                    && getTypeMapper().getColumnNames(propertyDescriptor).get(0).equals(columnName))
            {
                HatboxUtilities.spatialize(conn, "PUBLIC", tableName, columnName);
                HatboxUtilities.buildSpatialIndex((H2DatabaseState)getDatabaseState(), getCacheUtilities(), conn, tableName,
                        columnName);
            }
        }
    }
}
