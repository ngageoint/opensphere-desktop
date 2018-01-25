package io.opensphere.mantle.util.compiler;

/**
 * The Class DynamicCompilerUnavailableException.
 */
public class DynamicCompilerUnavailableException extends Exception
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new dynamic compiler unavailable exception.
     *
     * @param msg the msg
     */
    public DynamicCompilerUnavailableException(String msg)
    {
        super(msg);
    }

    /**
     * Instantiates a new dynamic compiler unavailable exception.
     *
     * @param msg the msg
     * @param cause the cause
     */
    public DynamicCompilerUnavailableException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    /**
     * Instantiates a new dynamic compiler unavailable exception.
     *
     * @param cause the cause
     */
    public DynamicCompilerUnavailableException(Throwable cause)
    {
        super(cause);
    }
}
