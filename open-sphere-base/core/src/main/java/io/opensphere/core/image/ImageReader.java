package io.opensphere.core.image;

import java.nio.ByteBuffer;

/**
 * Interface to an object that knows how to read images of a certain type.
 */
public interface ImageReader
{
    /**
     * Gets the image format this reader knows how to read.
     *
     * @return The image format.
     */
    String getImageFormat();

    /**
     * Reads the image from the specified bytes.
     *
     * @param imageBytes The image in bytes encoded in the format this reader
     *            knows how to read.
     * @return The read image.
     */
    Image readImage(ByteBuffer imageBytes);
}
