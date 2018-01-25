package io.opensphere.core.util.security;

import java.security.GeneralSecurityException;

/**
 * Exception indicating an error with a cipher.
 */
public class CipherException extends GeneralSecurityException
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public CipherException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
