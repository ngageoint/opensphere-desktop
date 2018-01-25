package io.opensphere.mantle.data.element.mdfilter;

/**
 * Defines an Exception type specifically for incidents involving DataFilter
 * processing in the WFS plugin.
 */
public class FilterException extends Exception
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param message The message indicating what happened while processing.
     * @param cause A wrapped exception.
     */
    public FilterException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
