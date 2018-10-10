package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.PreparedStatementUser;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.util.Utilities;

/**
 * Database task used when the schema version on disk is not what is expected.
 * This resets the version and drops existing tables.
 */
public class ResetSchemaTask extends DatabaseTask implements StatementUser<Void>
{
    /** The expected schema version. */
    private final String mySchemaVersion;

    /**
     * Constructor.
     *
     * @param schemaVersion The current schema version expected by the
     *            application.
     * @param databaseTaskFactory The database task factory.
     */
    protected ResetSchemaTask(String schemaVersion, DatabaseTaskFactory databaseTaskFactory)
    {
        super(databaseTaskFactory);
        Utilities.checkNull(schemaVersion, "schemaVersion");
        mySchemaVersion = schemaVersion;
    }

    @Override
    public String getTimingMessage()
    {
        return "Time to reset schema: ";
    }

    @Override
    public Void run(Connection conn, Statement stmt) throws CacheException
    {
        getDatabaseState().clearCreated();

        getCacheUtilities().execute(getSQLGenerator().generateDropAllObjects(), stmt);

        getCacheUtilities().execute(getSQLGenerator().generateCreateTable(TableNames.SCHEMA_VERSION,
                Collections.singletonMap(ColumnNames.VERSION, getTypeMapper().getSqlColumnDefinition(String.class, false)),
                new PrimaryKeyConstraint(ColumnNames.VERSION)), stmt);

        final String sql = getSQLGenerator().generateInsert(TableNames.SCHEMA_VERSION, ColumnNames.VERSION);
        new StatementAppropriator(conn).appropriateStatement((PreparedStatementUser<Void>)(unused, pstmt) ->
        {
            try
            {
                pstmt.setString(1, mySchemaVersion);
                getCacheUtilities().executeUpdate(pstmt, sql);
            }
            catch (SQLException e)
            {
                throw new CacheException("Failed to update schema version: " + e, e);
            }
            return null;
        }, sql);
        return null;
    }
}
