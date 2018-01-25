package io.opensphere.core.image;

/**
 * A service that provides images, potentially immediately.
 *
 * @param <T> The type of object to be used to look up the images.
 */
public interface ImmediateImageProvider<T> extends ImageProvider<T>
{
    /**
     * Determine if an image is available immediately.
     *
     * @return true when the image is already available.
     */
    boolean canProvideImageImmediately();
}
