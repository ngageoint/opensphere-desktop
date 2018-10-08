package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.PreparedStatementUser;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Database task that creates a temporary table to contain some ids.
 */
public class CreateIdJoinTableTask extends DatabaseTask implements StatementUser<String>
{
    /** The name of the column for the ids. */
    private final String myIdColumnName;

    /** Ids to be put into the table. */
    private final int[] myIds;

    /** Optional sequence column name. */
    private final String myOrderColumnName;

    /**
     * Constructor.
     *
     * @param ids The ids to be put in the table.
     * @param idColumnName The id column name.
     * @param orderColumnName If this is non-null, a sequence column will be
     *            created in the table using this column name that may be used
     *            to maintain order of the rows.
     * @param databaseTaskFactory The database task factory.
     */
    public CreateIdJoinTableTask(int[] ids, String idColumnName, String orderColumnName, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(ids, "ids");
        Utilities.checkNull(idColumnName, "idColumnName");
        myIds = ids.clone();
        myIdColumnName = idColumnName;
        myOrderColumnName = orderColumnName;
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to set up id join table with " + getIds().length + " ids: ";
    }

    @Override
    public String run(Connection conn, Statement stmt) throws CacheException
    {
        String tempTableName = getDatabaseState().getNextTempTableName();

        Map<String, String> columnNamesToTypes = New.map();
        columnNamesToTypes.put(getIdColumnName(), getTypeMapper().getSqlColumnDefinition(Integer.class, false));
        if (getOrderColumnName() != null)
        {
            columnNamesToTypes.put(getOrderColumnName(), getTypeMapper().getSqlColumnDefinition(Integer.class, null, false, 1));
        }

        String createTableSql = getSQLGenerator().generateCreateTemporaryTable(tempTableName, columnNamesToTypes,
                (PrimaryKeyConstraint)null);
        getCacheUtilities().execute(createTableSql, stmt);

        final String sql = getSQLGenerator().generateInsert(tempTableName, getIdColumnName());
        PreparedStatementUser<Void> user = new PreparedStatementUser<>()
        {
            @Override
            public Void run(Connection unused, PreparedStatement pstmt) throws CacheException
            {
                try
                {
                    for (int index = 0; index < getIds().length;)
                    {
                        int id = getIds()[index++];
                        pstmt.setInt(1, id);
                        if (getCacheUtilities().executeUpdate(pstmt, sql) != 1)
                        {
                            throw new CacheException("Failed to insert value into join table.");
                        }
                    }

                    return null;
                }
                catch (SQLException e)
                {
                    throw getCacheUtilities().createCacheException(sql, e);
                }
            }
        };

        new StatementAppropriator(conn).appropriateStatement(user, sql);

        // Create an index if there are enough ids.
        if (getIds().length > 50)
        {
            String createIndexSql = getSQLGenerator().generateCreateIndex(tempTableName + "_INDEX", tempTableName, false,
                    getIdColumnName());
            getCacheUtilities().execute(createIndexSql, stmt);
        }

        return tempTableName;
    }

    /**
     * Accessor for the idColumnName.
     *
     * @return The idColumnName.
     */
    protected String getIdColumnName()
    {
        return myIdColumnName;
    }

    /**
     * Accessor for the ids.
     *
     * @return The ids.
     */
    protected int[] getIds()
    {
        return myIds;
    }

    /**
     * Accessor for the orderColumnName.
     *
     * @return The orderColumnName.
     */
    protected String getOrderColumnName()
    {
        return myOrderColumnName;
    }
}
