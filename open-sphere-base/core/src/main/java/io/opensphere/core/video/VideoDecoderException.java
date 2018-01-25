package io.opensphere.core.video;

/**
 * An error during video decoding.
 */
public class VideoDecoderException extends Exception
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param message The message.
     */
    public VideoDecoderException(String message)
    {
        super(message);
    }
}
