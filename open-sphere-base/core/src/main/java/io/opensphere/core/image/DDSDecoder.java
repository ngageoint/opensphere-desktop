package io.opensphere.core.image;

import java.nio.ByteBuffer;

/**
 * Interface for a service that can decode an image from a Direct Draw Surface.
 */
@FunctionalInterface
public interface DDSDecoder
{
    /**
     * Decode a DDS image to RGB/A.
     *
     * @param input The input data.
     * @param compression DDS compression.
     * @param width The width of the image in pixels.
     * @param height The height of the image in pixels.
     * @return The output image data.
     */
    ByteBuffer decode(ByteBuffer input, Image.CompressionType compression, int width, int height);
}
