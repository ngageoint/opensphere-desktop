package io.opensphere.core.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import net.jcip.annotations.ThreadSafe;

import io.opensphere.core.util.collections.New;

/**
 * A wrapper for a {@link CancellableInputStream} that will capture any
 * exceptions that are thrown by any of the read methods so that they can be
 * later retrieved.
 */
@ThreadSafe
public class ExceptionCapturingCancellableInputStream extends CancellableInputStream
{
    /** The exceptions that were captured. */
    private final Collection<IOException> myExceptions = Collections.synchronizedCollection(New.list());

    /**
     * Constructor.
     *
     * @param stream The stream.
     */
    public ExceptionCapturingCancellableInputStream(CancellableInputStream stream)
    {
        super(stream);
    }

    /**
     * Constructor.
     *
     * @param stream The stream.
     * @param cancelHandler The cancel handler.
     */
    public ExceptionCapturingCancellableInputStream(InputStream stream, Runnable cancelHandler)
    {
        super(stream, cancelHandler);
    }

    /**
     * Constructor.
     *
     * @param name The name.
     * @param stream The stream.
     * @param cancelHandler The cancel handler.
     */
    public ExceptionCapturingCancellableInputStream(String name, InputStream stream, Runnable cancelHandler)
    {
        super(name, stream, cancelHandler);
    }

    /**
     * Get the exceptions.
     *
     * @return The exceptions.
     */
    public Collection<? extends IOException> getExceptions()
    {
        return New.unmodifiableCollection(myExceptions);
    }

    @Override
    public int read() throws IOException
    {
        try
        {
            return super.read();
        }
        catch (IOException e)
        {
            myExceptions.add(e);
            throw e;
        }
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        try
        {
            return super.read(b);
        }
        catch (IOException e)
        {
            myExceptions.add(e);
            throw e;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        try
        {
            return super.read(b, off, len);
        }
        catch (IOException e)
        {
            myExceptions.add(e);
            throw e;
        }
    }
}
