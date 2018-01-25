package io.opensphere.core.util.security;

import java.security.GeneralSecurityException;

/**
 * Exception indicating an error with a secret key provider.
 */
public class SecretKeyProviderException extends GeneralSecurityException
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param message The detail message.
     */
    public SecretKeyProviderException(String message)
    {
        super(message);
    }

    /**
     * Construct the exception.
     *
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public SecretKeyProviderException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
