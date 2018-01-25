package io.opensphere.auxiliary.codec;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import io.opensphere.core.image.DDSDecoder;
import io.opensphere.core.image.DDSEncoder;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.Image.CompressionType;
import io.opensphere.core.image.StreamingDDSImage;

/**
 * Service provider that encodes {@link BufferedImage}s to Direct Draw Surface
 * (DDS) encoded byte arrays and also decodes DDS images.
 */
@SuppressWarnings("PMD.GodClass")
public class RealtimeDDSEncoder implements DDSEncoder, DDSDecoder
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RealtimeDDSEncoder.class);

    @Override
    public ByteBuffer decode(ByteBuffer input, CompressionType compression, int width, int height)
    {
        // Convert the compression type from DDSImage to
        // DDSEncoder.CompressionType.
        io.opensphere.core.common.dds.DDSEncoder.CompressionType compType = null;
        switch (compression)
        {
            case D3DFMT_DXT1:
                compType = io.opensphere.core.common.dds.DDSEncoder.CompressionType.DXT1;
                break;
            case D3DFMT_DXT5:
                compType = io.opensphere.core.common.dds.DDSEncoder.CompressionType.DXT5;
                break;
            default:
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(RealtimeDDSEncoder.class.getSimpleName() + " does not support compression type: " + compression);
                }
                return null;
        }

        return ByteBuffer.wrap(new io.opensphere.core.common.dds.DDSEncoder().decodeDDS(width, height, compType, input));
    }

    @Override
    public ByteBuffer encode(BufferedImage input, Image.CompressionType compression) throws EncodingException
    {
        io.opensphere.core.common.dds.DDSEncoder compressor = new io.opensphere.core.common.dds.DDSEncoder();

        byte[] sourceArray = getSourceArray(input, compressor);

        io.opensphere.core.common.dds.DDSEncoder.CompressionType compType = getCompressionType(input, compression);

        ByteBuffer ddsBuffer = getTargetBuffer(input, compression);
        ddsBuffer.order(ByteOrder.LITTLE_ENDIAN);

        compressor.encodeDDS(input.getWidth(), input.getHeight(), compType, sourceArray, ddsBuffer);
        ddsBuffer.flip();

        return ddsBuffer;
    }

    @Override
    public InputStream encodeStreaming(final BufferedImage input, CompressionType compression, Executor executor,
            final Runnable encodingCompleteTask) throws EncodingException
    {
        boolean streamStarted = false;
        try
        {
            final PipedOutputStream outputStream = new PipedOutputStream();

            final AtomicBoolean inputStreamClosed = new AtomicBoolean();
            PipedInputStream resultStream;
            try
            {
                resultStream = new PipedInputStream(outputStream)
                {
                    @Override
                    public void close() throws IOException
                    {
                        inputStreamClosed.getAndSet(true);
                        super.close();
                    }
                };
            }
            catch (IOException e)
            {
                throw new EncodingException("Failed to setup piped stream: " + e, e);
            }

            final io.opensphere.core.common.dds.DDSEncoder.CompressionType compType = getCompressionType(input, compression);
            final int imageSize = getResultImageSize(input, compression);

            executor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                        oos.writeObject(new StreamingDDSImage(new StreamingDDSImage.Serializer()
                        {
                            @Override
                            public void writeToStream(ObjectOutputStream out) throws IOException
                            {
                                io.opensphere.core.common.dds.DDSEncoder compressor = new io.opensphere.core.common.dds.DDSEncoder();
                                byte[] sourceArray = getSourceArray(input, compressor);
                                out.writeInt(imageSize);
                                compressor.encodeDDS(input.getWidth(), input.getHeight(), compType, sourceArray, out);
                            }
                        }));
                    }
                    catch (IOException e)
                    {
                        if (inputStreamClosed.get())
                        {
                            if (LOGGER.isDebugEnabled())
                            {
                                LOGGER.debug("Failed to write image to stream (input stream closed): " + e, e);
                            }
                        }
                        else
                        {
                            LOGGER.warn("Failed to encode DDS: " + e, e);
                        }
                    }
                    finally
                    {
                        if (encodingCompleteTask != null)
                        {
                            encodingCompleteTask.run();
                        }
                        try
                        {
                            outputStream.close();
                        }
                        catch (IOException e)
                        {
                            LOGGER.error("Failed to close piped output stream: " + e, e);
                        }
                    }
                }
            });
            streamStarted = true;

            return resultStream;
        }
        finally
        {
            // fail-safe
            if (!streamStarted && encodingCompleteTask != null)
            {
                encodingCompleteTask.run();
            }
        }
    }

    /**
     * Get the ABGR source array for an image.
     *
     * @param image The input image.
     * @return The ABGR source array.
     */
    private byte[] getABGRSourceArray(BufferedImage image)
    {
        byte[] sourceArray;
        BufferedImage abgr;
        if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR)
        {
            abgr = image;
        }
        else
        {
            abgr = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D graphics = abgr.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
        }
        sourceArray = ((DataBufferByte)abgr.getData().getDataBuffer()).getData();
        return sourceArray;
    }

    /**
     * Get the BGR source array for an image.
     *
     * @param image The input image.
     * @return The BGR source array.
     */
    private byte[] getBGRSourceArray(BufferedImage image)
    {
        byte[] sourceArray;
        BufferedImage bgr;
        if (image.getType() == BufferedImage.TYPE_3BYTE_BGR)
        {
            bgr = image;
        }
        else
        {
            bgr = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D graphics = bgr.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
        }
        sourceArray = ((DataBufferByte)bgr.getData().getDataBuffer()).getData();
        return sourceArray;
    }

    /**
     * Get the {@link io.opensphere.core.common.dds.DDSEncoder.CompressionType}
     * appropriate for this image.
     *
     * @param input The input image.
     * @param compression The requested compression type.
     * @return The compression type to use.
     * @throws EncodingException If the requested compression type is not
     *             recognized.
     */
    private io.opensphere.core.common.dds.DDSEncoder.CompressionType getCompressionType(BufferedImage input,
            Image.CompressionType compression) throws EncodingException
    {
        io.opensphere.core.common.dds.DDSEncoder.CompressionType compType = null;
        switch (compression)
        {
            case D3DFMT_DXT1:
                compType = input.getColorModel().hasAlpha() ? io.opensphere.core.common.dds.DDSEncoder.CompressionType.DXT1_Transparent
                        : io.opensphere.core.common.dds.DDSEncoder.CompressionType.DXT1;
                break;
            case D3DFMT_DXT5:
                compType = input.getColorModel().hasAlpha() ? io.opensphere.core.common.dds.DDSEncoder.CompressionType.DXT5
                        : io.opensphere.core.common.dds.DDSEncoder.CompressionType.DXT1;
                break;
            case D3DFMT_A8R8G8B8:
            case D3DFMT_R8G8B8:
                compType = io.opensphere.core.common.dds.DDSEncoder.CompressionType.UNCOMPRESSED;
                break;
            default:
                throw new EncodingException(
                        RealtimeDDSEncoder.class.getSimpleName() + " does not support compression type: " + compression);
        }

        if (compType != io.opensphere.core.common.dds.DDSEncoder.CompressionType.UNCOMPRESSED
                && (input.getWidth() % 4 != 0 || input.getHeight() % 4 != 0))
        {
            throw new EncodingException(
                    "Cannot encode image to DXT: dimensions are " + input.getHeight() + "x" + input.getWidth());
        }

        return compType;
    }

    /**
     * Get the size of the final DDS image.
     *
     * @param input The input image.
     * @param compression The compression type for the DDS image.
     * @return The size in bytes.
     * @throws EncodingException If the compression type is not supported.
     */
    private int getResultImageSize(BufferedImage input, Image.CompressionType compression) throws EncodingException
    {
        int destDataSize = io.opensphere.core.common.dds.DDSEncoder.DDS_HEADER_SIZE;
        switch (compression)
        {
            case D3DFMT_DXT1:
                destDataSize += Math.ceil((float)input.getWidth() / 4) * Math.ceil((float)input.getHeight() / 4) * 8;
                break;
            case D3DFMT_DXT5:
                destDataSize += Math.ceil((float)input.getWidth() / 4) * Math.ceil((float)input.getHeight() / 4) * 16;
                break;
            case D3DFMT_A8R8G8B8:
                destDataSize += input.getWidth() * input.getHeight() * 4;
                break;
            case D3DFMT_R8G8B8:
                destDataSize += input.getWidth() * input.getHeight() * 3;
                break;
            default:
                throw new EncodingException(
                        RealtimeDDSEncoder.class.getSimpleName() + " does not support compression type: " + compression);
        }
        return destDataSize;
    }

    /**
     * Get the source array from the image and set the parameters in the encoder
     * based on the sample and color models.
     *
     * @param input The input image.
     * @param compressor The encoder.
     * @return The source array.
     */
    private byte[] getSourceArray(BufferedImage input, io.opensphere.core.common.dds.DDSEncoder compressor)
    {
        byte[] sourceArray;

        boolean isRGB;
        if (input.getColorModel().getNumComponents() == 4 && input.getSampleModel() instanceof PixelInterleavedSampleModel)
        {
            PixelInterleavedSampleModel sm = (PixelInterleavedSampleModel)input.getSampleModel();
            isRGB = sm.getBandOffsets().length == 4;
            for (int index = 0; index < sm.getBandOffsets().length && isRGB; ++index)
            {
                if (sm.getBandOffsets()[index] != index)
                {
                    isRGB = false;
                }
            }
        }
        else
        {
            isRGB = false;
        }
        if (isRGB)
        {
            sourceArray = ((DataBufferByte)input.getData().getDataBuffer()).getData();
        }
        else if (input.getColorModel().hasAlpha())
        {
            sourceArray = getABGRSourceArray(input);
            compressor.setColorOrder(3, 2, 1, 0);
        }
        else
        {
            sourceArray = getBGRSourceArray(input);
            compressor.setBGR();
        }
        return sourceArray;
    }

    /**
     * Get a byte buffer sized appropriately for the output image.
     *
     * @param input The input image.
     * @param compression The requested compression type.
     * @return The buffer.
     * @throws EncodingException If the requested compression type is not
     *             recognized.
     */
    private ByteBuffer getTargetBuffer(BufferedImage input, Image.CompressionType compression) throws EncodingException
    {
        return ByteBuffer.allocate(getResultImageSize(input, compression));
    }
}
