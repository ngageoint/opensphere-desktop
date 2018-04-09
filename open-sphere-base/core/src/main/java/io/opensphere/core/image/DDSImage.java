package io.opensphere.core.image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;

import javax.imageio.ImageIO;

import io.opensphere.core.image.DDSEncoder.EncodingException;
import io.opensphere.core.util.io.ByteBufferInputStream;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Data for a DDS image.
 */
@SuppressWarnings("PMD.GodClass")
public class DDSImage extends Image implements DDSEncodableImage
{
    /** Size of the DDS header. */
    public static final int DDS_HEADER_SIZE = 128;

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Data for the image. */
    private ByteBuffer myBuffer;

    /**
     * Get a {@link DDSImage} that represents a blank image without actually
     * using the memory.
     *
     * @param height The height of the image.
     * @param width The width of the image.
     * @return The image.
     */
    public static DDSImage getBlank(final int height, final int width)
    {
        return new DDSImage(ByteBuffer.allocate(0))
        {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public ByteBuffer getByteBuffer()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public ByteBuffer getByteBuffer(Rectangle region)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompressionType getCompressionType()
            {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getHeight()
            {
                return height;
            }

            @Override
            public long getSizeInBytes()
            {
                return 0L;
            }

            @Override
            public int getWidth()
            {
                return width;
            }

            @Override
            public boolean isBlank()
            {
                return true;
            }
        };
    }

    /**
     * Gets the image type from the image and image data.
     *
     * @param image the image
     * @param data the image data
     * @return the image type
     */
    private static int getImageType(Image image, ByteBuffer data)
    {
        int bytesPerPixel = data.array().length / (image.getWidth() * image.getHeight());
        return bytesPerPixel == 4 ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR;
    }

    /**
     * Construct from a byte array of DDS image data.
     *
     * @param buf The data.
     */
    public DDSImage(byte[] buf)
    {
        this(ByteBuffer.wrap(buf));
    }

    /**
     * Construct from a byte buffer of DDS image data.
     *
     * @param data The data.
     */
    public DDSImage(ByteBuffer data)
    {
        myBuffer = data.duplicate();
        myBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Construct from a DDS file.
     *
     * @param file The file.
     * @throws IOException If the file cannot be read.
     */
    public DDSImage(File file) throws IOException
    {
        FileInputStream inputStream = new FileInputStream(file);
        try
        {
            myBuffer = inputStream.getChannel().map(FileChannel.MapMode.PRIVATE, 0L, file.length());
            myBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        finally
        {
            inputStream.close();
        }
    }

    /**
     * Constructor for use by {@link ImageFactory}.
     */
    protected DDSImage()
    {
    }

    @Override
    public DDSImage asDDSImage() throws IOException
    {
        return this;
    }

    @Override
    public void dispose()
    {
        synchronized (this)
        {
            myBuffer = null;
        }
    }

    @Override
    public ByteBuffer getByteBuffer()
    {
        synchronized (this)
        {
            if (myBuffer == null)
            {
                throw new IllegalStateException("Image has been disposed.");
            }

            ByteBuffer buf = myBuffer.duplicate();
            buf.order(myBuffer.order());

            return buf;
        }
    }

    @Override
    public ByteBuffer getByteBuffer(Rectangle region)
    {
        // TODO: Does this work? It's using the image width as if it were bytes
        // instead of pixels. It seems like the desired DDS blocks need to be
        // calculated and then the corresponding bytes returned.

        ByteBuffer result = ByteBuffer.wrap(new byte[region.width * region.height]);

        int imgWidth = getWidth();
        ByteBuffer chunk = getByteBuffer();
        if (imgWidth == region.width)
        {
            chunk.position(DDS_HEADER_SIZE + region.y * imgWidth);
            chunk.limit(chunk.position() + result.capacity());
            result.put(chunk);
        }
        else
        {
            for (int i = 1; i <= region.height; ++i)
            {
                chunk.limit(DDS_HEADER_SIZE + (i + region.y) * imgWidth + region.x);
                chunk.position(chunk.limit() - region.width);
                result.put(chunk);
            }
        }

        return result.rewind();
    }

    @Override
    public CompressionType getCompressionType()
    {
        CompressionType type = CompressionType.UNDEFINED;

        synchronized (this)
        {
            if (myBuffer != null)
            {
                // Bytes 84 through 87 give the compression type in the DDS
                // header
                char one = (char)myBuffer.get(84);
                char two = (char)myBuffer.get(85);
                char three = (char)myBuffer.get(86);
                char four = (char)myBuffer.get(87);
                if (one == 'A' && two == 'R' && three == 'G' && four == 'B')
                {
                    type = CompressionType.D3DFMT_A8R8G8B8;
                }
                else if (one == 'R' && two == 'G' && three == 'B')
                {
                    type = CompressionType.D3DFMT_R8G8B8;
                }
                else if (one == 'X' && two == 'R' && three == 'G' && four == 'B')
                {
                    type = CompressionType.D3DFMT_X8R8G8B8;
                }
                else if (one == 'D' && two == 'X' && three == 'T')
                {
                    if (four == '1')
                    {
                        type = CompressionType.D3DFMT_DXT1;
                    }
                    else if (four == '2')
                    {
                        type = CompressionType.D3DFMT_DXT2;
                    }
                    else if (four == '3')
                    {
                        type = CompressionType.D3DFMT_DXT3;
                    }
                    else if (four == '4')
                    {
                        type = CompressionType.D3DFMT_DXT4;
                    }
                    else
                    {
                        type = CompressionType.D3DFMT_DXT5;
                    }
                }
            }
        }
        return type;
    }

    @Override
    public InputStream getDDSImageStream(Executor executor, boolean dispose) throws EncodingException
    {
        try
        {
            return new ByteBufferInputStream(getByteBuffer());
        }
        finally
        {
            if (dispose)
            {
                dispose();
            }
        }
    }

    @Override
    public int getHeight()
    {
        // The height is the 4th int in the header for the dds image.
        return getByteBuffer().getInt(12);
    }

    @Override
    public long getSizeInBytes()
    {
        return getByteBuffer().limit();
    }

    @Override
    public int getWidth()
    {
        // The height is the 5th int in the header for the dds image.
        return getByteBuffer().getInt(16);
    }

    @Override
    public boolean isBlank()
    {
        return false;
    }

    /**
     * Get if this image is compressed.
     *
     * @return {@code true} if the image is compressed.
     */
    @SuppressWarnings("PMD.MissingBreakInSwitch")
    public boolean isCompressed()
    {
        CompressionType compressionType = getCompressionType();
        switch (compressionType)
        {
            case D3DFMT_A8R8G8B8:
            case D3DFMT_R8G8B8:
            case D3DFMT_X8R8G8B8:
                return false;
            case D3DFMT_DXT1:
            case D3DFMT_DXT2:
            case D3DFMT_DXT3:
            case D3DFMT_DXT4:
            case D3DFMT_DXT5:
                return true;
            default:
                throw new UnexpectedEnumException(compressionType);
        }
    }

    /**
     * Get if the image has been disposed.
     *
     * @return {@code true} if the image has been disposed.
     */
    public boolean isDisposed()
    {
        return myBuffer == null;
    }

    /**
     * Converts the dds image to a jpg.
     *
     * @return The jpg stream or null if a {@link DDSDecoder} could not be
     *         found.
     * @throws IOException Thrown if an error occurred during conversion.
     */
    public byte[] toJpg() throws IOException
    {
        byte[] jpgBytes = null;
        if (isBlank())
        {
            jpgBytes = new byte[0];
        }
        else
        {
            for (DDSDecoder e : ServiceLoader.load(DDSDecoder.class))
            {
                ByteBuffer data = e.decode(getByteBuffer(), getCompressionType(), getWidth(), getHeight());
                if (data != null)
                {
                    BufferedImage buffImage = new BufferedImage(getWidth(), getHeight(), getImageType(this, data));
                    buffImage.getWritableTile(0, 0).setDataElements(0, 0, getWidth(), getHeight(), data.array());
                    ByteArrayOutputStream output = new ByteArrayOutputStream();

                    ImageIO.write(buffImage, "jpg", output);
                    jpgBytes = output.toByteArray();
                    break;
                }
            }
        }

        return jpgBytes;
    }

    /**
     * Read the object from a stream. This method is intended to be overridden
     * by subclasses.
     *
     * @param in The input stream.
     * @throws IOException If there's a problem reading the object from the
     *             stream.
     * @throws ClassNotFoundException If the object class cannot be loaded.
     */
    protected void doReadObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        synchronized (this)
        {
            myBuffer = ByteBuffer.wrap((byte[])in.readObject());
            myBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    /**
     * Write the object to a stream. This method is intended to be overridden by
     * subclasses.
     *
     * @param out The output stream.
     * @throws IOException If there's a problem writing the object.
     */
    protected void doWriteObject(ObjectOutputStream out) throws IOException
    {
        synchronized (this)
        {
            if (myBuffer == null)
            {
                throw new IllegalStateException("Image has been disposed.");
            }
            out.writeObject(myBuffer.array());
        }
    }

    @Override
    protected void setByteBuffer(ByteBuffer data, boolean usePool)
    {
        synchronized (this)
        {
            if (myBuffer != null)
            {
                throw new IllegalStateException("Cannot set byte buffer more than once.");
            }
            myBuffer = data.duplicate();
            myBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    /**
     * Read the object from a stream.
     *
     * @param in The input stream.
     * @throws IOException If there's a problem reading the object from the
     *             stream.
     * @throws ClassNotFoundException If the object class cannot be loaded.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        doReadObject(in);
    }

    /**
     * Write the object to a stream.
     *
     * @param out The output stream.
     * @throws IOException If there's a problem writing the object.
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        doWriteObject(out);
    }
}
