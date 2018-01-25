package io.opensphere.auxiliary.cache.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.ConnectionAppropriator;
import io.opensphere.core.cache.jdbc.ConnectionSource;
import io.opensphere.core.cache.jdbc.StatementAppropriator;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;

/**
 * A {@link ConnectionAppropriator} with H2 extensions.
 */
public class H2ConnectionAppropriator extends ConnectionAppropriator
{
    /**
     * Constructor.
     *
     * @param connectionSource The connection source.
     */
    public H2ConnectionAppropriator(ConnectionSource connectionSource)
    {
        super(connectionSource);
    }

    @Override
    public <T> T appropriateConnection(ConnectionUser<T> user, boolean transaction) throws CacheException
    {
        return super.appropriateConnection(user, transaction);
    }

    @Override
    protected <T> T runUserInTransaction(ConnectionUser<T> user, Connection conn) throws CacheException
    {
        try
        {
            setUndoLog(true, conn);
            return super.runUserInTransaction(user, conn);
        }
        finally
        {
            setUndoLog(false, conn);
        }
    }

    /**
     * Enable or disable the transaction log.
     *
     * @param b {@code true} if the log should be enabled.
     * @param conn The database connection.
     * @throws CacheException If there is a database error.
     */
    private void setUndoLog(final boolean b, Connection conn) throws CacheException
    {
        new StatementAppropriator(conn).appropriateStatement(new StatementUser<Void>()
        {
            @Override
            public Void run(Connection unused, Statement stmt) throws CacheException
            {
                try
                {
                    stmt.execute("set undo_log " + (b ? 1 : 0));
                }
                catch (SQLException e)
                {
                    throw new CacheException("Failed to set undo log: " + e, e);
                }
                return null;
            }
        });
    }
}
