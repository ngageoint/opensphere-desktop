package io.opensphere.core.cache;

import java.sql.SQLException;

/**
 * Exception indicating a problem with the {@link Cache}.
 */
public class CacheException extends Exception
{
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor with a message but no cause.
     *
     * @param message The exception message.
     */
    public CacheException(String message)
    {
        super(message);
    }

    /**
     * Constructor that takes an sql string, a db description, and a cause.
     *
     * @param sql The sql string that caused the problem.
     * @param dbString The db description.
     * @param e The cause.
     */
    public CacheException(String sql, String dbString, SQLException e)
    {
        this("Failed to execute query for sql [" + sql + "] on db " + dbString + ": " + e, e);
    }

    /**
     * Constructor with a message and nested cause.
     *
     * @param message The exception message.
     * @param cause The nested cause.
     */
    public CacheException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor with a cause but no message.
     *
     * @param cause The cause.
     */
    public CacheException(Throwable cause)
    {
        super(cause);
    }
}
