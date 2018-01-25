package io.opensphere.core.video;

/**
 * Interface to an object that wants to know of errors within the
 * {@link FLVStreamTranscoder}.
 */
public interface VideoErrorHandler
{
    /**
     * Called when an unrecoverable error occurs.
     *
     * @param message The message.
     * @param e The exception.
     */
    void error(String message, Throwable e);
}
