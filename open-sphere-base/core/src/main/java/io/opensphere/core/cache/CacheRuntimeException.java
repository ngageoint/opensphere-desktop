package io.opensphere.core.cache;

/**
 * RuntimeException indicating a problem with the {@link Cache}.
 */
public class CacheRuntimeException extends RuntimeException
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
    public CacheRuntimeException(String message)
    {
        super(message);
    }

    /**
     * Constructor with a message and nested cause.
     *
     * @param message The exception message.
     * @param cause The nested cause.
     */
    public CacheRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor with a cause but no message.
     *
     * @param cause The cause.
     */
    public CacheRuntimeException(Throwable cause)
    {
        super(cause);
    }
}
