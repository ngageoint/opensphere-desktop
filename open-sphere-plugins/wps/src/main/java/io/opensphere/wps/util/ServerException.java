package io.opensphere.wps.util;

/**
 * Exception for server errors.
 */
public class ServerException extends Exception
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
    public ServerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
