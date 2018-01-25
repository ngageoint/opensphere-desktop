package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.util.lang.Nulls;

/**
 * A database task that deletes all rows from tables whose names match a
 * pattern.
 */
public class DeleteAllTask extends DatabaseTask implements StatementUser<Void>
{
    /** The table name pattern. */
    private final String myTableNamePattern;

    /**
     * Constructor.
     *
     * @param tableNamePattern The table name pattern. Null may be used to
     *            indicate all tables.
     * @param databaseTaskFactory The database task factory.
     */
    public DeleteAllTask(String tableNamePattern, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        myTableNamePattern = tableNamePattern;
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to delete all rows from tables with names patching pattern ["
                + (myTableNamePattern == null ? "%" : myTableNamePattern) + "]: ";
    }

    @Override
    public Void run(Connection conn, Statement stmt) throws CacheException
    {
        Collection<String> tableNames;
        try
        {
            tableNames = getCacheUtilities().getTableNames(stmt.getConnection(), Nulls.STRING, Nulls.STRING, myTableNamePattern);
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to get connection from statement: " + e, e);
        }
        for (String tableName : tableNames)
        {
            if (!TableNames.SCHEMA_VERSION.equals(tableName))
            {
                getCacheUtilities().execute(getSQLGenerator().generateDropTable(tableName), stmt);
            }
        }
        return null;
    }

    /**
     * Get the table name pattern.
     *
     * @return The table name pattern.
     */
    protected String getTableNamePattern()
    {
        return myTableNamePattern;
    }
}
