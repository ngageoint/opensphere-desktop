package io.opensphere.core.pipeline.util;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.opensphere.core.image.Image;

/**
 * Image data for an image which is already loaded to the card.
 */
public class PreloadedTextureImage extends Image
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Pixel height of the texture. */
    private final int myHeight;

    /** Key for Texture which is already loaded to the card. */
    private final Object myTextureHandleKey;

    /** Pixel width of the texture. */
    private final int myWidth;

    /**
     * Construct a PreloadedTextureImage.
     *
     * @param textureHandleKey Cache key for the texture handle.
     * @param width the pixel width of the texture.
     * @param height the pixel height of the texture.
     */
    public PreloadedTextureImage(Object textureHandleKey, int width, int height)
    {
        myTextureHandleKey = textureHandleKey;
        myWidth = width;
        myHeight = height;
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public ByteBuffer getByteBuffer()
    {
        throw new UnsupportedOperationException("Cannot get raw image data for a pre-loaded texture.");
    }

    @Override
    public ByteBuffer getByteBuffer(Rectangle region)
    {
        throw new UnsupportedOperationException("Cannot get raw image data for a pre-loaded texture.");
    }

    @Override
    public int getHeight()
    {
        return myHeight;
    }

    @Override
    public long getSizeInBytes()
    {
        // This image takes up no space in the cache since it is already loaded
        // to the card.
        return 0;
    }

    /**
     * Get the handle to the texture key.
     *
     * @return The handle to the texture key.
     */
    public Object getTextureHandleKey()
    {
        return myTextureHandleKey;
    }

    @Override
    public int getWidth()
    {
        return myWidth;
    }

    @Override
    public boolean isBlank()
    {
        return false;
    }

    @Override
    protected void setByteBuffer(ByteBuffer data, boolean usePool) throws IOException
    {
        throw new UnsupportedOperationException("Cannot set raw image data for a pre-loaded texture.");
    }
}
