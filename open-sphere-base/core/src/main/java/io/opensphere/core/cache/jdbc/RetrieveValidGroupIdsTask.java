package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.PreparedStatementUser;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;

/**
 * Database task that determines which of a set of group ids are valid. (That
 * is, the groups exist and have not expired.)
 */
public class RetrieveValidGroupIdsTask extends DatabaseTask implements StatementUser<int[]>
{
    /** The group ids being validated. */
    private final int[] myGroupIds;

    /**
     * Constructor.
     *
     * @param groupIds The ids of the groups to validate.
     * @param databaseTaskFactory The database task factory.
     */
    public RetrieveValidGroupIdsTask(int[] groupIds, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        myGroupIds = Utilities.uniqueUnsorted(groupIds);
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to validate " + getGroupIds().length + " group ids: ";
    }

    @Override
    public int[] run(Connection conn, Statement stmt) throws CacheException
    {
        JoinTableColumn joinTableColumn;
        if (getGroupIds().length > CacheUtilities.ID_JOIN_THRESHOLD)
        {
            joinTableColumn = new JoinTableColumn(
                    getDatabaseTaskFactory().getCreateIdJoinTableTask(getGroupIds(), ColumnNames.JOIN_ID).run(conn, stmt));
        }
        else
        {
            joinTableColumn = null;
        }

        final TimeSpan expirationRange = TimeSpan.newUnboundedEndTimeSpan(System.currentTimeMillis());
        final String sql = getSQLGenerator().generateRetrieveGroupValuesSql(getGroupIds(), joinTableColumn,
                (DataModelCategory)null, (Collection<PropertyMatcher<?>>)null, (Collection<PropertyDescriptor<?>>)null,
                expirationRange, (Boolean)null);
        return new StatementAppropriator(conn).appropriateStatement(new PreparedStatementUser<int[]>()
        {
            @Override
            public int[] run(Connection unused, PreparedStatement pstmt) throws CacheException
            {
                try
                {
                    pstmt.setLong(1, expirationRange.getStart());
                    ResultSet rs = getCacheUtilities().executeQuery(pstmt, sql);
                    try
                    {
                        return getCacheUtilities().convertResultSetToIntArray(rs);
                    }
                    finally
                    {
                        rs.close();
                    }
                }
                catch (SQLException e)
                {
                    throw new CacheException("Failed to read group ids from cache: " + e, e);
                }
            }
        }, sql);
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
}
