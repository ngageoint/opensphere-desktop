package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.StatementAppropriator.PreparedStatementUser;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;

/**
 * This is responsible for obtaining database connections and ensuring that they
 * are closed once they are no longer needed.
 */
public class ConnectionAppropriator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ConnectionAppropriator.class);

    /** The source of the database connections. */
    private final ConnectionSource myConnectionSource;

    /**
     * Constructor.
     *
     * @param connectionSource The source of the database connections.
     */
    public ConnectionAppropriator(ConnectionSource connectionSource)
    {
        myConnectionSource = connectionSource;
    }

    /**
     * Obtain a database connection and call the specified connection user with
     * the connection. Close the database connection upon completion.
     *
     * @param <T> The type of object returned by the connection user.
     * @param user The connection user.
     * @param transaction If <code>true</code>, commands on this connection will
     *            be done as a single transaction.
     * @return The result of the call.
     * @throws CacheException If there is a database error.
     */
    public <T> T appropriateConnection(ConnectionUser<T> user, boolean transaction) throws CacheException
    {
        Connection conn = myConnectionSource.getConnection();
        try
        {
            if (transaction)
            {
                return runUserInTransaction(user, conn);
            }

            return runUserNoTransaction(user, conn);
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch (SQLException e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Failed to close connection: " + e, e);
                }
            }
        }
    }

    /**
     * Obtain a database connection and prepare a statement. Then call the
     * specified statement user with the connection and statement. Close the
     * statement and database connection upon completion.
     *
     * @param <T> The type of object returned by the statement user.
     * @param user The statement user.
     * @param transaction If <code>true</code>, commands on this connection will
     *            be done as a single transaction.
     * @param sql The SQL to use when preparing the statement.
     * @param columnNames The optional names of the columns to be
     *            <b>returned</b> from an <i>insert</i> statement.
     * @return The result of the call. Retrieve the returned values using
     *         {@link PreparedStatement#getGeneratedKeys()}.
     * @throws CacheException If there is a database error.
     */
    public <T> T appropriateStatement(final PreparedStatementUser<T> user, boolean transaction, final String sql,
            final String... columnNames) throws CacheException
    {
        return appropriateConnection(conn -> new StatementAppropriator(conn).appropriateStatement(user, sql, columnNames),
                transaction);
    }

    /**
     * Obtain a database connection and prepare a statement. Then call the
     * specified statement user with the connection and statement. Close the
     * statement and database connection upon completion.
     * <p>
     * This version does not not create a transaction.
     *
     * @param <T> The type of object returned by the statement user.
     * @param user The statement user.
     * @param sql The SQL to use when preparing the statement.
     * @param columnNames The optional names of the columns to be
     *            <b>returned</b> from an <i>insert</i> statement.
     * @return The result of the call. Retrieve the returned values using
     *         {@link PreparedStatement#getGeneratedKeys()}.
     * @throws CacheException If there is a database error.
     */
    public <T> T appropriateStatement(final PreparedStatementUser<T> user, final String sql, final String... columnNames)
            throws CacheException
    {
        return appropriateStatement(user, false, sql, columnNames);
    }

    /**
     * Obtain a database connection and create a statement. Then call the
     * specified statement user with the connection and statement. Close the
     * statement and database connection upon completion.
     * <p>
     * This version does not not create a transaction.
     *
     * @param <T> The type of object returned by the statement user.
     * @param user The statement user.
     * @return The result of the call.
     * @throws CacheException If there is a database error.
     */
    public <T> T appropriateStatement(final StatementUser<T> user) throws CacheException
    {
        return appropriateStatement(user, false);
    }

    /**
     * Obtain a database connection and create a statement. Then call the
     * specified statement user with the connection and statement. Close the
     * statement and database connection upon completion.
     *
     * @param <T> The type of object returned by the statement user.
     * @param user The statement user.
     * @param transaction If <code>true</code>, commands on this connection will
     *            be done as a single transaction.
     * @return The result of the call.
     * @throws CacheException If there is a database error.
     */
    public <T> T appropriateStatement(final StatementUser<T> user, boolean transaction) throws CacheException
    {
        return appropriateConnection(conn -> new StatementAppropriator(conn).appropriateStatement(user), transaction);
    }

    /**
     * Accessor for the connectionSource.
     *
     * @return The connectionSource.
     */
    protected ConnectionSource getConnectionSource()
    {
        return myConnectionSource;
    }

    /**
     * Run a connection user in a transaction. Roll back the transaction if
     * there are any exceptions.
     *
     * @param <T> The type of the connection user.
     * @param user The connection user.
     * @param conn The connection.
     * @return The return value of the connection user.
     * @throws CacheException If there is a database error.
     */
    protected <T> T runUserInTransaction(ConnectionUser<T> user, Connection conn) throws CacheException
    {
        try
        {
            try
            {
                conn.setAutoCommit(false);
                T result = user.run(conn);
                conn.commit();
                return result;
            }
            catch (CacheException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Rolling back transaction.");
                }
                conn.rollback();
                throw e;
            }
            catch (RuntimeException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Rolling back transaction.");
                }
                conn.rollback();
                throw e;
            }
            finally
            {
                conn.setAutoCommit(true);
            }
        }
        catch (SQLException e)
        {
            throw new CacheException(e);
        }
    }

    /**
     * Run a connection user without a transaction.
     *
     * @param <T> The type of the connection user.
     * @param user The connection user.
     * @param conn The connection.
     * @return The return value of the connection user.
     * @throws CacheException If there is a database error.
     */
    protected <T> T runUserNoTransaction(ConnectionUser<T> user, Connection conn) throws CacheException
    {
        return user.run(conn);
    }

    /**
     * Interface for functors that will use database connections created by this
     * appropriator.
     *
     * @param <T> The type of object returned by the connection user.
     */
    @FunctionalInterface
    public interface ConnectionUser<T>
    {
        /**
         * Method called with the connection. The connection is closed once the
         * method completes.
         *
         * @param conn The database connection.
         * @return The result of the method.
         * @throws CacheException If there is a problem executing the method.
         */
        T run(Connection conn) throws CacheException;
    }
}
