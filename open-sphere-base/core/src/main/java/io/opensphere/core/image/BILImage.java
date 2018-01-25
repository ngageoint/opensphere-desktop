package io.opensphere.core.image;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.opensphere.core.util.BufferUtilities;

/**
 * This class is for when an image is returned, but it is in fact BIL data and
 * not an image.
 */
public class BILImage extends Image
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The buffered data. */
    private byte[] myData;

    /** Height of the image. */
    private int myHeight;

    /** Width of the image. */
    private int myWidth;

    /**
     * Constructor for use by {@link ImageFactory}.
     */
    protected BILImage()
    {
    }

    @Override
    public void dispose()
    {
        myData = null;
    }

    @Override
    public ByteBuffer getByteBuffer()
    {
        return ByteBuffer.wrap(getData()).asReadOnlyBuffer();
    }

    @Override
    public ByteBuffer getByteBuffer(Rectangle region)
    {
        byte[] data = new byte[region.width * region.height];
        for (int i = 0; i < region.height; ++i)
        {
            int srcStart = (i + region.y) * myWidth + region.x;
            int destStart = i * region.width;
            System.arraycopy(getData(), srcStart, data, destStart, region.width);
        }

        return ByteBuffer.wrap(data);
    }

    @Override
    public int getHeight()
    {
        return myHeight;
    }

    @Override
    public long getSizeInBytes()
    {
        return getData().length;
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

    /**
     * Set the height.
     *
     * @param height the height to set
     */
    @Override
    public void setHeight(int height)
    {
        myHeight = height;
    }

    /**
     * Set the width.
     *
     * @param width the width to set
     */
    @Override
    public void setWidth(int width)
    {
        myWidth = width;
    }

    @Override
    protected void setByteBuffer(ByteBuffer buffer, boolean usePool) throws IOException
    {
        myData = BufferUtilities.toByteArray(buffer);
    }

    /**
     * Get the data array.
     *
     * @return The data array.
     * @throws IllegalStateException If the image has been disposed.
     */
    private byte[] getData() throws IllegalStateException
    {
        byte[] data = myData;
        if (data == null)
        {
            throw new IllegalStateException("Image has been disposed.");
        }
        return data;
    }
}
