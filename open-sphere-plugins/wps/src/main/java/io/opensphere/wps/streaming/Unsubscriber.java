package io.opensphere.wps.streaming;

/**
 * Unsubscribes from the specified stream id.
 */
@FunctionalInterface
public interface Unsubscriber
{
    /**
     * Unsubscribes from the stream id.
     *
     * @param context Contains information about the subscription to unsubscribe
     *            from.
     */
    void unsubscribe(SubscriptionContext context);
}
