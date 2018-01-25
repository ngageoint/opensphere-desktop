package io.opensphere.auxiliary.video;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.xuggle.ferry.RefCounted;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.video.IConverter;

import io.opensphere.core.util.Constants;
import io.opensphere.core.video.VideoContentHandler;

/**
 * Xuggler utilities.
 */
public final class XugglerUtilities
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(XugglerUtilities.class);

    /**
     * Get a wrapper for a {@link IConverter} that is {@link Closeable}.
     *
     * @param converter The {@link IConverter}.
     * @return The {@link Closeable}.
     */
    public static Closeable getClosableWrapper(final IConverter converter)
    {
        return new Closeable()
        {
            @Override
            public void close() throws IOException
            {
                if (converter != null)
                {
                    converter.delete();
                }
            }
        };
    }

    /**
     * Get a wrapper for a {@link RefCounted} that is {@link Closeable}.
     *
     * @param refCounted The {@link RefCounted}.
     * @return The {@link Closeable}.
     */
    public static Closeable getCloseableWrapper(final RefCounted refCounted)
    {
        return new Closeable()
        {
            @Override
            public void close()
            {
                if (refCounted != null)
                {
                    refCounted.delete();
                }
            }
        };
    }

    /**
     * Get the Decompression Time Stamp (DTS) for this packet in milliseconds.
     *
     * @param packet the packet
     * @return the Decompression Time Stamp (DTS) in milliseconds
     */
    public static long getDtsMillis(IPacket packet)
    {
        /* The doc for getTimeBase() says that the returned object needs to be
         * released, but this will cause vm crash. */
        double secondsPerTimeBase = packet.getTimeBase().getDouble();
        return (long)(packet.getDts() * secondsPerTimeBase * Constants.MILLI_PER_UNIT);
    }

    /**
     * Get the Presentation Time Stamp (PTS) for this packet in milliseconds.
     *
     * @param packet the packet
     * @return the Presentation Time Stamp (PTS) in milliseconds
     */
    public static long getPtsMillis(IPacket packet)
    {
        /* The doc for getTimeBase() says that the returned object needs to be
         * released, but this will cause vm crash. */
        double secondsPerTimeBase = packet.getTimeBase().getDouble();
        return (long)(packet.getPts() * secondsPerTimeBase * Constants.MILLI_PER_UNIT);
    }

    /**
     * Send a metadata packet to the handlers.
     *
     * @param packet The packet.
     * @param inputBuffer The buffer to contain the packet data.
     * @param handlers The handlers.
     * @return The buffer to contain the packet data.
     */
    @SuppressWarnings("PMD.AvoidArrayLoops")
    public static byte[] handleMetadataPacket(IPacket packet, byte[] inputBuffer,
            Collection<VideoContentHandler<? super ByteBuffer>> handlers)
    {
        byte[] buf;
        buf = inputBuffer == null || inputBuffer.length < packet.getSize() ? new byte[packet.getSize()] : inputBuffer;

        boolean success = true;
        try
        {
            packet.get(0, buf, 0, packet.getSize());
        }
        catch (IndexOutOfBoundsException e)
        {
            success = false;
            LOGGER.error("Ignoring metadata packet.", e);
        }

        if (success)
        {
            // Check to see if this is a custom KLV packet.
            if (buf[0] == 0)
            {
                byte[] tempBuf = new byte[buf.length / 2];
                for (int i = 1, j = 0; j < tempBuf.length; i += 2, j++)
                {
                    tempBuf[j] = buf[i];
                }

                buf = tempBuf;
            }

            for (VideoContentHandler<? super ByteBuffer> handler : handlers)
            {
                handler.handleContent(ByteBuffer.wrap(buf, 0, buf.length), packet.getDts());
            }
        }

        return buf;
    }

    /** Private constructor. */
    private XugglerUtilities()
    {
    }
}
