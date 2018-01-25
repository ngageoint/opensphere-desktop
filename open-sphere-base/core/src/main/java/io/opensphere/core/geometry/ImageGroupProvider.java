package io.opensphere.core.geometry;

import io.opensphere.core.image.ImageProvider;

/**
 * A service that provides image groups.
 *
 * @param <S> The type of object to be used to look up the image groups.
 */
public interface ImageGroupProvider<S> extends ImageProvider<S>
{
    /**
     * Retrieve an image group.
     *
     * @param key The key for the image group.
     * @return The images, or <code>null</code> if it cannot be retrieved.
     */
    ImageGroup getImages(S key);
}
