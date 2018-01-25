package io.opensphere.core.terrain.util;

/** Exception indicating an error during elevation reading. */
public class ElevationImageReaderException extends Exception
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Construct the exception. */
    public ElevationImageReaderException()
    {
    }

    /**
     * Construct the exception.
     *
     * @param message The error message.
     */
    public ElevationImageReaderException(String message)
    {
        super(message);
    }

    /**
     * Construct the exception.
     *
     * @param message The error message.
     * @param cause The wrapped exception.
     */
    public ElevationImageReaderException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Construct the exception.
     *
     * @param cause The wrapped exception.
     */
    public ElevationImageReaderException(Throwable cause)
    {
        super(cause);
    }
}
