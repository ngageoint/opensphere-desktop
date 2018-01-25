package io.opensphere.core.util.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import io.opensphere.core.util.Reader;

/**
 * Reads content from a stream into a byte buffer.
 */
public class StreamReader implements Reader
{
    /** One page of data. */
    private static final int PAGE_SIZE = 1 << 15;

    /** The content length in bytes. (-1 indicates unlimited.) */
    private final int myContentLength;

    /** The estimated content length in bytes. (-1 indicates unknown.) */
    private final int myEstimatedContentLength;

    /** The input stream. */
    private final InputStream myStream;

    /**
     * Create a stream reader with an unlimited content length.
     *
     * @param stream The input stream.
     */
    public StreamReader(InputStream stream)
    {
        this(stream, -1, -1);
    }

    /**
     * Create a stream reader with an expected content length. Any content
     * beyond the content length will be discarded.
     *
     * @param stream The input stream.
     * @param contentLength The expected content length in bytes, or <tt>-1</tt>
     *            if the estimated length is unknown.
     */
    public StreamReader(InputStream stream, int contentLength)
    {
        this(stream, contentLength, contentLength);
    }

    /**
     * Create a stream reader with an expected content length. The estimated
     * content length will be used to size the buffer, but will not cause data
     * to be truncated. Any content beyond the maximum content length will be
     * discarded.
     *
     * @param stream The input stream.
     * @param estimatedContentLength The estimated content length in bytes, or
     *            <tt>-1</tt> if the estimated length is unknown.
     * @param maxContentLength The maximum content length in bytes, or
     *            <tt>-1</tt> for unlimited.
     * @throws IllegalArgumentException If the estimated content length is
     *             larger than the maximum content length.
     */
    public StreamReader(InputStream stream, int estimatedContentLength, int maxContentLength) throws IllegalArgumentException
    {
        if (maxContentLength >= 0 && estimatedContentLength >= 0 && estimatedContentLength > maxContentLength)
        {
            throw new IllegalArgumentException("The estimatedContentLength cannot be greater than the maxContentLength.");
        }
        myStream = stream;
        myEstimatedContentLength = estimatedContentLength;
        myContentLength = maxContentLength;
    }

    /**
     * Read all the contents of the stream and write it to the given output
     * stream, but also return an input stream so that the contents can be read
     * again.
     *
     * @param outStream The stream to which to write the data.
     * @return A copy of the input stream.
     * @throws IOException If there is an exception reading the from the input
     *             stream or writing to the output.
     */
    public InputStream copyStream(OutputStream outStream) throws IOException
    {
        ByteBuffer buf = readStreamIntoBuffer();
        outStream.write(buf.array(), 0, buf.limit());
        return new ByteArrayInputStream(buf.array(), 0, buf.limit());
    }

    @Override
    public ByteBuffer readStreamIntoBuffer() throws IOException
    {
        return readStreamIntoBuffer((ByteBuffer)null);
    }

    @Override
    public ByteBuffer readStreamIntoBuffer(ByteBuffer buffer) throws IOException
    {
        // Do not close the channel because it will close the input stream,
        // which may not be desired if the input stream has multiple EOF
        // markers, such as a ZipInputStream.
        ReadableByteChannel channel = Channels.newChannel(myStream);

        ByteBuffer result = buffer;

        int lastReadLength;
        do
        {
            if (result == null || !result.hasRemaining())
            {
                ByteBuffer newBuffer;
                int bufferSize;
                if (result == null)
                {
                    if (myEstimatedContentLength >= 0 && (myContentLength < 0 || myEstimatedContentLength < myContentLength))
                    {
                        // Add one to the estimate just in case it's dead-on.
                        // One extra byte is needed to detect the end of the
                        // stream.
                        bufferSize = myEstimatedContentLength + 1;
                    }
                    else
                    {
                        bufferSize = myContentLength >= 0 ? myContentLength : PAGE_SIZE;
                    }
                }
                else if (myContentLength >= 0)
                {
                    bufferSize = myContentLength;
                }
                else
                {
                    bufferSize = result.limit() << 1;
                }

                newBuffer = ByteBuffer.allocate(bufferSize);
                if (result != null)
                {
                    newBuffer.put((ByteBuffer)result.rewind());
                }
                result = newBuffer;
            }
            else if (myContentLength >= 0 && result.limit() > myContentLength && result.capacity() >= myContentLength)
            {
                result.limit(myContentLength);
            }

            do
            {
                lastReadLength = channel.read(result);
            }
            while (lastReadLength >= 0 && result.hasRemaining());
        }
        while (lastReadLength >= 0 && (myContentLength < 0 || result.limit() < myContentLength));

        return (ByteBuffer)result.flip();
    }

    /**
     * Read all of the contents of the stream and put it into a String with the
     * given encoding.
     *
     * @param charset The charset to use when decoding the String.
     * @return The string.
     * @throws IOException If there is an exception reading from the stream.
     */
    public String readStreamIntoString(Charset charset) throws IOException
    {
        ByteBuffer buf = readStreamIntoBuffer();
        return new String(buf.array(), 0, buf.limit(), charset);
    }

    /**
     * Read all of the contents of the stream and write it to the given output
     * stream.
     *
     * @param outStream The stream to which to write the data.
     * @throws IOException If there is an exception reading the from the input
     *             stream or writing to the output.
     */
    public void readStreamToOutputStream(OutputStream outStream) throws IOException
    {
        byte[] data = new byte[PAGE_SIZE];
        int numBytes;
        while ((numBytes = myStream.read(data)) >= 0)
        {
            outStream.write(data, 0, numBytes);
        }
        outStream.flush();
    }
}
