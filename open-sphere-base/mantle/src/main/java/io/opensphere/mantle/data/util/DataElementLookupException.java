package io.opensphere.mantle.data.util;

/**
 * The Class DataElementLookupException.
 */
public class DataElementLookupException extends Exception
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new data element lookup exception.
     */
    public DataElementLookupException()
    {
        super();
    }

    /**
     * Instantiates a new data element lookup exception.
     *
     * @param message the message
     */
    public DataElementLookupException(String message)
    {
        super(message);
    }

    /**
     * Instantiates a new data element lookup exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public DataElementLookupException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Instantiates a new data element lookup exception.
     *
     * @param cause the cause
     */
    public DataElementLookupException(Throwable cause)
    {
        super(cause);
    }
}
