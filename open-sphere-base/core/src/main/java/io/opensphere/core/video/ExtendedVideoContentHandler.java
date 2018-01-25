package io.opensphere.core.video;

/**
 * Extended content handler for video.
 *
 * @param <T> The type of content handled.
 */
public interface ExtendedVideoContentHandler<T> extends VideoContentHandler<T>
{
    /**
     * Handle a packet of data.
     *
     * @param content The data.
     * @param packetPosition The packet position.
     */
    void handleContentWithPosition(T content, long packetPosition);
}
