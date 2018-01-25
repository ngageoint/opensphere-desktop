package io.opensphere.wps.streaming;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.server.StreamCancellable;
import io.opensphere.core.server.StreamHandler;
import io.opensphere.core.server.StreamingServer;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadControl;

/**
 * The class responsible for communicating with a Near Real Time streaming
 * server.
 */
public class WPSStreamingServer implements StreamingServer
{
    /**
     * Used to build the necessary components used to stream NRT data.
     */
    private final ComponentsFactory myFactory;

    /**
     * The map of currently active NRT streamers.
     */
    private final Map<UUID, Streamer> myStreamers = Collections.synchronizedMap(New.<UUID, Streamer>map());

    /**
     * Used to subscribe to NRT streams.
     */
    private final Subscriber mySubscriber;

    /**
     * Used to unsubscribe from streams.
     */
    private final Unsubscriber myUnsubscriber;

    /**
     * The url to the server.
     */
    private final URL myUrl;

    /**
     * Constructs a new streaming server.
     *
     * @param factory Used to build the necessary components used to stream NRT
     *            data.
     */
    public WPSStreamingServer(ComponentsFactory factory)
    {
        myUrl = factory.getURL();
        myFactory = factory;
        mySubscriber = myFactory.buildSubscriber();
        myUnsubscriber = myFactory.buildUnsubscriber();
    }

    @Override
    public List<String> getAvailableStreams()
    {
        return Collections.emptyList();
    }

    @Override
    public String getURL()
    {
        return myUrl.toString();
    }

    @Override
    public UUID start(String stream, StreamHandler handler, ExecutorService executor, DataFilter filter, Geometry spatialFilter)
        throws IOException
    {
        Streamer streamer = null;
        UUID streamId = null;

        synchronized (this)
        {
            SubscriptionContext context = mySubscriber.subscribeToStream(myUrl, stream, filter, spatialFilter);

            if (context != null)
            {
                streamer = myFactory.buildStreamer(context, handler);

                streamId = context.getStreamId();

                myStreamers.put(streamId, streamer);
            }
        }

        if (streamer != null)
        {
            StreamCancellable cancellable = new StreamCancellable(this, streamId);
            ThreadControl.addCancellable(cancellable);
            try
            {
                streamer.start();
            }
            finally
            {
                ThreadControl.removeCancellables(cancellable);
            }
        }

        return streamId;
    }

    @Override
    public UUID start(String stream, StreamHandler handler, ExecutorService executor) throws IOException
    {
        return start(stream, handler, executor, null, null);
    }

    @Override
    public synchronized void stop(UUID streamId)
    {
        Streamer streamer = myStreamers.remove(streamId);
        if (streamer != null)
        {
            streamer.stop();
            myUnsubscriber.unsubscribe(streamer.getContext());
        }
    }
}
