package io.opensphere.mantle.util.dynenum;

/**
 * The Class KeyIdExhaustionException.
 */
@SuppressWarnings("serial")
public class KeyIdExhaustionException extends RuntimeException
{
    /**
     * Instantiates a new key id exhaustion exception.
     */
    public KeyIdExhaustionException()
    {
        super();
    }

    /**
     * Instantiates a new key id exhaustion exception.
     *
     * @param message the message
     */
    public KeyIdExhaustionException(String message)
    {
        super(message);
    }

    /**
     * Instantiates a new key id exhaustion exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public KeyIdExhaustionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Instantiates a new key id exhaustion exception.
     *
     * @param cause the cause
     */
    public KeyIdExhaustionException(Throwable cause)
    {
        super(cause);
    }
}
