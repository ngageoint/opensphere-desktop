package io.opensphere.core.util.lang;

/**
 * Runtime exception thrown when an enum has an unexpected value. This indicates
 * that a value was added to the enum, but code using the enum has not been
 * updated to support it.
 */
public class UnexpectedEnumException extends RuntimeException
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param value The unexpected value of the enum.
     */
    public UnexpectedEnumException(Enum<?> value)
    {
        super("Encountered enum type [" + value.getClass().getSimpleName() + "] with unexpected value [" + value + "]");
    }
}
