package io.opensphere.mantle.data.cache;

/**
 * The Class DataElementLookupException.
 */
public class CacheQueryException extends RuntimeException
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new cache query exception.
     */
    public CacheQueryException()
    {
        super();
    }

    /**
     * Instantiates a new cache query exception.
     *
     * @param message the message
     */
    public CacheQueryException(String message)
    {
        super(message);
    }

    /**
     * Instantiates a new cache query exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public CacheQueryException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Instantiates a new cache query exception.
     *
     * @param cause the cause
     */
    public CacheQueryException(Throwable cause)
    {
        super(cause);
    }
}
