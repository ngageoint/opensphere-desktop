package io.opensphere.wps.streaming.factory;

import java.net.URL;

import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.server.StreamHandler;
import io.opensphere.wps.source.WPSRequestExecuter;
import io.opensphere.wps.streaming.ComponentsFactory;
import io.opensphere.wps.streaming.Streamer;
import io.opensphere.wps.streaming.Subscriber;
import io.opensphere.wps.streaming.SubscriptionContext;
import io.opensphere.wps.streaming.Unsubscriber;
import io.opensphere.wps.streaming.impl.StreamSubscriber;
import io.opensphere.wps.streaming.impl.StreamUnsubscriber;
import io.opensphere.wps.streaming.impl.StreamerImpl;

/**
 * Responsible for building the components necessary to stream data from an NRT
 * Streaming layer.
 */
public class ComponentsFactoryImpl implements ComponentsFactory
{
    /**
     * Used to make the WPS request to find out the streaming layers.
     */
    private final WPSRequestExecuter myExecuter;

    /**
     * Used to get the server object to make the get requests.
     */
    private final ServerProviderRegistry myRegistry;

    /**
     * The streaming url used to retrieve new data.
     */
    private final URL myUrl;

    /**
     * Constructs a new components factory.
     *
     * @param url The streaming url used to retrieve new data.
     * @param registry Used to get the server object to make the get requests.
     * @param requestExecuter Used to make the WPS request to find out the
     *            streaming layers.
     */
    public ComponentsFactoryImpl(URL url, ServerProviderRegistry registry, WPSRequestExecuter requestExecuter)
    {
        myUrl = url;
        myRegistry = registry;
        myExecuter = requestExecuter;
    }

    @Override
    public Streamer buildStreamer(SubscriptionContext context, StreamHandler handler)
    {
        return new StreamerImpl(context, handler, myRegistry);
    }

    @Override
    public Subscriber buildSubscriber()
    {
        return new StreamSubscriber(myRegistry);
    }

    @Override
    public Unsubscriber buildUnsubscriber()
    {
        return new StreamUnsubscriber(myExecuter);
    }

    @Override
    public URL getURL()
    {
        return myUrl;
    }
}
