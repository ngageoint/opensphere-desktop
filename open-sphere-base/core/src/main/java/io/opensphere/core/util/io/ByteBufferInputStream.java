package io.opensphere.core.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

import io.opensphere.core.util.Utilities;

/**
 * An input stream that reads from a byte buffer.
 */
public class ByteBufferInputStream extends InputStream
{
    /** The wrapped byte buffer. */
    private final ByteBuffer myBuffer;

    /**
     * Construct the input stream. Bytes will be read starting with the buffer's
     * position and ending at the buffer's limit. The buffer's position will be
     * moved as bytes are read, so the caller should duplicate the buffer if
     * necessary.
     *
     * @param buffer The buffer to read from.
     */
    public ByteBufferInputStream(ByteBuffer buffer)
    {
        myBuffer = Utilities.checkNull(buffer, "buffer");
    }

    @Override
    public int available()
    {
        return myBuffer.remaining();
    }

    @Override
    public synchronized void mark(int readlimit)
    {
        myBuffer.mark();
    }

    @Override
    public boolean markSupported()
    {
        return true;
    }

    @Override
    public int read()
    {
        return myBuffer.hasRemaining() ? myBuffer.get() & 0xFF : -1;
    }

    @Override
    public int read(byte[] b)
    {
        try
        {
            return super.read(b);
        }
        catch (IOException e)
        {
            throw new ImpossibleException(e);
        }
    }

    @Override
    public int read(byte[] array, int offset, int length)
    {
        Utilities.checkNull(array, "array");
        if (offset < 0 || length < 0 || length > array.length - offset)
        {
            throw new IndexOutOfBoundsException();
        }
        if (length == 0)
        {
            return 0;
        }
        else if (myBuffer.hasRemaining())
        {
            int len = Math.min(myBuffer.remaining(), length);
            myBuffer.get(array, offset, len);
            return len;
        }
        else
        {
            return -1;
        }
    }

    @Override
    public synchronized void reset() throws IOException
    {
        try
        {
            myBuffer.reset();
        }
        catch (InvalidMarkException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public long skip(long n)
    {
        int skipCount = (int)Math.min(n, myBuffer.remaining());
        myBuffer.position(myBuffer.position() + skipCount);
        return skipCount;
    }

    /** Runtime exception. */
    private static class ImpossibleException extends RuntimeException
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param t The wrapped throwable.
         */
        public ImpossibleException(Throwable t)
        {
            super(t);
        }
    }
}
