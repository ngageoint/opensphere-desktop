package io.opensphere.core.image;

import java.io.InputStream;

/**
 * A service that provides stream which is backed by an image.
 *
 * @param <T> The type of the key to be used to look up the images.
 */
@FunctionalInterface
public interface ImageStreamProvider<T>
{
    /**
     * Retrieve an image stream. The consumer is responsible for closing this
     * stream.
     *
     * @param key The key for the image.
     * @return The image stream, or <code>null</code> if it cannot be retrieved.
     */
    InputStream getImageStream(T key);
}
