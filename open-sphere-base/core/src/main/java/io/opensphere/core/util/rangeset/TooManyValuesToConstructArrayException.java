package io.opensphere.core.util.rangeset;

/**
 * Exception that is thrown if the number of elements trying to be used to
 * create an array or collection exceeds {@link Integer}.MAX_VALUE.
 */
public class TooManyValuesToConstructArrayException extends RuntimeException
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new too many values to construct array exception.
     */
    public TooManyValuesToConstructArrayException()
    {
        super();
    }

    /**
     * Instantiates a new too many values to construct array exception.
     *
     * @param msg the msg
     */
    public TooManyValuesToConstructArrayException(String msg)
    {
        super(msg);
    }

    /**
     * Instantiates a new too many values to construct array exception.
     *
     * @param msg the msg
     * @param cause the cause
     */
    public TooManyValuesToConstructArrayException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    /**
     * Instantiates a new too many values to construct array exception.
     *
     * @param cause the cause
     */
    public TooManyValuesToConstructArrayException(Throwable cause)
    {
        super(cause);
    }
}
