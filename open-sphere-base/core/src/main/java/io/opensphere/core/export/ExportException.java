package io.opensphere.core.export;

/**
 * Exception indicating an error during export.
 */
public class ExportException extends Exception
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param message The detail message.
     */
    public ExportException(String message)
    {
        super(message);
    }

    /**
     * Construct the exception.
     *
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public ExportException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
