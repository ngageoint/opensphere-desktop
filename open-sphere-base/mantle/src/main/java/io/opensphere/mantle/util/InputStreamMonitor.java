package io.opensphere.mantle.util;

/**
 * The listener interface for receiving monitorInputStream updates.
 */
public interface InputStreamMonitor
{
    /**
     * Input stream closed..
     */
    void inputStreamClosed();

    /**
     * Cancelled.
     *
     * @return true, if is cancelled.
     */
    boolean isCancelled();

    /**
     * Monitor update.
     *
     * @param totalRead the total number read
     * @param totalToRead the total number to read ( or 0 if unknown ).
     */
    void monitorUpdate(int totalRead, int totalToRead);
}
