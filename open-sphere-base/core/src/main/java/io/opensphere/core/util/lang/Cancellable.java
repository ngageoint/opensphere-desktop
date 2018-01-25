package io.opensphere.core.util.lang;

/**
 * Something that can be cancelled.
 */
public interface Cancellable
{
    /** Cancel. */
    void cancel();

    /**
     * Get if this is cancelled.
     *
     * @return {@code true} if cancelled.
     */
    boolean isCancelled();
}
