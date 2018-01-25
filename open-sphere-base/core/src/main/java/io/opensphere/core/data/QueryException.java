package io.opensphere.core.data;

/**
 * Exception indicating an error during a data provider query.
 */
public class QueryException extends Exception
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = -8021886993874016202L;

    /**
     * Creates a new exception.
     */
    public QueryException()
    {
        super();
    }

    /**
     * Creates a new exception, populated with the supplied root cause.
     *
     * @param pMessage a textual description of the failure.
     * @param pCause the root cause of the exception.
     */
    public QueryException(String pMessage, Throwable pCause)
    {
        super(pMessage, pCause);
    }

    /**
     * Creates a new exception, populated with the supplied message.
     *
     * @param pMessage a textual description of the failure.
     */
    public QueryException(String pMessage)
    {
        super(pMessage);
    }

    /**
     * Creates a new exception, populated with the supplied root cause.
     *
     * @param pCause the root cause of the exception.
     */
    public QueryException(Throwable pCause)
    {
        super(pCause);
    }
}
