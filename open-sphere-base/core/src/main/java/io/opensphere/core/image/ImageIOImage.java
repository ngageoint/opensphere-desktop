package io.opensphere.core.image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;

import io.opensphere.core.image.DDSEncoder.EncodingException;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.MappedObjectPool;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.io.ByteBufferInputStream;
import io.opensphere.core.util.lang.Cancellable;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.Serialization;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * An image that can be processed by Java ImageIO and converted to a DDS image.
 */
@SuppressWarnings("PMD.GodClass")
public class ImageIOImage extends Image implements DDSEncodableImage
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImageIOImage.class);

    /** System property indicating if blank checking is enabled. */
    private static final boolean BLANK_CHECKING_ENABLED = Boolean.getBoolean("opensphere.checkForBlankImages");

    /** Atomic updater for {@link #myBufferedImage}. */
    private static final AtomicReferenceFieldUpdater<ImageIOImage, BufferedImage> BUFFERED_IMAGE_UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(ImageIOImage.class, BufferedImage.class, "myBufferedImage");

    /** Procrastinating executor used to clean up the buffered image pool. */
    private static final Executor CLEANUP_EXECUTOR = CommonTimer.createProcrastinatingExecutor(10000, 30000);

    /** Pool for buffered images. */
    private static final MappedObjectPool<BufferedImageSpecs, BufferedImage> BUFFERED_IMAGE_POOL = new MappedObjectPool<>(
            BufferedImageSpecs.class, new LazyMap.Factory<BufferedImageSpecs, BufferedImage>()
            {
                @Override
                public BufferedImage create(BufferedImageSpecs specs)
                {
                    return specs.createBufferedImage();
                }
            }, 20, 20, CLEANUP_EXECUTOR);

    /** serialVersionUID. */
    private static final long serialVersionUID = 2L;

    /** The buffered image for my image data. */
    private transient volatile BufferedImage myBufferedImage;

    /** If a pooled buffered image is in use, this is the key into the pool. */
    private transient volatile BufferedImageSpecs myBufferedImageSpecs;

    /**
     * The cached byte buffer from a getByteBuffer call.
     */
    private ByteBuffer myByteBuffer;

    /**
     * A hint as to how to compress to a DDS image.
     */
    private CompressionType myCompressionHint = CompressionType.UNDEFINED;

    /**
     * The cached {@link DDSImage}.
     */
    private DDSImage myDDSImage;

    /**
     * True if the byte buffer should be cached after first call to
     * getByteBuffer.
     */
    private boolean myIsCacheByteBuffer;

    /**
     * True if the DDSImage will be cached after first call to asDDSImage, false
     * if the image should be recreated each call to asDDSImage.
     */
    private boolean myIsCacheDDSImage;

    /**
     * Read an image from a file.
     *
     * @param file The file.
     * @return The image.
     * @throws IOException If the file cannot be read.
     */
    public static ImageIOImage read(File file) throws IOException
    {
        final BufferedImage image = ImageIO.read(file);
        return image == null ? null : new ImageIOImage(image);
    }

    /**
     * Read an image from a stream.
     *
     * @param stream The stream.
     * @return The image.
     * @throws IOException If the file cannot be read.
     */
    public static ImageIOImage read(InputStream stream) throws IOException
    {
        final BufferedImage image = ImageIO.read(stream);
        return image == null ? null : new ImageIOImage(image);
    }

    /**
     * Read an image from a URL.
     *
     * @param url The URL.
     * @return The image.
     * @throws IOException If the file cannot be read.
     */
    public static ImageIOImage read(URL url) throws IOException
    {
        final BufferedImage image = ImageIO.read(url);
        return image == null ? null : new ImageIOImage(image);
    }

    /**
     * Construct the image.
     *
     * @param img existing buffered image.
     */
    public ImageIOImage(BufferedImage img)
    {
        myBufferedImage = img;
    }

    /**
     * Construct the image.
     *
     * @param data The data for the image.
     * @throws IOException If the image cannot be read from the array.
     */
    public ImageIOImage(byte[] data) throws IOException
    {
        this(ImageIO.read(new ByteArrayInputStream(data)));
    }

    /**
     * Constructor for use by {@link ImageFactory}.
     */
    protected ImageIOImage()
    {
    }

    @Override
    public DDSImage asDDSImage() throws IOException
    {
        if (myDDSImage == null || !myIsCacheDDSImage)
        {
            final CompressionType compression = getCompression();

            if (compression == CompressionType.D3DFMT_DXT5 && BLANK_CHECKING_ENABLED)
            {
                final long t0 = System.nanoTime();
                final boolean blank = isBlank();
                reportBlankImageTestingTime(System.nanoTime() - t0);
                if (blank)
                {
                    return DDSImage.getBlank(getHeight(), getWidth());
                }
            }

            String errorMsg = null;
            for (final DDSEncoder encoder : ServiceLoader.load(DDSEncoder.class))
            {
                final long t0 = System.nanoTime();
                ByteBuffer ddsBuffer;
                try
                {
                    ddsBuffer = encoder.encode(getAWTImage(), compression);
                    final long t1 = System.nanoTime();
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace(StringUtilities.formatTimingMessage(
                                "Time to encode [" + getAWTImage() + "] to DDS using " + compression + ": ", t1 - t0));
                    }
                    else if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(StringUtilities.formatTimingMessage("Time to encode from buffered image type "
                                + getAWTImage().getType() + " to DDS using " + compression + ": ", t1 - t0));
                    }
                    final DDSImage retImg = new DDSImage(ddsBuffer);
                    retImg.setCompressionHint(compression);

                    myDDSImage = retImg;
                }
                catch (final EncodingException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Exception in image encoding: " + e, e);
                    }
                    errorMsg = e.getMessage();
                }
            }

            if (myDDSImage == null)
            {
                if (errorMsg == null)
                {
                    throw new IOException("Failed to encode image to DDS: no available encoder.");
                }
                else
                {
                    throw new IOException("Failed to encode image to DDS: " + errorMsg);
                }
            }
        }

        return myDDSImage;
    }

    @Override
    public void dispose()
    {
        final BufferedImage image = BUFFERED_IMAGE_UPDATER.getAndSet(this, null);
        myByteBuffer = null;
        myDDSImage = null;
        if (image != null)
        {
            handleDispose(image);
        }
    }

    /**
     * Access to the AWT image.
     *
     * @return The buffered image.
     */
    public BufferedImage getAWTImage()
    {
        final BufferedImage image = myBufferedImage;
        if (image == null)
        {
            throw new IllegalStateException("Image has been disposed.");
        }
        return image;
    }

    @Override
    public ByteBuffer getByteBuffer()
    {
        if (myByteBuffer == null || !myIsCacheByteBuffer)
        {
            myByteBuffer = ByteBuffer.wrap(((DataBufferByte)getAWTImage().getData().getDataBuffer()).getData());
        }

        return myByteBuffer;
    }

    @Override
    public ByteBuffer getByteBuffer(Rectangle rect)
    {
        return ByteBuffer.wrap(((DataBufferByte)getAWTImage().getData(rect).getDataBuffer()).getData());
    }

    /**
     * A hint as to how to compress to a DDS image.
     * {@link Image.CompressionType#UNDEFINED} indicates that any compression is
     * acceptable.
     *
     * @return the compressionHint
     */
    public CompressionType getCompressionHint()
    {
        return myCompressionHint;
    }

    @Override
    public InputStream getDDSImageStream(Executor executor, final boolean dispose) throws EncodingException
    {
        boolean disposeHandled = false;
        try
        {
            final CompressionType compression = getCompression();

            if (compression == CompressionType.D3DFMT_DXT5 && BLANK_CHECKING_ENABLED)
            {
                final long t0 = System.nanoTime();
                final boolean blank = isBlank();
                reportBlankImageTestingTime(System.nanoTime() - t0);
                if (blank)
                {
                    final DDSImage dds = DDSImage.getBlank(getHeight(), getWidth());
                    try
                    {
                        return new ByteArrayInputStream(Serialization.serialize(dds));
                    }
                    catch (final IOException e)
                    {
                        throw new EncodingException("Failed to encode image to DDS: " + e, e);
                    }
                }
            }

            EncodingException err = null;
            for (final DDSEncoder encoder : ServiceLoader.load(DDSEncoder.class))
            {
                try
                {
                    final InputStream result = encoder.encodeStreaming(getAWTImage(), compression, executor, new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (dispose)
                            {
                                dispose();
                            }
                        }
                    });
                    disposeHandled = true;
                    return result;
                }
                catch (final EncodingException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(e, e);
                    }
                    if (err == null)
                    {
                        err = e;
                    }
                }
            }

            if (err == null)
            {
                throw new EncodingException("Failed to encode image to DDS: no available encoder.");
            }
            else
            {
                throw err;
            }
        }
        finally
        {
            if (dispose && !disposeHandled)
            {
                dispose();
            }
        }
    }

    @Override
    public int getHeight()
    {
        return getAWTImage().getHeight();
    }

    @Override
    public long getSizeInBytes()
    {
        return getAWTImage().getData().getDataBuffer().getSize();
    }

    @Override
    public int getWidth()
    {
        return getAWTImage().getWidth();
    }

    @Override
    public boolean isBlank()
    {
        int alphaPos;
        if (getAWTImage().getType() == BufferedImage.TYPE_4BYTE_ABGR)
        {
            alphaPos = 0;
        }
        else
        {
            return false;
        }

        final byte[] arr = ((DataBufferByte)getAWTImage().getData().getDataBuffer()).getData();

        final int rowOffsetSize = getHeight() % 16 == 0 ? 16 : getHeight() % 4 == 0 ? 4 : 1;
        final int colOffsetSize = getWidth() % 16 == 0 ? 16 : getWidth() % 4 == 0 ? 4 : 1;
        for (int rowOffset = 0; rowOffset < rowOffsetSize; ++rowOffset)
        {
            for (int colOffset = 0; colOffset < colOffsetSize; ++colOffset)
            {
                for (int row = rowOffset; row < getHeight(); row += rowOffsetSize)
                {
                    for (int col = colOffset; col < getWidth(); col += colOffsetSize)
                    {
                        if (arr[(row * getWidth() + col) * 4 + alphaPos] != 0)
                        {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Sets whether or not asDDSImage should cache the image for use later.
     *
     * @param isCacheOn True if the DDSImage will be cached after first call to
     *            asDDSImage, false if the image should be recreated each call
     *            to asDDSImage.
     */
    public void setCacheDDSImage(boolean isCacheOn)
    {
        myIsCacheDDSImage = isCacheOn;
        myDDSImage = null;
    }

    @Override
    public void setCompressionHint(CompressionType compressionHint)
    {
        myCompressionHint = compressionHint;
    }

    /**
     * Sets if the byte buffer should be cached after first call to get byte
     * buffer.
     *
     * @param cacheByteBuffer True if the byte buffer should be cached, false
     *            otherwise.
     */
    public void setIsCacheByteBuffer(boolean cacheByteBuffer)
    {
        myIsCacheByteBuffer = cacheByteBuffer;
        myByteBuffer = null;
    }

    /**
     * Handle the disposal of the {@link BufferedImage} after it has been
     * atomically retrieved from {@link #myBufferedImage}.
     *
     * @param image The image.
     */
    protected void handleDispose(BufferedImage image)
    {
        final BufferedImageSpecs key = myBufferedImageSpecs;
        myBufferedImageSpecs = null;
        if (key != null)
        {
            BUFFERED_IMAGE_POOL.surrender(key, image);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.image.Image#setByteBuffer(java.nio.ByteBuffer,
     *      boolean)
     */
    @Override
    protected void setByteBuffer(ByteBuffer buffer, boolean usePool) throws ImageFormatUnknownException, IOException
    {
        setInput(new ByteBufferInputStream(buffer), buffer.limit(), usePool);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.image.Image#setInput(java.io.InputStream, int,
     *      boolean)
     */
    @Override
    protected void setInput(InputStream input, int estimatedStreamLengthBytes, boolean usePool)
        throws ImageFormatUnknownException, IOException
    {
        try (BufferedInputStream in = new BufferedInputStream(input))
        {
            // mark the first 25 bytes as a read-ahead, as the ImageIO class
            // reads into the stream to determine
            // the format, and in case of an exception message, those bytes
            // should be recovered.
            in.mark(25);
            try (ImageInputStream iis = ImageIO.createImageInputStream(in))
            {
                if (!readToBuffer(iis, input, usePool))
                {
                    // attempt to read the stream as text, as it may have an
                    // error message in it:
                    if (LOGGER.isDebugEnabled())
                    {
                        in.reset();
                        final String errorMessage = ImageErrorProcessor.processError(in);
                        LOGGER.debug(errorMessage);
                    }
                    myBufferedImage = ImageUtil.BLANK_IMAGE;
                }
            }
        }
    }

    /**
     * Reads data from the supplied {@link ImageInputStream}, placing it into
     * the {@link #BUFFERED_IMAGE_POOL} and storing a reference in the
     * {@link #myBufferedImage} field upon successful read.
     *
     * @param pImageInput the source from which data is read.
     * @param pSourceInput the input stream wrapped in the
     *            {@link ImageInputStream}, used only to determine if the
     *            operation has been cancelled upon interruption.
     * @param pUsePool a flag used to force the use of the
     *            {@link #BUFFERED_IMAGE_POOL}.
     * @return true if the read operation was successful, false otherwise.
     * @throws IOException if the operation was cancelled during the read
     *             operation.
     */
    protected boolean readToBuffer(ImageInputStream pImageInput, InputStream pSourceInput, boolean pUsePool) throws IOException
    {
        boolean success = false;
        String error = "Unable to read image";
        final Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(pImageInput);

        if (!imageReaders.hasNext())
        {
            error = "Unable to read image because no image readers were found";
        }

        while (imageReaders.hasNext() && !success)
        {
            final ImageReader reader = imageReaders.next();
            try
            {
                final ImageReadParam param = reader.getDefaultReadParam();
                reader.setInput(pImageInput, true, true);
                final int width = reader.getWidth(0);
                final int height = reader.getHeight(0);

                final ImageTypeSpecifier chosenType = getImageTypeSpecifier(reader);
                if (chosenType == null)
                {
                    continue;
                }

                final BufferedImageSpecs key = new BufferedImageSpecs(width, height, chosenType);

                BufferedImage destination = null;
                try
                {
                    while (true)
                    {
                        try
                        {
                            destination = getBufferedImage(pUsePool, key);
                            break;
                        }
                        catch (final InterruptedException e)
                        {
                            if (LOGGER.isDebugEnabled())
                            {
                                LOGGER.debug(e, e);
                            }
                            if (pSourceInput instanceof Cancellable && ((Cancellable)pSourceInput).isCancelled())
                            {
                                throw new IOException("Stream is cancelled.", e);
                            }
                        }
                    }
                    param.setDestination(destination);
                    final BufferedImage img = reader.read(0, param);
                    myBufferedImage = img;
                    destination = null;
                    success = true;
                }
                finally
                {
                    if (pUsePool && destination != null)
                    {
                        BUFFERED_IMAGE_POOL.surrender(key, destination);
                    }
                }
            }
            finally
            {
                reader.dispose();
            }
        }

        if (!success)
        {
            LOGGER.error(error);
        }

        return success;
    }

    /**
     * Get a {@link BufferedImage} that can be used to read image data.
     *
     * @param usePool If the buffered image pool should be used.
     * @param key The key for the buffered image.
     * @return The buffered image.
     * @throws InterruptedException If the thread is interrupted while waiting
     *             for a pooled image.
     */
    private synchronized BufferedImage getBufferedImage(boolean usePool, BufferedImageSpecs key) throws InterruptedException
    {
        BufferedImage bi;
        if (usePool)
        {
            bi = BUFFERED_IMAGE_POOL.take(key);
            myBufferedImageSpecs = key;
        }
        else
        {
            bi = key.createBufferedImage();
            myBufferedImageSpecs = null;
        }
        return bi;
    }

    /**
     * Get the compression mode, either from the compression hint or by
     * examining the image parameters.
     *
     * @return The compression type.
     */
    private CompressionType getCompression()
    {
        CompressionType compression = getCompressionHint();
        if (compression == Image.CompressionType.UNDEFINED)
        {
            compression = getAWTImage().getColorModel().hasAlpha()
                    ? isDXTCompressible() ? CompressionType.D3DFMT_DXT5 : CompressionType.D3DFMT_A8R8G8B8
                    : isDXTCompressible() ? CompressionType.D3DFMT_DXT1 : CompressionType.D3DFMT_R8G8B8;
            setCompressionHint(compression);
        }
        return compression;
    }

    /**
     * Choose an image type that the given reader can decode to, preferring a
     * BGR type since that's what the DDS encoder prefers.
     *
     * @param reader The image reader.
     * @return The image type, or {@code null} if none were found.
     * @throws IOException If there's an error reading the input image.
     */
    private ImageTypeSpecifier getImageTypeSpecifier(ImageReader reader) throws IOException
    {
        // Seek a BGR type since that's what the DDS encoder likes.
        ImageTypeSpecifier chosenType = null;
        for (final Iterator<ImageTypeSpecifier> imageTypes = reader.getImageTypes(0); imageTypes.hasNext();)
        {
            final ImageTypeSpecifier type = imageTypes.next();
            if (isBGR(type))
            {
                chosenType = type;
                break;
            }
            else if (chosenType == null)
            {
                chosenType = type;
            }
        }
        return chosenType;
    }

    /**
     * Determine if this image type specifier is for an 4-byte ABGR or 3-byte
     * BGR image.
     *
     * @param type The image type specifier.
     * @return {@code true} if it's the right type.
     */
    private boolean isBGR(ImageTypeSpecifier type)
    {
        for (final int componentSize : type.getColorModel().getComponentSize())
        {
            if (componentSize != 8)
            {
                return false;
            }
        }
        final SampleModel sampleModel = type.getSampleModel();
        if (!(sampleModel instanceof PixelInterleavedSampleModel))
        {
            return false;
        }
        final int[] bandOffsets = ((PixelInterleavedSampleModel)sampleModel).getBandOffsets();
        for (int index = 0; index < bandOffsets.length; ++index)
        {
            if (bandOffsets[index] != bandOffsets.length - index - 1)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Determine if my image can be compressed using DXT.
     *
     * @return {@code true} if the image is compressible using DXT.
     */
    private boolean isDXTCompressible()
    {
        return getAWTImage().getWidth() % 4 == 0 && getAWTImage().getHeight() % 4 == 0;
    }

    /**
     * Read this object from an input stream. This overrides the standard
     * de-serialization behavior.
     *
     * @param in The stream.
     * @throws IOException If there is an error reading from the stream.
     * @throws ClassNotFoundException If a class from the stream cannot be
     *             loaded.
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        myCompressionHint = (CompressionType)in.readObject();
        myBufferedImage = ImageIO.read(in);
    }

    /**
     * Report the time it took to do blank image testing.
     *
     * @param deltaNanos The time in nanoseconds.
     */
    private void reportBlankImageTestingTime(long deltaNanos)
    {
        if (deltaNanos > 20000000)
        {
            LOGGER.warn(StringUtilities.formatTimingMessage("Blank image testing took ", deltaNanos));
        }
        else if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(StringUtilities.formatTimingMessage("Blank image testing took ", deltaNanos));
        }
    }

    /**
     * Write this object to a stream. This overrides the standard serialization
     * behavior.
     *
     * @param out The stream.
     * @throws IOException If there is an error writing to the stream.
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.writeObject(myCompressionHint);
        ImageIO.write(getAWTImage(), "png", out);
    }

    /**
     * The parameters necessary to construct a buffered image.
     */
    private static class BufferedImageSpecs
    {
        /** The image width. */
        private final int myHeight;

        /** The image type. */
        @Nonnull
        private final ImageTypeSpecifier myImageType;

        /** The image height. */
        private final int myWidth;

        /**
         * Construct the image specifications.
         *
         * @param width The width of the image.
         * @param height The height of the image.
         * @param imageType The type of the image.
         */
        public BufferedImageSpecs(int width, int height, ImageTypeSpecifier imageType)
        {
            myWidth = width;
            myHeight = height;
            myImageType = Utilities.checkNull(imageType, "imageType");
        }

        /**
         * Create a buffered image.
         *
         * @return The buffered image.
         */
        public BufferedImage createBufferedImage()
        {
            return myImageType.createBufferedImage(myWidth, myHeight);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {
                return false;
            }
            final BufferedImageSpecs other = (BufferedImageSpecs)obj;
            return myHeight == other.myHeight && myWidth == other.myWidth && EqualsHelper.equals(myImageType, other.myImageType);
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + myHeight;
            result = prime * result + myImageType.hashCode();
            result = prime * result + myWidth;
            return result;
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + "[" + myImageType + ", width: " + myWidth + ", height: " + myHeight + "]";
        }
    }
}
