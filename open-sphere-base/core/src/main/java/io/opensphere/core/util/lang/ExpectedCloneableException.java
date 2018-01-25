package io.opensphere.core.util.lang;

/**
 * Runtime exception indicating that {@link CloneNotSupportedException} was
 * thrown when cloning a class that was expected to be cloneable.
 */
public class ExpectedCloneableException extends RuntimeException
{
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param cause The causing {@link CloneNotSupportedException}.
     */
    public ExpectedCloneableException(CloneNotSupportedException cause)
    {
        super(cause);
    }
}
