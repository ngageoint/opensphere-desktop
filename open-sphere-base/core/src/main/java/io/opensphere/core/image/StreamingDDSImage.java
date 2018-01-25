package io.opensphere.core.image;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.MappedObjectPool;
import io.opensphere.core.util.io.StreamReader;

/**
 * A special {@link DDSImage} that allows the specification of a
 * {@link Serializer} that will be called when this object is serialized. This
 * is to allow the image to be encoded as it is serialized.
 */
public class StreamingDDSImage extends DDSImage
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(StreamingDDSImage.class);

    /**
     * The thread local byte buffer, used to avoid excessive memory allocation.
     */
    private static final ThreadLocal<MappedObjectPool<Integer, ByteBuffer>> ourThreadByteBufferPool = new ThreadLocal<>();

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /** If a buffer was retrieved from a pool, this is the pool. */
    private transient MappedObjectPool<Integer, ByteBuffer> myByteBufferPool;

    /** The serializer. */
    private final transient Serializer mySerializer;

    /**
     * Set the byte buffer pool for this thread.
     *
     * @param pool The pool.
     */
    public static void setThreadByteBufferPool(MappedObjectPool<Integer, ByteBuffer> pool)
    {
        ourThreadByteBufferPool.set(pool);
    }

    /**
     * Constructor.
     *
     * @param serializer The object that will encode the image.
     */
    public StreamingDDSImage(Serializer serializer)
    {
        mySerializer = Utilities.checkNull(serializer, "serializer");
    }

    @Override
    public synchronized void dispose()
    {
        if (!isDisposed())
        {
            ByteBuffer byteBuffer = getByteBuffer();
            super.dispose();

            if (myByteBufferPool != null)
            {
                myByteBufferPool.surrender(Integer.valueOf(byteBuffer.capacity()), byteBuffer);
            }
        }
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    protected synchronized void doReadObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        if (myByteBufferPool != null)
        {
            throw new IllegalStateException("Byte buffer pool can only be set once.");
        }
        int imageSize = in.readInt();
        myByteBufferPool = ourThreadByteBufferPool.get();
        ByteBuffer buffer;
        try
        {
            buffer = myByteBufferPool == null ? null : myByteBufferPool.take(Integer.valueOf(imageSize));
            if (buffer != null && buffer.position() > 0)
            {
                buffer.rewind();

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Postion is not zero " + buffer.position() + " Capacity " + buffer.capacity());
                }
            }
        }
        catch (InterruptedException e)
        {
            throw new ClosedByInterruptException();
        }
        try
        {
            buffer = new StreamReader(in, imageSize).readStreamIntoBuffer(buffer);
        }
        catch (IOException | RuntimeException | Error e)
        {
            // If there's any failure, return the buffer to the pool.
            if (myByteBufferPool != null && buffer != null)
            {
                myByteBufferPool.surrender(Integer.valueOf(imageSize), buffer);
            }
            throw e;
        }
        setByteBuffer(buffer, false);
    }

    @Override
    protected void doWriteObject(ObjectOutputStream out) throws IOException
    {
        mySerializer.writeToStream(out);
    }

    /** An object that will handle writing the DDS data to the output stream. */
    @FunctionalInterface
    public interface Serializer
    {
        /**
         * Write the DDS data to the output stream. The first four bytes must be
         * an integer that indicates the number of bytes in the image. Following
         * that should be the bytes composing the image.
         *
         * @param out The output stream.
         * @throws IOException If there is an error writing to the stream.
         */
        void writeToStream(ObjectOutputStream out) throws IOException;
    }
}
