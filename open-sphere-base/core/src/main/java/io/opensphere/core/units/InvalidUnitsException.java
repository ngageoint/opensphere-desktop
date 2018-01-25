package io.opensphere.core.units;

/**
 * Exception indicating invalid units were encountered.
 */
public class InvalidUnitsException extends RuntimeException
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param type The invalid type.
     */
    public InvalidUnitsException(Class<?> type)
    {
        super("Invalid units [" + type + "] encountered.");
    }

    /**
     * Constructor.
     *
     * @param type The invalid type.
     * @param e The cause.
     */
    public InvalidUnitsException(Class<?> type, Exception e)
    {
        super("Invalid units [" + type + "] encountered: " + e, e);
    }
}
