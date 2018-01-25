package io.opensphere.mantle.util.proxypool;

/**
 * The Class MaxPoolSizeExceededException.
 */
public class MaxPoolSizeExceededException extends Exception
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 0L;

    /**
     * Instantiates a new max pool size exceeded exception.
     */
    public MaxPoolSizeExceededException()
    {
        super();
    }

    /**
     * Instantiates a new max pool size exceeded exception.
     *
     * @param message the message
     */
    public MaxPoolSizeExceededException(String message)
    {
        super(message);
    }

    /**
     * Instantiates a new max pool size exceeded exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public MaxPoolSizeExceededException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Instantiates a new max pool size exceeded exception.
     *
     * @param cause the cause
     */
    public MaxPoolSizeExceededException(Throwable cause)
    {
        super(cause);
    }
}
