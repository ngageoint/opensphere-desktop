package io.opensphere.auxiliary.cache.jdbc;

import java.sql.Connection;
import java.sql.Statement;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.DatabaseTaskFactory;
import io.opensphere.core.cache.jdbc.PurgeGroupsTask;
import io.opensphere.core.cache.jdbc.TableNames;

/**
 * Extension to {@link PurgeGroupsTask} that also removes the Hatbox tables.
 */
public class HatboxPurgeGroupsTask extends PurgeGroupsTask
{
    /**
     * Constructor.
     *
     * @param groupIds The group ids.
     * @param databaseTaskFactory The database task factory.
     */
    public HatboxPurgeGroupsTask(int[] groupIds, DatabaseTaskFactory databaseTaskFactory)
    {
        super(groupIds, databaseTaskFactory);
    }

    @Override
    protected void purgeGroupTable(int groupId, Connection conn, Statement stmt) throws CacheException
    {
        String tableName = TableNames.getDataTableName(groupId);
        HatboxUtilities.despatialize((H2DatabaseState)getDatabaseState(), conn, "PUBLIC", tableName);
        super.purgeGroupTable(groupId, conn, stmt);
    }
}
