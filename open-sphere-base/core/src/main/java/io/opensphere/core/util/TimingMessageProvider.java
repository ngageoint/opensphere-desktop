package io.opensphere.core.util;

/**
 * Interface for classes that can provide timing messages.
 */
@FunctionalInterface
public interface TimingMessageProvider
{
    /**
     * Get the timing message.
     *
     * @return The timing message.
     */
    String getTimingMessage();
}
