package io.opensphere.wps.streaming;

import java.io.IOException;

/**
 * Continues retrieving new data for a given layer.
 */
public interface Streamer
{
    /**
     * Gets the data about the subscription.
     *
     * @return The data about the subscription.
     */
    SubscriptionContext getContext();

    /**
     * Starts the streaming of data.
     *
     * @throws IOException If there was an error communicating with the server.
     */
    void start() throws IOException;

    /**
     * Stops the streaming of data.
     */
    void stop();
}
