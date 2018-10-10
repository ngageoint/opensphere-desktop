package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.util.Utilities;

/**
 * Database task for deleting groups.
 */
public class DeleteGroupsTask extends AbstractDeleteGroupsTask
{
    /** The group ids being deleted. */
    private final int[] myGroupIds;

    /**
     * Constructor.
     *
     * @param groupIds The ids of the groups to be deleted.
     * @param databaseTaskFactory The database task factory.
     */
    public DeleteGroupsTask(int[] groupIds, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(groupIds, "groupIds");
        myGroupIds = groupIds.clone();
    }

    @Override
    public String getTimingMessage()
    {
        if (getGroupIds().length < 10)
        {
            return "Group ids " + Arrays.toString(getGroupIds()) + " cleared from cache in ";
        }
        return getGroupIds().length + " group ids cleared from cache in ";
    }

    /**
     * Accessor for the groupIds.
     *
     * @return The groupIds.
     */
    protected int[] getGroupIds()
    {
        return myGroupIds;
    }

    @Override
    protected String getWhereExpression(Connection conn, Statement stmt) throws CacheException
    {
        if (getGroupIds().length > CacheUtilities.ID_JOIN_THRESHOLD)
        {
            String joinTableName = getDatabaseTaskFactory().getCreateIdJoinTableTask(getGroupIds(), ColumnNames.JOIN_ID).run(conn,
                    stmt);
            return getSQLGenerator().generateWhereExpression(ColumnNames.GROUP_ID, new JoinTableColumn(joinTableName));
        }
        return getSQLGenerator().generateWhereExpression(ColumnNames.GROUP_ID, getGroupIds());
    }
}
