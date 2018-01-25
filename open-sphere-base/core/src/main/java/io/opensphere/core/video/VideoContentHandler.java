package io.opensphere.core.video;

import io.opensphere.core.util.Service;

/**
 * Content handler for video.
 *
 * @param <T> The type of content handled.
 */
public interface VideoContentHandler<T> extends Service
{
    /**
     * Handle a packet of data.
     *
     * @param content The data.
     * @param ptsMS The time since stream start at which the packet should be
     *            presented in milliseconds.
     */
    void handleContent(T content, long ptsMS);
}
