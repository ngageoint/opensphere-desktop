package io.opensphere.core.util.security;

/** Exception indicating a problem with the private key provider. */
public class PrivateKeyProviderRuntimeException extends RuntimeSecurityException
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param cause The cause of the exception.
     */
    public PrivateKeyProviderRuntimeException(PrivateKeyProviderException cause)
    {
        super(cause.getMessage(), cause);
    }
}
