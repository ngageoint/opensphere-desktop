package io.opensphere.core.cache;

/**
 * Exception that indicates the database is already in use by another process.
 */
public class DatabaseAlreadyOpenException extends CacheException
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Constructor. */
    public DatabaseAlreadyOpenException()
    {
        super("Database is already in use by another process.");
    }
}
