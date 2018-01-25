package io.opensphere.core.util.security;

import java.security.GeneralSecurityException;

/** Exception indicating a problem with the private key provider. */
public class PrivateKeyProviderException extends GeneralSecurityException
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param message The exception message.
     */
    public PrivateKeyProviderException(String message)
    {
        super(message);
    }

    /**
     * Construct the exception.
     *
     * @param message The exception message.
     * @param cause The cause of the exception.
     */
    public PrivateKeyProviderException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
