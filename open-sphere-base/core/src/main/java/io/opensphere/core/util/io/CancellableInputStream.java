package io.opensphere.core.util.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.Cancellable;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.util.lang.ThreadControl;

/**
 * A wrapped {@link InputStream} that has a {@link Runnable} to be called when
 * {@link #cancel()} is called.
 */
@ThreadSafe
public class CancellableInputStream extends FilterInputStream implements Cancellable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CancellableInputStream.class);

    /** The task to be run when the input stream is cancelled. */
    private final Runnable myCancelHandler;

    /** Flag indicating if the stream has been cancelled. */
    private boolean myCancelled;

    /** Name for the stream used for logging. */
    @Nullable
    private final String myName;

    /**
     * Constructor.
     *
     * @param stream The wrapped input stream.
     */
    public CancellableInputStream(CancellableInputStream stream)
    {
        this(Nulls.STRING, stream, (Runnable)null);
    }

    /**
     * Constructor.
     *
     * @param stream The wrapped input stream.
     * @param cancelHandler The task to be run when the input stream is
     *            cancelled.
     */
    public CancellableInputStream(InputStream stream, Runnable cancelHandler)
    {
        this(Nulls.STRING, stream, cancelHandler);
    }

    /**
     * Constructor.
     *
     * @param name The name for the stream.
     * @param stream The wrapped input stream.
     * @param cancelHandler The task to be run when the input stream is
     *            cancelled.
     */
    public CancellableInputStream(String name, InputStream stream, Runnable cancelHandler)
    {
        super(stream);
        myCancelHandler = cancelHandler;
        myName = name;

        ThreadControl.addCancellable(this);
    }

    @Override
    public synchronized void cancel()
    {
        if (!myCancelled)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Cancelling stream " + myName);
            }
            myCancelled = true;
            if (myCancelHandler != null)
            {
                myCancelHandler.run();
            }

            if (getWrappedInputStream() instanceof CancellableInputStream)
            {
                ((CancellableInputStream)getWrappedInputStream()).cancel();
            }
        }
    }

    @Override
    public void close() throws IOException
    {
        ThreadControl.removeCancellables(this);
        super.close();
    }

    /**
     * Get the wrapped input stream.
     *
     * @return The input stream.
     */
    public InputStream getWrappedInputStream()
    {
        return in;
    }

    @Override
    public synchronized boolean isCancelled()
    {
        return myCancelled;
    }
}
