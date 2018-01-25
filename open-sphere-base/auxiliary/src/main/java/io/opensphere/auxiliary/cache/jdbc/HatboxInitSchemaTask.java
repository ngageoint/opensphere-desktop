package io.opensphere.auxiliary.cache.jdbc;

import java.sql.Connection;
import java.sql.Statement;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.InitSchemaTask;
import io.opensphere.core.cache.jdbc.TableNames;

/**
 * A task for initializing the schema. This does not affect the version table.
 */
public class HatboxInitSchemaTask extends InitSchemaTask
{
    /**
     * Constructor.
     *
     * @param databaseTaskFactory The database task factory.
     */
    public HatboxInitSchemaTask(HatboxDatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
    }

    @Override
    protected void createGroupTableIndex(String groupTableName, Connection conn, Statement stmt) throws CacheException
    {
        if (groupTableName.equals(TableNames.getGroupTableName(Geometry.class)))
        {
            HatboxUtilities.spatialize(conn, H2CacheImpl.SCHEMA_NAME, groupTableName, "VALUE");
            HatboxUtilities.buildSpatialIndex((H2DatabaseState)getDatabaseState(), getCacheUtilities(), conn, groupTableName,
                    "VALUE");
        }
        else
        {
            super.createGroupTableIndex(groupTableName, conn, stmt);
        }
    }
}
