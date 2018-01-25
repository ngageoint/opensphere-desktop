package io.opensphere.core.util.lang;

/**
 * Runtime exception that indicates an unintended branch of code has been
 * reached.
 */
public class ImpossibleException extends RuntimeException
{
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param throwable The causing {@link Throwable}.
     */
    public ImpossibleException(Throwable throwable)
    {
        super(throwable);
    }
}
