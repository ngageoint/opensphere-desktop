package io.opensphere.core.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * Writes bytes to a buffer that is a list of chunked up byte arrays. This
 * should be used when the bytes are in the megabyte range to help the garbage
 * collector reclaim memory.
 *
 */
public class ListOfBytesOutputStream extends OutputStream
{
    /**
     * The maximum size a chunk can be.
     */
    private static final int ourMaxArraySize = 100000;

    /**
     * The buffer where data is stored.
     */
    private final List<byte[]> myBuffer = New.list();

    /**
     * The number of valid bytes in the buffer.
     */
    private int myCount;

    @Override
    public void close() throws IOException
    {
    }

    /**
     * The maximum number of bytes a byte[] element will be in toArrays.
     *
     * @return The maximum length of a byte[] chunk.
     */
    public int getChunkSize()
    {
        return ourMaxArraySize;
    }

    /**
     * Resets the <code>count</code> field of this byte array output stream to
     * zero, so that all currently accumulated output in the output stream is
     * discarded. The output stream can be used again, reusing the already
     * allocated buffer space.
     */
    public synchronized void reset()
    {
        myCount = 0;
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return the value of the <code>count</code> field, which is the number of
     *         valid bytes in this output stream.
     */
    public synchronized int size()
    {
        return myCount;
    }

    /**
     * Copies the buffer to a new list of bytes.
     *
     * @return The list of bytes whose maximum size will be getChunkSize.
     */
    public synchronized List<byte[]> toArrays()
    {
        List<byte[]> arrays = New.list();

        int listPos = myCount / ourMaxArraySize;
        int arrayPos = myCount % ourMaxArraySize;

        if (!myBuffer.isEmpty())
        {
            for (int i = 0; i < listPos; i++)
            {
                byte[] array = new byte[ourMaxArraySize];
                System.arraycopy(myBuffer.get(i), 0, array, 0, array.length);
                arrays.add(array);
            }

            if (arrayPos != 0)
            {
                byte[] lastArray = new byte[arrayPos];
                byte[] lastArraySource = myBuffer.get(listPos);
                System.arraycopy(lastArraySource, 0, lastArray, 0, lastArray.length);
                arrays.add(lastArray);
            }
        }

        return arrays;
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len)
    {
        if (off < 0 || off > b.length || len < 0 || off + len - b.length > 0)
        {
            throw new IndexOutOfBoundsException();
        }

        int written = 0;
        while (written < len)
        {
            int listPos = myCount / ourMaxArraySize;
            int arrayPos = myCount % ourMaxArraySize;

            if (myBuffer.size() <= listPos)
            {
                myBuffer.add(new byte[ourMaxArraySize]);
            }

            byte[] array = myBuffer.get(listPos);

            int startPos = written + off;
            int endLength = len - written;
            int endPos = arrayPos + endLength;
            if (endPos >= array.length)
            {
                endLength = array.length - arrayPos;
            }

            System.arraycopy(b, startPos, array, arrayPos, endLength);
            myCount += endLength;
            written += endLength;
        }
    }

    @Override
    public void write(int b) throws IOException
    {
        write(new byte[] { (byte)b });
    }
}
