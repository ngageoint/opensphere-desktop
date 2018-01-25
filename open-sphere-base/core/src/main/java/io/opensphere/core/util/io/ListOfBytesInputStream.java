package io.opensphere.core.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.opensphere.core.util.Utilities;

/**
 * An input stream containing a chunked list of byte arrays. This is to be used
 * when the byte array data is huge. Large byte arrays are hard on the garbage
 * collector unlike many small ones.
 *
 */
public class ListOfBytesInputStream extends InputStream
{
    /**
     * The data for the stream.
     */
    private final List<byte[]> myBuffer;

    /**
     * The count.
     */
    private final int myCount;

    /**
     * The current mark.
     */
    private int myMark;

    /**
     * The maximum size of each chunk.
     */
    private final int myMaxChunkSize;

    /**
     * The current byte position.
     */
    private int myPosition;

    /**
     * Constructs a new input stream that reads for a list of byte[].
     *
     * @param data The data of the stream.
     * @param length The number of bytes total in data.
     * @param maxChunkSize The maximum size a given byte[] can be within data.
     */
    public ListOfBytesInputStream(List<byte[]> data, int length, int maxChunkSize)
    {
        myBuffer = data;
        myPosition = 0;
        myCount = length;
        myMaxChunkSize = maxChunkSize;
    }

    @Override
    public synchronized int available()
    {
        return myCount - myPosition;
    }

    @Override
    public void close() throws IOException
    {
    }

    @Override
    public synchronized void mark(int readAheadLimit)
    {
        myMark = myPosition;
    }

    @Override
    public boolean markSupported()
    {
        return true;
    }

    @Override
    public synchronized int read()
    {
        int listPos = myPosition / myMaxChunkSize;
        int arrayPos = myPosition % myMaxChunkSize;
        int value = -1;
        if (myPosition < myCount)
        {
            value = myBuffer.get(listPos)[arrayPos] & 0xff;
            myPosition++;
        }
        return value;
    }

    @Override
    public synchronized int read(byte[] bytes, int off, int len)
    {
        Utilities.checkNull(bytes, "bytes");
        if (off < 0 || len < 0 || len > bytes.length - off)
        {
            throw new IndexOutOfBoundsException();
        }

        if (myPosition >= myCount)
        {
            return -1;
        }

        int avail = myCount - myPosition;
        int theLength = len;
        if (len > avail)
        {
            theLength = avail;
        }
        if (theLength <= 0)
        {
            return 0;
        }

        for (int i = off; i < theLength + off; i++)
        {
            bytes[i] = (byte)read();
        }

        return len;
    }

    @Override
    public synchronized void reset()
    {
        myPosition = myMark;
    }

    @Override
    public synchronized long skip(long n)
    {
        long k = myCount - myPosition;
        if (n < k)
        {
            k = n < 0 ? 0 : n;
        }

        myPosition += k;
        return k;
    }
}
