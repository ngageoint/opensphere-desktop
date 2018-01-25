package io.opensphere.auxiliary.cache.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.DatabaseTaskFactory;
import io.opensphere.core.cache.jdbc.DeleteAllTask;
import io.opensphere.core.util.lang.Nulls;

/**
 * Extension to {@link DeleteAllTask} that removes hatbox triggers and tables.
 */
public class HatboxDeleteAllTask extends DeleteAllTask
{
    /**
     * Constructor.
     *
     * @param databaseTaskFactory The database task factory.
     * @param tableNamePattern The table name pattern. Null may be used to
     *            indicate all tables.
     */
    public HatboxDeleteAllTask(DatabaseTaskFactory databaseTaskFactory, String tableNamePattern)
    {
        super(tableNamePattern, databaseTaskFactory);
    }

    @Override
    public Void run(Connection conn, Statement stmt) throws CacheException
    {
        // Also need to remove triggers so the indices will be rebuilt.
        Collection<String> triggerNames = HatboxUtilities.getTriggerNames(getCacheUtilities(), stmt);
        for (String trigger : triggerNames)
        {
            getCacheUtilities().execute(getSQLGenerator().generateDropTrigger(trigger), stmt);
        }

        // Need to drop hatbox tables so they will be rebuilt.
        Collection<String> hatboxTableNames;
        try
        {
            String tableNamePattern = getTableNamePattern();
            if (tableNamePattern == null)
            {
                tableNamePattern = "%";
            }
            hatboxTableNames = getCacheUtilities().getTableNames(stmt.getConnection(), Nulls.STRING, Nulls.STRING,
                    tableNamePattern + "_HATBOX");
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to get connection from statement: " + e, e);
        }
        for (String tableName : hatboxTableNames)
        {
            String sql = getSQLGenerator().generateDropTable(tableName);
            getCacheUtilities().execute(sql, stmt);
        }

        return super.run(conn, stmt);
    }
}
