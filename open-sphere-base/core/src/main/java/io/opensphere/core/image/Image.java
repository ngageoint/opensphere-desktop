package io.opensphere.core.image;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.Nulls;

/**
 * Base class for utilities that convert various image formats to DDS images.
 */
public abstract class Image implements Serializable, AutoCloseable
{
    /** Executor to use for transcoding images. */
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new NamedThreadFactory("Image"));

    /** Factory for images. */
    private static final ImageFactory IMAGE_FACTORY = new ImageFactory();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(Image.class);

    /** serialVersionUID. */
    private static final long serialVersionUID = 2L;

    /**
     * Encode an image as a DDS on a separate thread. Return an input stream
     * from which the DDS can be read.
     *
     * @param in The input stream.
     * @param imageFormat The input image format.
     * @param contentLength The content length for the image.
     * @param metrics Optional metrics object that will be populated with the
     *            appropriate metrics if provided.
     * @return The input stream from which a {@link DDSImage} can be
     *         deserialized.
     * @throws ImageFormatUnknownException If the image format is not
     *             recognized.
     * @throws IOException If there is an error reading from the input stream.
     */
    public static InputStream getDDSImageStream(InputStream in, String imageFormat, int contentLength, ImageMetrics metrics)
        throws ImageFormatUnknownException, IOException
    {
        return IMAGE_FACTORY.getDDSImageStream(in, imageFormat, contentLength, metrics, EXECUTOR);
    }

    /**
     * Read an image from a stream.
     *
     * @param stream The input stream.
     * @return The image, or {@code null} if the image type is not supported.
     * @throws IOException If there is an error reading from the stream.
     * @throws ImageFormatUnknownException If the image format is not
     *             recognized.
     */
    public static Image read(InputStream stream) throws IOException, ImageFormatUnknownException
    {
        return read(stream, false);
    }

    /**
     * Read an image from a stream.
     *
     * @param stream The input stream.
     * @param ddsDesired Indicates if a DDS image is preferred.
     * @return The image, or {@code null} if the image type is not supported.
     * @throws IOException If there is an error reading from the stream.
     * @throws ImageFormatUnknownException If the image format is not
     *             recognized.
     */
    public static Image read(InputStream stream, boolean ddsDesired) throws IOException, ImageFormatUnknownException
    {
        return read(stream, ddsDesired, (ImageMetrics)null);
    }

    /**
     * Read an image from a stream.
     *
     * @param stream The input stream.
     * @param ddsDesired Indicates if a DDS image is preferred.
     * @param metrics Optional metrics object that will be populated with the
     *            appropriate metrics if provided.
     * @return The image, or {@code null} if the image type is not supported.
     * @throws IOException If there is an error reading from the stream.
     * @throws ImageFormatUnknownException If the image format is not
     *             recognized.
     */
    public static Image read(InputStream stream, boolean ddsDesired, ImageMetrics metrics)
        throws IOException, ImageFormatUnknownException
    {
        return read(stream, -1, Nulls.STRING, ddsDesired, false, metrics);
    }

    /**
     * Read an image from a stream.
     *
     * @param stream The input stream.
     * @param contentLengthBytes The expected content length in bytes (-1 for
     *            unknown).
     * @param contentType The expected content type (may be {@code null}).
     * @param ddsDesired Indicates if a DDS image is preferred.
     * @param useBufferedImagePool Indicates if the buffered image pool is to be
     *            used. Either this or {@code ddsDesired} can be specified, but
     *            not both. If this is {@code true}, {@link Image#dispose()}
     *            must be called to return the buffered image to the pool.
     * @param metrics Optional metrics object that will be populated with the
     *            appropriate metrics if provided.
     * @return The image, or {@code null} if the image type is not supported.
     * @throws ImageFormatUnknownException If the image format is not
     *             recognized.
     */
    public static Image read(InputStream stream, int contentLengthBytes, String contentType, boolean ddsDesired,
            boolean useBufferedImagePool, ImageMetrics metrics)
        throws ImageFormatUnknownException
    {
        return IMAGE_FACTORY.createImage(stream, contentLengthBytes, contentType, ddsDesired, useBufferedImagePool, metrics);
    }

    /**
     * Read an image from a stream.
     *
     * @param stream The input stream.
     * @param contentLengthBytes The expected content length in bytes (-1 for
     *            unknown).
     * @param contentType The expected content type (may be {@code null}).
     * @param metrics Optional metrics object that will be populated with the
     *            appropriate metrics if provided.
     * @return The image, or {@code null} if the image type is not supported.
     * @throws ImageFormatUnknownException If the image format is not
     *             recognized.
     */
    public static Image read(InputStream stream, int contentLengthBytes, String contentType, ImageMetrics metrics)
        throws ImageFormatUnknownException
    {
        return read(stream, contentLengthBytes, contentType, false, false, metrics);
    }

    @Override
    public void close()
    {
        dispose();
    }

    /**
     * Dispose of this image. This will free the resources used, and the image
     * may no longer be used. Implementations must be idempotent.
     */
    public abstract void dispose();

    /**
     * Get the raw data for the image. This may include an image header,
     * depending on the type of image.
     *
     * @return The image data.
     */
    public abstract ByteBuffer getByteBuffer();

    /**
     * Get the raw data for the image within the given region. This will never
     * include an image header.
     *
     * @param region the region over which to return the image data.
     * @return The image data.
     */
    public abstract ByteBuffer getByteBuffer(Rectangle region);

    /**
     * Get the type of compression used for this image.
     *
     * @return The type of compression used for this image.
     */
    public CompressionType getCompressionType()
    {
        return CompressionType.UNDEFINED;
    }

    /**
     * Get the pixel height of the image.
     *
     * @return The pixel height of the image.
     */
    public abstract int getHeight();

    /**
     * Get the size of this image in bytes.
     *
     * @return The size of the image.
     */
    public abstract long getSizeInBytes();

    /**
     * Get the pixel width of the image.
     *
     * @return The pixel width of the image.
     */
    public abstract int getWidth();

    /**
     * Determine if my image is blank.
     *
     * @return {@code true} if the image is blank.
     */
    public abstract boolean isBlank();

    /**
     * Set the compressionHint.
     *
     * @param compressionHint the compressionHint to set
     */
    public void setCompressionHint(CompressionType compressionHint)
    {
    }

    /**
     * Set the pixel height of the image.
     *
     * @param height The pixel height of the image.
     */
    public void setHeight(int height)
    {
        if (height != getHeight())
        {
            LOGGER.warn("Attempted to set height to " + height + ". Actual height is " + getHeight());
        }
    }

    /**
     * Set the width height of the image.
     *
     * @param width The pixel width of the image.
     */
    public void setWidth(int width)
    {
        if (width != getWidth())
        {
            LOGGER.warn("Attempted to set width to " + width + ". Actual width is " + getWidth());
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        dispose();
        super.finalize();
    }

    /**
     * Set the raw data for the image.
     *
     * @param data The image data.
     * @param usePool Indicates if pooled objects should be used. If this is
     *            {@code true}, {@link #dispose()} must be called when use of
     *            the pooled objects is complete.
     * @throws ImageFormatUnknownException If the data format is not recognized.
     * @throws IOException If an  image input stream cannot be created.
     */
    protected abstract void setByteBuffer(ByteBuffer data, boolean usePool) throws ImageFormatUnknownException, IOException;

    /**
     * Set the input stream containing the raw data for the image.
     *
     * @param input The input stream.
     * @param estimatedStreamLengthBytes The estimated number of bytes in the
     *            stream, or -1 if unknown.
     * @param usePool Indicates if pooled objects should be used. If this is
     *            {@code true}, {@link #dispose()} must be called when use of
     *            the pooled objects is complete.
     * @throws ImageFormatUnknownException If the data format is not recognized.
     * @throws IOException If an  image input stream cannot be created.
     */
    protected void setInput(InputStream input, int estimatedStreamLengthBytes, boolean usePool)
        throws ImageFormatUnknownException, IOException
    {
        setByteBuffer(new StreamReader(input, estimatedStreamLengthBytes).readStreamIntoBuffer(), usePool);
    }

    /** Enumeration of supported compression types. */
    public enum CompressionType
    {
        /** DDS compression format. */
        D3DFMT_A8R8G8B8,

        /** DDS compression format. */
        D3DFMT_DXT1,

        /** DDS compression format. */
        D3DFMT_DXT2,

        /** DDS compression format. */
        D3DFMT_DXT3,

        /** DDS compression format. */
        D3DFMT_DXT4,

        /** DDS compression format. */
        D3DFMT_DXT5,

        /** DDS compression format. */
        D3DFMT_R8G8B8,

        /** DDS compression format. */
        D3DFMT_X8R8G8B8,

        /** Undefined compression format. */
        UNDEFINED,

        ;
    }
}
