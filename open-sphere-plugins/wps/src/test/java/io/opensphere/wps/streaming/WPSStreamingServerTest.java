package io.opensphere.wps.streaming;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.server.StreamHandler;
import io.opensphere.core.util.concurrent.FixedThreadPoolExecutor;
import io.opensphere.test.core.matchers.EasyMockHelper;

/**
 * Tests the {@link WPSStreamingServer} class.
 */
public class WPSStreamingServerTest
{
    /**
     * A non streaming layer.
     */
    private static final String ourNonStreamingLayer = "static";

    /**
     * The streaming layer.
     */
    private static final String ourStreamingLayer = "layer2";

    /**
     * The wps url.
     */
    private static final String ourWpsUrl = "https://somehose/ogc/wps";

    /**
     * Test the getUrl method.
     *
     * @throws MalformedURLException Bad URL.
     */
    @Test
    public void testGetURL() throws MalformedURLException
    {
        EasyMockSupport support = new EasyMockSupport();

        Subscriber subscriber = support.createNiceMock(Subscriber.class);
        Unsubscriber unsubscriber = support.createNiceMock(Unsubscriber.class);
        ComponentsFactory factory = createFactory(support, subscriber, unsubscriber);

        support.replayAll();

        WPSStreamingServer server = new WPSStreamingServer(factory);

        assertEquals(ourWpsUrl, server.getURL().toString());

        support.verifyAll();
    }

    /**
     * Tests the start and stop functions.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testStartAndStop() throws IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        UUID theStreamId = UUID.randomUUID();

        SubscriptionContext context = new SubscriptionContext();
        context.setStreamId(theStreamId);

        StreamHandler handler = support.createNiceMock(StreamHandler.class);
        Subscriber subscriber = createSubscriber(support, context);
        Unsubscriber unsubscriber = support.createNiceMock(Unsubscriber.class);
        Streamer streamer = createStreamer(support, context);
        ComponentsFactory factory = createFactory(support, subscriber, unsubscriber);
        EasyMock.expect(factory.buildStreamer(EasyMock.eq(context), EasyMock.eq(handler))).andReturn(streamer);

        support.replayAll();

        WPSStreamingServer server = new WPSStreamingServer(factory);

        // expect nothing to happen.
        server.stop(UUID.randomUUID());

        // expect nothing to happen.
        UUID streamId = server.start(ourNonStreamingLayer, handler, new FixedThreadPoolExecutor(2));
        assertNull(streamId);

        // expect a stream to have started.
        streamId = server.start(ourStreamingLayer, handler, new FixedThreadPoolExecutor(2));
        assertEquals(theStreamId, streamId);

        // expect stream to stop.
        server.stop(streamId);

        support.verifyAll();
    }

    /**
     * Creates and easy mocked {@link ComponentsFactory}.
     *
     * @param support Used to create the mock.
     * @param subscriber The subscriber the factory should return.
     * @param unsubscriber The unsubscriber the factory should return.
     * @return The {@link ComponentsFactory}.
     * @throws MalformedURLException Bad URL.
     */
    private ComponentsFactory createFactory(EasyMockSupport support, Subscriber subscriber, Unsubscriber unsubscriber)
        throws MalformedURLException
    {
        ComponentsFactory factory = support.createMock(ComponentsFactory.class);

        EasyMock.expect(factory.getURL()).andReturn(new URL(ourWpsUrl));
        EasyMock.expect(factory.buildSubscriber()).andReturn(subscriber);
        EasyMock.expect(factory.buildUnsubscriber()).andReturn(unsubscriber);

        return factory;
    }

    /**
     * Creates an easy mocked {@link Streamer}.
     *
     * @param support Used to create the mock.
     * @param context The context the Streamer should return.
     * @return The mocked {@link Streamer}.
     * @throws IOException Bad IO.
     */
    private Streamer createStreamer(EasyMockSupport support, SubscriptionContext context) throws IOException
    {
        Streamer streamer = support.createMock(Streamer.class);

        streamer.start();
        streamer.stop();
        EasyMock.expect(streamer.getContext()).andReturn(context);

        return streamer;
    }

    /**
     * Creates an easy mocked {@link Subscriber}.
     *
     * @param support Used to create the mock.
     * @param context The context the subscriber should return.
     * @return The mocked {@link Subscriber}.
     * @throws MalformedURLException Bad URL.
     */
    private Subscriber createSubscriber(EasyMockSupport support, SubscriptionContext context) throws MalformedURLException
    {
        Subscriber subscriber = support.createMock(Subscriber.class);

        EasyMock.expect(subscriber.subscribeToStream(EasyMockHelper.eq(new URL(ourWpsUrl)), EasyMock.eq(ourNonStreamingLayer),
                EasyMock.<DataFilter>isNull(), EasyMock.<Geometry>isNull())).andReturn(null);
        EasyMock.expect(subscriber.subscribeToStream(EasyMockHelper.eq(new URL(ourWpsUrl)), EasyMock.eq(ourStreamingLayer),
                EasyMock.<DataFilter>isNull(), EasyMock.<Geometry>isNull())).andReturn(context);

        return subscriber;
    }
}
