package io.opensphere.core.image;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;

/**
 * An image which provides a stream which is backed by an image.
 *
 * @param <T> The type of the key to be used to look up the image.
 */
public class StreamingImage<T> extends Image
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StreamingImage.class);

    /** The height of the image. */
    private final int myHeight;

    /** The provider for the image's data stream. */
    private final transient ImageStreamProvider<T> myImageStreamProvider;

    /** The image key for my associated image data. */
    private final T myKey;

    /** The width of the image. */
    private final int myWidth;

    /**
     * Constructor.
     *
     * @param streamProvider The provider of an image through a stream.
     * @param key The image key for my associated image data.
     * @param width The width of the image.
     * @param height The height of the image.
     */
    public StreamingImage(ImageStreamProvider<T> streamProvider, T key, int width, int height)
    {
        Utilities.checkNull(streamProvider, "key");
        Utilities.checkNull(key, "streamProvider");
        myKey = key;
        myImageStreamProvider = streamProvider;
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
        ByteBuffer buffer = null;
        try
        {
            buffer = ByteBuffer.wrap(IOUtils.toByteArray(getInputStream()));
        }
        catch (IOException e)
        {
            LOGGER.error(e, e);
        }
        return buffer;
    }

    @Override
    public ByteBuffer getByteBuffer(Rectangle region)
    {
        return null;
    }

    @Override
    public int getHeight()
    {
        return myHeight;
    }

    /**
     * Get the inputStream. The consumer is responsible for closing this stream.
     *
     * @return the inputStream
     */
    public InputStream getInputStream()
    {
        return myImageStreamProvider.getImageStream(myKey);
    }

    @Override
    public long getSizeInBytes()
    {
        return 0L;
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
        throw new UnsupportedOperationException("Streaming images cannot contain their own buffer");
    }
}
