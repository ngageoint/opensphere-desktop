package io.opensphere.server.util;

/**
 * Exception for server errors.
 */
public class OGCServerException extends Exception
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param message The message.
     * @param cause The wrapped exception.
     */
    public OGCServerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
