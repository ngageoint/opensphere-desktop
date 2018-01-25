package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheIdUtilities;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.util.Utilities;

/**
 * Database task that deletes individual models.
 */
public class DeleteModelsTask extends DatabaseTask implements StatementUser<Void>
{
    /** The ids being deleted. */
    private final long[] myCombinedIds;

    /**
     * Constructor.
     *
     * @param combinedIds The combined ids to be deleted.
     * @param databaseTaskFactory The database task factory.
     */
    public DeleteModelsTask(long[] combinedIds, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(combinedIds, "combinedIds");
        myCombinedIds = combinedIds.clone();
    }

    @Override
    public String getTimingMessage()
    {
        if (myCombinedIds.length < 10)
        {
            return "Ids " + Arrays.toString(myCombinedIds) + " cleared from cache in ";
        }
        else
        {
            return myCombinedIds.length + " ids cleared from cache in ";
        }
    }

    @Override
    public Void run(final Connection conn, final Statement stmt) throws CacheException
    {
        CacheIdUtilities.forEachGroup(myCombinedIds, new CacheIdUtilities.DatabaseGroupFunctor()
        {
            @Override
            public void run(long[] combinedIds, int groupId, int[] dataIds) throws CacheException
            {
                String tableName = TableNames.getDataTableName(groupId);

                String sql;
                if (dataIds.length > CacheUtilities.ID_JOIN_THRESHOLD)
                {
                    String joinTableName = getDatabaseTaskFactory().getCreateIdJoinTableTask(dataIds, ColumnNames.JOIN_ID)
                            .run(conn, stmt);
                    sql = getSQLGenerator().generateDelete(tableName, new JoinTableColumn(joinTableName));
                }
                else
                {
                    sql = getSQLGenerator().generateDelete(tableName, dataIds);
                }
                getCacheUtilities().execute(sql, stmt);
            }
        });
        return null;
    }
}
