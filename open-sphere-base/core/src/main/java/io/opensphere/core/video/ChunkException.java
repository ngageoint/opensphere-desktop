package io.opensphere.core.video;

/** An error occurred while chunking video. */
public class ChunkException extends Exception
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param message The message.
     */
    public ChunkException(String message)
    {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message The message.
     * @param cause The cause.
     */
    public ChunkException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
