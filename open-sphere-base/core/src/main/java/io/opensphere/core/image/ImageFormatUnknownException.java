package io.opensphere.core.image;

/** Exception indicating an unknown image stream format. */
public class ImageFormatUnknownException extends Exception
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the exception.
     *
     * @param message The error message.
     */
    public ImageFormatUnknownException(String message)
    {
        super(message);
    }
}
