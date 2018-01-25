package io.opensphere.core.util.image;

/** A runtime exception that indicates a problem with loading an icon. */
public class IconRuntimeException extends RuntimeException
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param message The message.
     * @param cause The cause.
     */
    public IconRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
