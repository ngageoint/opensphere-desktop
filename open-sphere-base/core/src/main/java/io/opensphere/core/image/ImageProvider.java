package io.opensphere.core.image;

/**
 * A service that provides images.
 *
 * @param <T> The type of object to be used to look up the images.
 */
@FunctionalInterface
public interface ImageProvider<T>
{
    /**
     * Retrieve an image.
     *
     * @param key The key for the image.
     * @return The image, or <code>null</code> if it cannot be retrieved.
     */
    Image getImage(T key);
}
