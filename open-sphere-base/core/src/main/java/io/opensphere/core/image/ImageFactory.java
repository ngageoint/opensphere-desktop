package io.opensphere.core.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import io.opensphere.core.image.DDSEncoder.EncodingException;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.io.CancellableInputStream;

/**
 * Factory for creating {@link Image} objects.
 */
public class ImageFactory
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImageFactory.class);

    /**
     * Create an image from an input stream and an image format.
     *
     * @param stream The input stream.
     * @param contentLengthBytes The estimated length of the stream, in bytes,
     *            or -1 if the length is unknown.
     * @param imageFormat The image format.
     * @param ddsDesired Indicates if a DDS image is preferred.
     * @param useBufferedImagePool Indicates if the buffered image pool is to be
     *            used. Either this or {@code ddsDesired} can be specified, but
     *            not both. If this is {@code true}, {@link Image#dispose()}
     *            must be called to return the buffered image to the pool.
     * @param metrics Optional metrics object that will be populated with the
     *            appropriate metrics if provided.
     * @return The image object.
     * @throws ImageFormatUnknownException If the image format is not
     *             recognized.
     */
    public Image createImage(InputStream stream, int contentLengthBytes, String imageFormat, boolean ddsDesired,
            boolean useBufferedImagePool, ImageMetrics metrics) throws ImageFormatUnknownException
    {
        if (ddsDesired && useBufferedImagePool)
        {
            throw new IllegalArgumentException("Only one of ddsDesired or useBufferedImagePool may be specified.");
        }

        try
        {
            final Image image = Format.getClass(imageFormat).newInstance();
            if (ddsDesired && image instanceof DDSEncodableImage)
            {
                // Use the buffered image pool to conserve memory. The image
                // must be disposed to return the buffered image to the pool.
                final boolean usePool = true;

                setImageInput(image, stream, contentLengthBytes, metrics, usePool);
                try
                {
                    return convertToDDS((DDSEncodableImage)image, metrics);
                }
                catch (IOException e)
                {
                    LOGGER.warn("Failed to get DDS image: " + e, e);
                    return createImage(stream, contentLengthBytes, imageFormat, false, useBufferedImagePool, metrics);
                }
                finally
                {
                    image.dispose();
                }
            }
            else
            {
                setImageInput(image, stream, contentLengthBytes, metrics, useBufferedImagePool);
                return image;
            }
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            LOGGER.error("Failed to instantiate concrete image class: " + e, e);
        }
        catch (SocketException e)
        {
            if (stream instanceof CancellableInputStream)
            {
                if (((CancellableInputStream)stream).isCancelled())
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Failed to decode image: " + e, e);
                    }
                }
                else
                {
                    LOGGER.error("Failed to decode image: " + e, e);
                }
            }
            else
            {
                LOGGER.error("Failed to decode image: " + e, e);
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to decode image: " + e, e);
        }
        return null;
    }

    /**
     * Encode an image as a DDS on a separate thread. Return an input stream
     * from which the DDS can be read.
     *
     * @param in The input stream.
     * @param imageFormat The input image format.
     * @param contentLength The content length for the image.
     * @param metrics Optional metrics object that will be populated with the
     *            appropriate metrics if provided.
     * @param executor An executor to use for encoding the image.
     * @return The input stream from which a {@link DDSImage} can be
     *         deserialized.
     * @throws ImageFormatUnknownException If the image format is not
     *             recognized.
     * @throws IOException If there is an error reading from the input stream.
     */
    public InputStream getDDSImageStream(InputStream in, String imageFormat, int contentLength, ImageMetrics metrics,
            Executor executor) throws ImageFormatUnknownException, IOException
    {
        try
        {
            final Image image = Format.getClass(imageFormat).newInstance();
            if (image instanceof DDSEncodableImage)
            {
                // Use the buffered image pool to conserve memory. The image
                // must be disposed to return the buffered image to the pool.
                final boolean useBufferedImagePool = true;

                setImageInput(image, in, contentLength, metrics, useBufferedImagePool);
                try
                {
                    return ((DDSEncodableImage)image).getDDSImageStream(executor, true);
                }
                catch (EncodingException e)
                {
                    LOGGER.warn("Failed to get DDS image: " + e, e);
                    return in;
                }
            }
            else
            {
                return in;
            }
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            LOGGER.error("Failed to instantiate concrete image class: " + e, e);
        }
        return null;
    }

    /**
     * Convert an image to DDS.
     *
     * @param image The input image.
     * @param metrics The optional metrics.
     * @return The DDS image.
     * @throws IOException If the image cannot be converted.
     */
    private Image convertToDDS(DDSEncodableImage image, ImageMetrics metrics) throws IOException
    {
        long t0 = metrics == null ? -1L : System.nanoTime();
        DDSImage dds = image.asDDSImage();
        if (metrics != null)
        {
            metrics.setEncodeTimeNanoseconds(System.nanoTime() - t0);
        }
        return dds;
    }

    /**
     * Set the input stream in the image.
     *
     * @param image The image.
     * @param stream The input stream.
     * @param contentLengthBytes The estimated length of the stream, in bytes,
     *            or -1 if the length is unknown.
     * @param metrics The optional metrics.
     * @param useBufferedImagePool If the buffered image pool should be used. If
     *            this is {@code true}, the image must be disposed to return the
     *            buffered image to the pool.
     *
     * @throws ImageFormatUnknownException If the image format is not
     *             recognized.
     * @throws IOException If the image input stream cannot be created.
     */
    private void setImageInput(Image image, InputStream stream, int contentLengthBytes, ImageMetrics metrics,
            boolean useBufferedImagePool) throws ImageFormatUnknownException, IOException
    {
        boolean success = false;
        try
        {
            long t0 = metrics == null ? -1L : System.nanoTime();
            image.setInput(stream, contentLengthBytes, useBufferedImagePool);
            if (metrics != null)
            {
                metrics.setDecodeTimeNanoseconds(System.nanoTime() - t0);
            }
            success = true;
        }
        finally
        {
            // fail-safe
            if (!success && useBufferedImagePool)
            {
                image.dispose();
            }
        }
    }

    /**
     * Enumeration of supported image formats.
     */
    public enum Format
    {
        /** PNG format. */
        PNG(MimeType.PNG, ImageIOImage.class),

        /** BIL terrain format. */
        BIL(MimeType.BIL, BILImage.class),

        /** JPEG format. */
        JPEG(MimeType.JPEG, ImageIOImage.class),

        /** JPG format. */
        JPG(MimeType.JPG, ImageIOImage.class),

        /** GIF format. */
        GIF(MimeType.GIF, ImageIOImage.class),

        /** DDS format. */
        DDS(MimeType.DDS, DDSImage.class),

        /** UNKNOWN format. */
        UNKNOWN(null, ImageIOImage.class),

        ;

        /** The class used to model the image format. */
        private final Class<? extends Image> myClass;

        /** The content type for the image format. */
        private final MimeType myName;

        /**
         * Find the format with the given content type. Returns {@link #UNKNOWN}
         * if the given name is unrecognized.
         *
         * @param name The content type.
         * @return The format.
         */
        public static Format findFormat(String name)
        {
            if (name != null)
            {
                Format[] values = Format.values();
                for (Format format : values)
                {
                    if (format.getMimeType() != null && format.getMimeType().getMimeType().equals(name))
                    {
                        return format;
                    }
                }
            }
            return UNKNOWN;
        }

        /**
         * Get the image class for a format content type.
         *
         * @param format The content type.
         * @return The image class.
         */
        public static Class<? extends Image> getClass(String format)
        {
            return findFormat(format).myClass;
        }

        /**
         * Construct a format type.
         *
         * @param mimeType The content type of the format.
         * @param cl The class used to model the format.
         */
        Format(MimeType mimeType, Class<? extends Image> cl)
        {
            myName = mimeType;
            myClass = cl;
        }

        /**
         * Accessor for the MIME type.
         *
         * @return The MIME type.
         */
        public MimeType getMimeType()
        {
            return myName;
        }
    }
}
