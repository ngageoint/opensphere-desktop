package io.opensphere.core.util.security;

/** Exception indicating a runtime error in a security component. */
public class RuntimeSecurityException extends RuntimeException
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public RuntimeSecurityException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Construct the exception.
     *
     * @param cause The cause of the exception.
     */
    public RuntimeSecurityException(Throwable cause)
    {
        super(cause.getMessage(), cause);
    }
}
