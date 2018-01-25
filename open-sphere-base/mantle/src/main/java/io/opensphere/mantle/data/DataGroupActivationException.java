package io.opensphere.mantle.data;

/** Exception indicating a problem activating a data group. */
public class DataGroupActivationException extends Exception
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param message The message.
     * @param cause The cause.
     */
    public DataGroupActivationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param message The message.
     */
    public DataGroupActivationException(String message)
    {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param cause The cause.
     */
    public DataGroupActivationException(Throwable cause)
    {
        super(cause);
    }
}
