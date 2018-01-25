package io.opensphere.core.appl.versions.controller;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A tracker used to track the progress of an automated installation.
 */
public class InstallProgressTracker
{
    /**
     * The total number of files retrieved during the transaction.
     */
    private final AtomicInteger filesRetrieved = new AtomicInteger(0);

    /**
     * The total number of bytes copied locally during the transaction.
     */
    private final AtomicLong bytesCopied = new AtomicLong(0);

    /**
     * The total number of bytes downloaded from remote sources during the
     * transaction.
     */
    private final AtomicLong bytesDownloaded = new AtomicLong(0);

    /**
     * Increments the {@link #filesRetrieved} counter, and returns the new
     * value.
     *
     * @return the incremented value.
     */
    public int incrementRetrievedFiles()
    {
        return filesRetrieved.incrementAndGet();
    }

    /**
     * Adds to the {@link #bytesCopied} counter, and returns the new value.
     *
     * @param addAmount the amount to add to the counter.
     * @return the new value.
     */
    public long addToBytesCopied(long addAmount)
    {
        return bytesCopied.addAndGet(addAmount);
    }

    /**
     * Adds to the {@link #bytesDownloaded} counter, and returns the new value.
     *
     * @param addAmount the amount to add to the counter.
     * @return the new value.
     */
    public long addToBytesDownloaded(long addAmount)
    {
        return bytesDownloaded.addAndGet(addAmount);
    }

    /**
     * Gets the value of the {@link #filesRetrieved} field.
     *
     * @return the value stored in the {@link #filesRetrieved} field.
     */
    public int getFilesRetrieved()
    {
        return filesRetrieved.get();
    }

    /**
     * Gets the value of the {@link #bytesCopied} field.
     *
     * @return the value stored in the {@link #bytesCopied} field.
     */
    public long getBytesCopied()
    {
        return bytesCopied.get();
    }

    /**
     * Gets the value of the {@link #bytesDownloaded} field.
     *
     * @return the value stored in the {@link #bytesDownloaded} field.
     */
    public long getBytesDownloaded()
    {
        return bytesDownloaded.get();
    }
}
