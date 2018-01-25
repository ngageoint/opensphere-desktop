package io.opensphere.core.util.security;

import java.security.GeneralSecurityException;

/**
 * Exception that indicates an error decrypting data.
 */
public class DecryptionException extends GeneralSecurityException
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param message The exception message.
     */
    public DecryptionException(String message)
    {
        super(message);
    }

    /**
     * Construct the exception.
     *
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public DecryptionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
