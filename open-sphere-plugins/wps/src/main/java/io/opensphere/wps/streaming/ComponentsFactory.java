package io.opensphere.wps.streaming;

import java.net.URL;

import io.opensphere.core.server.StreamHandler;

/**
 * Responsible for building the components necessary to stream data from an NRT
 * Streaming layer.
 */
public interface ComponentsFactory
{
    /**
     * Builds a {@link Streamer}.
     *
     * @param context Data about the subscription.
     * @param handler The object to be notified of new data.
     * @return A newly constructed {@link Streamer}.
     */
    Streamer buildStreamer(SubscriptionContext context, StreamHandler handler);

    /**
     * Builds a {@link Subscriber}.
     *
     * @return A newly constructed {@link Subscriber}.
     */
    Subscriber buildSubscriber();

    /**
     * Builds an {@link Unsubscriber}.
     *
     * @return The {@link Unsubscriber}.
     */
    Unsubscriber buildUnsubscriber();

    /**
     * Gets the WPS url for the server.
     *
     * @return The WPS url.
     */
    URL getURL();
}
