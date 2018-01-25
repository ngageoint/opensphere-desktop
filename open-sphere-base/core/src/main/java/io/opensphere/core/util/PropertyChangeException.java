package io.opensphere.core.util;

/** Indicates a problem making a property change. */
public class PropertyChangeException extends Exception
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param message The message.
     */
    public PropertyChangeException(String message)
    {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param cause The cause.
     */
    public PropertyChangeException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message The message.
     * @param cause The cause.
     */
    public PropertyChangeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
