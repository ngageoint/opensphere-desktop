package io.opensphere.core.units;

/**
 * Exception generated when a string cannot be parsed into units.
 */
public class UnitsParseException extends RuntimeException
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param msg The detail message.
     */
    public UnitsParseException(String msg)
    {
        super(msg);
    }

    /**
     * Constructor.
     *
     * @param msg The detail message.
     * @param cause The cause.
     */
    public UnitsParseException(String msg, Exception cause)
    {
        super(msg, cause);
    }
}
