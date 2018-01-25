package io.opensphere.csvcommon.parse;

/**
 * Exception indicating an error during CSV parsing.
 */
public class CSVParseException extends Exception
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param message The message.
     * @param cause The cause.
     */
    public CSVParseException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
