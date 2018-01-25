package io.opensphere.core.video;

/**
 * An error during video encoding.
 */
public class VideoEncoderException extends Exception
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param message The message.
     * @param cause The cause.
     */
    public VideoEncoderException(String message, Exception cause)
    {
        super(message);
    }
}
