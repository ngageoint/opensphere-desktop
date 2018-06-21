package io.opensphere.core.util.io;

import java.io.IOException;
import java.io.InputStream;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.opensphere.core.util.Utilities;

/**
 * A wrapper for an {@link InputStream} that silently ignores attempts to close
 * the stream, but passes all other calls through to the wrapped stream.
 */
public class UncloseableInputStream extends InputStream
{
    /** The wrapped input stream. */
    private final InputStream myWrappedStream;

    /**
     * Constructor.
     *
     * @param wrappedStream The wrapped stream.
     */
    public UncloseableInputStream(@NonNull InputStream wrappedStream)
    {
        Utilities.checkNull(wrappedStream, "wrappedStream");

        myWrappedStream = wrappedStream;
    }

    @Override
    public int available() throws IOException
    {
        return myWrappedStream.available();
    }

    /**
     * Access to the wrapped stream.
     *
     * @return The wrapped input stream.
     */
    public InputStream getWrappedStream()
    {
        return myWrappedStream;
    }

    @Override
    public synchronized void mark(int readlimit)
    {
        myWrappedStream.mark(readlimit);
    }

    @Override
    public boolean markSupported()
    {
        return myWrappedStream.markSupported();
    }

    @Override
    public int read() throws IOException
    {
        return myWrappedStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return myWrappedStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return myWrappedStream.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException
    {
        myWrappedStream.reset();
    }

    @Override
    public long skip(long n) throws IOException
    {
        return myWrappedStream.skip(n);
    }
}
