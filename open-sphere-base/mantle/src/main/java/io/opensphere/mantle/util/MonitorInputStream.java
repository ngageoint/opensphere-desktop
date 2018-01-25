package io.opensphere.mantle.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.opensphere.core.util.lang.NamedThreadFactory;

/**
 * The Class MonitorInputStream.
 */
public class MonitorInputStream extends FilterInputStream
{
    /** The Constant ourEventExecutorService. */
    private final ThreadPoolExecutor myExecutor = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("MonitorInputStream"));

    /** The Listeners. */
    private final InputStreamMonitor myMonitor;

    /** The nread. */
    private int myNRead;

    /** The size. */
    private int mySize;

    /**
     * Constructs an object to monitor the progress of an input stream.
     *
     * @param pInputStream The input stream to be monitored.
     * @param listener the {@link InputStreamMonitor}
     */
    public MonitorInputStream(InputStream pInputStream, InputStreamMonitor listener)
    {
        super(pInputStream);
        myMonitor = listener;
        try
        {
            mySize = pInputStream.available();
        }
        catch (IOException ioe)
        {
            mySize = 0;
        }
        fireMonitorUpdate(0, mySize);
    }

    /**
     * Overrides <code>FilterInputStream.close</code> to close the progress
     * monitor as well as the stream.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void close() throws IOException
    {
        in.close();
        myExecutor.execute(() -> myMonitor.inputStreamClosed());
    }

    /**
     * Overrides <code>FilterInputStream.read</code> to update the progress
     * monitor after the read.
     *
     * @return the int
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized int read() throws IOException
    {
        int c = in.read();
        if (c >= 0)
        {
            fireMonitorUpdate(++myNRead, mySize);
        }
        if (myMonitor.isCancelled())
        {
            InterruptedIOException exc = new InterruptedIOException("progress");
            exc.bytesTransferred = myNRead;
            throw exc;
        }
        return c;
    }

    /**
     * Overrides <code>FilterInputStream.read</code> to update the progress
     * monitor after the read.
     *
     * @param b the b
     * @return the int
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized int read(byte[] b) throws IOException
    {
        int nr = in.read(b);
        if (nr > 0)
        {
            fireMonitorUpdate(myNRead += nr, mySize);
        }
        if (myMonitor.isCancelled())
        {
            InterruptedIOException exc = new InterruptedIOException("progress");
            exc.bytesTransferred = myNRead;
            throw exc;
        }
        return nr;
    }

    /**
     * Overrides <code>FilterInputStream.read</code> to update the progress
     * monitor after the read.
     *
     * @param b the b
     * @param off the off
     * @param len the len
     * @return the int
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException
    {
        int nr = in.read(b, off, len);
        if (nr > 0)
        {
            fireMonitorUpdate(myNRead += nr, mySize);
        }
        if (myMonitor.isCancelled())
        {
            InterruptedIOException exc = new InterruptedIOException("progress");
            exc.bytesTransferred = myNRead;
            throw exc;
        }
        return nr;
    }

    /**
     * Overrides <code>FilterInputStream.reset</code> to reset the progress
     * monitor as well as the stream.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized void reset() throws IOException
    {
        in.reset();
        myNRead = mySize - in.available();
        fireMonitorUpdate(myNRead, mySize);
    }

    /**
     * Overrides <code>FilterInputStream.skip</code> to update the progress
     * monitor after the skip.
     *
     * @param n the n
     * @return the long
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public synchronized long skip(long n) throws IOException
    {
        long nr = in.skip(n);
        if (nr > 0)
        {
            fireMonitorUpdate(myNRead += nr, mySize);
        }
        return nr;
    }

    /**
     * Fire merge map updated.
     *
     * @param totalRead the total read
     * @param totalToRead the total to read
     */
    private void fireMonitorUpdate(final int totalRead, final int totalToRead)
    {
        myExecutor.execute(() -> myMonitor.monitorUpdate(totalRead, totalToRead));
    }
}
