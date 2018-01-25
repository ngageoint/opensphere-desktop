package io.opensphere.core.common.filter.expression;

/**
 * Signals that an {@link Expression} has some sort of error.
 */
public class ExpressionException extends RuntimeException
{
    /**
     * The default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>ExpressionException</code> with <code>null</code> as
     * its error detail message.
     */
    public ExpressionException()
    {
    }

    /**
     * Constructs an <code>ExpressionException</code> with the specified detail
     * message.
     *
     * @param message the detail message.
     */
    public ExpressionException(String message)
    {
        super(message);
    }

    /**
     * Constructs an <code>ExpressionException</code> with the specified detail
     * cause and a detail message of
     * <code>(cause == null ? null : cause.toString())</code>. This constructor
     * is useful for exceptions that are little more than wrappers for other
     * throwables.
     *
     * @param cause the cause.
     */
    public ExpressionException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructs an <code>ExpressionException</code> with the specified detail
     * message and cause.
     * <p>
     * Note that the detail message associated with cause is not automatically
     * incorporated into this exception's detail message.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public ExpressionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
