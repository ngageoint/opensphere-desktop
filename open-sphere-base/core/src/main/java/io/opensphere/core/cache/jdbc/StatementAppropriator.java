package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.util.Utilities;

/**
 * This is responsible for creating database statements and ensuring that they
 * are closed once they are no longer needed.
 */
public class StatementAppropriator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StatementAppropriator.class);

    /**
     * A database connection to use.
     */
    private final Connection myConnection;

    /**
     * Construct a statement appropriator that will use a single database
     * connection. The connection will not be closed.
     *
     * @param conn The database connection.
     */
    public StatementAppropriator(Connection conn)
    {
        Utilities.checkNull(conn, "conn");
        myConnection = conn;
    }

    /**
     * Construct a statement appropriator that will use the database connection
     * associated with the input statement. The connection will not be closed.
     *
     * @param stmt The statement that will provide the database connection.
     */
    public StatementAppropriator(Statement stmt)
    {
        this(CacheUtilities.getConnectionFromStatement(stmt));
    }

    /**
     * Create a prepared database statement and call the specified statement
     * user with the created statement. Close the database statement upon
     * completion.
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
        return runUser(myConnection, user, sql, columnNames);
    }

    /**
     * Create a database statement and call the specified statement user with
     * the created statement. Close the database statement upon completion.
     *
     * @param <T> The type of object returned by the statement user.
     * @param user The statement user.
     * @return The result of the call.
     * @throws CacheException If there is a database error.
     */
    public <T> T appropriateStatement(final StatementUser<T> user) throws CacheException
    {
        return runUser(myConnection, user);
    }

    /**
     * Create a statement, run the statement user, and close the statement.
     *
     * @param <T> The type of object returned by the statement user.
     * @param conn The database connection.
     * @param puser The statement user.
     * @param sql The SQL to use when preparing the statement.
     * @param columnNames The optional names of the columns to be
     *            <b>returned</b> from an <i>insert</i> statement.
     * @return The result of the call.
     * @throws CacheException If there is a database error.
     */
    private <T> T runUser(Connection conn, PreparedStatementUser<T> puser, String sql, String... columnNames)
            throws CacheException
    {
        PreparedStatement pstmt;
        try
        {
            pstmt = conn.prepareStatement(sql, columnNames);
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to create database statement: " + e, e);
        }
        try
        {
            return puser.run(conn, pstmt);
        }
        finally
        {
            try
            {
                pstmt.close();
            }
            catch (SQLException e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Failed to close result set: " + e, e);
                }
            }
        }
    }

    /**
     * Create a statement, run the statement user, and close the statement.
     *
     * @param <T> The type of object returned by the statement user.
     * @param conn The database connection.
     * @param user The statement user.
     * @return The result of the call.
     * @throws CacheException If there is a database error.
     */
    @SuppressWarnings("PMD.CloseResource")
    private <T> T runUser(Connection conn, StatementUser<T> user) throws CacheException
    {
        Statement stmt;
        try
        {
            stmt = conn.createStatement();
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to create database statement: " + e, e);
        }
        try
        {
            return user.run(conn, stmt);
        }
        finally
        {
            try
            {
                stmt.close();
            }
            catch (SQLException e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Failed to close statement: " + e, e);
                }
            }
        }
    }

    /**
     * Interface for functors that will use prepared database statements created
     * by this appropriator.
     *
     * @param <T> The type of object returned by the statement user.
     */
    @FunctionalInterface
    public interface PreparedStatementUser<T>
    {
        /**
         * Method called with the statement. The statement is closed once the
         * method completes.
         *
         * @param conn The database connection.
         * @param pstmt The database statement.
         * @return The result of the method.
         * @throws CacheException If there is a problem executing the method.
         */
        T run(Connection conn, PreparedStatement pstmt) throws CacheException;
    }

    /**
     * Interface for functors that will use database statements created by this
     * appropriator.
     *
     * @param <T> The type of object returned by the statement user.
     */
    @FunctionalInterface
    public interface StatementUser<T>
    {
        /**
         * Method called with the statement. The statement is closed once the
         * method completes.
         *
         * @param conn The database connection.
         * @param stmt The database statement.
         * @return The result of the method.
         * @throws CacheException If there is a problem executing the method.
         */
        T run(Connection conn, Statement stmt) throws CacheException;
    }
}
